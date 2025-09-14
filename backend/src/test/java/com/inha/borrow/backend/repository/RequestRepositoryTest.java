package com.inha.borrow.backend.repository;

import com.inha.borrow.backend.enums.ApiErrorCode;
import com.inha.borrow.backend.enums.RequestState;
import com.inha.borrow.backend.enums.RequestType;
import com.inha.borrow.backend.enums.Role;
import com.inha.borrow.backend.model.dto.item.ItemDto;
import com.inha.borrow.backend.model.dto.request.PatchRequestDto;
import com.inha.borrow.backend.model.dto.request.SaveRequestDto;
import com.inha.borrow.backend.model.dto.request.SaveRequestResultDto;
import com.inha.borrow.backend.model.dto.user.admin.SaveAdminDto;
import com.inha.borrow.backend.model.dto.user.borrower.BorrowerDto;
import com.inha.borrow.backend.model.entity.Item;
import com.inha.borrow.backend.model.entity.request.Request;
import com.inha.borrow.backend.model.exception.ResourceNotFoundException;
import com.inha.borrow.backend.service.JwtTokenService;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@JdbcTest
@Import({ RequestRepository.class, ItemRepository.class, BorrowerRepository.class, AdminRepository.class })
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Slf4j
class RequestRepositoryTest {
    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private BorrowerRepository borrowerRepository;

    @TestConfiguration
    static class TestConfig {
        @Bean
        PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }

        @Bean
        JwtTokenService jwtTokenService() {
            return new JwtTokenService() {
                @Override
                public String createToken(String id) {
                    return "test-token-" + id;
                }
            };
        }
    }

    private String adminId = "testAdmin";
    private String borrowerId = "testBorrower";
    private SaveRequestDto saveRequestDto;

    @BeforeEach
    void setUp() {
        requestRepository.deleteAll();
        itemRepository.deleteAll();
        borrowerRepository.deleteAll();

        ItemDto itemDto = ItemDto.builder()
                .name("대여물품1")
                .location("대여물품 보관 위치")
                .password("password")
                .price(1000)
                .build();

        BorrowerDto borrowerDto = BorrowerDto.builder()
                .id(borrowerId)
                .password("password")
                .email("test@test.com")
                .name("borrower")
                .phonenumber("010-0000-0000")
                .studentNumber("12341234")
                .accountNumber("1111-11111-1111")
                .refreshToken("refresh")
                .build();

        SaveAdminDto saveAdminDto = SaveAdminDto.builder()
                .id(adminId)
                .name("adminName")
                .position(Role.DIVISION_MEMBER)
                .phonenumber("010-0000-0000")
                .email("asd@naver.com")
                .division("TEST")
                .build();

        adminRepository.saveAdmin(saveAdminDto);
        borrowerRepository.save(borrowerDto);
        int itemId = itemRepository.save(itemDto).getId();
        saveRequestDto = SaveRequestDto.builder()
                .itemId(itemId)
                .borrowerId(borrowerId)
                .borrowerAt(Timestamp.from(Instant.now()))
                .returnAt(Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS)))
                .type(RequestType.BORROW)
                .build();
    }

    @Test
    @DisplayName("요청 저장 및 조회 성공")
    void saveSuccessTest() {
        // given
        // given: 성공 케이스와 동일하게 한 건 저장
        ItemDto itemDto = ItemDto.builder()
                .name("param")
                .location("loc")
                .password("pw")
                .price(100)
                .build();
        Item item = itemRepository.save(itemDto);
        SaveRequestDto borrowRequestDto = SaveRequestDto.builder()
                .itemId(item.getId())
                .borrowerId(borrowerId)
                .borrowerAt(Timestamp.from(Instant.now()))
                .returnAt(Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS)))
                .type(RequestType.BORROW)
                .build();
        SaveRequestResultDto dto = requestRepository.save(borrowRequestDto);
        log.info("test {}", dto.getRequestId());
        // when
        Request requestByBorrowerIdWithRequestId = requestRepository.findById(borrowerId, dto.getRequestId());
        Request requestByRequestId = requestRepository.findById(null, dto.getRequestId());
        // then
        assertThat(requestByBorrowerIdWithRequestId.getBorrowerId()).isEqualTo(borrowRequestDto.getBorrowerId());
        assertThat(requestByBorrowerIdWithRequestId.getId()).isEqualTo(requestByRequestId.getId());
    }

    @Test
    @DisplayName("요청 단건 조회 (실패-borrowerId 또는 요청 아이디 미존재)")
    void findByIdFailForNotFoundBorrowerId() {
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> requestRepository.findById("321", 1));
        assertThat(ex.getErrorCode()).isEqualTo(ApiErrorCode.REQUEST_NOT_FOUND.name());
        assertThat(ex.getErrorMessage()).isEqualTo(ApiErrorCode.REQUEST_NOT_FOUND.getMessage());
    }

    @ParameterizedTest(name = "조건 조회 성공: borrowerId={0}, type={1}, state={2} -> size={3}")
    @CsvSource({
            "testBorrower, BORROW, PENDING, 1",
            "testBorrower, BORROW, '', 1",
            "testBorrower, '', PENDING, 1",
            "testBorrower, '', '', 1",
            "'', '', 'PENDING', 1",
            "'', 'BORROW', '', 1",
            "'', '', '', 1"
    })
    @DisplayName("리퀘스트 조건 다건 조회 파라미터 성공")
    void findByCondition_param_success(String borrowerIdParam, String type, String state, int expectedSize) {
        // given
        requestRepository.save(saveRequestDto);
        // when
        List<Request> result = requestRepository.findRequestsByCondition(borrowerIdParam, type, state);
        // then
        assertThat(result.size()).isEqualTo(expectedSize);
    }

    @ParameterizedTest(name = "조건 조회 실패: borrowerId={0}, type={1}, state={2}")
    @CsvSource({
            "wrongBorrower, BORROW, PENDING",
            "testBorrower, RETURN, PENDING",
            "testBorrower, BORROW, ASSIGNED"
    })
    @DisplayName("리퀘스트 조건 다건 조회 파라미터 실패")
    void findByCondition_param_fail(String borrowerIdParam, String type, String state) {
        // given
        requestRepository.save(saveRequestDto);
        // when & then
        assertThatThrownBy(() -> requestRepository.findRequestsByCondition(borrowerIdParam,
                type, state))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("리퀘스트 전체 조회 성공")
    void findAll() {
        // given
        // when
        requestRepository.save(saveRequestDto);
        // then
        List<Request> result = requestRepository.findAll();
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("리퀘스트 전체 조회 (실패 조회값 없음)")
    void findAllFail() {
        assertThatThrownBy(() -> requestRepository.findAll())
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("리퀘스트 수정 성공")
    void patchRequest() {
        // given
        Timestamp changedBorrowAt = Timestamp.from(Instant.now().plus(3, ChronoUnit.DAYS));
        Timestamp returnAt = Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS));

        PatchRequestDto patchRequestDto = PatchRequestDto.builder()
                .borrowerAt(changedBorrowAt)
                .returnAt(returnAt)
                .build();
        int requestId = requestRepository.save(saveRequestDto).getRequestId();
        // when
        requestRepository.patchRequest(patchRequestDto, requestId, borrowerId);
        // then
        Request result = requestRepository.findById(borrowerId, requestId);

        Date.from(Instant.now().plus(3, ChronoUnit.DAYS));

        // 시간단위는 밀리세컨드가 차이날 수 밖에 없어서 날짜 단위 절사
        assertThat(result.getBorrowAt().toLocalDateTime().truncatedTo(ChronoUnit.DAYS))
                .isEqualTo(changedBorrowAt.toLocalDateTime().truncatedTo(ChronoUnit.DAYS));
    }

    @Test
    @DisplayName("리퀘스트 수정 (실패 borrowerId 미존재")
    void patchRequestFailNotFoundBorrowerId() {
        PatchRequestDto patchRequestDto = new PatchRequestDto();
        patchRequestDto.setBorrowerAt(Timestamp.valueOf(LocalDateTime.now().plusDays(7)));
        patchRequestDto.setReturnAt(Timestamp.valueOf(LocalDateTime.now().plusDays(8)));
        assertThatThrownBy(() -> requestRepository.patchRequest(patchRequestDto, 1,
                borrowerId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("리퀘스트 취소 성공")
    void cancelRequestSuccessTest() {
        int requestId = requestRepository.save(saveRequestDto).getRequestId();
        requestRepository.cancelRequest(requestId, borrowerId);
        assertThrows(ResourceNotFoundException.class, () -> {
            requestRepository.findById(borrowerId, requestId);
        });
    }

    @Test
    @DisplayName("리퀘스트 취소 (실패 borrowerId 미존재")
    void cancelRequestFailNotFoundBorrowerId() {
        assertThatThrownBy(() -> requestRepository.cancelRequest(3,
                borrowerId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("리퀘스트 평가 성공")
    void updateRequestState() {
        int requestId = requestRepository.save(saveRequestDto).getRequestId();
        requestRepository.updateRequestState(RequestState.ASSIGNED, requestId);
        Request result = requestRepository.findById(borrowerId, requestId);
        assertThat(result.getState()).isEqualTo(RequestState.ASSIGNED);
    }

    @Test
    @DisplayName("리퀘스트 평가 (실패 존재하지 않는 요청")
    void updateRequestStateFailNotFoundBorrowerId() {
        assertThatThrownBy(() -> requestRepository.updateRequestState(RequestState.ASSIGNED, 3))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("대여요청에 대한 담당자 지정 메서드 테스트(성공)")
    void manageRequestSuccessTest() {
        // given
        int requestId = requestRepository.save(saveRequestDto).getRequestId();
        // when
        requestRepository.manageRequest(adminId, requestId);
        Request request = requestRepository.findById(null, requestId);
        // then
        assertThat(request.getManager().getId()).isEqualTo(adminId);
        assertThat(request.getState()).isEqualTo(RequestState.ASSIGNED);
    }

    @Test
    @DisplayName("대여요청에 대한 담당자 지정 메서드 테스트(실패)")
    void manageRequestFailTest() {
        // given
        // when
        // then
        assertThrows(ResourceNotFoundException.class, () -> {
            requestRepository.manageRequest("mockAdmin", 123);
        });
    }

    @Test
    @DisplayName("요청ID로 담당자/아이템/타입/상태 조회 성공")
    void findManagerAndItemIdById_success() {
        // given
        int requestId = requestRepository.save(saveRequestDto).getRequestId();
        // when: 담당자 지정하여 상태 ASSIGNED로 변경
        requestRepository.manageRequest(adminId, requestId);
        Request result = requestRepository.findManagerAndItemIdById(requestId);
        // then
        assertThat(result.getItemId()).isEqualTo(saveRequestDto.getItemId());
        assertThat(result.getType()).isEqualTo(RequestType.BORROW);
        assertThat(result.getState()).isEqualTo(RequestState.ASSIGNED);
        assertThat(result.getManager().getId()).isEqualTo(adminId);
    }

    @Test
    @DisplayName("요청ID로 담당자/아이템/타입/상태 조회 실패 - 요청 미존재")
    void findManagerAndItemIdById_fail_notFound() {
        // given
        int notExistsId = 9999999;
        // when & then
        assertThrows(ResourceNotFoundException.class, () -> {
            requestRepository.findManagerAndItemIdById(notExistsId);
        });
    }

    @Test
    @DisplayName("요청ID로 상태 조회 성공")
    void findRequestStateById_success() {
        // given
        int requestId = requestRepository.save(saveRequestDto).getRequestId();
        // then: 초기 상태는 PENDING
        assertThat(requestRepository.findRequestStateById(requestId))
                .isEqualTo(RequestState.PENDING);
        // when: 상태를 ASSIGNED로 변경 후 재조회
        requestRepository.manageRequest(adminId, requestId);
        assertThat(requestRepository.findRequestStateById(requestId))
                .isEqualTo(RequestState.ASSIGNED);
    }

    @Test
    @DisplayName("요청ID로 상태 조회 실패 - 요청 미존재")
    void findRequestStateById_fail_notFound() {
        // given
        int notExistsId = 8888888;
        // when & then
        assertThrows(ResourceNotFoundException.class, () -> {
            requestRepository.findRequestStateById(notExistsId);
        });
    }

}

package com.inha.borrow.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inha.borrow.backend.enums.ItemState;
import com.inha.borrow.backend.enums.RequestState;
import com.inha.borrow.backend.enums.RequestType;
import com.inha.borrow.backend.enums.Role;
import com.inha.borrow.backend.model.dto.item.BorrowedItemDto;
import com.inha.borrow.backend.model.dto.item.ItemDto;
import com.inha.borrow.backend.model.dto.request.PatchRequestDto;
import com.inha.borrow.backend.model.dto.user.admin.SaveAdminDto;
import com.inha.borrow.backend.model.dto.user.borrower.BorrowerDto;
import com.inha.borrow.backend.model.entity.Item;
import com.inha.borrow.backend.model.entity.request.Request;
import com.inha.borrow.backend.model.dto.request.SaveRequestDto;
import com.inha.borrow.backend.model.entity.user.Admin;
import com.inha.borrow.backend.model.entity.user.Borrower;
import com.inha.borrow.backend.model.exception.InvalidValueException;
import com.inha.borrow.backend.model.exception.ResourceNotFoundException;
import com.inha.borrow.backend.repository.AdminRepository;
import com.inha.borrow.backend.repository.BorrowerRepository;
import com.inha.borrow.backend.repository.ItemRepository;
import com.inha.borrow.backend.repository.RequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class RequestServiceTest {
        @Autowired
        private RequestService requestService;

        @Autowired
        private RequestRepository requestRepository;

        @Autowired
        private ItemRepository itemRepository;

        @Autowired
        private BorrowerRepository borrowerRepository;

        @Autowired
        private AdminRepository adminRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;

        private ObjectMapper objectMapper = new ObjectMapper();

        private int requestId;
        private Item savedItem;
        private SaveRequestDto borrowRequestDto;
        private Borrower borrower;
        private Admin admin;

        @BeforeEach
        void setUp() {
                // 전체초기화
                requestRepository.deleteAll();
                borrowerRepository.deleteAll();
                itemRepository.deleteAll();

                String adminId = "adminId";
                String borrowerId = "borrowerId";

                // 대여자 생성
                BorrowerDto borrowerDto = BorrowerDto.builder()
                                .id(borrowerId)
                                .password(passwordEncoder.encode("Absssf1@2"))
                                .email("123")
                                .name("123")
                                .phonenumber("123")
                                .studentNumber("123")
                                .accountNumber("123")
                                .refreshToken("123")
                                .build();
                borrowerRepository.save(borrowerDto);

                // 관리자 생성
                SaveAdminDto saveAdminDto = SaveAdminDto.builder()
                                .id(adminId)
                                .name("admin")
                                .position(Role.DIVISION_MEMBER)
                                .phonenumber("010-0000-0000")
                                .email("admin@test")
                                .division("TEST")
                                .build();
                adminRepository.saveAdmin(saveAdminDto);

                // 아이템 생성
                ItemDto itemDto = new ItemDto("우산", "3층", "123", 123);
                savedItem = itemRepository.save(itemDto);

                // 요청생성 DTO
                borrowRequestDto = SaveRequestDto.builder()
                                .itemId(savedItem.getId())
                                .borrowerId(borrowerId)
                                .borrowerAt(Timestamp.from(Instant.now()))
                                .returnAt(Timestamp.from(Instant.now()))
                                .type(RequestType.BORROW)
                                .build();

                // 전역 borrower 변수 설정
                borrower = Borrower.builder()
                                .id(borrowerId)
                                .build();

                // 전역 admin변수 설정
                admin = Admin.builder()
                                .id(adminId)
                                .build();

                // 공통으로 사용할 요청 저장 및 ID 보관
                requestId = requestService.saveRequest(borrowRequestDto).getRequestId();
                // 저장 과정에서 아이템 상태가 REVIEWING 으로 변경되므로, 다른 테스트 준비를 위해 원복
                itemRepository.updateState(ItemState.AFFORD, savedItem.getId());
        }

        @Test
        @DisplayName("대여신청 요청 저장(성공)")
        void saveBorrowRequestSuccessTest() {
                // given
                int requestId = requestService.saveRequest(borrowRequestDto).getRequestId();
                // when
                // then
                Request result = requestService.findById(admin, requestId);
                BorrowedItemDto item = itemRepository.findById(savedItem.getId());
                assertThat(result.getId()).isEqualTo(requestId);
                assertThat(item.getState()).isEqualTo(ItemState.REVIEWING);
        }

        @Test
        @DisplayName("대여신청 요청 저장(실패-빌릴수 없는 상태의 물품 대여시도)")
        void saveBorrowRequestFailTest() {
                // given
                itemRepository.updateState(ItemState.REVIEWING, savedItem.getId());
                // when
                // then
                assertThrows(InvalidValueException.class, () -> {
                        requestService.saveRequest(borrowRequestDto);
                });
        }

        @Test
        @DisplayName("반납신청 요청 저장(성공)")
        void saveBorrowRetrunSuccessTest() {
                // given
                int borrowRequestId = requestId;
                SaveRequestDto returnRequestDto = SaveRequestDto.builder()
                                .borrowerId(borrower.getId())
                                .prevRequestId(borrowRequestId)
                                .itemId(savedItem.getId())
                                .borrowerAt(Timestamp.from(Instant.now()))
                                .returnAt(Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS)))
                                .type(RequestType.RETURN)
                                .build();
                // when : 기존 요청이 허가상태여야지만 반납신청이 가능함
                requestService.updateRequestState(RequestState.PERMIT, borrowRequestId);
                int returnRequestId = requestService.saveRequest(returnRequestDto).getRequestId();
                // then
                Request result = requestService.findById(null, returnRequestId);
                assertThat(result.getId()).isEqualTo(returnRequestId);
        }

        @Test
        @DisplayName("반납신청 요청 저장(실패-이전 대여요청 없음)")
        void saveBorrowReturnFailTest() {
                // given
                SaveRequestDto returnRequestDto = SaveRequestDto.builder()
                                .prevRequestId(999999)
                                .itemId(savedItem.getId())
                                .borrowerAt(Timestamp.from(Instant.now()))
                                .returnAt(Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS)))
                                .type(RequestType.RETURN)
                                .build();
                // when
                // then
                assertThrows(ResourceNotFoundException.class, () -> {
                        requestService.saveRequest(returnRequestDto);
                });
        }

        @Test
        @DisplayName("리퀘스트 조회 성공(대여자)")
        void findByIdForBorrowerSuccessTest() {
                // given
                int expectedId = requestId;
                // when
                // then
                Request result = requestService.findById(borrower, expectedId);
                assertThat(result.getId()).isEqualTo(expectedId);
                assertThat(result.getBorrowerId()).isEqualTo(borrower.getId());
        }

        @Test
        @DisplayName("리퀘스트 조회 성공(관리자)")
        void findByIdForAdminSuccessTest() throws Exception {
                // given
                int expectedId = requestId;
                // when
                // then
                Request result = requestService.findById(admin, expectedId);
                System.out.println(objectMapper.writeValueAsString(result));
                assertThat(result.getId()).isEqualTo(expectedId);
                assertThat(result.getBorrowerId()).isEqualTo(borrower.getId());
        }

        @Test
        @DisplayName("리퀘스트 조회 실패(없는 요청)")
        void findByIdFailTest() {
                // given
                // when
                // then
                assertThrows(ResourceNotFoundException.class, () -> {
                        requestService.findById(null, 9999);
                });
        }

        @ParameterizedTest(name = "Borrower path: borrowerId={0}, type={1}, state={2} -> size={3}")
        @CsvSource({
                        "'', BORROW, PENDING, 1",
                        "'', BORROW, '', 1",
                        "'', '', PENDING, 1",
                        "'', '', '', 1",
                        "wrongBorrower, BORROW, PENDING, 1"
        })
        @DisplayName("리퀘스트 조건 조회 파라미터 - 대여자 경로 성공")
        void findByCondition_borrower_param_success(String borrowerIdParam, String type, String state,
                        int expectedSize) throws Exception {
                // given
                // when
                List<Request> result = requestService.findByCondition(borrower, borrowerIdParam, type, state);
                // then
                System.out.println(objectMapper.writeValueAsString(result));
                assertThat(result.size()).isEqualTo(expectedSize);
        }

        @ParameterizedTest(name = "Borrower path fail: borrowerId={0}, type={1}, state={2}")
        @CsvSource({
                        "'', RETURN, PENDING",
                        "'', BORROW, ASSIGNED",
                        "wrongBorrower, RETURN, PENDING"
        })
        @DisplayName("리퀘스트 조건 조회 파라미터 - 대여자 경로 실패")
        void findByCondition_borrower_param_fail(String borrowerIdParam, String type, String state) {
                // given
                requestService.saveRequest(borrowRequestDto);
                // when & then
                assertThatThrownBy(() -> requestService.findByCondition(borrower, borrowerIdParam, type, state))
                                .isInstanceOf(ResourceNotFoundException.class);
        }

        @ParameterizedTest(name = "Admin path: borrowerId={0}, type={1}, state={2} -> size={3}")
        @CsvSource({
                        "borrowerId, BORROW, PENDING, 1",
                        "borrowerId, BORROW, '', 1",
                        "borrowerId, '', PENDING, 1",
                        "borrowerId, '', '', 1",
                        "'', '', '', 1"
        })
        @DisplayName("리퀘스트 조건 조회 파라미터 - 관리자 경로 성공")
        void findByCondition_admin_param_success(String borrowerIdParam, String type, String state, int expectedSize) {
                // given
                // when
                List<Request> result = requestService.findByCondition(admin, borrowerIdParam, type, state);
                // then
                assertThat(result.size()).isEqualTo(expectedSize);
        }

        @ParameterizedTest(name = "Admin path fail: borrowerId={0}, type={1}, state={2}")
        @CsvSource({
                        "wrongBorrower, BORROW, PENDING",
                        "borrowerId, RETURN, PENDING",
                        "borrowerId, BORROW, ASSIGNED"
        })
        @DisplayName("리퀘스트 조건 조회 파라미터 - 관리자 경로 실패")
        void findByCondition_admin_param_fail(String borrowerIdParam, String type, String state) {
                // given
                requestService.saveRequest(borrowRequestDto);
                // when & then
                assertThatThrownBy(() -> requestService.findByCondition(admin, borrowerIdParam, type, state))
                                .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("리퀘스트 수정 성공")
        void patchRequestSuccessTest() {
                // given
                int borrowRequestId = requestId;
                PatchRequestDto dto = PatchRequestDto.builder()
                                .borrowerAt(Timestamp.from(Instant.now().plus(5, ChronoUnit.DAYS)))
                                .returnAt(Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS)))
                                .build();
                // when & then
                requestService.patchRequest(dto, borrowRequestId, borrower.getId());
                Request result = requestService.findById(borrower, borrowRequestId);
                assertThat(result.getBorrowAt().toLocalDateTime().truncatedTo(ChronoUnit.DAYS))
                                .isEqualTo(dto.getBorrowerAt().toLocalDateTime().truncatedTo(ChronoUnit.DAYS));
        }

        @Test
        @DisplayName("리퀘스트 수정 실패-PENDING상태 아닌 요청에 대한 수정")
        void patchRequestFailForStateTest() {
                // given
                int borrowRequestId = requestId;
                requestService.updateRequestState(RequestState.ASSIGNED, borrowRequestId);
                PatchRequestDto dto = PatchRequestDto.builder()
                                .borrowerAt(Timestamp.from(Instant.now().plus(5, ChronoUnit.DAYS)))
                                .returnAt(Timestamp.from(Instant.now().plus(7, ChronoUnit.DAYS)))
                                .build();
                // when & then
                assertThrows(InvalidValueException.class, () -> {
                        requestService.patchRequest(dto, borrowRequestId, borrower.getId());
                });
        }

        @Test
        @DisplayName("리퀘스트 취소 성공(대여요청만 취소가능)")
        void cancelRequestSuccessTest() {
                // given
                int requestId = this.requestId;
                // when
                requestService.cancelRequest(requestId, borrower.getId());
                // then
                assertThrows(ResourceNotFoundException.class, () -> {
                        requestService.findById(admin, requestId);
                });
        }

        @Test
        @DisplayName("리퀘스트 취소 실패(대여요청만 취소가능)")
        void cancelRequestFalseTest() {
                // given
                int borrowRequestId = requestId;
                SaveRequestDto returnRequestDto = SaveRequestDto.builder()
                                .borrowerId(borrower.getId())
                                .itemId(savedItem.getId())
                                .prevRequestId(borrowRequestId)
                                .returnAt(Timestamp.from(Instant.now()))
                                .borrowerAt(Timestamp.from(Instant.now()))
                                .type(RequestType.RETURN)
                                .build();
                requestService.updateRequestState(RequestState.PERMIT, borrowRequestId);
                int returnRequestId = requestService.saveRequest(returnRequestDto).getRequestId();
                // when
                // then
                assertThrows(InvalidValueException.class, () -> {
                        requestService.cancelRequest(returnRequestId, borrower.getId());
                });
        }

        @Test
        @DisplayName("요청 상태변경 메서드 테스트(성공)")
        void updateRequestStateSuccessTest() {
                // given
                int requestId = this.requestId;
                // when
                requestService.updateRequestState(RequestState.PERMIT, requestId);
                // then
                Request result = requestService.findById(admin, requestId);
                assertThat(result.getState()).isEqualTo(RequestState.PERMIT);
        }

        @Test
        @DisplayName("요청 담당자 지정 테스트")
        void manageRequestTest() {
                // given
                int requestId = this.requestId;
                // when
                requestService.manageRequest(admin.getId(), requestId);
                Request request = requestService.findById(admin, requestId);
                // then
                assertThat(request.getManager().getId()).isEqualTo(admin.getId());
                assertThat(request.getState()).isEqualTo(RequestState.ASSIGNED);
        }
}

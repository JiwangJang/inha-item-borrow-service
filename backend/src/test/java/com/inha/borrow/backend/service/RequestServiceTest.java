package com.inha.borrow.backend.service;

import com.inha.borrow.backend.enums.RequestState;
import com.inha.borrow.backend.enums.RequestType;
import com.inha.borrow.backend.enums.Role;
import com.inha.borrow.backend.model.dto.item.ItemDto;
import com.inha.borrow.backend.model.dto.request.PatchRequestDto;
import com.inha.borrow.backend.model.dto.user.admin.SaveAdminDto;
import com.inha.borrow.backend.model.dto.user.borrower.BorrowerDto;
import com.inha.borrow.backend.model.entity.Item;
import com.inha.borrow.backend.model.entity.request.Request;
import com.inha.borrow.backend.model.dto.request.SaveRequestDto;
import com.inha.borrow.backend.model.entity.user.Borrower;
import com.inha.borrow.backend.repository.AdminRepository;
import com.inha.borrow.backend.repository.BorrowerRepository;
import com.inha.borrow.backend.repository.ItemRepository;
import com.inha.borrow.backend.repository.RequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AdminRepository adminRepository;

    private int requestId;
    private Item savedItem;
    private BorrowerDto borrowerDto;
    private SaveRequestDto saveRequestDto;
    private Borrower borrower;

    @BeforeEach
    void setUp() {
        // 전체초기화
        requestRepository.deleteAll();
        borrowerRepository.deleteAll();
        itemRepository.deleteAll();

        // 대여자 생성
        borrowerDto = BorrowerDto.builder()
                .id("123")
                .password(passwordEncoder.encode("Absssf1@2"))
                .email("123")
                .name("123")
                .phonenumber("123")
                .studentNumber("123")
                .accountNumber("123")
                .refreshToken("123")
                .build();
        borrowerRepository.save(borrowerDto);

        // 아이템 생성
        ItemDto itemDto = new ItemDto("우산", "3층", "123", 123);
        savedItem = itemRepository.save(itemDto);

        // 요청생성
        saveRequestDto = SaveRequestDto.builder()
                .itemId(savedItem.getId())
                .borrowerId("123")
                .borrowerAt(Timestamp.valueOf(LocalDateTime.of(2025, 8, 31, 17, 22, 0)))
                .returnAt(Timestamp.valueOf(LocalDateTime.of(2025, 9, 3, 17, 22, 0)))
                .type(RequestType.BORROW)
                .build();

        // 전역 borrower 변수 설정
        List<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority(Role.BORROWER.name()));
        borrower = Borrower.builder()
                .id("123")
                .password(passwordEncoder.encode("Absssf1@2"))
                .email("123")
                .name("123")
                .phonenumber("123")
                .studentNumber("123")
                .accountNumber("123")
                .refreshToken("123")
                .authorities(authorities)
                .build();
        requestId = requestRepository.save(saveRequestDto);

        // 관리자 설정
        SaveAdminDto saveAdminDto = SaveAdminDto.builder()
                .id("testAdmin")
                .name("test")
                .position(Role.DIVISION_MEMBER)
                .phonenumber("010-0000-0000")
                .email("test@test")
                .division("TEST")
                .build();
        adminRepository.saveAdmin(saveAdminDto);
    }

    @Test
    @DisplayName("리퀘스트 저장 성공")
    void saveRequest() {
        requestRepository.deleteAll();
        int requestId = requestService.saveRequest(borrower, saveRequestDto, savedItem.getId());
        Request result = requestRepository.findById(borrowerDto.getId(), requestId);
        assertThat(result.getBorrowerAt()).isEqualTo(Timestamp.valueOf(LocalDateTime.of(2025, 8, 31, 17, 22, 0)));
    }

    @Test
    @DisplayName("리퀘스트 조회 성공")
    void findById() {
        Request result = requestService.findById(borrower, requestId);
        assertThat(result.getBorrowerId()).isEqualTo("123");
    }

    @Test
    @DisplayName("리퀘스트 조회 (실패 권한 없음)")
    void findByIdFailNotAllowed() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        borrower.setAuthorities(authorities);
        assertThatThrownBy(() -> requestService.findById(borrower, requestId))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("조건 조회 성공")
    void findByCondition() {
        List<Request> result = requestService.findByCondition(borrower, borrowerDto.getId(), "BORROW", "PENDING");
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("조건 조회 (실패 권한 없음)")
    void findByConditionFailNotAllowed() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        borrower.setAuthorities(authorities);
        assertThatThrownBy(() -> requestService.findByCondition(borrower, borrowerDto.getId(), "BORROW", "PENDING"))
                .isInstanceOf(AccessDeniedException.class);

    }

    @Test
    @DisplayName("전체 조회 성공")
    void findAll() {
        List<Request> result = requestService.findAll();
        assertThat(result).isNotEmpty();
    }

    @Test
    @DisplayName("리퀘스트 수정 성공")
    void patchRequest() {
        PatchRequestDto dto = PatchRequestDto.builder()
                .borrowerAt(Timestamp.valueOf(LocalDateTime.of(2025, 9, 1, 10, 0)))
                .returnAt(Timestamp.valueOf(LocalDateTime.of(2025, 9, 4, 10, 0)))
                .build();

        requestService.patchRequest(dto, requestId, "123");

        Request result = requestService.findById(borrower, requestId);
        assertThat(result.getBorrowerAt().toLocalDateTime().withNano(0))
                .isEqualTo(dto.getBorrowerAt().toLocalDateTime().withNano(0));
    }

    @Test
    @DisplayName("리퀘스트 취소 성공")
    void cancelRequest() {
        requestService.cancelRequest(requestId, "123");
        Request result = requestService.findById(borrower, requestId);
        assertThat(result.getCancel()).isTrue();
    }

    @Test
    @DisplayName("리퀘스트 상태 평가 성공")
    void evaluationRequest() {
        requestService.evaluationRequest(RequestState.ASSIGNED, requestId);
        Request result = requestService.findById(borrower, requestId);
        assertThat(result.getState()).isEqualTo(RequestState.ASSIGNED);
    }

    @Test
    @DisplayName("요청 담당자 지정 테스트")
    void manageRequestTest() {
        // given
        String adminId = "testAdmin";
        // when
        requestService.manageRequest(adminId, String.valueOf(requestId));
        Request request = requestService.findById(borrower, requestId);
        // then
        assertThat(request.getManager()).isEqualTo(adminId);
        assertThat(request.getState()).isEqualTo(RequestState.ASSIGNED);
    }
}
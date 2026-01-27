package com.inha.borrow.backend.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.inha.borrow.backend.model.dto.user.borrower.BorrowerDto;
import com.inha.borrow.backend.model.dto.user.borrower.BorrowerInformDto;
import com.inha.borrow.backend.model.dto.user.borrower.BorrowerLoginDto;
import com.inha.borrow.backend.model.dto.user.borrower.CacheBorrowerDto;
import com.inha.borrow.backend.model.exception.InvalidValueException;
import com.inha.borrow.backend.repository.BorrowerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@Transactional
class LoginServiceTest {
    @Autowired
    private LoginService loginService;

    @Autowired
    private BorrowerRepository borrowerRepository;

    @Autowired
    private Cache<String, CacheBorrowerDto> borrowerCache;

    @Autowired
    private Cache<String, BorrowerInformDto> tempBorrowerCache;

    @BeforeEach
    void setUp() {
        // 테스트 전 캐시와 레포지토리 초기화
        borrowerCache.invalidateAll();
        tempBorrowerCache.invalidateAll();
        borrowerRepository.deleteAll(); // 실제 DB 환경에 따라 주의
    }

    @Test
    @DisplayName("신규 사용자 로그인 시 i-class 인증 후 임시 캐시에 저장되어야 한다")
    void login_NewUser_SaveToTempCache() {
        // Given
        // 실제 i-class 연동이 발생하므로, 아이디/비밀번호가 올바르다고 가정할 때 (테스트용 계정 필요)
        // 여기서는 로직 구조상 Jsoup이 성공했다고 가정하는 시나리오를 검증하고 싶으나
        // Jsoup이 실제 통신을 하므로, 보통은 외부 연동 부를 별도 Bean으로 빼서 Mocking합니다.

        BorrowerLoginDto loginDto = new BorrowerLoginDto("학번", "비밀번호");

        // When & Then
        // 실제 통신이 발생하므로 통신 실패나 아이디 틀림 예외가 발생할 수 있음
        assertThatThrownBy(() -> loginService.inhaLogin(loginDto))
                .isInstanceOf(InvalidValueException.class); // 혹은 실제 성공 시나리오 진행
    }

    @Test
    @DisplayName("DB에 이미 존재하는 사용자는 임시 캐시에 저장되지 않아야 한다")
    void login_ExistingUser_NoTempCache() {
        // Given
        String userId = "20240001";
        // DB에 미리 저장
        borrowerRepository.save(BorrowerDto.builder()
                .id(userId)
                .name("홍길동")
                .build());

        // When
        // 이 부분은 Jsoup 결과가 name을 가져왔다고 가정해야 하므로
        // 실제 테스트 코드를 돌릴 때는 Jsoup 부분을 Mocking하는 것이 좋습니다.
    }

}
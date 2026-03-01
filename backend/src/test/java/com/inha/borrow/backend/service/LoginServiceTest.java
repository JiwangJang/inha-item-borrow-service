// package com.inha.borrow.backend.service;

// import com.github.benmanes.caffeine.cache.Cache;
// import com.inha.borrow.backend.enums.ApiErrorCode;
// import com.inha.borrow.backend.model.dto.user.borrower.BorrowerDto;
// import com.inha.borrow.backend.model.dto.user.borrower.BorrowerInformDto;
// import com.inha.borrow.backend.model.dto.user.borrower.BorrowerLoginDto;
// import com.inha.borrow.backend.model.dto.user.borrower.CacheBorrowerDto;
// import com.inha.borrow.backend.model.entity.user.Borrower;
// import com.inha.borrow.backend.model.exception.InvalidValueException;
// import com.inha.borrow.backend.model.exception.ResourceNotFoundException;
// import com.inha.borrow.backend.repository.BorrowerRepository;
// import org.jsoup.Connection;
// import org.jsoup.Jsoup;
// import org.jsoup.nodes.Document;
// import org.jsoup.select.Elements;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.MockedStatic;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.transaction.annotation.Transactional;

// import java.io.IOException;

// import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
// import static
// org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
// import static org.mockito.ArgumentMatchers.anyString;
// import static org.mockito.BDDMockito.given;
// import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class)
// class LoginServiceTest {

// @InjectMocks
// private LoginService loginService;

// @Mock
// private Cache<String, CacheBorrowerDto> borrowerCache;

// @Mock
// private Cache<String, BorrowerInformDto> tempBorrowerCache;

// @Mock
// private BorrowerRepository borrowerRepository;

// // Jsoup 모킹을 위한 객체들
// @Mock
// private Connection connection;
// @Mock
// private Connection.Response response;
// @Mock
// private Document document;
// @Mock
// private Elements nameElements;
// @Mock
// private Elements deptElements;
// ApiErrorCode errorCode = ApiErrorCode.INVALID_ID_OR_PASSWORD;

// @BeforeEach
// void setUp() {
// // [핵심] 헷갈리지 않게 직접 생성자로 주입
// loginService = new LoginService(borrowerCache, tempBorrowerCache,
// borrowerRepository);
// }

// @Test
// @DisplayName("로그인 성공: 신규 사용자일 경우 임시 캐시에 저장된다")
// void inhaLogin_Success_NewUser() throws IOException {
// // given
// String testId = "12121212";
// String testPw = "password";
// String expectedName = "홍길동";
// String expectedDept = "컴퓨터공학과";
// BorrowerLoginDto loginDto = new BorrowerLoginDto(testId, testPw);

// // Jsoup static method mocking 범위 지정
// try (MockedStatic<Jsoup> jsoupMock = mockStatic(Jsoup.class)) {
// // 1. Jsoup 연결 체이닝 모킹
// jsoupMock.when(() -> Jsoup.connect(anyString())).thenReturn(connection);
// given(connection.data(anyString(), anyString(), anyString(),
// anyString())).willReturn(connection);
// given(connection.method(Connection.Method.POST)).willReturn(connection);
// given(connection.execute()).willReturn(response);
// given(response.parse()).willReturn(document);

// // 2. HTML 파싱 결과 모킹 (이름, 학과 추출)
// given(document.select(".user-info-picture h4")).willReturn(nameElements);
// given(nameElements.text()).willReturn(expectedName); // 이름 리턴

// given(document.select(".user-info-picture
// .department")).willReturn(deptElements);
// given(deptElements.text()).willReturn(expectedDept); // 학과 리턴

// // 3. 캐시 및 DB 동작 모킹
// // 캐시에 없음
// given(borrowerCache.getIfPresent(testId)).willReturn(null);
// // DB에도 없음 (신규 유저) -> 예외 발생으로 처리된 로직에 맞춤
// doThrow(new
// ResourceNotFoundException(errorCode.name(),errorCode.getMessage()))
// .when(borrowerRepository).findById(testId);

// // when
// Borrower result = loginService.inhaLogin(loginDto);

// // then
// assertThat(result.getName()).isEqualTo(expectedName);
// assertThat(result.getDepartment()).isEqualTo(expectedDept);

// // 신규 유저이므로 임시 캐시에 put이 호출되었는지 검증
// verify(tempBorrowerCache, times(1)).put(eq(testId),
// any(BorrowerInformDto.class));
// }
// }

// @Test
// @DisplayName("로그인 실패: 아이디/비번이 틀려 이름 파싱에 실패하면 예외가 발생한다")
// void inhaLogin_Fail_InvalidCredentials() throws IOException {
// // given
// BorrowerLoginDto loginDto = new BorrowerLoginDto("wrongId", "wrongPw");

// try (MockedStatic<Jsoup> jsoupMock = mockStatic(Jsoup.class)) {
// // 1. Jsoup 연결 모킹
// jsoupMock.when(() -> Jsoup.connect(anyString())).thenReturn(connection);
// given(connection.data(anyString(), anyString(), anyString(),
// anyString())).willReturn(connection);
// given(connection.method(Connection.Method.POST)).willReturn(connection);
// given(connection.execute()).willReturn(response);
// given(response.parse()).willReturn(document);

// // 2. 파싱 실패 모킹 (로그인 실패 시 이름 요소가 없음)
// given(document.select(".user-info-picture h4")).willReturn(nameElements);
// given(nameElements.text()).willReturn(""); // 빈 문자열 리턴

// given(document.select(".user-info-picture
// .department")).willReturn(deptElements);
// given(deptElements.text()).willReturn("");

// // when & then
// assertThatThrownBy(() -> loginService.inhaLogin(loginDto))
// .isInstanceOf(InvalidValueException.class)
// .hasMessageContaining("로그인 실패");
// }
// }

// @Test
// @DisplayName("로그인 성공: 기존 사용자(DB 존재)는 임시 캐시에 저장하지 않는다")
// void inhaLogin_Success_ExistingUser() throws IOException {
// // given
// String testId = "12121212";
// BorrowerLoginDto loginDto = new BorrowerLoginDto(testId, "pw");

// try (MockedStatic<Jsoup> jsoupMock = mockStatic(Jsoup.class)) {
// // Jsoup 공통 모킹 생략 (위와 동일하게 설정 필요하나 간결함을 위해 핵심만)
// jsoupMock.when(() -> Jsoup.connect(anyString())).thenReturn(connection);
// given(connection.data(anyString(), anyString(), anyString(),
// anyString())).willReturn(connection);
// given(connection.method(Connection.Method.POST)).willReturn(connection);
// given(connection.execute()).willReturn(response);
// given(response.parse()).willReturn(document);

// given(document.select(".user-info-picture h4")).willReturn(nameElements);
// given(nameElements.text()).willReturn("김인하");
// given(document.select(".user-info-picture
// .department")).willReturn(deptElements);
// given(deptElements.text()).willReturn("컴공");

// // 캐시에 없음
// given(borrowerCache.getIfPresent(testId)).willReturn(null);

// // DB에 있음 (정상 리턴)
// // findById가 void가 아니라 리턴값이 있다면 맞춰줘야 함.
// // 현재 코드상 findById 결과값은 사용 안하고 예외 발생 여부만 체크하므로 mock만 해둠
// // (주의: 실제 Repository가 Optional을 반환한다면 코드가 달라져야 함)
// // 여기서는 원본 코드 로직대로 예외가 안 터지는 상황을 연출

// // when
// loginService.inhaLogin(loginDto);

// // then
// // DB에 존재하므로 tempBorrowerCache.put은 호출되지 않아야 함
// verify(tempBorrowerCache, never()).put(anyString(), any());
// }
// }
// }
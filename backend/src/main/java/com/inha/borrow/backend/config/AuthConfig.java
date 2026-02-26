package com.inha.borrow.backend.config;

import java.util.List;

import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import com.github.benmanes.caffeine.cache.Cache;
import com.inha.borrow.backend.config.auth.handler.LoginFailureHandler;
import com.inha.borrow.backend.config.auth.handler.LoginSuccessHandler;
import com.inha.borrow.backend.model.dto.user.borrower.CacheBorrowerDto;
import com.inha.borrow.backend.service.AdminService;
import com.inha.borrow.backend.service.ItemService;
import com.inha.borrow.backend.service.RequestService;
import com.inha.borrow.backend.service.StudentCouncilFeeVerificationService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inha.borrow.backend.config.auth.admin.AdminAuthenticationFilter;
import com.inha.borrow.backend.config.auth.admin.AdminAuthenticationProvider;
import com.inha.borrow.backend.config.auth.borrowers.BorrowerAuthenticationFilter;
import com.inha.borrow.backend.config.auth.borrowers.BorrowerAuthenticationProvider;
import com.inha.borrow.backend.config.auth.handler.LogoutSuccessHandler;
import com.inha.borrow.backend.enums.ApiErrorCode;
import com.inha.borrow.backend.enums.Role;
import com.inha.borrow.backend.model.dto.apiResponse.ApiResponse;
import com.inha.borrow.backend.model.dto.apiResponse.ErrorResponse;

@Configuration
@EnableWebSecurity(debug = true)
public class AuthConfig {

	private final ObjectMapper objectMapper;

	AuthConfig(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Bean
	LoginFailureHandler loginFailureHandler(ObjectMapper objectMapper) {
		return new LoginFailureHandler(objectMapper);
	}

	@Bean
	BorrowerAuthenticationFilter borrowerAuthenticationFilter(
			AuthenticationManager authenticationManager,
			LoginSuccessHandler loginSuccessHandler,
			LoginFailureHandler loginFailureHandler) {
		BorrowerAuthenticationFilter filter = new BorrowerAuthenticationFilter(authenticationManager);
		filter.setAuthenticationSuccessHandler(loginSuccessHandler);
		filter.setAuthenticationFailureHandler(loginFailureHandler);
		return filter;
	}

	@Bean
	AdminAuthenticationFilter adminAuthenticationFilter(
			AuthenticationManager authenticationManager,
			LoginSuccessHandler loginSuccessHandler,
			LoginFailureHandler loginFailureHandler) {
		AdminAuthenticationFilter filter = new AdminAuthenticationFilter(authenticationManager);
		filter.setAuthenticationSuccessHandler(loginSuccessHandler);
		filter.setAuthenticationFailureHandler(loginFailureHandler);
		return filter;
	}

	@Bean
	SecurityFilterChain securityFilterChain(
			HttpSecurity httpSecurity,
			BorrowerAuthenticationFilter borrowerAuthenticationFilter,
			AdminAuthenticationFilter adminAuthenticationFilter,
			LogoutSuccessHandler logoutSuccessHandler)
			throws Exception {

		httpSecurity
				.csrf((csrf) -> csrf.disable())
				.cors(Customizer -> corsConfigurationSource())
				.formLogin((form) -> form.disable())
				.exceptionHandling(exceptionHandler -> {
					exceptionHandler
							.authenticationEntryPoint((req, res, ex) -> {
								// 인증되지 않은 사용자가 요청을 보냈을 경우 401으로 응답
								ApiErrorCode errorCode = ApiErrorCode.NOT_LOGINED;
								ErrorResponse errorResponse = new ErrorResponse(errorCode.name(),
										errorCode.getMessage());
								ApiResponse<ErrorResponse> apiResponse = new ApiResponse<ErrorResponse>(false,
										errorResponse);
								res.setStatus(HttpStatus.SC_UNAUTHORIZED);
								res.setContentType(ContentType.APPLICATION_JSON.getMimeType());
								res.getWriter().write(objectMapper.writeValueAsString(apiResponse));
							})
							.accessDeniedHandler((req, res, ex) -> {
								// 허용되지 않는 권한을 가진 사용자가 요청을 보냈을 경우 403으로 응답
								ApiErrorCode errorCode = ApiErrorCode.NOT_ALLOWED;
								ErrorResponse errorResponse = new ErrorResponse(errorCode.name(),
										errorCode.getMessage());
								ApiResponse<ErrorResponse> apiResponse = new ApiResponse<ErrorResponse>(false,
										errorResponse);
								res.setStatus(HttpStatus.SC_FORBIDDEN);
								res.setContentType(ContentType.APPLICATION_JSON.getMimeType());
								res.getWriter().write(objectMapper.writeValueAsString(apiResponse));
							});
				})
				.anonymous((anonymous) -> {
					anonymous.disable();
				})
				.logout(logout -> {
					logout.logoutUrl("/logout")
							.invalidateHttpSession(true)
							.deleteCookies("JSESSIONID")
							.logoutSuccessHandler(logoutSuccessHandler);
				})
				.authorizeHttpRequests((authorize) -> {
					authorize
							// 인증과 관련된 경로는 누구나 접근 가능하다.
							.requestMatchers("/borrowers/auth/**", "/admins/login")
							.permitAll()
							//
							// CORS 허용
							.requestMatchers(HttpMethod.OPTIONS, "/**")
							.permitAll()
							//
							// /notice 관련 인증 설정
							// /notice GET 요청은 누구나 접근가능
							.requestMatchers(HttpMethod.GET, "/notices")
							.permitAll()
							// /notice/* GET 요청은 누구나 접근가능
							.requestMatchers(HttpMethod.GET, "/notices/*")
							.permitAll()
							// /notice/* GET이외 요청은 관리자만 접근가능
							.requestMatchers("/notices/*")
							.hasAnyAuthority(Role.DIVISION_MEMBER.name())
							// /notice POST 요청은 관리자만 접근가능
							.requestMatchers(HttpMethod.POST, "/notices")
							.hasAnyAuthority(Role.DIVISION_MEMBER.name())
							//
							// /student-council-fee-verification 관련 인증 설정
							// /student-council-fee-verification GET 요청은 학생회 임원만 가능
							.requestMatchers(HttpMethod.GET, "/student-council-fee-verification")
							.hasAnyAuthority(Role.DIVISION_MEMBER.name())
							// /student-council-fee-verification/permit,dney,modify는 관리자만 접근가능
							.requestMatchers(HttpMethod.PATCH, "/student-council-fee-verification/*/**")
							.hasAnyAuthority(Role.DIVISION_MEMBER.name())
							//
							// /borrowers 관련 인증설정
							// /borrowers 경로는 국장이상만 접근 가능하다
							.requestMatchers("/borrowers")
							.hasAuthority(Role.DIVISION_HEAD.name())
							// /borrowers/{borrower-id} 아래경로는 국원 이상만 접근 가능하다
							.requestMatchers("/borrowers/*/info/ban")
							.hasAuthority(Role.DIVISION_MEMBER.name())
							// /borrower/info와 그 아래 경로는 대여자만 접근가능하다.
							.requestMatchers("/borrowers/info", "/borrowers/info/**")
							.hasAuthority(Role.BORROWER.name())
							// /borrowers/search 경로는 국원 이상만 접근 가능하다
							.requestMatchers("/borrowers/search")
							.hasAuthority(Role.DIVISION_MEMBER.name())
							//
							// /items 관련 인증설정
							// /items 이하 경로에 대한 GET요청은 모든유저가 가능하다. 단, 사용자의 권한에 따라 볼 수 있는 정보가 제한된다.
							.requestMatchers(HttpMethod.GET, "/items", "/items/**")
							.permitAll()
							// /items 이하 경로에 대한 GET을 제외한 요청들은 국원이상만 접근가능하며, 모든정보를 볼 수 있다.
							.requestMatchers(HttpMethod.POST, "/items")
							.hasAuthority(Role.DIVISION_MEMBER.name())
							.requestMatchers(HttpMethod.PUT, "/items/**")
							.hasAuthority(Role.DIVISION_MEMBER.name())
							.requestMatchers(HttpMethod.DELETE, "/items/**")
							.hasAuthority(Role.DIVISION_MEMBER.name())
							//
							// /divisions 관련 인증설정
							// 부서 목록을 가져오는 경로는 국장권한 이상만 접근가능하다
							.requestMatchers(HttpMethod.GET, "/divisions")
							.hasAuthority(Role.DIVISION_HEAD.name())
							// 부서 정보와 관련된 등로그 수정, 삭제 관련 경로는 학생회장만 접근가능하다
							.requestMatchers("/divisions")
							.hasAuthority(Role.PRESIDENT.name())
							//
							// /admins 관련 인증설정
							// 관리자의 목록을 가져오는 경로, 관리자 본인의 개인정보나 비밀번호를 수정하는 경로, 관리자 본인의 정보를 조회하는 경로는
							// 국원권한(DIVISION_MEMBER) 이상만 접근 가능하다
							.requestMatchers("/admins", "/admins/info", "/admins/info/password")
							.hasAuthority(Role.DIVISION_MEMBER.name())
							// 보조관리자를 생성하고 삭제하는 경로는 학생회장(PRESIDENT)만 접근 가능하다
							.requestMatchers("/admins/sub-admin", "/admins/sub-admin/*")
							.hasAuthority(Role.PRESIDENT.name())
							// 관리자의 부서나 직급을 수정하는건 국장권한(DIVISION_HEAD)부터 가능하다
							.requestMatchers("/admins/info/*/division", "/admins/info/*/position")
							.hasAuthority(Role.DIVISION_HEAD.name())
							//
							// /requests 관련 인증설정
							// 대여요청을 조회하는 경로는 대여자나 관리자 권한이면 접근 가능하다
							.requestMatchers(HttpMethod.GET, "/requests", "/requests/*")
							.hasAnyAuthority(Role.BORROWER.name(), Role.DIVISION_MEMBER.name())
							// 대여요청 또는 반납요청을 하는 경로는 대여자 권한으로 로그인 한 사람만 가능하다
							// 대여요청을 수정하거나 취소하는 경로는 대여자 권한으로 로그인 한 사람만 가능하다
							// 자신의 대여요청을 가져오는 경로는 대여자 권한으로 로그인 한 사람만 가능하다
							.requestMatchers(HttpMethod.POST, "/requests")
							.hasAuthority(Role.BORROWER.name())
							.requestMatchers("/requests/*/patch",
									"requests/*/cancel")
							.hasAuthority(Role.BORROWER.name())
							// 대여요청의 상태를 수정하는 경로는 학생회 임원만 가능하다
							// 대여요청의 목록을 여러 조건을 걸어서 가져오는 경로는 학생회 임원만 가능하다
							// 다른 사람의 대여요청 단건조회는 학생회 임원만 가능하다
							.requestMatchers("/requests/*/manage")
							.hasAuthority(Role.DIVISION_MEMBER.name())
							// 대여요청의 목록을 여러 조건을 걸어서 가져오는 경로는 학생회 임원, 대여자 둘 다 가능하다
							.requestMatchers(HttpMethod.GET, "/requests")
							.hasAnyAuthority(Role.BORROWER.name(), Role.DIVISION_MEMBER.name())
							//
							// /responses 관련 인증설정
							.requestMatchers("/responses", "/responses/*")
							.hasAuthority(Role.DIVISION_MEMBER.name())
							// agreement 관련 인증설정
							.requestMatchers("/agreement/borrower/*", "/agreement/version/*")
							.hasAnyAuthority(Role.DIVISION_MEMBER.name())
							.requestMatchers(HttpMethod.POST, "/agreement")
							.hasAnyAuthority(Role.BORROWER.name(), Role.DIVISION_MEMBER.name())
							.requestMatchers(HttpMethod.GET, "/agreement")
							.hasAnyAuthority(Role.DIVISION_MEMBER.name())
							// 이외의 경로는 무조건 인증 필요함
							.anyRequest().authenticated();

				})
				.addFilterAt(adminAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
				.addFilterBefore(borrowerAuthenticationFilter,
						UsernamePasswordAuthenticationFilter.class);

		return httpSecurity.build();
	}

	@Bean
	AuthenticationManager authenticationManager(
			AdminAuthenticationProvider adminAuthenticationProvider,
			BorrowerAuthenticationProvider borrowerAuthenticationProvider) {

		return new ProviderManager(List.of(adminAuthenticationProvider, borrowerAuthenticationProvider));
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();

		// Next.js dev server origin
		config.setAllowedOrigins(List.of("http://localhost:3000"));

		// Allow typical methods + preflight
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

		// Allow headers commonly used by browsers/axios
		config.setAllowedHeaders(List.of("*"));

		// If you use cookies/session (JSESSIONID), this must be true and origin cannot
		// be "*"
		config.setAllowCredentials(true);

		// Cache preflight response (seconds)
		config.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}

	@Bean
	BCryptPasswordEncoder bCryptPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	static RoleHierarchy roleHierarchy() {
		return RoleHierarchyImpl.withRolePrefix("")
				.role(Role.PRESIDENT.name()).implies(Role.VICE_PRESIDENT.name())
				.role(Role.VICE_PRESIDENT.name()).implies(Role.DIVISION_HEAD.name())
				.role(Role.DIVISION_HEAD.name()).implies(Role.DIVISION_MEMBER.name())
				.build();
	}

	@Bean
	LoginSuccessHandler loginSuccessHandler(Cache<String, CacheBorrowerDto> borrowerCache,
			ObjectMapper objectMapper,
			ItemService itemService,
			RequestService requestService,
			StudentCouncilFeeVerificationService studentCouncilFeeVerificationService,
			AdminService adminService) {
		return new LoginSuccessHandler(borrowerCache, objectMapper, itemService, requestService);
	}

	@Bean
	LogoutSuccessHandler logoutSuccessHandler() {
		return new LogoutSuccessHandler();
	}
}

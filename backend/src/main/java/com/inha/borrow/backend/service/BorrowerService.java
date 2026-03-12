package com.inha.borrow.backend.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.inha.borrow.backend.enums.ApiErrorCode;
import com.inha.borrow.backend.enums.Role;
import com.inha.borrow.backend.enums.SearchType;
import com.inha.borrow.backend.model.dto.user.borrower.*;

import com.inha.borrow.backend.model.entity.user.Borrower;
import com.inha.borrow.backend.model.exception.ResourceNotFoundException;

import org.jsoup.Jsoup;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import com.inha.borrow.backend.repository.BorrowerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 대여자와 관련된 작업을 하는 클래스
 * 
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BorrowerService {
    private final BorrowerRepository borrowerRepository;
    private final Cache<String, BorrowerCacheData> borrowerCache;
    private final Cache<String, TempBorrowerInfoCacheData> tempBorrowerCache;
    private final String LOGIN_URL = "https://learn.inha.ac.kr/login/index.php";
    private final List<String> DEPARTMENT_LIST = List.of("소프트웨어융합공학과", "메카트로닉스공학과", "반도체산업융합학과", "금융투자학과", "산업경영학과");

    // --------- 생성 메서드 ---------
    /**
     * 대여자 저장메서드(BorrowerAgreementService사용)
     * 
     * @param dto
     */
    public void saveBorrower(SaveBorrowerDto dto) {
        borrowerRepository.save(dto);
    }

    // --------- 조회 메서드 ---------

    /**
     * 전체 대여자 캐시 반환받는 메서드
     * 
     * @return List<BorrowerCacheData>
     */
    public List<BorrowerCacheData> findAllForCache() {
        return borrowerRepository.findAllForCache();
    }

    /**
     * 대여자 캐시정보 반환하는 메서드
     *
     * @param Borrower
     * @return 대여자 정보
     * @author 형민재
     */
    public BorrowerCacheData findCacheById(Borrower borrower) {
        String borrowerId = borrower.getId();
        BorrowerCacheData borrowerCacheData = borrowerCache.getIfPresent(borrowerId);
        TempBorrowerInfoCacheData tempBorrowerInfoCacheData = tempBorrowerCache.getIfPresent(borrowerId);

        if (borrowerCacheData == null && tempBorrowerInfoCacheData != null) {
            borrowerCacheData = BorrowerCacheData.builder()
                    .id(borrowerId)
                    .name(tempBorrowerInfoCacheData.getName())
                    .department(tempBorrowerInfoCacheData.getDepartment())
                    .build();
        }

        return borrowerCacheData;
    }

    /**
     * 검색 타입에 따라 대여자 정보를 반환하는 함수
     * 
     * @param keyword
     * @param searchType
     * @return
     */
    public List<BorrowerCacheData> searchBorrowerCache(String keyword, SearchType searchType) {
        Set<String> userIds = borrowerCache.asMap().keySet();
        ArrayList<BorrowerCacheData> result = new ArrayList<>();

        userIds.forEach((String id) -> {
            if (searchType == SearchType.ID && id.contains(keyword)) {
                // 학번(ID)로 찾은경우
                result.add(borrowerCache.getIfPresent(id));
            } else {
                // 이름으로 찾은 경우
                BorrowerCacheData current = borrowerCache.getIfPresent(id);
                if (current.getName().contains(keyword)) {
                    result.add(current);
                }
            }
        });

        return result;
    }

    // --------- 수정 메서드 ---------

    /**
     * 대여자의 핸드폰 번호를 수정하는 메서드
     *
     * @param Borrower             borrower
     * @param UpdatePhonenumberDto dto
     * @author 형민재
     */
    public void updatePhoneNumber(Borrower borrower, UpdatePhonenumberDto dto) {
        String borrowerId = borrower.getId();
        borrowerRepository.updatePhoneNumber(borrower, dto);
        // 캐시 초기화
        borrowerCache.invalidate(borrowerId);
        refreshBorrowerCacheData(borrowerId);
    }

    /**
     * 대여자의 반환계좌 정보를 수정하는 메서드
     *
     * @param Borrower               borrower
     * @param UpdateAccountNumberDto dto
     *
     * @author 형민재
     */
    public void updateAccountNumber(Borrower borrower, UpdateAccountNumberDto dto) {
        String borrowerId = borrower.getId();
        borrowerRepository.updateAccountNumber(borrower, dto);
        // 캐시 초기화
        borrowerCache.invalidate(borrowerId);
        refreshBorrowerCacheData(borrowerId);
    }

    /**
     * 대여자의 금지 정보를 수정하는 메서드
     *
     * @param Borrower     borrower
     * @param UpdateBanDto dto
     * @author 형민재
     */
    public void updateBan(Borrower borrower, UpdateBanDto dto) {
        String borrowerId = borrower.getId();
        borrowerRepository.updateBan(borrower, dto);
        // 캐시 초기화
        borrowerCache.invalidate(borrowerId);
        refreshBorrowerCacheData(borrowerId);
    }

    // --------- 특수 메서드 ---------

    /**
     * 특정 사용자 캐시 초기화 메서드
     * 
     * @param borrowerId
     * @return
     */
    public void refreshBorrowerCacheData(String borrowerId) {
        Borrower borrower = Borrower.builder()
                .id(borrowerId)
                .build();
        BorrowerCacheData cacheData = borrowerRepository.findByIdForCache(borrower);
        borrowerCache.put(borrowerId, cacheData);
    }

    /**
     * i-class 연동 로그인 메서드
     *
     * @param borrowerLoginDto
     * @author 형민재
     */
    public Borrower inhaLogin(BorrowerLoginDto borrowerLoginDto) {
        try {
            List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(Role.BORROWER.name()));
            Response response = Jsoup.connect(LOGIN_URL)
                    .data("username", borrowerLoginDto.getId(), "password", borrowerLoginDto.getPassword())
                    .method(Method.POST)
                    .execute();

            // HTML에서 이름과 학과 추출
            Document doc = response.parse();
            String name = doc.select(".user-info-picture h4").text();
            String department = doc.select(".user-info-picture .department").text();

            if (name.isEmpty()) {
                // 이름이 null이면 로그인 실패
                throw new BadCredentialsException(ApiErrorCode.CHECK_YOUR_INFO.name());
            }

            if (!DEPARTMENT_LIST.contains(department)) {
                // 타 단과대는 이용 불가
                throw new BadCredentialsException(ApiErrorCode.INVALID_ID.name());
            }
            Borrower borrower = Borrower.builder()
                    .id(borrowerLoginDto.getId())
                    .name(name)
                    .department(department)
                    .authorities(authorities)
                    .build();

            // 캐시에 있는 사용자인지 확인
            BorrowerCacheData borrowerCacheData = borrowerCache.getIfPresent(borrowerLoginDto.getId());

            if (borrowerCacheData == null) {
                // 캐쉬에 없는 대여자라면 DB에 있는지 확인(1시간 마다 동기화 되므로)
                try {
                    Borrower tempBorrower = Borrower.builder()
                            .id(borrowerLoginDto.getId())
                            .build();
                    borrowerRepository.findByIdForCache(tempBorrower);
                } catch (ResourceNotFoundException e) {
                    // 캐시에 임시저장하기 위한 객체
                    TempBorrowerInfoCacheData tempBorrowerInfoCache = new TempBorrowerInfoCacheData(name, department);
                    // db에 정보 있는지 확인 후 없다면 신규 사용자 이므로 임시 캐쉬에 저장
                    tempBorrowerCache.put(borrowerLoginDto.getId(), tempBorrowerInfoCache);
                }
            }

            return borrower;
        } catch (IOException e) {
            throw new RuntimeException("로그인 시도 중 연결 실패", e);
        }
    }
}

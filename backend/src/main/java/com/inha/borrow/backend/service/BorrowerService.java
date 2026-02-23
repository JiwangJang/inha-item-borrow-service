package com.inha.borrow.backend.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.inha.borrow.backend.cache.CacheScheduledTask;
import com.inha.borrow.backend.enums.ApiErrorCode;
import com.inha.borrow.backend.enums.Role;
import com.inha.borrow.backend.enums.SearchType;
import com.inha.borrow.backend.model.dto.user.borrower.*;

import com.inha.borrow.backend.model.entity.StudentCouncilFeeVerification;
import com.inha.borrow.backend.model.entity.user.Borrower;
import com.inha.borrow.backend.model.exception.ResourceNotFoundException;

import com.inha.borrow.backend.repository.StudentCouncilFeeVerificationRepository;
import org.jsoup.Jsoup;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.springframework.security.access.AccessDeniedException;
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
    private final StudentCouncilFeeVerificationRepository studentCouncilFeeVerificationRepository;
    private final Cache<String, CacheBorrowerDto> borrowerCache;
    private final Cache<String, TempBorrowerInfoDto> tempBorrowerCache;
    private final CacheScheduledTask cacheScheduledTask;
    private final String LOGIN_URL = "https://learn.inha.ac.kr/login/index.php";

    public CacheBorrowerDto getMyInfo(String borrowerId) {
        CacheBorrowerDto result = borrowerCache.getIfPresent(borrowerId);

        return result;
    }

    /**
     * i-class를 활용한 로그인 메서드
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
                throw new BadCredentialsException("");
            }

            Borrower borrower = Borrower.builder()
                    .id(borrowerLoginDto.getId())
                    .name(name)
                    .department(department)
                    .authorities(authorities)
                    .build();

            // 있는 사용자인지 확인
            CacheBorrowerDto dto = borrowerCache.getIfPresent(borrowerLoginDto.getId());

            if (dto == null) {
                // 캐쉬에 없는 대여자라면 DB에 있는지 확인(1시간 마다 동기화 되므로)
                try {
                    borrowerRepository.findById(borrowerLoginDto.getId());
                } catch (ResourceNotFoundException e) {
                    // 캐시에 임시저장하기 위한 DTO
                    TempBorrowerInfoDto borrowerInformDto = new TempBorrowerInfoDto(name, department);
                    // db에 정보 있는지 확인 후 없다면 신규 사용자 이므로 임시 캐쉬에 저장
                    tempBorrowerCache.put(borrowerLoginDto.getId(), borrowerInformDto);
                }
            }

            return borrower;
        } catch (IOException e) {
            throw new RuntimeException("로그인 시도 중 연결 실패", e);
        }
    }

    /**
     * 대여자를 id로 찾는 메서드
     *
     * @param id
     * @return 대여자 정보
     * @author 형민재
     */

    public Borrower findById(String id) {
        CacheBorrowerDto dto = borrowerCache.getIfPresent(id);
        if (dto == null) {
            dto = cacheScheduledTask.refreshBorrowerCache(id);
        }
        Borrower borrower = Borrower.builder()
                .id(dto.getId())
                .name(dto.getName())
                .department(dto.getDepartment())
                .phonenumber(dto.getPhoneNumber())
                .accountNumber(dto.getAccountNumber())
                .ban(dto.isBan())
                .build();
        return borrower;
    }

    /**
     * 대여자들의 정보를 반환하는 메서드
     *
     * @return 대여자 정보
     * @author 형민재
     */

    public List<Borrower> findAll() {
        return borrowerRepository.findAll();
    }

    /**
     * 검색 타입에 따라 대여자 정보를 반환하는 함수
     * 
     * @param keyword
     * @param searchType
     * @return
     */
    public List<CacheBorrowerDto> searchBorrower(String keyword, SearchType searchType) {
        Set<String> userIds = borrowerCache.asMap().keySet();
        ArrayList<CacheBorrowerDto> result = new ArrayList<>();

        userIds.forEach((String id) -> {
            if (searchType == SearchType.ID && id.contains(keyword)) {
                // 학번(ID)로 찾은경우
                result.add(borrowerCache.getIfPresent(id));
            } else {
                // 이름으로 찾은 경우
                CacheBorrowerDto current = borrowerCache.getIfPresent(id);
                if (current.getName().contains(keyword)) {
                    result.add(current);
                }
            }
        });

        return result;
    }

    /**
     * 대여자의 이름을 수정하는 메서드
     *
     * @param name
     * @param id
     * @author 형민재
     */
    public void patchName(String name, String id) {
        borrowerRepository.patchName(name, id);
        deleteCache(id);
    }

    /**
     * 대여자의 핸드폰 번호를를 수정하는 메서드
     *
     * @param borrowerId
     * @param dto
     * @author 형민재
     */
    public void patchPhoneNumber(String borrowerId, PatchPhonenumberDto dto) {
        String newPhonenumber = dto.getNewPhonenumber();
        borrowerRepository.patchPhoneNumber(newPhonenumber, borrowerId);
        deleteCache(borrowerId);
    }

    /**
     * 대여자의 반환계좌 정보를 수정하는 메서드
     *
     * @param accountNumber
     * @param id
     *
     * @author 형민재
     */
    public void patchAccountNumber(String accountNumber, String id) {
        borrowerRepository.patchAccountNumber(accountNumber, id);
        deleteCache(id);
    }

    /**
     * 아이디로 대여자의 전화번호 계좌번호를 저장하는 메서드
     *
     * @param dto
     * @param id
     *
     * @author 형민재
     */
    public void savePhoneAccountNumber(String id, SavePhoneAccountNumberDto dto) {
        StudentCouncilFeeVerification council = studentCouncilFeeVerificationRepository.findRequestByBorrowerId(id);
        if (council != null && council.isVerify()) {
            borrowerRepository.savePhoneAccountNumber(id, dto);
        } else {
            ApiErrorCode apiErrorCode = ApiErrorCode.NOT_ALLOWED_COUNCIL_FEE;
            throw new AccessDeniedException(apiErrorCode.name() + ":" + apiErrorCode.getMessage());
        }
    }

    /**
     * 대여자의 금지 정보를 수정하는 메서드
     *
     * @param ban
     * @param id
     * @author 형민재
     */
    public void patchBan(boolean ban, String id) {
        borrowerRepository.patchBan(ban, id);
        deleteCache(id);
    }

    public void deleteCache(String borrowerId) {
        borrowerCache.invalidate(borrowerId);
    }
}

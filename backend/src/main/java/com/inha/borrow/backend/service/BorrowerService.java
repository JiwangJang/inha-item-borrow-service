package com.inha.borrow.backend.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.inha.borrow.backend.enums.Role;
import com.inha.borrow.backend.model.dto.user.borrower.TempBorrowerInfoDto;
import com.inha.borrow.backend.model.dto.user.borrower.BorrowerLoginDto;
import com.inha.borrow.backend.model.dto.user.borrower.CacheBorrowerDto;
import com.inha.borrow.backend.model.dto.user.borrower.PatchPhonenumberDto;

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
import java.util.List;

/**
 * 대여자와 관련된 작업을 하는 클래스
 * 
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BorrowerService {
    private final BorrowerRepository borrowerRepository;
    private final Cache<String, CacheBorrowerDto> borrowerCache;
    private final Cache<String, TempBorrowerInfoDto> tempBorrowerCache;
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
        return borrowerRepository.findById(id);
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
     * 대여자의 정보를 수정하는 메서드
     *
     * @param name
     * @param id
     * @author 형민재
     */
    public void patchName(String name, String id) {
        borrowerRepository.patchName(name, id);
    }

    /**
     * 대여자의 정보를 수정하는 메서드
     *
     * @param borrowerId
     * @param dto
     * @author 형민재
     */
    public void patchPhoneNumber(String borrowerId, PatchPhonenumberDto dto) {
        String newPhonenumber = dto.getNewPhonenumber();
        borrowerRepository.patchPhoneNumber(newPhonenumber, borrowerId);
    }

    /**
     * 대여자의 정보를 수정하는 메서드
     *
     * @param accountNumber
     * @param id
     * @author 형민재
     */
    public void patchAccountNumber(String accountNumber, String id) {
        borrowerRepository.patchAccountNumber(accountNumber, id);
    }

    /**
     * 대여자의 정보를 수정하는 메서드
     *
     * @param ban
     * @param id
     * @author 형민재
     */
    public void patchBan(boolean ban, String id) {
        borrowerRepository.patchBan(ban, id);
    }
}

package com.inha.borrow.backend.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.inha.borrow.backend.enums.ApiErrorCode;
import com.inha.borrow.backend.model.dto.user.borrower.BorrowerInformDto;
import com.inha.borrow.backend.model.dto.user.borrower.BorrowerLoginDto;
import com.inha.borrow.backend.model.dto.user.borrower.CacheBorrowerDto;
import com.inha.borrow.backend.model.entity.user.Borrower;
import com.inha.borrow.backend.model.exception.InvalidValueException;
import com.inha.borrow.backend.model.exception.ResourceNotFoundException;
import com.inha.borrow.backend.repository.BorrowerRepository;
import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class LoginService {
    private final Cache<String, CacheBorrowerDto> borrowerCache;
    private final Cache<String, BorrowerInformDto> tempBorrowerCache;
    private final BorrowerRepository borrowerRepository;
    String url = "https://learn.inha.ac.kr/login/index.php";


    /**
     * i-class를 활용한 로그인 메서드
     *
     * @param borrowerLoginDto
     * @author 형민재
     */
    public Borrower inhaLogin(BorrowerLoginDto borrowerLoginDto) {
        try {
            Connection.Response response = Jsoup.connect(url)
                    .data("username", borrowerLoginDto.getId(), "password", borrowerLoginDto.getPassword())
                    .method(Connection.Method.POST)
                    .execute();

            Document doc = response.parse();
            String name = doc.select(".user-info-picture h4").text();
            String department = doc.select(".user-info-picture .department").text();
            if (name.isEmpty()) { //null 체크로 로그인 성공실패 확인
                ApiErrorCode apiErrorCode = ApiErrorCode.INVALID_ID_OR_PASSWORD;
                throw new InvalidValueException(apiErrorCode.name(), apiErrorCode.getMessage());
            }
            BorrowerInformDto borrowerInformDto = new BorrowerInformDto(name, department);
            Borrower borrower = new Borrower(null,name,null,null,false,null,department);
            CacheBorrowerDto dto = borrowerCache.getIfPresent(borrowerLoginDto.getId());
            boolean isExistInDb = true;
            if (dto == null) { // 캐쉬에 정보 있는지 확인
                try {
                    borrowerRepository.findById(borrowerLoginDto.getId());
                } catch (ResourceNotFoundException e) {
                    isExistInDb = false;
                }
                if (!isExistInDb) { // db에 정보 있는지 확인 후 없다면 신규 사용자 이므로 임시 캐쉬에 저장
                    tempBorrowerCache.put(borrowerLoginDto.getId(), borrowerInformDto);
                }
            }
            return borrower; // 이름과 학과만 채우고 나머지는 null 반환
        } catch (IOException e) {
            throw new RuntimeException("로그인 시도 중 연결 실패", e);
        }
    }
}

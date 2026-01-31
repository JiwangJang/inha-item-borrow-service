package com.inha.borrow.backend.service;

import com.inha.borrow.backend.enums.ApiErrorCode;
import com.inha.borrow.backend.model.dto.user.borrower.BorrowerLoginDto;
import com.inha.borrow.backend.model.exception.InvalidValueException;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class PortalLoginService {
    String url = "https://learn.inha.ac.kr/login/index.php";

    public BorrowerLoginDto inhaLogin(String userId, String userPassword) {
        try {
            Connection.Response response = Jsoup.connect(url)
                    .data("username", userId, "password", userPassword)
                    .method(Connection.Method.POST)
                    .execute();

            Document doc = response.parse();
            String name = doc.select(".user-info-picture h4").text();
            String department = doc.select(".user-info-picture .department").text();
            if (name.isEmpty()) {
                ApiErrorCode apiErrorCode = ApiErrorCode.INVALID_ID_OR_PASSWORD;
                throw new InvalidValueException(apiErrorCode.name(), apiErrorCode.getMessage());
            }
            BorrowerLoginDto borrowerLoginDto = new BorrowerLoginDto(name, department);
            return borrowerLoginDto;
        } catch (IOException e) {
            throw new RuntimeException("로그인 시도 중 연결 실패",e);
        }
    }
}

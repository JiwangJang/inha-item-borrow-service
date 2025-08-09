package com.inha.borrow.backend.service;

import java.security.SecureRandom;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.stereotype.Service;

import com.inha.borrow.backend.cache.IdCache;
import com.inha.borrow.backend.cache.SMSCodeCache;
import com.inha.borrow.backend.cache.SignUpSessionCache;
import com.inha.borrow.backend.enums.ApiErrorCode;
import com.inha.borrow.backend.model.auth.SMSCode;
import com.inha.borrow.backend.model.exception.InvalidValueException;
import com.inha.borrow.backend.model.exception.MessageServiceException;
import com.inha.borrow.backend.model.exception.ResourceNotFoundException;

import lombok.AllArgsConstructor;
import net.nurigo.sdk.message.exception.NurigoEmptyResponseException;
import net.nurigo.sdk.message.exception.NurigoMessageNotReceivedException;
import net.nurigo.sdk.message.model.Message;

@Service
@AllArgsConstructor
public class BorrowerVerificationService {
    private final DefaultMessageService messageService;
    private final IdCache idCache;
    private final SMSCodeCache smsCodeCache;
    private final SignUpSessionCache signUpSessionCache;

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final int VERIFICATION_CODE_LENGTH = 6;

    private String getSMSCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < VERIFICATION_CODE_LENGTH; i++) {
            code.append(secureRandom.nextInt(10));
        }
        return code.toString();
    }

    public void verifyId(String id) {
        if (!id.matches("^[a-zA-Z0-9]{4,10}$")) {
            ApiErrorCode errorCode = ApiErrorCode.INVALID_ID;
            throw new InvalidValueException(errorCode.name(), errorCode.getMessage());
        }
        if (idCache.contains(id)) {
            ApiErrorCode errorCode = ApiErrorCode.EXIST_ID;
            throw new InvalidValueException(errorCode.name(), errorCode.getMessage());
        }

        signUpSessionCache.set(id);
    }

    public void verifyPassword(String id, String password) {
        // 조건 : 영어 대소문자와 숫자, 특수기호(!@#$%^&*()_\-+=)를 포함하여 9~13자
        if (!password.matches(
                "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_\\-+=])[A-Za-z\\d!@#$%^&*()_\\-+=]{9,13}$")) {
            ApiErrorCode errorCode = ApiErrorCode.INVALID_PASSWORD;
            throw new InvalidValueException(errorCode.name(), errorCode.getMessage());
        }
        signUpSessionCache.passwordCheckSuccess(id);
    }

    public void sendSMSCode(String id, String phoneNumber) {
        // 사용자가 몇번보냈는지 확인 필요
        // String code = getVerificationCode();
        // Message message = new Message();
        // 폰번호는 000-0000-0000에서 -를 빼고 입력
        // message.setFrom("01088800495");
        // message.setTo(phoneNumber);
        // message.setText("인하대학교 미래융합대학 물품대여서비스입니다. 인증번호는 [" + code + "] 입니다.");

        // try {
        // messageService.send(message);
        // } catch (Exception e) {
        // throw new MessageServiceException();
        // }

        // 만료됐거나 없는 아이디인지 확인
        signUpSessionCache.get(id);
        // 임시 코드
        String code = "123456";
        smsCodeCache.set(id, new SMSCode(code));

        signUpSessionCache.extendTtl(id);
    }

    public void verifySMSCode(String id, String inputedCode) {
        SMSCode code = smsCodeCache.get(id);
        if (!code.getCode().equals(inputedCode)) {
            ApiErrorCode errorCode = ApiErrorCode.INCORRECT_CODE;
            throw new InvalidValueException(errorCode.name(), errorCode.getMessage());
        }

        signUpSessionCache.phoneCheckSuccess(id);
        smsCodeCache.remove(id);
    }
}

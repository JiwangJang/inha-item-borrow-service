package com.inha.borrow.backend.cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.springframework.stereotype.Component;

import com.inha.borrow.backend.model.auth.SignUpSession;
import com.inha.borrow.backend.model.exception.SignUpSessionExpiredException;
import com.inha.borrow.backend.model.exception.ResourceNotFoundException;
import com.inha.borrow.backend.util.ServiceUtils;

/**
 * 대여자가 회원가입신청 할 때 대여자의 인증상황을 저장해두는 클래스
 * 
 * @author 장지왕
 */
@Component
public class SignUpSessionCache {
    private final ConcurrentHashMap<String, SignUpSession> cache = new ConcurrentHashMap<>();

    private void computeIfValid(String id, Consumer<SignUpSession> consumer) {
        SignUpSession session = get(id);
        if (session != null) {
            consumer.accept(session);
        }
    }

    /**
     * 특정 대여자의 인증상황을 가져오는 메서드
     * 
     * @param id 대상 대여자 아이디
     * @return SignUpSession
     * @throws ResourceNotFoundException 저장되지 않은 유저의 상태를 가져오려 할때
     * @throws
     */
    public SignUpSession get(String id) {
        SignUpSession session = cache.get(id);
        if (session == null)
            throw new ResourceNotFoundException();
        if (session.getTtl() <= System.currentTimeMillis()) {
            remove(id);
            throw new SignUpSessionExpiredException();
        }

        return session;
    }

    /**
     * 특정유저의 인증상황을 새로 저장하는 메서드
     * <p>
     * 대여자가 아이디 중복검사 및 조건검사를 하고 메서드를 호출할 것
     * <p>
     * 기본적으로 아이디 중복검사 및 조건검사가 완료됐다고 간주
     * 
     * @param id 대상 대여자 아이디
     */
    public void set(String id) {
        cache.put(id, new SignUpSession());
    }

    /**
     * 특정유저의 인증상황을 지우는 메서드
     * <p>
     * ttl이 지난 경우나 신청을 마친 경우 사용
     * 
     * @param id 대상 대여자 아아디
     */
    public void remove(String id) {
        cache.remove(id);
    }

    /**
     * 핸드폰 인증을 완료했음을 표시하는 메서드
     * 검증 완료후 유효기간을 늘려준다
     * 
     * @param id 대상 대여자 아이디
     */
    public void phoneCheckSuccess(String id) {
        computeIfValid(id, (session) -> {
            session.setPhoneCheck(true);
            session.setTtl(ServiceUtils.getTtl());
        });
    }

    /**
     * 비밀번호 검증을 완료했음을 표시하는 메서드
     * 검증 완료후 유효기간을 늘려준다
     * 
     * @param id 대상 대여자 아이디
     */
    public void PasswordCheckSuccess(String id) {
        computeIfValid(id, (session) -> {
            session.setPasswordCheck(true);
            session.setTtl(ServiceUtils.getTtl());
        });
    }

    /**
     * 세션연장 메서드
     * 
     * @param id 대상 대여자 아아디
     */
    public void extendTtl(String id) {
        // 인증요청 버튼 눌렀을 때 세션 연장하기 위힌 메서드
        cache.computeIfPresent(id, (k, v) -> {
            v.setTtl(ServiceUtils.getTtl());
            return v;
        });
    }

    /**
     * 최종 회원가입 신청시 모든 검증을 통과했는지 확인하는 메서드
     * 
     * @param id 대상 대여자 아아디
     * @return T or F
     * @throws ResourceNotFoundException
     */
    public boolean isAllPassed(String id) {
        SignUpSession session = cache.get(id);
        return session.isIdCheck() && session.isPasswordCheck() && session.isPhoneCheck();
    }
}
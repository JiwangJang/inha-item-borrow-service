package com.inha.borrow.backend.cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.springframework.stereotype.Component;

import com.inha.borrow.backend.enums.ApiErrorCode;
import com.inha.borrow.backend.model.auth.SignUpSession;
import com.inha.borrow.backend.model.exception.InvalidValueException;
import com.inha.borrow.backend.model.exception.ResourceNotFoundException;
import com.inha.borrow.backend.util.ServiceUtils;

/**
 * 대여자가 회원가입신청 할 때 대여자의 인증상황을 저장해두는 클래스
 * 
 * @author 장지왕
 */
@Component
public class SignUpSessionCache {
    private final IdCache idCache;
    private final ConcurrentHashMap<String, SignUpSession> cache = new ConcurrentHashMap<>();

    public SignUpSessionCache(IdCache idCache) {
        this.idCache = idCache;
    }

    /**
     * 유효한 회원가입 세션에 대해서만 consumer를 동작시키는 메서드
     * 
     * @param id       회원가입 하려는 아이디
     * @param consumer 해당 객체에 적용할 동작
     */
    private void computeIfValid(String id, Consumer<SignUpSession> consumer) {
        SignUpSession session = get(id);
        if (session != null) {
            consumer.accept(session);
        }
    }

    /**
     * 주기적으로 오래된 회원가입 세션을 지워주는 메서드
     */
    public void removeOldSignUpSession() {
        cache.forEach((id, session) -> {
            long ttl = session.getTtl();
            if (ttl <= System.currentTimeMillis()) {
                cache.remove(id);
            }
        });
    }

    /**
     * 특정 대여자의 인증상황을 가져오는 메서드
     * 
     * @param id 대상 대여자 아이디
     * @return SignUpSession
     * @throws ResourceNotFoundException 저장되지 않은 유저의 상태를 가져오려 할때
     * @throws InvalidValueException     회원가입 세션이 만료됐을때
     */
    public SignUpSession get(String id) {
        SignUpSession session = cache.get(id);
        if (session == null) {
            ApiErrorCode errorCode = ApiErrorCode.NOT_FOUND;
            throw new ResourceNotFoundException(errorCode.name(), errorCode.getMessage());
        }
        if (session.getTtl() <= System.currentTimeMillis()) {
            remove(id);
            ApiErrorCode errorCode = ApiErrorCode.SIGN_UP_SESSION_EXPIRED;
            throw new InvalidValueException(errorCode.name(), errorCode.getMessage());
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
        idCache.setNewUser(id);
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
        idCache.extendTtl(id);
    }

    /**
     * 비밀번호 검증을 완료했음을 표시하는 메서드
     * 검증 완료후 유효기간을 늘려준다
     * 
     * @param id 대상 대여자 아이디
     */
    public void passwordCheckSuccess(String id) {
        computeIfValid(id, (session) -> {
            session.setPasswordCheck(true);
            session.setTtl(ServiceUtils.getTtl());
        });
        idCache.extendTtl(id);
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
        idCache.extendTtl(id);
    }

    /**
     * 최종 회원가입 신청시 모든 검증을 통과했는지 확인하는 메서드
     * 모두 통과했다면 아이디를 영구로 전환하고 SignUpSession을 삭제한다
     * 
     * @param id 대상 대여자 아아디
     * @return T or F
     * @throws ResourceNotFoundException
     */
    public boolean isAllPassed(String id) {
        SignUpSession session = get(id);
        boolean allPass = session.isIdCheck() && session.isPasswordCheck() && session.isPhoneCheck();
        if (allPass) {
            // SignUpSession 삭제
            remove(id);
            // 아이디 영구전환
            idCache.fixSignUpId(id);
        }
        return allPass;
    }

    // 테스트용 메서드
    /**
     * 테스트용 세션을 등록하는 메서드
     * 
     * @param id
     * @param ttl
     */
    public void setForTest(String id, long ttl) {
        cache.put(id, new SignUpSession(ttl));
    }

    /**
     * 캐시 초기화 메서드
     */
    public void deleteAll() {
        cache.clear();
    }
}
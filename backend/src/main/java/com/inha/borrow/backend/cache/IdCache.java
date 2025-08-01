package com.inha.borrow.backend.cache;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.inha.borrow.backend.util.ServiceUtils;

import jakarta.annotation.PostConstruct;

/**
 * 현재 가입시도중이거나 가입된 아이디 목록을 저장하는 클래스
 * 
 * @author 장지왕
 */
@Component
public class IdCache {
    // 아이디가 키, 유효시간(방금 아이디 검증한 경우, 10분인데 새로운 요청할 때마다 증가)이 값
    private final ConcurrentHashMap<String, Long> cache = new ConcurrentHashMap<>();
    private final JdbcTemplate jdbcTemplate;

    public IdCache(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void init() {
        String sql = "SELECT id FROM borrower UNION SELECT id FROM signup_request;";
        List<String> idList = jdbcTemplate.queryForList(sql, String.class);
        for (String id : idList) {
            setOldUser(id);
        }
    }

    /**
     * 아이디가 존재하는지 확인하는 메서드
     * 
     * @param id
     * @return 아이디 존재여부
     */
    public boolean contains(String id) {
        Long ttl = cache.get(id);
        if (ttl == null) {
            return false;
        }
        if (ttl == 0L) {
            return true; // 기존 유저
        }
        boolean isValid = ttl > System.currentTimeMillis();
        if (!isValid) {
            remove(id);
        }
        return isValid;
    }

    public void setOldUser(String id) {
        cache.put(id, 0l);
    }

    /**
     * 아이디 추가 메서드(신청하려는 유저)
     * 
     * @param id 대여자 아이디
     */
    public void setNewUser(String id) {
        // 회원가입 캐시와 똑같이 10분을 준다
        cache.put(id, ServiceUtils.getTtl());
    }

    /**
     * 아이디 삭제 메서드
     * 사용자가 탈퇴할 때나 유효기간이 만료된 아이디 제거할 때 사용
     * 
     * @param id 대여자 아아디
     */
    public void remove(String id) {
        cache.remove(id);
    }

    /**
     * 아이디 수정 메서드
     * 사용자가 아이디를 수정했을때 사용
     * 
     * @param oldId 기존 아이디
     * @param newId 새로운 아이디
     */
    public void revise(String oldId, String newId) {
        cache.remove(oldId);
        cache.put(newId, 0l);
    }

    /**
     * 아이디 수명 연장 메서드
     */
    public void extendTtl(String id) {
        cache.computeIfPresent(id, (k, v) -> {
            return ServiceUtils.getTtl();
        });
    }

    /**
     * 회원가입 신청시 사용하는 메서드
     * ttl값을 0으로 둬서 영구적인 아이디로 변환
     */
    public void fixSignUpId(String id) {
        cache.computeIfPresent(id, (k, v) -> {
            return 0l;
        });
    }
}

package com.inha.borrow.backend.cache;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * 현재 가입시도중이거나 가입된 아이디 목록을 저장하는 클래스
 * 
 * @author 장지왕
 */
@Component
public class IdCache {
    public Set<String> cache = ConcurrentHashMap.newKeySet();
    private final JdbcTemplate jdbcTemplate;

    public IdCache(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void init() {
        String sql = "SELECT id FROM borrower UNION SELECT id FROM signup_request;";
        List<String> idList = jdbcTemplate.queryForList(sql, String.class);
        for (String id : idList) {
            add(id);
        }
    }

    /**
     * 아이디가 존재하는지 확인하는 메서드
     * 
     * @param id
     * @return 아이디 존재여부
     */
    public boolean contains(String id) {
        return cache.contains(id);
    }

    /**
     * 아이디 추가 메서드
     * 
     * @param id 대여자 아이디
     */
    public void add(String id) {
        cache.add(id);
    }

    /**
     * 아이디 삭제 메서드
     * 사용자가 탈퇴할 때 사용
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
        cache.add(newId);
    }
}

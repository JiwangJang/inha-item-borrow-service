package com.inha.borrow.backend.service;

import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.inha.borrow.backend.repository.BorrowerRepository;

import lombok.AllArgsConstructor;

/**
 * 대여자와 관련된 작업을 하는 클래스
 * 
 */
@Service
@AllArgsConstructor
public class BorrowerService implements UserDetailsService {
    private BorrowerRepository borrowerRepository;

    /**
     * 대여자 계정 정보를 가져오는 메서드
     * 대여자 인증과정에서 사용됨
     * 
     * @param id 대여자 아이디
     * @return UserDetails 대여자 정보
     * @author 장지왕
     */
    @Override
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
        try {
            return borrowerRepository.findById(id);
        } catch (Exception e) {
            if (e.getClass() == IncorrectResultSizeDataAccessException.class) {
                throw new UsernameNotFoundException("등록되지 않은 사용자입니다.");
            } else {
                throw e;
            }
        }
    }
}

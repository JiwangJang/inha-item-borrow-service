package com.inha.borrow.backend.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.inha.borrow.backend.model.exception.ResourceNotFoundException;
import com.inha.borrow.backend.repository.AdminRepository;

import lombok.AllArgsConstructor;

/**
 * 관리자와 관련된 작업을 하는 클래스
 * 
 */
@Service
@AllArgsConstructor
public class AdminService implements UserDetailsService {
    private AdminRepository adminRepository;

    /**
     * 관리자 계정 정보를 가져오는 메서드
     * 관리자 인증과정에서 사용됨
     * 
     * @param id 관리자 아이디
     * @return UserDetails 관리자 정보
     * @author 장지왕
     */
    @Override
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
        try {
            return adminRepository.findById(id);
        } catch (ResourceNotFoundException e) {
            throw new UsernameNotFoundException(id);
        }
    }
}

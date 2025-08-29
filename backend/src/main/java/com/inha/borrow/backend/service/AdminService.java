package com.inha.borrow.backend.service;

import java.util.List;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.inha.borrow.backend.enums.ApiErrorCode;
import com.inha.borrow.backend.model.dto.user.admin.SaveAdminDto;
import com.inha.borrow.backend.model.dto.user.admin.UpdateAdminInfoDto;
import com.inha.borrow.backend.model.dto.user.admin.UpdateDivisionDto;
import com.inha.borrow.backend.model.dto.user.admin.UpdatePasswordDto;
import com.inha.borrow.backend.model.dto.user.admin.UpdatePositionDto;
import com.inha.borrow.backend.model.entity.user.Admin;
import com.inha.borrow.backend.model.exception.InvalidValueException;
import com.inha.borrow.backend.model.exception.ResourceNotFoundException;
import com.inha.borrow.backend.repository.AdminRepository;

import lombok.RequiredArgsConstructor;

/**
 * 관리자와 관련된 작업을 하는 클래스
 * 
 */
@Service
@RequiredArgsConstructor
public class AdminService implements UserDetailsService {
    private final PasswordEncoder passwordEncoder;
    private final AdminRepository adminRepository;

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

    public Admin findById(String id) {
        return adminRepository.findById(id);
    }

    public List<Admin> findAllAdmins() {
        return adminRepository.findAllAdmins();
    }

    public void saveAdmin(SaveAdminDto saveAdminDto) {
        adminRepository.saveAdmin(saveAdminDto);
    }

    public void updateAdminInfo(String id, UpdateAdminInfoDto updateAdminInfoDto) {
        adminRepository.updateAdminInfo(id, updateAdminInfoDto);
    }

    public void updatePassword(String id, UpdatePasswordDto updatePasswordDto) {
        String originPassword = adminRepository.findPasswordById(id);
        if (!passwordEncoder.matches(updatePasswordDto.getOriginPassword(), originPassword)) {
            ApiErrorCode errorCode = ApiErrorCode.INCORRECT_PASSWORD;
            throw new InvalidValueException(errorCode.name(), errorCode.getMessage());
        }
        String encodedPassword = passwordEncoder.encode(updatePasswordDto.getNewPassword());
        adminRepository.updatePassword(id, encodedPassword);
    }

    public void updatePosition(Admin admin, String targetAdminId, UpdatePositionDto updatePositionDto) {
        adminRepository.updatePosition(admin, targetAdminId, updatePositionDto);
    }

    public void updateDivision(Admin admin, String targetAdminId, UpdateDivisionDto updateDivisionDto) {
        adminRepository.updateDivision(admin, targetAdminId, updateDivisionDto);
    }

    public void deleteAdmin(String id) {
        adminRepository.deleteAdmin(id);
    }
}

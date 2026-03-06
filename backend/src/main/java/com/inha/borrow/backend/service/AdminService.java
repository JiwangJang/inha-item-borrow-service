package com.inha.borrow.backend.service;

import java.util.List;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.inha.borrow.backend.enums.ApiErrorCode;
import com.inha.borrow.backend.model.dto.user.admin.SaveAdminDto;
import com.inha.borrow.backend.model.dto.user.admin.UpdatePasswordDto;
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

    // --------- 생성 메서드 ---------

    /**
     * 새로운 관리자를 등록하는 메서드
     * 
     * @param saveAdminDto
     */
    public void saveAdmin(SaveAdminDto saveAdminDto) {
        adminRepository.saveAdmin(saveAdminDto);
    }

    // --------- 조회 메서드 ---------

    /**
     * 관리자 계정 정보를 가져오는 메서드
     * 관리자 인증과정에서 사용됨
     * 
     * @param id 관리자 아이디
     * @return UserDetails 관리자 정보
     * @author 장지왕
     */
    @Override
    public UserDetails loadUserByUsername(String adminId) throws UsernameNotFoundException {
        try {
            Admin admin = Admin.builder()
                    .id(adminId)
                    .build();
            return adminRepository.findById(admin);
        } catch (ResourceNotFoundException e) {
            throw new UsernameNotFoundException(adminId);
        }
    }

    /**
     * DB에서 관리자를 조회하는 메서드(나중에 캐시로 바꿀 필요 있음)
     * 
     * @param admin
     * @return
     */
    public Admin findById(Admin admin) {
        Admin result = adminRepository.findById(admin);
        result.setPassword(null);
        return result;
    }

    /**
     * 등록된 모든 관리자를 조회하는 메서드
     * 
     * @return
     */
    public List<Admin> findAllAdmins() {
        return adminRepository.findAllAdmins();
    }

    // --------- 수정 메서드 ---------

    /**
     * 관리자의 비밀번호를 수정하는 메서드
     * 
     * @param admin
     * @param updatePasswordDto
     */
    public void updatePassword(Admin admin, UpdatePasswordDto updatePasswordDto) {
        String originPassword = adminRepository.findPasswordById(admin);
        if (!passwordEncoder.matches(updatePasswordDto.getOriginPassword(), originPassword)) {
            ApiErrorCode errorCode = ApiErrorCode.INCORRECT_PASSWORD;
            throw new InvalidValueException(errorCode.name(), errorCode.getMessage());
        }
        String encodedPassword = passwordEncoder.encode(updatePasswordDto.getNewPassword());

        updatePasswordDto.setNewPassword(encodedPassword);
        adminRepository.updatePassword(admin, updatePasswordDto);
    }

    // --------- 삭제 메서드 ---------

    /**
     * 관리자를 삭제하는 메서드(soft-delete)
     * 
     * @param id
     */
    public void deleteAdmin(String id) {
        adminRepository.deleteAdmin(id);
    }
}

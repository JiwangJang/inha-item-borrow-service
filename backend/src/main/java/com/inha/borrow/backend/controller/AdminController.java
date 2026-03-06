package com.inha.borrow.backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.inha.borrow.backend.model.dto.apiResponse.ApiResponse;
import com.inha.borrow.backend.model.dto.user.admin.SaveAdminDto;
import com.inha.borrow.backend.model.dto.user.admin.UpdatePasswordDto;
import com.inha.borrow.backend.model.entity.user.Admin;
import com.inha.borrow.backend.service.AdminService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/admins")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    // --------- 생성 메서드 ---------

    /**
     * 새로운 대여자를 생성하는 메서드
     * 
     * @param saveAdminDto
     * @return
     */
    @PostMapping("/sub-admin")
    public ResponseEntity<Void> saveAdmin(@Valid @RequestBody SaveAdminDto saveAdminDto) {
        adminService.saveAdmin(saveAdminDto);
        return ResponseEntity.created(URI.create("/admins/info")).build();
    }

    // --------- 조회 메서드 ---------

    /**
     * 현재 저장돼 있는 전체 관리자의 정보를 가져오는 메서드
     * 
     * @return List<Admin>
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Admin>>> findAllAdmins() {
        List<Admin> admins = adminService.findAllAdmins();
        ApiResponse<List<Admin>> apiResponse = new ApiResponse<List<Admin>>(true, admins);
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * 현재 요청을 보낸 관리자의 정보를 조회하는 메서드
     * 
     * @param admin
     * @return adminInfo
     */
    @GetMapping("/info")
    public ResponseEntity<ApiResponse<Admin>> findById(@AuthenticationPrincipal Admin admin) {
        Admin foundedAdmin = adminService.findById(admin.getId());
        ApiResponse<Admin> apiResponse = new ApiResponse<>(true, foundedAdmin);
        return ResponseEntity.ok(apiResponse);
    }

    // --------- 수정메서드 ---------

    /**
     * 본인의 비밀번호를 수정하는 메서드
     * 
     * @param admin
     * @param updatePasswordDto
     * @return
     */
    @PatchMapping("/info/password")
    public ResponseEntity<Void> updatePassword(@AuthenticationPrincipal Admin admin,
            @Valid @RequestBody UpdatePasswordDto updatePasswordDto) {
        adminService.updatePassword(admin.getId(), updatePasswordDto);
        return ResponseEntity.noContent().build();
    }

    // --------- 삭제메서드 ---------

    /**
     * 특정 관리자의 계정을 삭제하는 메서드(Soft-delete)
     * 
     * @param id
     * @return
     */
    @DeleteMapping("/sub-admin/{id}")
    public ResponseEntity<Void> deleteAdmin(@PathVariable("id") String id) {
        adminService.deleteAdmin(id);
        return ResponseEntity.noContent().build();
    }
}

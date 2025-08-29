package com.inha.borrow.backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.inha.borrow.backend.model.dto.response.ApiResponse;
import com.inha.borrow.backend.model.dto.user.admin.SaveAdminDto;
import com.inha.borrow.backend.model.dto.user.admin.UpdateAdminInfoDto;
import com.inha.borrow.backend.model.dto.user.admin.UpdateDivisionDto;
import com.inha.borrow.backend.model.dto.user.admin.UpdatePasswordDto;
import com.inha.borrow.backend.model.dto.user.admin.UpdatePositionDto;
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

    @GetMapping
    public ResponseEntity<ApiResponse<List<Admin>>> findAllAdmins() {
        List<Admin> admins = adminService.findAllAdmins();
        ApiResponse<List<Admin>> apiResponse = new ApiResponse<List<Admin>>(true, admins);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/info")
    public ResponseEntity<ApiResponse<Admin>> findById(@AuthenticationPrincipal String id) {
        Admin admin = (Admin) adminService.findById(id);
        ApiResponse<Admin> apiResponse = new ApiResponse<>(true, admin);
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/sub-admin")
    public ResponseEntity<Void> saveAdmin(@Valid @RequestBody SaveAdminDto saveAdminDto) {
        adminService.saveAdmin(saveAdminDto);
        return ResponseEntity.created(URI.create("/admins/info")).build();
    }

    @PatchMapping("/info")
    public ResponseEntity<Void> updateAdminInfo(@AuthenticationPrincipal String id,
            @Valid @RequestBody UpdateAdminInfoDto updateAdminInfoDto) {
        adminService.updateAdminInfo(id, updateAdminInfoDto);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/info/password")
    public ResponseEntity<Void> updatePassword(@AuthenticationPrincipal String id,
            @Valid @RequestBody UpdatePasswordDto updatePasswordDto) {
        adminService.updatePassword(id, updatePasswordDto);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/info/{id}/position")
    public ResponseEntity<Void> updatePosition(@PathVariable String targetAdminId,
            @AuthenticationPrincipal Admin admin,
            @Valid @RequestBody UpdatePositionDto updatePositionDto) {
        adminService.updatePosition(admin, targetAdminId, updatePositionDto);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/info/{id}/division")
    public ResponseEntity<Void> updateDivision(@PathVariable String targetAdminId,
            @AuthenticationPrincipal Admin admin,
            @Valid @RequestBody UpdateDivisionDto updateDivisionDto) {
        adminService.updateDivision(admin, targetAdminId, updateDivisionDto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/sub-admin/{id}")
    public ResponseEntity<Void> deleteAdmin(@PathVariable("id") String id) {
        adminService.deleteAdmin(id);
        return ResponseEntity.noContent().build();
    }
}

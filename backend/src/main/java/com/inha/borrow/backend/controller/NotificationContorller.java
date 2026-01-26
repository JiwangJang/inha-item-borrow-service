package com.inha.borrow.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.inha.borrow.backend.model.dto.apiResponse.ApiResponse;
import com.inha.borrow.backend.model.dto.notification.CheckNotificationDto;
import com.inha.borrow.backend.model.dto.notification.FindNotificationDto;
import com.inha.borrow.backend.model.entity.Notification;
import com.inha.borrow.backend.model.entity.user.Borrower;
import com.inha.borrow.backend.service.NotificationService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/notification")
public class NotificationContorller {
    private final NotificationService service;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Notification>>> findAllNotifications(
            @AuthenticationPrincipal Borrower borrower,
            @RequestBody FindNotificationDto dto) {
        dto.setBorrowerId(borrower.getId());
        List<Notification> results = service.findAllNotifications(dto);
        ApiResponse<List<Notification>> response = new ApiResponse<List<Notification>>(true, results);
        return ResponseEntity.ok(response);
    }

    @PatchMapping
    public ResponseEntity<Void> checkNotification(
            @AuthenticationPrincipal Borrower borrower,
            @RequestBody CheckNotificationDto dto) {
        dto.setBorrowerId(borrower.getId());
        service.checkNotification(dto);
        return ResponseEntity.ok().build();
    }

}

package com.inha.borrow.backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import com.inha.borrow.backend.model.dto.apiResponse.ApiResponse;
import com.inha.borrow.backend.model.dto.notice.ModifyNoticeDto;
import com.inha.borrow.backend.model.dto.notice.PostNoticeDto;
import com.inha.borrow.backend.model.entity.Notice;
import com.inha.borrow.backend.model.entity.user.Admin;
import com.inha.borrow.backend.service.NoticeService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
@RequiredArgsConstructor
@RequestMapping("/notice")
public class NoticeController {
    private final NoticeService service;

    @GetMapping("{id}")
    public ResponseEntity<ApiResponse<Notice>> findNoticeById(@PathVariable String id) {
        Notice notice = service.findNoticeById(Integer.parseInt(id));
        ApiResponse<Notice> result = new ApiResponse<Notice>(true, notice);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Notice>>> findAllNotices() {
        List<Notice> notices = service.findAllNotices();
        ApiResponse<List<Notice>> result = new ApiResponse<>(true, notices);
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Integer>>> postNotice(
            @AuthenticationPrincipal Admin admin,
            @RequestBody PostNoticeDto dto) {
        int id = service.postNotice(admin.getId(), dto);
        ApiResponse<Map<String, Integer>> response = new ApiResponse<Map<String, Integer>>(true, Map.of("id", id));
        return ResponseEntity.status(201).body(response);
    }

    @PatchMapping("{id}")
    public ResponseEntity<Void> modifyNotice(
            @AuthenticationPrincipal Admin admin,
            @PathVariable String id,
            @RequestBody ModifyNoticeDto dto) {
        dto.setId(Integer.parseInt(id));
        service.modifyNotice(admin.getId(), dto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotice(
            @PathVariable String id) {
        int noticeId = Integer.parseInt(id);
        service.deleteNotice(noticeId);
        return ResponseEntity.noContent().build();
    }

}

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
@RequestMapping("/notices")
public class NoticeController {
    private final NoticeService service;

    // --------- 생성 메서드 ---------

    /**
     * 공지를 등록하는 메서드
     * 
     * @param admin
     * @param dto
     * @return
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Integer>>> saveNotice(
            @AuthenticationPrincipal Admin admin,
            @RequestBody PostNoticeDto dto) {
        int id = service.postNotice(admin.getId(), dto);
        ApiResponse<Map<String, Integer>> response = new ApiResponse<Map<String, Integer>>(true, Map.of("id", id));
        return ResponseEntity.status(201).body(response);
    }

    // --------- 조회 메서드 ---------

    /**
     * 공지 전체 조회 메서드
     * 
     * @return
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Notice>>> findAllNotices() {
        List<Notice> notices = service.findAllNotices();
        ApiResponse<List<Notice>> result = new ApiResponse<>(true, notices);
        return ResponseEntity.ok(result);
    }

    // --------- 수정 메서드 ---------

    /**
     * 공지 내용 수정 메서드
     * 
     * @param admin
     * @param id
     * @param dto
     * @return
     */
    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateNotice(
            @AuthenticationPrincipal Admin admin,
            @PathVariable("id") String id,
            @RequestBody ModifyNoticeDto dto) {
        dto.setId(Integer.parseInt(id));
        service.modifyNotice(admin.getId(), dto);
        return ResponseEntity.ok().build();
    }

    // --------- 삭제 메서드 ---------
    /**
     * 공지를 삭제하는 메서드(hard-delete)
     * 
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotice(
            @PathVariable("id") String id) {
        int noticeId = Integer.parseInt(id);
        service.deleteNotice(noticeId);
        return ResponseEntity.noContent().build();
    }

}

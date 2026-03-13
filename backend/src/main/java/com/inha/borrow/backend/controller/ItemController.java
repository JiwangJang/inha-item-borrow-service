package com.inha.borrow.backend.controller;

import com.inha.borrow.backend.model.dto.apiResponse.ApiResponse;
import com.inha.borrow.backend.model.dto.item.DeleteItemDto;
import com.inha.borrow.backend.model.dto.item.SaveItemDto;
import com.inha.borrow.backend.model.dto.item.UpdateItemDto;
import com.inha.borrow.backend.model.entity.Item;
import com.inha.borrow.backend.model.entity.user.User;
import com.inha.borrow.backend.service.ItemService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * item객체 관련 컨트롤러
 * 
 * @author 장지왕
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;

    // --------- 생성 메서드 ---------

    /**
     * 새로운 대여물품을 등록하는 메서드
     * 
     * @param itemDto
     * @return
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Item>> saveItem(@RequestBody @Valid SaveItemDto dto) {
        Item newItem = itemService.saveItem(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, newItem));
    }

    // --------- 조회 메서드 ---------
    /**
     * 등록된 모든 대여물품을 조회하는 메서드
     * 
     * @param user
     * @return
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Item>>> findAll(@AuthenticationPrincipal User user) {
        List<Item> result = itemService.findAll(user);
        return ResponseEntity.ok(new ApiResponse<>(true, result));
    }

    // --------- 수정 메서드 ---------

    /**
     * 대여물품의 정보를 수정하는 메서드
     * 
     * @param id
     * @param itemReviseRequestDto
     * @return
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updateItem(
            @PathVariable("id") int id,
            @Valid @RequestBody UpdateItemDto dto) {
        itemService.updateItem(dto, id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new ApiResponse<>(true, null));
    }

    // --------- 삭제 메서드 ---------

    /**
     * 대여물품을 삭제하는 메서드
     * 
     * @param id
     * @param deleteRequestDto
     * @return
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteItem(
            @PathVariable("id") int id,
            @Valid @RequestBody DeleteItemDto dto) {
        itemService.deleteItem(dto, id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new ApiResponse<>(true, null));
    }
}

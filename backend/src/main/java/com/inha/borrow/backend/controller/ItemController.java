package com.inha.borrow.backend.controller;

import com.inha.borrow.backend.model.dto.item.ItemDeleteRequestDto;
import com.inha.borrow.backend.model.dto.item.ItemDto;
import com.inha.borrow.backend.model.dto.item.ItemReviseRequestDto;
import com.inha.borrow.backend.model.dto.response.ApiResponse;
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

    @GetMapping
    public ResponseEntity<ApiResponse<List<Item>>> getAllItems(@AuthenticationPrincipal User user) {
        List<Item> result = itemService.getAllItem(user);
        return ResponseEntity.ok(new ApiResponse<>(true, result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Item>> getItemById(@AuthenticationPrincipal User user,
            @PathVariable("id") int id) {
        Item result = itemService.getItemById(user, id);
        return ResponseEntity.ok(new ApiResponse<Item>(true, result));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Item>> createItem(@RequestBody @Valid ItemDto itemDto) {
        Item newItem = itemService.createItem(itemDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, newItem));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updateItem(@PathVariable("id") int id,
            @Valid @RequestBody ItemReviseRequestDto itemReviseRequestDto) {
        itemService.updateItemDetail(id, itemReviseRequestDto);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new ApiResponse<>(true, null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteItem(@PathVariable("id") int id,
            @Valid @RequestBody ItemDeleteRequestDto deleteRequestDto) {
        itemService.deleteItem(id, deleteRequestDto);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new ApiResponse<>(true, null));
    }
}

package com.inha.borrow.backend.controller;

import com.inha.borrow.backend.model.item.Item;
import com.inha.borrow.backend.model.item.ItemDeleteRequestDto;
import com.inha.borrow.backend.model.response.ApiResponse;
import com.inha.borrow.backend.service.ItemService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ApiResponse<List<Item>>> getAllItems() {
        List<Item> result = itemService.getAllItem();
        return ResponseEntity.ok(new ApiResponse<>(true, result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Item>> getItemById(@PathVariable int id) {
        Item result = itemService.getItemById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, result));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Item>> createItem(@RequestBody Item item) {
        Item newItem = itemService.createItem(item);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, newItem));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updateItem(@PathVariable int id, @RequestBody Item item) {
        itemService.updateItemDetail(item, id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new ApiResponse<>(true, null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteItem(@PathVariable int id,
            @RequestBody ItemDeleteRequestDto deleteRequestDto) {
        itemService.deleteItem(id, deleteRequestDto);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new ApiResponse<>(true, null));
    }
}

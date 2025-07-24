package com.inha.borrow.backend.controller;

import com.inha.borrow.backend.domain.Item;
import com.inha.borrow.backend.service.ItemService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
public class ItemController {
    private final ItemService service;

    public ItemController(ItemService service) {
        this.service = service;
    }
    @PostMapping("/items")
    public Item createItem(@RequestBody Item item){
        return service.createItem(item);
    }
    @GetMapping("/items")
    public List<Item> getAllItems(){
        return service.getAllItem();
    }
    @GetMapping("/items/{id}")
    public Optional<Item> getItemById(@PathVariable int id) {
        return service.getItemById(id);
    }
    @DeleteMapping("/items/{id}")
    public boolean deleteItem(@PathVariable int id, @RequestParam String reason){
        return service.deleteItem(id,reason);
    }
    @PutMapping("/items/{id}")
    public boolean updateItem(@PathVariable int id, @RequestBody Item item){
        return service.updateItemDetail(item,id);
    }

}

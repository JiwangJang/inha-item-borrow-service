package com.inha.borrow.backend.service;

import com.inha.borrow.backend.domain.Item;
import com.inha.borrow.backend.repository.ItemRepository;

import java.util.List;
import java.util.Optional;

public class ItemService {
    private final ItemRepository repository;

    public ItemService(ItemRepository repository) {
        this.repository = repository;
    }
    public Item createItem(Item item){
        Item saved = repository.save(item);
        if(saved.getId()==0) throw new IllegalArgumentException("id 생성 실패");
        return saved;
    }
    public List<Item> getAllItem(){
       return repository.findAll();
    }

    public Optional<Item> getItemById(int itemId){
        return repository.findById(itemId);
    }
    public boolean deleteItem(int id, String reason){
        Optional<Item> item = repository.findById(id);
        if(item.isEmpty())return false;
        repository.deleteItem(id, reason);
        return true;
    }
    public boolean updateItemDetail(Item item, int id){
        Optional<Item> items = repository.findById(id);
        if(items.isEmpty())return false;
        repository.updateItem(item, id);
        return true;
    }

}

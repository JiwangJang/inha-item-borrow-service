package com.inha.borrow.backend.service;

import com.inha.borrow.backend.model.dto.item.ItemDeleteRequestDto;
import com.inha.borrow.backend.model.dto.item.ItemDto;
import com.inha.borrow.backend.model.dto.item.ItemReviseRequestDto;
import com.inha.borrow.backend.model.entity.Item;
import com.inha.borrow.backend.repository.ItemRepository;

import lombok.AllArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Service;

/**
 * Item객체와 관련된 작업을 수행하는 서비스 클래스
 * 
 * @author 장지왕
 */
@Service
@AllArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;

    /**
     * Item객체를 저장하는 메서드
     * 
     * @param item
     * @return 저장된 item객체
     * @author 장지왕
     */
    public Item createItem(ItemDto item) {
        return itemRepository.save(item);
    }

    /**
     * 모든 Item객체를 가져오는 메서드
     * 
     * @return Item객체 목록
     * @author 장지왕
     */
    public List<Item> getAllItem() {
        return itemRepository.findAll();
    }

    /**
     * 특정 Item을 찾는 메서드
     * 
     * @param id
     * @return Item
     * @author 장지왕
     */
    public Item getItemById(int itemId) {
        return itemRepository.findById(itemId);
    }

    /**
     * 특정 Item을 삭제하는 메서드
     * 
     * @param id           삭제할 Item의 아이디
     * @param deleteReason 삭제 이유
     */
    public void deleteItem(int id, ItemDeleteRequestDto deleteRequestDto) {
        itemRepository.deleteItem(id, deleteRequestDto);
    }

    /**
     * 특정 Item객체의 변경사항을 반영하는 메서드
     * 
     * @param item 변경 내용이 담긴 Item
     * @param id   변경할 Item객체의 아이디
     */
    public void updateItemDetail(ItemReviseRequestDto itemReviseRequestDto, int id) {
        itemRepository.updateItem(itemReviseRequestDto, id);
    }
}

package com.inha.borrow.backend.service;

import com.inha.borrow.backend.enums.ApiErrorCode;
import com.inha.borrow.backend.enums.ItemState;
import com.inha.borrow.backend.model.dto.item.DeleteItemDto;
import com.inha.borrow.backend.model.dto.item.SaveItemDto;
import com.inha.borrow.backend.model.dto.item.UpdateItemDto;
import com.inha.borrow.backend.model.entity.Item;
import com.inha.borrow.backend.model.entity.user.Admin;
import com.inha.borrow.backend.model.entity.user.User;
import com.inha.borrow.backend.model.exception.InvalidValueException;
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

    // --------- 생성 메서드 ---------

    /**
     * Item객체를 저장하는 메서드
     * 
     * @param item
     * @return 저장된 item객체
     * @author 장지왕
     */
    public Item saveItem(SaveItemDto dto) {
        return itemRepository.saveItem(dto);
    }

    // --------- 조회 메서드 ---------
    /**
     * 모든 Item객체를 가져오는 메서드
     * <p>
     * 권한에 따라 호출하는 메서드가 다르다
     * 
     * @return Item객체 목록
     * @author 장지왕
     */
    public List<Item> findAll(User user) {
        if (user instanceof Admin) {
            // 관리자인 경우
            return itemRepository.findAllForAdmin();
        } else {
            return itemRepository.findAllForNotAdmin();
        }
    }

    /**
     * 특정 대여물품의 상태를 조회하는 메서드(requestService에서 사용)
     * 
     * @param id
     * @return
     */
    public ItemState findItemStateById(int id) {
        Item item = Item.builder().id(id).build();
        return itemRepository.findItemStateById(item);
    }

    // --------- 수정 메서드 ---------

    /**
     * 특정 Item객체의 변경사항을 반영하는 메서드
     * 
     * @param itemReviseRequestDto 변경 내용이 담긴 Item
     * @param id                   변경할 Item객체의 아이디
     *
     * @author 장지왕
     */
    public void updateItem(UpdateItemDto dto, int itemId) {
        Item item = Item.builder().id(itemId).build();
        itemRepository.updateItem(item, dto);
    }

    /**
     * Item의 state를 변경하는 메서드
     * 
     * @author 형민재
     */
    public void updateState(ItemState state, int id) {
        Item item = Item.builder().id(id).build();
        itemRepository.updateState(item, state);
    }

    // --------- 삭제 메서드 ---------

    /**
     * 특정 Item을 삭제하는 메서드
     * 
     * @param id               삭제할 Item의 아이디
     * @param deleteRequestDto 삭제 이유
     *
     * @author 장지왕 (수정 : 형민재)
     */
    public void deleteItem(DeleteItemDto dto, int itemId) {
        Item deletedItem = Item.builder()
                .id(itemId)
                .deleteReason(dto.getDeleteReason())
                .build();
        ItemState foundedItemState = findItemStateById(itemId);
        if (foundedItemState == ItemState.AFFORD) {
            itemRepository.deleteItem(deletedItem);
        } else {
            ApiErrorCode apiErrorCode = ApiErrorCode.ITEM_STATUS_NOT_AFFORD;
            throw new InvalidValueException(apiErrorCode.name(), apiErrorCode.getMessage());
        }
    }
}

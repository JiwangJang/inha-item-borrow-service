package com.inha.borrow.backend.service;

import com.inha.borrow.backend.enums.ItemState;
import com.inha.borrow.backend.model.dto.item.BorrowedItemDto;
import com.inha.borrow.backend.model.dto.item.ItemDeleteRequestDto;
import com.inha.borrow.backend.model.dto.item.ItemDto;
import com.inha.borrow.backend.model.dto.item.ItemReviseRequestDto;
import com.inha.borrow.backend.model.entity.Item;
import com.inha.borrow.backend.model.entity.user.Admin;
import com.inha.borrow.backend.model.entity.user.User;
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
     * <p>
     * 권한에 따라 호출하는 메서드가 다르다
     * 
     * @return Item객체 목록
     * @author 장지왕
     */
    public List<Item> getAllItem(User user) {
        if (user instanceof Admin) {
            // 관리자인 경우
            return itemRepository.findAllForAdmin();
        } else {
            return itemRepository.findAllForNotAdmin();
        }
    }

    /**
     * 특정 Item을 찾는 메서드
     * 
     * @param id
     * @return Item
     * @author 장지왕
     */
    public Item getItemById(User user, int itemId) {
        BorrowedItemDto foundItem = itemRepository.findById(itemId);
        if (user instanceof Admin
                || (foundItem.getBorrowerId() != null && foundItem.getBorrowerId().equals(user.getId()))) {
            // 관리자이거나 대여한 사람이면 모든 정보를 볼 수 있음
            return foundItem.getAllInfoItem();
        } else {
            // 그렇지 않으면 부분적인 정보(Item아이디, 이름, 가격, 상태)만 볼 수 있음.
            return foundItem.getParticialInfoItem();
        }
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
    public void updateItemDetail(int id, ItemReviseRequestDto itemReviseRequestDto) {
        itemRepository.updateItem(itemReviseRequestDto, id);
    }

    /**
     * Item의 state를 변경하는 메서드
     * 
     * @author 형민재
     */
    public void updateState(ItemState state, int id) {
        itemRepository.updateState(state, id);
    }

    public ItemState findItemStateById(int id) {
        return itemRepository.findItemStateById(id);
    }
}

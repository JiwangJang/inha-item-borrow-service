package com.inha.borrow.backend.service;

import com.inha.borrow.backend.enums.ApiErrorCode;
import com.inha.borrow.backend.enums.ItemState;
import com.inha.borrow.backend.enums.RequestState;
import com.inha.borrow.backend.enums.RequestType;
import com.inha.borrow.backend.model.dto.request.PatchRequestDto;
import com.inha.borrow.backend.model.entity.request.Request;
import com.inha.borrow.backend.model.dto.request.SaveRequestDto;
import com.inha.borrow.backend.model.dto.request.SaveRequestResultDto;
import com.inha.borrow.backend.model.entity.user.Borrower;
import com.inha.borrow.backend.model.entity.user.User;
import com.inha.borrow.backend.model.exception.InvalidValueException;
import com.inha.borrow.backend.repository.RequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 리퀘스트 서비스
 * 
 * @author 형민재
 */
@Service
@RequiredArgsConstructor
public class RequestService {
    private final RequestRepository requestRepository;
    private final ItemService itemService;

    /**
     * 리퀘스트를 저장하는 메서드
     * 
     * @param saveRequestDto
     * @author 형민재(수정 : 장지왕)
     */
    @Transactional
    public SaveRequestResultDto saveRequest(SaveRequestDto saveRequestDto) {
        // 변수 선언
        RequestType type = saveRequestDto.getType();
        int itemId = saveRequestDto.getItemId();
        int prevRequestId = saveRequestDto.getPrevRequestId();
        if (type == RequestType.BORROW) {
            // 대여신청일 경우 대여물품이 실제로 빌릴 수 있는 상태인지 확인
            ItemState itemState = itemService.findItemStateById(itemId);
            if (itemState != ItemState.AFFORD) {
                ApiErrorCode apiErrorCode = ApiErrorCode.INVALID_ITEM_ID;
                throw new InvalidValueException(apiErrorCode.name(), apiErrorCode.getMessage());
            }
            itemService.updateState(ItemState.REVIEWING, itemId);
        } else {
            // 반납일 경우 이전에 보냈던 요청이 있는지 확인
            RequestState prevRequestState = requestRepository.findRequestStateById(prevRequestId);
            if (prevRequestState != RequestState.PERMIT) {
                ApiErrorCode apiErrorCode = ApiErrorCode.INVALID_REQUEST_ID;
                throw new InvalidValueException(apiErrorCode.name(), apiErrorCode.getMessage());
            }
        }
        return requestRepository.save(saveRequestDto);
    }

    /**
     * 리퀘스트 전체 조회하는 메서드
     * 
     * @author 형민재
     */
    public List<Request> findAll() {
        return requestRepository.findAll();
    }

    /**
     * ID로 리퀘스트를 가져오는 메서드
     * 
     * @param requestId
     * @author 형민재
     */
    public Request findById(User user, int requestId) {
        if (user instanceof Borrower) {
            return requestRepository.findById(user.getId(), requestId);
        } else {
            // 관리자는 모든 대여자의 요청에 대해 조회할 수 있다.
            return requestRepository.findById(null, requestId);
        }
    }

    /**
     * 리퀘스트를 여러 조건으로 가져오는 메서드
     * 
     * @param borrowerId
     * @param state
     * @param type
     * @author 형민재
     */
    public List<Request> findByCondition(User user, String borrowerId, String type, String state) {
        if (user instanceof Borrower) {
            return requestRepository.findRequestsByCondition(user.getId(), type, state);
        } else {
            return requestRepository.findRequestsByCondition(borrowerId, type, state);
        }
    }

    /**
     * ID로 리퀘스트를 수정하는 메서드
     * 
     * @param patchRequestDto
     * @param requestId
     * @param borrowerId
     * @author 형민재
     */
    public void patchRequest(PatchRequestDto patchRequestDto, int requestId, String borrowerId) {
        requestRepository.patchRequest(patchRequestDto, requestId, borrowerId);
    }

    /**
     * ID로 리퀘스트를 취소하는 메서드
     * 
     * @param requestId
     * @param borrowerId
     * @author 형민재
     */
    public void cancelRequest(int requestId, String borrowerId) {
        requestRepository.cancelRequest(requestId, borrowerId);
    }

    /**
     * ID로 리퀘스트의 state를 변경하는 메서드
     * 
     * @param state
     * @param requestId
     * @author 형민재
     */
    public void evaluationRequest(RequestState state, int requestId) {
        requestRepository.evaluationRequest(state, requestId);
    }

    public void manageRequest(String adminId, String requestId) {
        requestRepository.manageRequest(adminId, requestId);
    }
}

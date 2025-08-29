package com.inha.borrow.backend.service;


import com.inha.borrow.backend.enums.ApiErrorCode;
import com.inha.borrow.backend.enums.ItemState;
import com.inha.borrow.backend.enums.RequestState;
import com.inha.borrow.backend.model.entity.request.FindRequest;
import com.inha.borrow.backend.model.entity.request.SaveRequest;
import com.inha.borrow.backend.repository.ItemRepository;
import com.inha.borrow.backend.repository.RequestRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.List;


/**
 * 리퀘스트 서비스
 * @author 형민재
 */
@Service
@RequiredArgsConstructor
public class RequestService {

    private final RequestRepository requestRepository;
    private final ItemRepository itemRepository;

    /**
     * 리퀘스트를 저장하는 메서드
     * @param saveRequest
     * @author 형민재
     */
    public SaveRequest saveRequest(SaveRequest saveRequest){
       return requestRepository.save(saveRequest);
    }

    /**
     * 리퀘스트 전체 조회하는 메서드
     * @author 형민재
     */
    public List<FindRequest> findAll(){
        return requestRepository.findAll();
    }

    /**
     * ID로 리퀘스트를 가져오는 메서드
     * @param requestId
     * @author 형민재
     */
    public FindRequest findById(int requestId){
        return requestRepository.findById(requestId);
    }

    public List<FindRequest> findByCondition(String borrowerId, String type, String state){
        return requestRepository.findByCondition(borrowerId,type,state);
    }

    /**
     * ID로 리퀘스트를 수정하는 메서드
     * @param saveRequest
     * @param requestId
     * @author 형민재
     */
    public void patchRequest(SaveRequest saveRequest, int requestId, String borrowerId){
        requestRepository.patchRequest(saveRequest,requestId);
    }

    /**
     * ID로 리퀘스트를 취소하는 메서드
     * @param cancel
     * @param requestId
     * @author 형민재
     */
    public void cancelRequest(boolean cancel, int requestId, String borrowerId){
        requestRepository.cancelRequest(cancel,requestId);
    }

    /**
     * ID로 리퀘스트를 state를 변경하는 메서드
     * @param state
     * @param requestId
     * @author 형민재
     */
    public void evaluationRequest(RequestState state, int requestId, int itemId){
        requestRepository.evaluationRequest(state,requestId);
        if(RequestState.PERMIT == state){
            itemRepository.updateState(ItemState.BORROWED,itemId);
        }
    }
}

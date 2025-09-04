package com.inha.borrow.backend.service;

import com.inha.borrow.backend.enums.ItemState;
import com.inha.borrow.backend.enums.RequestState;
import com.inha.borrow.backend.model.dto.request.PatchRequestDto;
import com.inha.borrow.backend.model.entity.request.Request;
import com.inha.borrow.backend.model.dto.request.SaveRequestDto;
import com.inha.borrow.backend.repository.RequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;


/**
 * 리퀘스트 서비스
 * @author 형민재
 */
@Service
@RequiredArgsConstructor
public class RequestService {

    private final RequestRepository requestRepository;
    private final ItemService itemService;

    /**
     * 리퀘스트를 저장하는 메서드
     * @param saveRequestDto
     * @author 형민재
     */
    public SaveRequestDto saveRequest(SaveRequestDto saveRequestDto, int itemId){
        itemService.updateState(ItemState.REVIEWING,itemId);
       return requestRepository.save(saveRequestDto);
    }

    /**
     * 리퀘스트 전체 조회하는 메서드
     * @author 형민재
     */
    public List<Request> findAll(){
        return requestRepository.findAll();
    }

    /**
     * ID로 리퀘스트를 가져오는 메서드
     * @param requestId
     * @author 형민재
     */
    public Request findById(int requestId){
        return requestRepository.findById(requestId);
    }

    /**
     * 리퀘스트를 여러 조건으로 가져오는 메서드
     * @param borrowerId
     * @param state
     * @param type
     * @author 형민재
     */
    public List<Request> findByCondition(String borrowerId, String type, String state){
        return requestRepository.findByCondition(borrowerId,type,state);
    }

    /**
     * 사용자가 자신이 요청한 리퀘스트를 가져오는 메서드
     * @param borrowerId
     * @author 형민재
     */
    public List<Request> findRequestUser(String borrowerId){
        return requestRepository.findRequestUser(borrowerId);
    }

    /**
     * ID로 리퀘스트를 수정하는 메서드
     * @param patchRequestDto
     * @param requestId
     * @param borrowerId
     * @author 형민재
     */
    public void patchRequest(PatchRequestDto patchRequestDto, int requestId, String borrowerId){
        requestRepository.patchRequest(patchRequestDto,requestId,borrowerId);
    }

    /**
     * ID로 리퀘스트를 취소하는 메서드
     * @param requestId
     * @param borrowerId
     * @author 형민재
     */
    public void cancelRequest(int requestId, String borrowerId){
        requestRepository.cancelRequest(requestId, borrowerId);
    }

    /**
     * ID로 리퀘스트의 state를 변경하는 메서드
     * @param state
     * @param requestId
     * @author 형민재
     */
    public void evaluationRequest(RequestState state, int requestId){
        requestRepository.evaluationRequest(state,requestId);
    }
}

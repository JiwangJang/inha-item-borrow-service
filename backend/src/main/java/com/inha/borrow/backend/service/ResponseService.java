package com.inha.borrow.backend.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.inha.borrow.backend.enums.ApiErrorCode;
import com.inha.borrow.backend.enums.ItemState;
import com.inha.borrow.backend.enums.RequestState;
import com.inha.borrow.backend.enums.RequestType;
import com.inha.borrow.backend.enums.ResponseType;
import com.inha.borrow.backend.model.dto.response.PatchResponseDto;
import com.inha.borrow.backend.model.dto.response.SaveResponseDto;
import com.inha.borrow.backend.model.entity.Response;
import com.inha.borrow.backend.model.entity.request.Request;
import com.inha.borrow.backend.model.exception.InvalidValueException;
import com.inha.borrow.backend.repository.ItemRepository;
import com.inha.borrow.backend.repository.RequestRepository;
import com.inha.borrow.backend.repository.ResponseRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResponseService {
    private final ResponseRepository responseRepository;
    private final RequestRepository requestRepository;
    private final ItemRepository itemRepository;

    @Transactional
    public Response createResponse(String adminId, SaveResponseDto dto) {
        // 요청객체 조회해서 담당자 맞는지 확인
        Request request = requestRepository.findManagerAndItemIdById(dto.getRequestId());
        String rejectReason = dto.getRejectReason();
        int requestId = dto.getRequestId();
        String manager = request.getManager().getId();
        int itemId = request.getItem().getId();
        RequestType requestType = request.getType();
        ResponseType responseType = dto.getType();
        RequestState requestState = request.getState();
        boolean isPermit = !StringUtils.hasText(rejectReason);

        if (!adminId.equals(manager)) {
            throw new AccessDeniedException("해당 요청의 담당자만 답변할 수 있습니다.");
        }

        if (!requestType.name().equals(responseType.name())) {
            ApiErrorCode apiErrorCode = ApiErrorCode.INVALID_RESPONSE_TYPE;
            throw new InvalidValueException(apiErrorCode.name(), apiErrorCode.getMessage());
        }

        if (requestState != RequestState.ASSIGNED) {
            ApiErrorCode apiErrorCode = ApiErrorCode.ALREADY_RESPONEDED_REQUEST;
            throw new InvalidValueException(apiErrorCode.name(), apiErrorCode.getMessage());
        }

        // 아이템 및 요청객체 상태 변경
        if (requestType == RequestType.BORROW) {
            // 대여요청인 경우
            if (isPermit) {
                requestRepository.updateRequestState(RequestState.PERMIT, requestId);
                itemRepository.updateState(ItemState.BORROWED, itemId);
            } else {
                requestRepository.updateRequestState(RequestState.REJECT, requestId);
                itemRepository.updateState(ItemState.AFFORD, itemId);
            }
        } else {
            // 반납요청인 경우
            if (isPermit) {
                requestRepository.updateRequestState(RequestState.PERMIT, requestId);
                itemRepository.updateState(ItemState.AFFORD, itemId);
            } else {
                requestRepository.updateRequestState(RequestState.REJECT, requestId);
                itemRepository.updateState(ItemState.REVIEWING, itemId);
            }
        }
        return responseRepository.save(dto);
    }

    public void updateResponse(String adminId, String responseId, PatchResponseDto dto) {
        // 요청객체 조회해서 담당자 맞는지 확인
        Request request = requestRepository.findManagerAndItemIdById(dto.getRequestId());
        String rejectReason = dto.getRejectReason();
        int requestId = dto.getRequestId();
        String manager = request.getManager().getId();
        int itemId = request.getItem().getId();
        RequestType requestType = request.getType();
        RequestState requestState = request.getState();
        boolean isPermit = !StringUtils.hasText(rejectReason);

        if (!adminId.equals(manager)) {
            throw new AccessDeniedException("해당 요청의 담당자만 답변할 수 있습니다.");
        }

        if (requestType == RequestType.BORROW) {
            ApiErrorCode apiErrorCode = ApiErrorCode.NOT_ALLOWED_RESPONSE_TYPE;
            throw new InvalidValueException(apiErrorCode.name(), apiErrorCode.getMessage());
        }

        if (requestState != RequestState.REJECT) {
            ApiErrorCode apiErrorCode = ApiErrorCode.ALREADY_RESPONEDED_REQUEST;
            throw new InvalidValueException(apiErrorCode.name(), apiErrorCode.getMessage());
        }

        if (isPermit) {
            requestRepository.updateRequestState(RequestState.PERMIT, requestId);
            itemRepository.updateState(ItemState.AFFORD, itemId);
            responseRepository.update(responseId, rejectReason);
        }
    }
}

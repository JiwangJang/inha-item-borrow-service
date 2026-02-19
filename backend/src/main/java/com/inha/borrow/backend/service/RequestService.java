package com.inha.borrow.backend.service;

import com.inha.borrow.backend.enums.ApiErrorCode;
import com.inha.borrow.backend.enums.ItemState;
import com.inha.borrow.backend.enums.RequestState;
import com.inha.borrow.backend.enums.RequestType;
import com.inha.borrow.backend.model.dto.request.PatchRequestDto;
import com.inha.borrow.backend.model.entity.StudentCouncilFeeVerification;
import com.inha.borrow.backend.model.entity.request.Request;
import com.inha.borrow.backend.model.dto.request.SaveRequestDto;
import com.inha.borrow.backend.model.dto.request.SaveRequestResultDto;
import com.inha.borrow.backend.model.entity.user.Borrower;
import com.inha.borrow.backend.model.entity.user.User;
import com.inha.borrow.backend.model.exception.InvalidValueException;
import com.inha.borrow.backend.repository.RequestRepository;
import com.inha.borrow.backend.repository.StudentCouncilFeeVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

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
    private final StudentCouncilFeeVerificationRepository studentCouncilFeeVerificationRepository;

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
        StudentCouncilFeeVerification council = studentCouncilFeeVerificationRepository
                .findRequestByBorrowerId(saveRequestDto.getBorrowerId());
        if (!council.isVerify()) {
            ApiErrorCode apiErrorCode = ApiErrorCode.NOT_ALLOWED_COUNCIL_FEE;
            throw new AccessDeniedException(apiErrorCode.name() + ":" + apiErrorCode.getMessage());
        }

        // borrowAt < returnAt
        if (!saveRequestDto.getBorrowAt().isBefore(saveRequestDto.getReturnAt())) {
            // 대여요청이든 반납요청이든 동일하게 적용
            ApiErrorCode apiErrorCode = ApiErrorCode.INVALID_VALUE;
            throw new InvalidValueException(apiErrorCode.name(), "반납일시는 대여일시보다 이후여야 합니다.");
        }

        if (type == RequestType.BORROW) {
            OffsetDateTime now = OffsetDateTime.now(ZoneId.of("Asia/Seoul"));

            // 1시간 이후인지
            if (saveRequestDto.getBorrowAt().isBefore(now.plusHours(1))) {
                ApiErrorCode apiErrorCode = ApiErrorCode.INVALID_VALUE;
                throw new InvalidValueException(apiErrorCode.name(), "대여일시는 현재 시각보다 한시간 이후여야 합니다.");
            }

            // 대여신청일 경우 대여물품이 실제로 빌릴 수 있는 상태인지 확인
            ItemState itemState = itemService.findItemStateById(itemId);
            if (itemState != ItemState.AFFORD) {
                ApiErrorCode apiErrorCode = ApiErrorCode.INVALID_ITEM_ID;
                throw new InvalidValueException(apiErrorCode.name(), apiErrorCode.getMessage());
            }
            itemService.updateState(ItemState.REVIEWING, itemId);
        } else {
            // 반납일 경우 이전에 보냈던 요청이 있는지 확인
            RequestState prevRequestState = requestRepository.findRequestStateById(prevRequestId, RequestType.BORROW);
            if (prevRequestState != RequestState.PERMIT) {
                ApiErrorCode apiErrorCode = ApiErrorCode.INVALID_REQUEST_ID;
                throw new InvalidValueException(apiErrorCode.name(), apiErrorCode.getMessage());
            }
        }
        return requestRepository.save(saveRequestDto);
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
            return requestRepository.findRequestsByCondition(user.getId(), null, type, state);
        } else {
            return requestRepository.findRequestsByCondition(borrowerId, user.getId(), type, state);
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
    @Transactional
    public void patchRequest(PatchRequestDto patchRequestDto, int requestId, String borrowerId) {
        Map<String, Object> result = requestRepository.findRequestStateAndTypeAndBorrowAtById(requestId);
        RequestState state = RequestState.valueOf((String) result.get("state"));
        RequestType type = RequestType.valueOf((String) result.get("type"));
        OffsetDateTime borrowAt = ((Timestamp) result.get("borrow_at")).toInstant().atZone(ZoneId.of("Asia/Seoul"))
                .toOffsetDateTime();
        OffsetDateTime now = OffsetDateTime.now();

        // 사용자가 대여요청을 수정한 경우 -> 대여 요청시간이 현재보다 한시간 뒤인지 + 반납요청시간이 대여요청시간 이후인지 확인
        // 사용자가 반납요청을 수정한 경우 -> 반납 요청시간이 현재시간보다 이후인지.

        if (state != RequestState.PENDING) {
            // 관리자에게 배정되기 전까지는 수정가능
            ApiErrorCode apiErrorCode = ApiErrorCode.CAN_NOT_MODIFY_REQUEST;
            throw new InvalidValueException(apiErrorCode.name(), apiErrorCode.getMessage());
        }

        if (type == RequestType.BORROW) {
            // 1시간 이후인지
            if (patchRequestDto.getBorrowAt().isBefore(now.plusHours(1))) {
                ApiErrorCode apiErrorCode = ApiErrorCode.INVALID_VALUE;
                throw new InvalidValueException(apiErrorCode.name(), "대여일시는 현재 시각보다 한시간 이후여야 합니다.");
            }
            if (!patchRequestDto.getBorrowAt().isBefore(patchRequestDto.getReturnAt())) {
                // 대여요청이든 반납요청이든 동일하게 적용
                ApiErrorCode apiErrorCode = ApiErrorCode.INVALID_VALUE;
                throw new InvalidValueException(apiErrorCode.name(), "반납일시는 대여일시보다 이후여야 합니다.");
            }
        } else {
            if (!OffsetDateTime.now().isBefore(patchRequestDto.getReturnAt())) {
                // 반납요청시각이 현재보다 이전이면 거부
                ApiErrorCode apiErrorCode = ApiErrorCode.INVALID_VALUE;
                throw new InvalidValueException(apiErrorCode.name(), "반납일시는 현재보다 이전일 수 없습니다.");
            }
            if (!patchRequestDto.getBorrowAt().isEqual(borrowAt)) {
                ApiErrorCode apiErrorCode = ApiErrorCode.INVALID_VALUE;
                throw new InvalidValueException(apiErrorCode.name(), "반납요청의 대여시간은 수정할 수 없습니다.");
            }

        }
        requestRepository.patchRequest(patchRequestDto, requestId, borrowerId);

    }

    /**
     * ID로 리퀘스트를 취소하는 메서드
     * 
     * @param requestId
     * @param borrowerId
     * @author 형민재
     */
    @Transactional
    public void cancelRequest(int requestId, String borrowerId) {
        Map<String, String> typeAndItemId = requestRepository.findRequestItemIdAndStateById(requestId);
        RequestState state = RequestState.valueOf(typeAndItemId.get("state"));

        if (state != RequestState.PENDING) {
            // 관리자에게 배정되기 전까진 취소가능
            ApiErrorCode apiErrorCode = ApiErrorCode.CAN_NOT_CANCEL_REQUEST;
            throw new InvalidValueException(apiErrorCode.name(), apiErrorCode.getMessage());
        }
        requestRepository.cancelRequest(requestId, borrowerId);
        itemService.updateState(ItemState.AFFORD, Integer.parseInt(typeAndItemId.get("item_id")));
    }

    /**
     * 담당자 지정 기능 메서드
     * 
     * @param adminId
     * @param requestId
     */
    public void manageRequest(String adminId, int requestId) {
        requestRepository.manageRequest(adminId, requestId);
    }

    /**
     * 요청 상태 변경메서드 (다른 서비스 호출용)
     * 
     * @param state
     * @param requestId
     */
    public void updateRequestState(RequestState state, int requestId) {
        requestRepository.updateRequestState(state, requestId);
    }
}

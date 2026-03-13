package com.inha.borrow.backend.service;

import com.inha.borrow.backend.enums.ApiErrorCode;
import com.inha.borrow.backend.enums.ItemState;
import com.inha.borrow.backend.enums.RequestState;
import com.inha.borrow.backend.enums.RequestType;
import com.inha.borrow.backend.model.entity.request.Request;
import com.inha.borrow.backend.model.dto.request.SaveRequestDto;
import com.inha.borrow.backend.model.dto.request.UpdateRequestDto;
import com.inha.borrow.backend.model.dto.user.borrower.BorrowerCacheData;
import com.inha.borrow.backend.model.entity.user.Admin;
import com.inha.borrow.backend.model.entity.user.Borrower;
import com.inha.borrow.backend.model.entity.user.User;
import com.inha.borrow.backend.model.exception.InvalidValueException;
import com.inha.borrow.backend.repository.RequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.github.benmanes.caffeine.cache.Cache;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 리퀘스트 서비스
 * 
 * @author 형민재
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RequestService {
    private final RequestRepository requestRepository;
    private final ItemService itemService;
    private final Cache<String, BorrowerCacheData> borrowerCache;

    // --------- 생성 메서드 ---------

    /**
     * 리퀘스트를 저장하는 메서드
     * 
     * @param saveRequestDto
     * @author 형민재(수정 : 장지왕)
     */
    @Transactional
    public Request saveRequest(Borrower borrower, SaveRequestDto saveRequestDto) {
        // 변수 선언은 함수 시작부분에 하는것이 좋다
        String borrowerId = borrower.getId();
        RequestType type = saveRequestDto.getType();
        int itemId = saveRequestDto.getItemId();
        int prevRequestId = saveRequestDto.getPrevRequestId();
        BorrowerCacheData cacheBorrower = borrowerCache.getIfPresent(borrowerId);
        Map<String, Object> recentRequestInfo = requestRepository
                .findRecentRequestInfo(borrower);

        if (cacheBorrower == null) {
            // 캐시가 제대로 등록되지 않은 경우 대여 불가 -> 학생회비 납부여부 확인 불가
            throw new AccessDeniedException("등록되지 않은 사용자입니다.");
        }

        if (cacheBorrower.isBan()) {
            // 이용금지당한 사용자는 물품대여 불가능
            throw new AccessDeniedException("이용이 금지된 사용자입니다.");
        }

        if (cacheBorrower.getAgreementVersion() == null) {
            // 개인정보 동의 하지 않은 경우 물품대여 불가능
            throw new AccessDeniedException("개인정보 동의를 먼저 하셔야합니다.");
        }

        if (!cacheBorrower.isVerify()) {
            // 학생회비 납부 인증이 되지 않은 경우 물품대여 불가능
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
            LocalDateTime now = LocalDateTime.now();

            if (recentRequestInfo != null) {
                // 대여요청의 경우 이전 요청이 null일수도 있음(첫요청)
                // 대여 신청은 최근 요청의 타입이 RETURN이고 상태는 PERMIT이거나 BORROW면서 REJECT여야 한다.
                RequestType recentRequestType = (RequestType) recentRequestInfo.get("type");
                RequestState recentRequestState = (RequestState) recentRequestInfo.get("state");

                if (recentRequestType == RequestType.BORROW) {
                    // 최근 요청이 대여인 경우
                    if (recentRequestState != RequestState.REJECT) {
                        throw new InvalidValueException(ApiErrorCode.REQUEST_EXIST.name(),
                                ApiErrorCode.REQUEST_EXIST.getMessage());
                    }
                } else {
                    // 최근 요청이 대여인 반납인 경우
                    if (recentRequestState != RequestState.PERMIT) {
                        throw new InvalidValueException(ApiErrorCode.REQUEST_EXIST.name(),
                                ApiErrorCode.REQUEST_EXIST.getMessage());
                    }
                }
            }

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
            // 반납일 경우 최신 요청이 BORROW이고 PERMIT이어야 함
            int recentRequestId = (int) recentRequestInfo.get("id");
            RequestType recentRequestType = (RequestType) recentRequestInfo.get("type");
            RequestState recentRequestState = (RequestState) recentRequestInfo.get("state");
            LocalDateTime recentRequestBorrowAt = (LocalDateTime) recentRequestInfo.get("borrow_at");
            LocalDateTime borrowAt = saveRequestDto.getBorrowAt();

            if (recentRequestType != RequestType.BORROW && recentRequestState != RequestState.PERMIT) {
                // 최신 요청 상태 및 타입 검사
                throw new InvalidValueException(ApiErrorCode.REQUEST_EXIST.name(),
                        ApiErrorCode.REQUEST_EXIST.getMessage());
            }

            if (recentRequestId != prevRequestId) {
                // 최신 요청 아이디와 사용자가 보낸 이전 요청의 아이디가 같은지 검사
                throw new InvalidValueException(ApiErrorCode.INVALID_REQUEST_ID.name(),
                        ApiErrorCode.INVALID_REQUEST_ID.getMessage());
            }

            if (!recentRequestBorrowAt.isEqual(borrowAt)) {
                throw new InvalidValueException(ApiErrorCode.INVALID_VALUE.name(),
                        "대여요청 시간은 이전 요청과 동일해야합니다.");
            }
        }
        return requestRepository.saveRequest(borrower, saveRequestDto);
    }

    // --------- 조회 메서드 ---------

    /**
     * 리퀘스트를 여러 조건으로 가져오는 메서드
     * 
     * @param borrowerId
     * @param state
     * @param type
     * @author 형민재
     */
    public List<Request> findRequestsByCondition(User user, String borrowerId, String type, String state) {
        if (user instanceof Borrower) {
            return requestRepository.findRequestsByCondition(user.getId(), null, type, state);
        } else {
            return requestRepository.findRequestsByCondition(borrowerId, user.getId(), type, state);
        }
    }

    /**
     * 특정 요청의 담당자와 대여물품 번호를 조회하는 메서드(ResponseService 사용)
     * 
     * @param requestId
     * @return
     */
    public Request findManagerAndItemIdById(int requestId) {
        return requestRepository.findManagerAndItemIdById(requestId);
    }

    // --------- 수정 메서드 ---------

    /**
     * ID로 리퀘스트를 수정하는 메서드
     * 
     * @param patchRequestDto
     * @param requestId
     * @param borrowerId
     * @author 형민재
     */
    @Transactional
    public void updateRequest(Borrower borrower, UpdateRequestDto updateRequestDto, int requestId) {
        Map<String, Object> result = requestRepository.findRequestStateAndTypeAndBorrowAtById(requestId);
        RequestState state = RequestState.valueOf((String) result.get("state"));
        RequestType type = RequestType.valueOf((String) result.get("type"));
        LocalDateTime borrowAt = (LocalDateTime) result.get("borrow_at");
        LocalDateTime now = LocalDateTime.now();

        // 사용자가 대여요청을 수정한 경우 -> 대여 요청시간이 현재보다 한시간 뒤인지 + 반납요청시간이 대여요청시간 이후인지 확인
        // 사용자가 반납요청을 수정한 경우 -> 반납 요청시간이 현재시간보다 이후인지.

        if (type == RequestType.BORROW) {
            // 대여요청인 경우
            if (state != RequestState.PENDING) {
                // 관리자에게 배정되기 전까지는 수정가능
                ApiErrorCode apiErrorCode = ApiErrorCode.CAN_NOT_MODIFY_REQUEST;
                throw new InvalidValueException(apiErrorCode.name(), apiErrorCode.getMessage());
            }
            // 1시간 이후인지
            if (updateRequestDto.getBorrowAt().isBefore(now.plusHours(1))) {
                ApiErrorCode apiErrorCode = ApiErrorCode.INVALID_VALUE;
                throw new InvalidValueException(apiErrorCode.name(), "대여일시는 현재 시각보다 한시간 이후여야 합니다.");
            }
            if (!updateRequestDto.getBorrowAt().isBefore(updateRequestDto.getReturnAt())) {
                // 대여요청이든 반납요청이든 동일하게 적용
                ApiErrorCode apiErrorCode = ApiErrorCode.INVALID_VALUE;
                throw new InvalidValueException(apiErrorCode.name(), "반납일시는 대여일시보다 이후여야 합니다.");
            }
        } else {
            // 반납요청인 경우
            if (state == RequestState.ASSIGNED || state == RequestState.PERMIT) {
                ApiErrorCode apiErrorCode = ApiErrorCode.INVALID_VALUE;
                throw new InvalidValueException(apiErrorCode.name(), "보류중 또는 거부됨이 아닌 반납 요청은 수정이 불가합니다.");
            }
            if (!LocalDateTime.now().isBefore(updateRequestDto.getReturnAt())) {
                // 반납요청시각이 현재보다 이전이면 거부
                ApiErrorCode apiErrorCode = ApiErrorCode.INVALID_VALUE;
                throw new InvalidValueException(apiErrorCode.name(), "반납일시는 현재보다 이전일 수 없습니다.");
            }
            if (!updateRequestDto.getBorrowAt().isEqual(borrowAt)) {
                ApiErrorCode apiErrorCode = ApiErrorCode.INVALID_VALUE;
                throw new InvalidValueException(apiErrorCode.name(), "반납요청의 대여시간은 수정할 수 없습니다.");
            }
        }
        requestRepository.updateRequest(borrower, updateRequestDto, requestId);
    }

    /**
     * ID로 리퀘스트를 취소하는 메서드
     * 
     * @param requestId
     * @param borrowerId
     * @author 형민재
     */
    @Transactional
    public void updateRequestCancel(Borrower borrower, int requestId) {
        Map<String, String> typeAndItemId = requestRepository.findRequestItemIdAndStateById(requestId);
        RequestState state = RequestState.valueOf(typeAndItemId.get("state"));

        if (state != RequestState.PENDING) {
            // 관리자에게 배정되기 전까진 취소가능
            ApiErrorCode apiErrorCode = ApiErrorCode.CAN_NOT_CANCEL_REQUEST;
            throw new InvalidValueException(apiErrorCode.name(), apiErrorCode.getMessage());
        }
        requestRepository.updateRequestCancel(borrower, requestId);
        itemService.updateState(ItemState.AFFORD, Integer.parseInt(typeAndItemId.get("item_id")));
    }

    /**
     * 담당자 지정 기능 메서드
     * 
     * @param adminId
     * @param requestId
     */
    @Transactional
    public void updateRequestManager(Admin admin, int requestId) {
        Request request = requestRepository.findManagerAndItemIdById(requestId);
        if (request.getManager().getId() != null) {
            throw new InvalidValueException(ApiErrorCode.ALREADY_ASSIGNED.name(),
                    ApiErrorCode.ALREADY_ASSIGNED.getMessage());
        }
        requestRepository.updateRequestManager(admin, requestId);
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

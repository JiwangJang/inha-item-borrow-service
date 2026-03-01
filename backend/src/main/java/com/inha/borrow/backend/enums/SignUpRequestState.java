package com.inha.borrow.backend.enums;

/**
 * SignUpRequest의 상태를 표시함
 * <p>
 * PENDING : 아직 대기중
 * <p>
 * REJECT : 거절됨
 * <p>
 * PERMIT : 허가됨
 */
public enum SignUpRequestState {
    PENDING,
    REJECT,
    PERMIT
}

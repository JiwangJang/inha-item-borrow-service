package com.inha.borrow.backend.model.entity;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentCouncilFeeVerification {
    private String id;
    private boolean verify;
    private String s3Link;
    private LocalDateTime requestAt;
    private LocalDateTime responseAt;
    private String denyReason;
}

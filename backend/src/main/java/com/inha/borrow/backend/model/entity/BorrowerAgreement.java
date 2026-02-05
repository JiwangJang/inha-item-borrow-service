package com.inha.borrow.backend.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BorrowerAgreement {
    private int id;
    private String borrowerId;
    private LocalDateTime agreedAt;
    private String version;
}

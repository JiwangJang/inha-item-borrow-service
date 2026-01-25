package com.inha.borrow.backend.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentCouncilFee {
    String id;
    boolean verify;
    String s3Link;
    Timestamp requestAt;
    Timestamp responseAt;
    String denyReason;


}

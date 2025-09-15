package com.inha.borrow.backend.model.dto.request;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaveRequestResultDto {
    private int requestId;
    private Timestamp createdAt;
}

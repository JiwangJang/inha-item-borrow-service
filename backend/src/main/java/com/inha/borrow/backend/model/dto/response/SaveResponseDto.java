package com.inha.borrow.backend.model.dto.response;

import com.inha.borrow.backend.enums.ResponseType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaveResponseDto {
    int requestId;
    String rejectReason;
    ResponseType type;

}

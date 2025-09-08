package com.inha.borrow.backend.model.dto.apiResponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@AllArgsConstructor
public class ErrorResponse {
    String errorCode;
    String errorMessage;
}

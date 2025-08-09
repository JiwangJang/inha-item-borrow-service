package com.inha.borrow.backend.model.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EvaluationRequest {
    private String state;
    private String rejectReason;
}

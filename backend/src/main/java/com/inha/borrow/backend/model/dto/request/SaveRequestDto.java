package com.inha.borrow.backend.model.dto.request;

import com.inha.borrow.backend.enums.RequestType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaveRequestDto {
    private Timestamp returnAt;
    private Timestamp borrowerAt;
    private RequestType type;
}

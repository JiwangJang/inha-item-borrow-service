package com.inha.borrow.backend.model.dto.user.borrower;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BorrowerLoginDto {
    String id;
    String department;

}

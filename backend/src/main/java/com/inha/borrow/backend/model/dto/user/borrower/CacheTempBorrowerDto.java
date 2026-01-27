package com.inha.borrow.backend.model.dto.user.borrower;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

/**
 * 임시 캐쉬 dto
 *
 * @author 형민재
 */
public class CacheTempBorrowerDto {
    String id;
    String name;
    String department;

}

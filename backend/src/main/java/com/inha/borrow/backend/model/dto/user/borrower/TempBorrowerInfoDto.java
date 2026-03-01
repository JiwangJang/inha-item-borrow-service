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
 * i-class에서 이름과 학과 추출한 것을 임시 저장히기 위한 dto
 *
 * @author 형민재
 */
public class TempBorrowerInfoDto {
    String name;
    String department;
}

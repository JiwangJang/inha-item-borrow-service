package com.inha.borrow.backend.model.dto.notice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModifyNoticeDto {
    private int id;
    private String newTitle;
    private String newContent;
}

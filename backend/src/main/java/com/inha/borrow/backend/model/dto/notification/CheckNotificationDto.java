package com.inha.borrow.backend.model.dto.notification;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CheckNotificationDto {
    private String borrowerId;
    private List<Integer> ids;
}

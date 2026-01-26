package com.inha.borrow.backend.model.dto.notification;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FindNotificationDto {
    private String borrowerId;
    private LocalDateTime lastNotifyAt;
}

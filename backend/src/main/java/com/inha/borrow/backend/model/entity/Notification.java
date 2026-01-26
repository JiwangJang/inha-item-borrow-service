package com.inha.borrow.backend.model.entity;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Notification {
    private int id;
    private boolean read;
    private String content;
    private String targetId;
    private LocalDateTime notifyAt;
}

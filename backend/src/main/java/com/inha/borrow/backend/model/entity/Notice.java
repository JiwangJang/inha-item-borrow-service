package com.inha.borrow.backend.model.entity;

import java.sql.Timestamp;

import com.inha.borrow.backend.enums.Role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notice {
    private int id;
    private String title;
    private String content;
    private String authorId;
    private Timestamp postedAt;
    private Timestamp updatedAt;
    private String adminName;
    private Role adminPosition;
}

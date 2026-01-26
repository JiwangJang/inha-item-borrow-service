package com.inha.borrow.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.inha.borrow.backend.model.dto.notification.AddNotificationDto;
import com.inha.borrow.backend.model.dto.notification.CheckNotificationDto;
import com.inha.borrow.backend.model.dto.notification.FindNotificationDto;
import com.inha.borrow.backend.model.entity.Notification;
import com.inha.borrow.backend.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository repository;

    public void addNotification(AddNotificationDto dto) {
        repository.addNotification(dto.getContent(), dto.getTargetId());
    }

    public List<Notification> findAllNotifications(FindNotificationDto dto) {
        return repository.findAllNotifications(dto.getBorrowerId(), dto.getLastNotifyAt());
    }

    public void checkNotification(CheckNotificationDto dto) {
        repository.checkNotification(dto.getBorrowerId(), dto.getIds());
    }

}

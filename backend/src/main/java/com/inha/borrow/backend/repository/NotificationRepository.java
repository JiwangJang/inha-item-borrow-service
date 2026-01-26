package com.inha.borrow.backend.repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.inha.borrow.backend.model.entity.Notification;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class NotificationRepository {
    private JdbcTemplate jdbcTemplate;

    public void addNotification(String content, String targetId) {
        String sql = "INSERT INTO notification(content, target_id) VALUES(?, ?);";

        jdbcTemplate.update(sql, content, targetId);
    }

    public List<Notification> findAllNotifications(String borrowerId, LocalDateTime lastNotifyAt) {
        ArrayList<Object> params = new ArrayList<>();
        params.add(borrowerId);
        params.add(lastNotifyAt);

        String sql = """
                SELECT * FROM notification
                        WHERE target_id = ?
                        AND notify_at < ?
                        ORDER BY notify_at DESC
                        LIMIT 20;
                """;

        List<Notification> results = jdbcTemplate.query(sql, (resultSet, rowNum) -> {
            return Notification.builder()
                    .id(resultSet.getInt("id"))
                    .read(resultSet.getBoolean("read"))
                    .content(resultSet.getString("content"))
                    .targetId(resultSet.getString("target_id"))
                    .notifyAt(resultSet.getTimestamp("notify_at").toLocalDateTime())
                    .build();
        }, borrowerId, lastNotifyAt);

        return results;
    }

    public void checkNotification(String borrowerId, List<Integer> ids) {
        if (ids == null || ids.isEmpty())
            return;

        String placeholders = String.join(",", Collections.nCopies(ids.size(), "?"));

        String sql = """
                    UPDATE notification
                    SET is_read = 1, read_at = NOW()
                    WHERE user_id = ?
                      AND is_read = false
                      AND id IN (%s);
                """.formatted(placeholders);

        List<Object> params = new ArrayList<>();
        params.add(borrowerId);
        params.addAll(ids);

        jdbcTemplate.update(sql, params.toArray());
    }
}

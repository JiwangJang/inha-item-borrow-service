package com.inha.borrow.backend.repository;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.inha.borrow.backend.model.entity.Notice;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class NoticeRepository {
    private final JdbcTemplate jdbcTemplate;

    /**
     * 공지사항을 등록하는 메서드
     * 
     * @param title
     * @param content
     * @param authorId
     * @author 장지왕
     */
    public void postNotice(String title, String content, String authorId) {
        String sql = "INSERT INTO notice(title, content, author_id) VALUES(?, ?, ?);";
        jdbcTemplate.update(sql, title, content, authorId);
    }

    /**
     * 공지 다건 조회
     * 
     * @return 공지 목록
     * @author 장지왕
     */
    public List<Notice> findAllNotices() {
        String sql = "SELECT * FROM notice ORDER BY id DESC;";
        return jdbcTemplate.query(sql, (col, rowNum) -> {
            return Notice.builder()
                    .id(col.getInt("id"))
                    .title(col.getString("title"))
                    .content(col.getString("content"))
                    .postedAt(col.getTimestamp("posted_at").toLocalDateTime())
                    .updatedAt(col.getTimestamp("updated_at").toLocalDateTime())
                    .authorId(col.getString("author_id"))
                    .build();
        });
    }

    /**
     * 공지 단건조회
     * 
     * @param id
     * @return 공지 단건
     * @author 장지왕
     */
    public Notice findNoticeById(int id) {
        String sql = "SELECT * FROM notice WHERE id = ?;";
        return jdbcTemplate.queryForObject(sql, (col, num) -> {
            return Notice.builder()
                    .id(col.getInt("id"))
                    .title(col.getString("title"))
                    .content(col.getString("content"))
                    .postedAt(col.getTimestamp("posted_at").toLocalDateTime())
                    .updatedAt(col.getTimestamp("updated_at").toLocalDateTime())
                    .authorId(col.getString("author_id"))
                    .build();
        }, id);
    }

    /**
     * 공지 수정 메서드
     * 
     * @param id
     * @param newTitle
     * @param newContent
     * @author 장지왕
     */
    public void modifyNotice(int id, String newTitle, String newContent, String authorId) {
        String sql = "UPDATE notice SET title = ?, content = ?, author_id = ? WHERE id = ?;";
        jdbcTemplate.update(sql, id, newTitle, authorId, newContent);
    }

    /**
     * 공지 삭제 메서드
     * 
     * @param id
     * @author 장지왕
     */
    public void deleteNotice(int id) {
        String sql = "DELETE FROM notice WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}

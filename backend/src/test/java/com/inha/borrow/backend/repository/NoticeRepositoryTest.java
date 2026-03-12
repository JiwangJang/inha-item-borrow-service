package com.inha.borrow.backend.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.context.annotation.Import;

import com.inha.borrow.backend.model.entity.Notice;
import com.inha.borrow.backend.model.exception.ResourceNotFoundException;
import org.springframework.transaction.annotation.Transactional;

@JdbcTest
@Import(NoticeRepository.class)
@Transactional
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class NoticeRepositoryTest {
    // 테스트 안돌려봄 돌려봐야함
    @Autowired
    private NoticeRepository repository;
    private Notice notice;

    private String testAuthorId = "authorId";

    @BeforeEach
    void setUp() {
        notice = Notice.builder()
                .title("testTitle")
                .content("testContent")
                .build();
    }

    @Test
    @DisplayName("postNotice 메서드 테스트 - 공지 등록 테스트")
    public void postNoticeTest() {
        // given
        // when
        repository.postNotice(testAuthorId , notice);
        List<Notice> notices = repository.findAllNotices();

        // then
        assertTrue(notices.size() >= 1);
    }

    @Test
    @DisplayName("findAllNotices 메서드 테스트 - 공지 전체 조회 테스트")
    public void findAllNoticesTest() {
        // given
        // when
        repository.postNotice(testAuthorId , notice);
        repository.postNotice(testAuthorId , notice);
        repository.postNotice(testAuthorId , notice);
        repository.postNotice(testAuthorId , notice);
        List<Notice> notices = repository.findAllNotices();

        // then
        assertTrue(notices.size() >= 4);
    }

    @Test
    @DisplayName("findAllNotices 메서드 테스트 - 공지 단건 조회 테스트")
    public void findNoticeByIdTest() {
        // given
        // when
        int id = repository.postNotice(testAuthorId , notice);
        Notice notice = repository.findNoticeById(id);

        // then
        assertEquals(notice.getContent(), notice.getContent());
        assertEquals(notice.getTitle(), notice.getTitle());
        assertEquals(notice.getAuthorId(), testAuthorId);
    }

    @Test
    @DisplayName("modifyNotice 메서드 테스트 - 공지 수정 테스트")
    public void modifyNoticeTest() {
        // given
        int id = repository.postNotice(testAuthorId , notice);
        String newTitle = "newTitle";
        String newContent = "newContent";
        String newAuthorId = "newAuthorId";

        // when
        repository.modifyNotice(id, newTitle, newContent, newAuthorId);
        Notice notice = repository.findNoticeById(id);

        // then
        assertEquals(notice.getTitle(), newTitle);
        assertEquals(notice.getContent(), newContent);
        assertEquals(notice.getAuthorId(), newAuthorId);
    }

    @Test
    @DisplayName("deleteNotice 메서드 테스트 - 공지 삭제 테스트")
    public void deleteNoticeTest() {
        // given
        int id = repository.postNotice(testAuthorId , notice);

        // when
        repository.deleteNotice(id);

        // then
        assertThrows(ResourceNotFoundException.class, () -> repository.findNoticeById(id));
    }

}

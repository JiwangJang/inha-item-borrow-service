package com.inha.borrow.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.inha.borrow.backend.model.dto.notice.ModifyNoticeDto;
import com.inha.borrow.backend.model.dto.notice.PostNoticeDto;
import com.inha.borrow.backend.model.entity.Notice;
import com.inha.borrow.backend.model.exception.ResourceNotFoundException;

@SpringBootTest
@Transactional
public class NoticeServiceTest {
        @Autowired
        private NoticeService noticeService;

        String testTitle = "title";
        String testContent = "content";
        String testAuthorId = "test_admin";

        @Test
        @DisplayName("공지 등록 테스트")
        public void postNoticeTest() {
                // given
                PostNoticeDto dto = PostNoticeDto.builder()
                                .title(testTitle)
                                .content(testContent)
                                .build();

                // when
                int createdId = noticeService.postNotice(testAuthorId, dto);
                Notice createdNotice = noticeService.findNoticeById(createdId);

                // then
                assertEquals(testTitle, createdNotice.getTitle());
                assertEquals(testContent, createdNotice.getContent());
                assertEquals(testAuthorId, createdNotice.getAuthorId());
        }

        @Test
        @DisplayName("공지 전체 조회 테스트")
        public void findAllNoticesTest() {
                // given
                PostNoticeDto dto1 = PostNoticeDto.builder()
                                .title(testTitle)
                                .content(testContent)
                                .build();
                PostNoticeDto dto2 = PostNoticeDto.builder()
                                .title(testTitle)
                                .content(testContent)
                                .build();

                noticeService.postNotice(testAuthorId, dto1);
                noticeService.postNotice(testAuthorId, dto2);

                // when
                List<Notice> results = noticeService.findAllNotices();

                // then
                assertEquals(2, results.size());
                assertEquals(testTitle, results.get(0).getTitle());
                assertEquals(testTitle, results.get(1).getTitle());
        }

        @Test
        @DisplayName("공지 수정 테스트")
        public void modifyNoticeTest() {
                // given
                String newTitle = "newTitle";
                String newContent = "newContent";
                String newAuthorId = "test_admin";
                PostNoticeDto postDto = PostNoticeDto.builder()
                                .title(testTitle)
                                .content(testContent)
                                .build();
                int id = noticeService.postNotice(newAuthorId, postDto);

                // when
                ModifyNoticeDto modifyDto = ModifyNoticeDto.builder()
                                .id(id)
                                .newTitle(newTitle)
                                .newContent(newContent)
                                .build();
                noticeService.modifyNotice(newAuthorId, modifyDto);
                Notice notice = noticeService.findNoticeById(id);

                // then
                assertEquals(newTitle, notice.getTitle());
                assertEquals(newContent, notice.getContent());
                assertEquals(newAuthorId, notice.getAuthorId());
        }

        @Test
        @DisplayName("공지 삭제 테스트")
        public void deleteNoticeTest() {
                // given
                PostNoticeDto postDto = PostNoticeDto.builder()
                                .title(testTitle)
                                .content(testContent)
                                .build();
                int id = noticeService.postNotice(testAuthorId, postDto);

                // when-then
                assertThrows(ResourceNotFoundException.class, () -> noticeService.findNoticeById(id));
        }
}

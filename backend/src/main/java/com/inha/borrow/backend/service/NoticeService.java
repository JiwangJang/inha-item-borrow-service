package com.inha.borrow.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.inha.borrow.backend.model.dto.notice.ModifyNoticeDto;
import com.inha.borrow.backend.model.dto.notice.PostNoticeDto;
import com.inha.borrow.backend.model.entity.Notice;
import com.inha.borrow.backend.repository.NoticeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NoticeService {
    private final NoticeRepository repository;

    /**
     * 공지사항 등록 메서드
     * 
     * @param adminId
     * @param dto
     * @author 장지왕
     */
    public int postNotice(String adminId, PostNoticeDto dto) {
        Notice notice = Notice.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .authorId(adminId)
                .build();
        return repository.postNotice(adminId,notice);
    }

    /**
     * 공지사항 다건 조회 메서드
     * 
     * @return 공지 목록
     * @author 장지왕
     */
    public List<Notice> findAllNotices() {
        return repository.findAllNotices();
    }

    /**
     * 공지사항 단건 조회 메서드
     * 
     * @param id
     * @return 공지 단건
     * @author 장지왕
     */
    public Notice findNoticeById(int id) {
        return repository.findNoticeById(id);
    }

    /**
     * 공지 수정 메서드
     * 
     * @param adminId
     * @param dto
     * @author 장지왕
     */
    public void modifyNotice(String adminId, ModifyNoticeDto dto) {
        repository.modifyNotice(dto.getId(), dto.getNewTitle(), dto.getNewContent(), adminId);
    }

    /**
     * 공지 삭제메서드
     * 
     * @param id
     * @author 장지왕
     */
    public void deleteNotice(int id) {
        repository.deleteNotice(id);
    }
}

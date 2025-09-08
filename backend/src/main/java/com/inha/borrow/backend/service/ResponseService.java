package com.inha.borrow.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.inha.borrow.backend.model.dto.response.SaveResponseDto;
import com.inha.borrow.backend.model.entity.Response;
import com.inha.borrow.backend.repository.ResponseRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResponseService {
    private final ResponseRepository responseRepository;

    @Transactional
    public Response createResponse(String adminId, SaveResponseDto dto) {
        // 요청객체 조회해서 담당자 맞는지 확인
        // 아이템 객체 상태 변경
        // 요청객체 상태 변경
        return responseRepository.save(dto);
    }
}

package com.inha.borrow.backend.service;

import java.net.URI;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.github.benmanes.caffeine.cache.Cache;
import com.inha.borrow.backend.model.dto.studentCouncilFeeVerification.DenyFeeVerificationDto;
import com.inha.borrow.backend.model.dto.studentCouncilFeeVerification.ModifyVerificationResponseDto;
import com.inha.borrow.backend.model.dto.user.borrower.CacheBorrowerDto;
import com.inha.borrow.backend.model.entity.StudentCouncilFeeVerification;
import com.inha.borrow.backend.repository.StudentCouncilFeeVerificationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentCouncilFeeVerificationService {
    private final StudentCouncilFeeVerificationRepository repository;
    private final S3Service s3Service;
    private final String folder = "student-council-fee";
    private final Cache<String, CacheBorrowerDto> borrowerCache;

    /**
     * 사용자가 새로운(또는 거절후) 요청을 등록할 때 사용
     * 
     * @param id                사용자 아이디
     * @param verificationImage 인증 사진
     * @author 장지왕
     */
    public void verificationRequestSave(String borrowerId, MultipartFile verificationImage) {
        CacheBorrowerDto dto = borrowerCache.getIfPresent(borrowerId);
        if (dto != null && dto.getS3Link() != null) {
            log.info("사진삭제 {}", dto.getS3Link());
            s3Service.deleteFile(dto.getS3Link());
        }

        String s3Link = s3Service.uploadFile(verificationImage, "student-council-fee");
        if (dto != null) {
            dto.setS3Link(s3Link);
        }

        repository.verificationRequestSave(borrowerId, s3Link);
        borrowerCache.put(borrowerId, dto);
    }

    /**
     * 맨처음 개인정보수집 동의 했을때 사용하는 메서드
     * 
     * @param id
     * @author 장지왕
     */
    public void initalSave(String borrowerId) {
        repository.initialSave(borrowerId);
    }

    /**
     * 관리자가 처리해야할 요청과 처리한 요청을 볼 떄 사용하는 메서드(다건조회)
     * 
     * @return 인증요청 목록
     * @author 장지왕
     */
    public List<StudentCouncilFeeVerification> findAllRequests() {
        return repository.findAllRequests();
    }

    /**
     * 관리자나 사용자가 단건조회할 때 사용하는 메서드
     * 
     * @return 인증요청 객체
     * @author 장지왕
     */
    public StudentCouncilFeeVerification findRequestByBorrowerId(String borrowerId) {
        // 우선적으로 캐시 찾고 캐시에 없으면 DB에서 찾도록 변경하기
        return repository.findRequestByBorrowerId(borrowerId);
    }

    /**
     * 관리자가 사용자의 학생회비 납부인증을 승인할 때 사용하는 메서드
     * 
     * @param id
     * @author 장지왕
     */
    public void permitVerificationRequest(int id) {
        repository.updateForAdmin(id, true, null);
        // 여기서는 캐시 업데이트하기
    }

    /**
     * 관리자가 사용자의 학생회비 납부인증을 거부할 때 사용하는 메서드
     * 
     * @param id
     * @param denyReason
     * @author 장지왕
     */
    public void denyVerificationRequest(int id, DenyFeeVerificationDto dto) {
        repository.updateForAdmin(id, false, dto.getDenyReason());
        // 여기서는 캐시 업데이트하기
    }

    /**
     * 관리자가 사용자의 학생회비 납부인증을 수정할 때 사용하는 메서드
     * 
     * @param id
     * @param denyReason
     * @author 장지왕
     */
    public void modifyVerificationResponse(int id, ModifyVerificationResponseDto dto) {
        if (dto.isVerify()) {
            repository.updateForAdmin(id, true, null);
            // 여기서는 캐시 업데이트하기
        } else {
            repository.updateForAdmin(id, false, dto.getDenyReason());
            // 여기서는 캐시 업데이트하기
        }
    }

    /**
     * 사용자가 요청을 취소할 때 사용
     * 
     * @param id
     * @author 장지왕
     */
    public void cancel(String borrowerId) {
        s3Service.deleteFile(folder + "/" + borrowerId);
        repository.cancel(borrowerId);
        // 여기서는 캐시 업데이트하기
    }
}

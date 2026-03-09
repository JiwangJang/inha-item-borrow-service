package com.inha.borrow.backend.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.github.benmanes.caffeine.cache.Cache;
import com.inha.borrow.backend.enums.ApiErrorCode;
import com.inha.borrow.backend.model.dto.studentCouncilFeeVerification.UpdateStudentCouncilFeeVerificationDenyDto;
import com.inha.borrow.backend.model.dto.studentCouncilFeeVerification.UpdateStudentCouncilFeeVerificationPermitDto;
import com.inha.borrow.backend.model.dto.studentCouncilFeeVerification.UpdateStudentCouncilFeeVerificationResponseDto;
import com.inha.borrow.backend.model.dto.user.borrower.BorrowerCacheData;
import com.inha.borrow.backend.model.entity.StudentCouncilFeeVerification;
import com.inha.borrow.backend.model.entity.user.Borrower;
import com.inha.borrow.backend.model.exception.InvalidValueException;
import com.inha.borrow.backend.repository.StudentCouncilFeeVerificationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentCouncilFeeVerificationService {
    private final StudentCouncilFeeVerificationRepository repository;
    private final BorrowerService borrowerService;
    private final S3Service s3Service;
    private final Cache<String, BorrowerCacheData> borrowerCache;

    // --------- 생성 메서드 ---------

    /**
     * 맨처음 개인정보수집 동의 했을때 사용하는 메서드
     * 
     * @param id
     * @author 장지왕
     */
    public void initalSave(Borrower borrower) {
        repository.initialSave(borrower);
    }

    /**
     * 사용자가 새로운(또는 거절후) 요청을 등록할 때 사용
     * 
     * @param id                사용자 아이디
     * @param verificationImage 인증 사진
     * @author 장지왕
     */
    public void verificationRequestSave(Borrower borrower, MultipartFile verificationImage) {
        String borrowerId = borrower.getId();
        BorrowerCacheData borrowerCacheData = borrowerCache.getIfPresent(borrowerId);
        if (borrowerCacheData == null) {
            throw new AccessDeniedException("개인정보 수집 이용에 동의해주셔야 등록 가능합니다.");
        }

        if (borrowerCacheData != null && borrowerCacheData.getS3Link() != null) {
            s3Service.deleteFile(borrowerCacheData.getS3Link());
        }

        String s3Link = s3Service.uploadFile(verificationImage, "student-council-fee");
        if (borrowerCacheData != null) {
            borrowerCacheData.setS3Link(s3Link);
        }

        repository.verificationRequestSave(borrower, s3Link);
        borrowerCache.put(borrowerId, borrowerCacheData);
    }

    // --------- 조회 메서드 ---------

    /**
     * 관리자가 처리해야할 요청과 처리한 요청을 볼 떄 사용하는 메서드(다건조회)
     * 
     * @return 인증요청 목록
     * @author 장지왕
     */
    public List<StudentCouncilFeeVerification> findAll() {
        return repository.findAll();
    }

    /**
     * 대여자가 단건조회할 때 사용하는 메서드
     * 
     * @return 인증요청 객체
     * @author 장지왕
     */
    public StudentCouncilFeeVerification findByBorrowerId(Borrower borrower) {
        return repository.findByBorrowerId(borrower);
    }

    /**
     * 관리자가 사용자의 학생회비 납부인증을 승인할 때 사용하는 메서드
     * 
     * @param id
     * @author 장지왕
     */
    public void updateStudentCouncilFeeVerificationPermit(
            UpdateStudentCouncilFeeVerificationPermitDto dto,
            int verificationId) {
        StudentCouncilFeeVerification verification = StudentCouncilFeeVerification
                .builder()
                .id(verificationId)
                .build();
        repository.updateForAdmin(verification, true, null);
        borrowerService.refreshBorrowerCacheData(dto.getBorrowerId());
    }

    // --------- 수정 메서드 ---------

    /**
     * 관리자가 사용자의 학생회비 납부인증을 거부할 때 사용하는 메서드
     * 
     * @param id
     * @param denyReason
     * @author 장지왕
     */
    public void updateStudentCouncilFeeVerificationDeny(UpdateStudentCouncilFeeVerificationDenyDto dto,
            int verificationId) {
        StudentCouncilFeeVerification verification = StudentCouncilFeeVerification
                .builder()
                .id(verificationId)
                .build();
        repository.updateForAdmin(verification, false, dto.getDenyReason());
        borrowerService.refreshBorrowerCacheData(dto.getBorrowerId());
    }

    /**
     * 관리자가 사용자의 학생회비 납부인증을 수정할 때 사용하는 메서드
     * 
     * @param id
     * @param denyReason
     * @author 장지왕
     */
    public void updateStudentCouncilFeeVerificationResponse(
            UpdateStudentCouncilFeeVerificationResponseDto dto,
            int verificationId) {
        StudentCouncilFeeVerification verification = StudentCouncilFeeVerification
                .builder()
                .id(verificationId)
                .build();
        if (dto.isVerify()) {
            repository.updateForAdmin(verification, true, null);
        } else {
            repository.updateForAdmin(verification, false, dto.getDenyReason());
        }
        borrowerService.refreshBorrowerCacheData(dto.getBorrowerId());
    }

    /**
     * 사용자가 요청을 취소할 때 사용
     * 
     * @param id
     * @author 장지왕
     */
    public void updateStudentCouncilFeeVerificationCancel(Borrower borrower) {
        String borrowerId = borrower.getId();
        BorrowerCacheData borrowerCacheData = borrowerCache.getIfPresent(borrowerId);
        if (borrowerCacheData.isVerify()) {
            ApiErrorCode code = ApiErrorCode.NOT_ALLOWED_VALUE;
            throw new InvalidValueException(code.name(), "승인된 학생회비 인증요청은 취소가 불가능합니다.");
        }
        s3Service.deleteFile(borrowerCacheData.getS3Link());
        repository.updateStudentCouncilFeeVerificationCancel(borrower);
        borrowerService.refreshBorrowerCacheData(borrowerId);
    }
}

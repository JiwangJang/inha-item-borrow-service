package com.inha.borrow.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.inha.borrow.backend.model.dto.studentCouncilFeeVerification.DenyFeeVerificationDto;
import com.inha.borrow.backend.model.dto.studentCouncilFeeVerification.FindFeeVerificationRequestDto;
import com.inha.borrow.backend.model.dto.studentCouncilFeeVerification.ModifyVerificationResponseDto;
import com.inha.borrow.backend.model.entity.StudentCouncilFeeVerification;
import com.inha.borrow.backend.model.entity.user.Borrower;
import com.inha.borrow.backend.model.entity.user.User;
import com.inha.borrow.backend.repository.StudentCouncilFeeVerificationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StudentCouncilFeeVerificationService {
    private final StudentCouncilFeeVerificationRepository repository;
    private final NotificationService notificationService;
    private final S3Service s3Service;
    private final String folder = "student-council-fee";

    /**
     * 사용자가 새로운(또는 거절후) 요청을 등록할 때 사용
     * 
     * @param id                사용자 아이디
     * @param verificationImage 인증 사진
     * @author 장지왕
     */
    public void verificationRequestSave(String id, MultipartFile verificationImage) {
        String s3Link = s3Service.uploadFile(verificationImage, "student-council-fee", id);
        // 기존 사진이 있을경우 삭제하는 로직 추가(캐시 확인)
        repository.verificationRequestSave(id, s3Link);
    }

    /**
     * 맨처음 개인정보수집 동의 했을때 사용하는 메서드
     * 
     * @param id
     * @author 장지왕
     */
    public void initalSave(String id) {
        repository.initialSave(id);
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
    public StudentCouncilFeeVerification findRequestById(User user, FindFeeVerificationRequestDto dto) {
        String id = "";
        if (user instanceof Borrower) {
            id = user.getId();
        } else {
            id = dto.getId();
        }

        return repository.findRequestById(id);
    }

    /**
     * 관리자가 사용자의 학생회비 납부인증을 승인할 때 사용하는 메서드
     * 
     * @param id
     * @author 장지왕
     */
    public void permitVerificationRequest(String id) {
        String content = "학생회비 납부인증이 완료됐습니다.";
        notificationService.addNotification(content, id);
        repository.updateForAdmin(id, true, null);
    }

    /**
     * 관리자가 사용자의 학생회비 납부인증을 거부할 때 사용하는 메서드
     * 
     * @param id
     * @param denyReason
     * @author 장지왕
     */
    public void denyVerificationRequest(DenyFeeVerificationDto dto) {
        String content = "학생회비 납부인증이 거절됐습니다. 자세한 내용은 거절 사유를 확인해주세요.";
        notificationService.addNotification(content, dto.getId());
        repository.updateForAdmin(dto.getId(), false, dto.getDenyReason());
    }

    /**
     * 관리자가 사용자의 학생회비 납부인증을 수정할 때 사용하는 메서드
     * 
     * @param id
     * @param denyReason
     * @author 장지왕
     */
    public void modifyVerificationResponse(ModifyVerificationResponseDto dto) {
        if (dto.isVerify()) {
            String content = "학생회비 납부인증이 완료됐습니다.";
            notificationService.addNotification(content, dto.getId());
            repository.updateForAdmin(dto.getId(), true, null);
        } else {
            String content = "학생회비 납부인증이 거절됐습니다. 자세한 내용은 거절 사유를 확인해주세요.";
            notificationService.addNotification(content, dto.getId());
            repository.updateForAdmin(dto.getId(), false, dto.getDenyReason());
        }
    }

    /**
     * 사용자가 요청을 취소할 때 사용
     * 
     * @param id
     * @author 장지왕
     */
    public void cancel(String id) {
        s3Service.deleteFile(folder + "/" + id);
        repository.cancel(id);
    }
}

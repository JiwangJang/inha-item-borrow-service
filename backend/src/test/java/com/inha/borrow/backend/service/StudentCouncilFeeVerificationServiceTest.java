package com.inha.borrow.backend.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.inha.borrow.backend.model.dto.studentCouncilFeeVerification.DenyFeeVerificationDto;
import com.inha.borrow.backend.model.dto.studentCouncilFeeVerification.FindFeeVerificationRequestDto;
import com.inha.borrow.backend.model.dto.studentCouncilFeeVerification.ModifyVerificationResponseDto;
import com.inha.borrow.backend.model.dto.studentCouncilFeeVerification.PermitFeeVerificationDto;
import com.inha.borrow.backend.model.entity.StudentCouncilFeeVerification;
import com.inha.borrow.backend.model.entity.user.Admin;
import com.inha.borrow.backend.model.entity.user.Borrower;
import com.inha.borrow.backend.repository.StudentCouncilFeeVerificationRepository;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

@SpringBootTest
@Transactional
@Slf4j
public class StudentCouncilFeeVerificationServiceTest {
    // 인증하는거 없어서 안돌아감 -> 나중에 인증 하는거 완료되고 돌려봐야함
    @Autowired
    private StudentCouncilFeeVerificationService service;

    @Autowired
    private StudentCouncilFeeVerificationRepository repository;

    @Mock
    private MultipartFile verificationImage;

    private String testId = "12345678";
    private String s3Url = "https://s3.amazonaws.com/student-council-fee/test-user-123/image.jpg";

    @Test
    @DisplayName("verifyRequestSave 메서드 테스트")
    public void testVerificationRequestSave() {
        // given
        repository.initialSave(testId);
        service.verificationRequestSave(testId, verificationImage);

        // when
        StudentCouncilFeeVerification verification = repository.findRequestById(testId);
        log.info(verification.getRequestAt().toString());

        // then
        assertSame(verification.getS3Link(), s3Url);
    }

    @Test
    @DisplayName("initialSave 메서드 테스트")
    public void testInitialSave() {
        // given
        service.initalSave(testId);

        // when
        StudentCouncilFeeVerification verification = repository.findRequestById(testId);

        // then
        assertNull(verification.getRequestAt());
    }

    @Test
    @DisplayName("findAllRequests 메서드 테스트")
    public void testFindAllRequests() {
        // given
        String testId1 = "1111111";
        String testId2 = "22222222";

        // when
        service.initalSave(testId1);
        service.initalSave(testId2);
        List<StudentCouncilFeeVerification> results = service.findAllRequests();

        // then
        StudentCouncilFeeVerification result1 = results.get(0);
        StudentCouncilFeeVerification result2 = results.get(1);
        assertTrue(results.size() >= 2);
        assertNull(result1.getRequestAt());
        assertNull(result2.getRequestAt());
    }

    @Test
    @DisplayName("findRequestById 메서드 테스트-사용자요청시")
    public void testFindRequestByIdWithBorrower() {
        // given
        Borrower borrower = Borrower.builder()
                .id(testId)
                .build();
        repository.initialSave(testId);
        repository.verificationRequestSave(testId, s3Url);

        // when
        StudentCouncilFeeVerification result = service.findRequestById(borrower, null);

        // then
        assertNull(result.getRequestAt());
    }

    @Test
    @DisplayName("findRequestById 메서드 테스트-관리자요청시")
    public void testFindRequestByIdWithAdmin() {
        // given
        Admin admin = Admin.builder()
                .id(testId)
                .build();
        repository.initialSave(testId);
        repository.verificationRequestSave(testId, s3Url);

        // when
        FindFeeVerificationRequestDto dto = new FindFeeVerificationRequestDto(testId);
        StudentCouncilFeeVerification result = service.findRequestById(admin, dto);

        // then
        assertSame(result.getS3Link(), s3Url);
    }

    @Test
    @DisplayName("permitVerificationRequest 메서드 테스트")
    public void testPermitVerificationRequest() {
        // given
        PermitFeeVerificationDto dto = new PermitFeeVerificationDto(testId);
        repository.initialSave(testId);

        // when
        service.permitVerificationRequest(dto.getId());
        StudentCouncilFeeVerification result = repository.findRequestById(testId);

        // then
        assertFalse(result.isVerify());
    }

    @Test
    @DisplayName("denyVerificationRequest 메서드 테스트")
    public void testDenyVerificationRequest() {
        // given
        DenyFeeVerificationDto dto = new DenyFeeVerificationDto(testId, "invalid");
        repository.initialSave(testId);

        // when
        service.denyVerificationRequest(dto);
        StudentCouncilFeeVerification result = repository.findRequestById(testId);

        // then
        assertFalse(result.isVerify());
    }

    @Test
    @DisplayName("modifyVerificationResponse 메서드 테스트- 승인 -> 거절")
    public void testModifyVerificationResponseApprove() {
        // given
        repository.initialSave(testId);
        repository.verificationRequestSave(testId, s3Url);
        repository.updateForAdmin(testId, true, null);
        ModifyVerificationResponseDto dto = new ModifyVerificationResponseDto(testId, false, "reason");

        // when
        service.modifyVerificationResponse(dto);
        StudentCouncilFeeVerification result = repository.findRequestById(testId);
        // then
        assertFalse(result.isVerify());
    }

    @Test
    @DisplayName("modifyVerificationResponse 메서드 테스트- 거절 -> 승인")
    public void testModifyVerificationResponseDeny() {
        // given
        repository.initialSave(testId);
        repository.verificationRequestSave(testId, s3Url);
        repository.updateForAdmin(testId, false, "ss");
        ModifyVerificationResponseDto dto = new ModifyVerificationResponseDto(testId, true, null);

        // when
        service.modifyVerificationResponse(dto);
        StudentCouncilFeeVerification result = repository.findRequestById(testId);

        // then
        assertTrue(result.isVerify());
    }

    @Test
    @DisplayName("cancel 메서드 테스트")
    public void testCancel() {
        // given
        repository.initialSave(testId);
        repository.verificationRequestSave(testId, s3Url);

        // when
        service.cancel(testId);
        StudentCouncilFeeVerification result = repository.findRequestById(testId);

        // then
        assertNull(result.getRequestAt());
    }
}

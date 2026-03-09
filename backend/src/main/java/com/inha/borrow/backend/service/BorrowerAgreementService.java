
package com.inha.borrow.backend.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.inha.borrow.backend.enums.ApiErrorCode;
import com.inha.borrow.backend.model.dto.agreement.AgreementDto;
import com.inha.borrow.backend.model.dto.user.borrower.BorrowerCacheData;
import com.inha.borrow.backend.model.dto.user.borrower.SaveBorrowerDto;
import com.inha.borrow.backend.model.dto.user.borrower.TempBorrowerInfoCacheData;
import com.inha.borrow.backend.model.entity.user.Borrower;
import com.inha.borrow.backend.model.exception.ResourceNotFoundException;
import com.inha.borrow.backend.repository.BorrowerAgreementRepository;
import com.inha.borrow.backend.repository.BorrowerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class BorrowerAgreementService {
    private final BorrowerAgreementRepository borrowerAgreementRepository;
    private final BorrowerRepository borrowerRepository;
    private final Cache<String, BorrowerCacheData> borrowerCache;
    private final Cache<String, TempBorrowerInfoCacheData> tempBorrowerCache;
    private final StudentCouncilFeeVerificationService studentCouncilFeeVerificationService;

    /**
     * 개인정보동의를 저장하기 위한 메서드
     *
     * @param borrowerId
     * @param agreementDto
     * @author 형민재
     */
    public int saveAgreement(Borrower borrower, AgreementDto agreementDto) {
        String borrowerId = borrower.getId();
        BorrowerCacheData borrowerCacheData = borrowerCache.getIfPresent(borrowerId);
        TempBorrowerInfoCacheData tempBorrowerCacheData = tempBorrowerCache.getIfPresent(borrowerId);

        if (tempBorrowerCacheData == null && borrowerCacheData == null) {
            ApiErrorCode apiErrorCode = ApiErrorCode.NOT_FOUND_CACHE;
            throw new ResourceNotFoundException(apiErrorCode.name(), apiErrorCode.getMessage());
        }

        if (borrowerCacheData == null && tempBorrowerCacheData != null) {
            // 신규유저인 경우 동작하는 로직(학생회비 납부인증 테이블 등록 및 대여자 테이블 등록, 대여자 캐시 등록)
            SaveBorrowerDto saveBorrowerDto = SaveBorrowerDto.builder()
                    .id(borrowerId)
                    .name(tempBorrowerCacheData.getName())
                    .department(tempBorrowerCacheData.getDepartment())
                    .accountNumber(agreementDto.getAccountNumber())
                    .phonenumber(agreementDto.getPhoneNumber())
                    .build();

            borrowerRepository.save(saveBorrowerDto);

            borrowerCacheData = BorrowerCacheData.builder()
                    .id(borrowerId)
                    .name(tempBorrowerCacheData.getName())
                    .department(tempBorrowerCacheData.getDepartment())
                    .phoneNumber(agreementDto.getPhoneNumber())
                    .accountNumber(agreementDto.getAccountNumber())
                    .ban(false)
                    .verify(false)
                    .s3Link(null)
                    .agreementVersion(agreementDto.getVersion())
                    .build();

            // 대여자 캐시에 등록
            borrowerCache.put(borrowerId, borrowerCacheData);
            // 개인정보 동의후 임시캐시는 지운다
            tempBorrowerCache.invalidate(borrowerId);
            studentCouncilFeeVerificationService.initalSave(borrower);
        }

        // 기존유저는 바로 저장 메서드 실행
        return borrowerAgreementRepository.saveAgreement(borrowerId, agreementDto.getVersion());
    }
}

package com.inha.borrow.backend.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.inha.borrow.backend.enums.ApiErrorCode;
import com.inha.borrow.backend.model.dto.agreement.AgreementDto;
import com.inha.borrow.backend.model.dto.user.borrower.BorrowerDto;
import com.inha.borrow.backend.model.dto.user.borrower.CacheBorrowerDto;
import com.inha.borrow.backend.model.dto.user.borrower.TempBorrowerInfoDto;
import com.inha.borrow.backend.model.entity.BorrowerAgreement;
import com.inha.borrow.backend.model.exception.ResourceNotFoundException;
import com.inha.borrow.backend.repository.BorrowerAgreementRepository;
import com.inha.borrow.backend.repository.BorrowerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BorrowerAgreementService {
    private final BorrowerAgreementRepository borrowerAgreementRepository;
    private final BorrowerRepository borrowerRepository;
    private final Cache<String, CacheBorrowerDto> borrowerCache;
    private final Cache<String, TempBorrowerInfoDto> tempBorrowerCache;

    /**
     * 개인정보동의를 저장하기 위한 메서드
     *
     * @param borrowerId
     * @param agreementDto
     * @author 형민재
     */
    public int saveAgreement(String borrowerId, AgreementDto agreementDto) {
        log.info(borrowerId);
        TempBorrowerInfoDto borrowerInfo = tempBorrowerCache.getIfPresent(borrowerId);
        if (borrowerInfo == null) {
            ApiErrorCode apiErrorCode = ApiErrorCode.NOT_FOUND_CACHE;
            throw new ResourceNotFoundException(apiErrorCode.name(), apiErrorCode.getMessage());
        }
        BorrowerDto borrowerDto = new BorrowerDto(borrowerId, borrowerInfo.getName(),
                borrowerInfo.getDepartment(), agreementDto.getPhoneNumber(), agreementDto.getAccountNumber());
        borrowerRepository.save(borrowerDto);

        CacheBorrowerDto dto = CacheBorrowerDto.builder()
                .id(borrowerId)
                .name(borrowerInfo.getName())
                .department(borrowerInfo.getDepartment())
                .phoneNumber(agreementDto.getPhoneNumber())
                .accountNumber(agreementDto.getAccountNumber())
                .ban(false)
                .verify(false)
                .s3Link(null)
                .agreementVersion(agreementDto.getVersion())
                .build();

        borrowerCache.put(borrowerId, dto);
        return borrowerAgreementRepository.saveAgreement(borrowerId, agreementDto.getVersion());
    }

    /**
     * 개인정보동의를 불러오기 위한 메서드
     *
     * @author 형민재
     */
    public List<BorrowerAgreement> findAllAgreement() {
        return borrowerAgreementRepository.findAllAgreement();
    }

    /**
     * 개인정보동의를 version으로 불러오는 메서드
     *
     * @param version
     * @author 형민재
     */
    public List<BorrowerAgreement> findByVersion(String version) {
        return borrowerAgreementRepository.findbyVersion(version);
    }

    /**
     * 개인정보동의를 borrowerId로 불러오는 메서드
     *
     * @param borrowerId
     * @author 형민재
     */
    public List<BorrowerAgreement> findByBorrower(String borrowerId) {
        return borrowerAgreementRepository.findByBorrowerId(borrowerId);
    }
}

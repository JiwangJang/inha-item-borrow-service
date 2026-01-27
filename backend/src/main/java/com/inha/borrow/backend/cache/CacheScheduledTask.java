package com.inha.borrow.backend.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.inha.borrow.backend.model.dto.user.borrower.CacheBorrowerDto;
import com.inha.borrow.backend.model.entity.StudentCouncilFeeVerification;
import com.inha.borrow.backend.model.entity.user.Borrower;
import com.inha.borrow.backend.repository.BorrowerRepository;
import com.inha.borrow.backend.repository.StudentCouncilFeeVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class CacheScheduledTask {
    private final BorrowerRepository borrowerRepository;
    private final StudentCouncilFeeVerificationRepository studentCouncilFeeVerificationRepository;
    private final Cache<String, CacheBorrowerDto> borrowerCache;
    private static final int ONE_HOUR = 3_600_000;

    // 시간단위는 ms임, 한시간마다 동작되는 메서드
    @Scheduled(fixedRate = ONE_HOUR)
    public void cleanExpiredCache() {
        log.info("[Scheduled] 캐시 정리작업 수행");
        borrowerCache.cleanUp();
    }

    /**
     * 기존 사용자 캐쉬 리프레쉬를 위한 메서드
     *
     * @author 형민재
     */
    @Scheduled(fixedRate = ONE_HOUR)
    public void refreshBorrowerCache(){
        List<Borrower> borrowers = borrowerRepository.findAll();
        List<StudentCouncilFeeVerification> councilFees = studentCouncilFeeVerificationRepository.findAllRequests();

        Map<String, StudentCouncilFeeVerification> feeMap = councilFees.stream()
                .collect(Collectors.toMap(
                        v -> v.getId(),
                        v -> v,
                        (existing, replacement) -> existing // 중복 시 첫 번째 것 유지
                ));


        Map<String, CacheBorrowerDto> finalCacheMap = borrowers.stream()
                .collect(Collectors.toMap(
                        Borrower::getId,
                        b -> {
                            StudentCouncilFeeVerification v = feeMap.get(b.getId());

                            return CacheBorrowerDto.builder()
                                    .id(b.getId())
                                    .name(b.getName())
                                    .department(b.getDepartment())
                                    .phoneNumber(b.getPhonenumber())
                                    .accountNumber(b.getAccountNumber())
                                    .ban(b.isBan())
                                    .verify(v != null && v.isVerify())
                                    .s3Link(v != null ? v.getS3Link() : null)
                                    .build();
                        }
                ));

        borrowerCache.putAll(finalCacheMap);
        log.info("사용자 캐시 갱신 완료. 총 {}명", finalCacheMap.size());

    }
}

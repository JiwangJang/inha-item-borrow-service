package com.inha.borrow.backend.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.inha.borrow.backend.model.dto.user.borrower.CacheBorrowerDto;
import com.inha.borrow.backend.repository.BorrowerRepository;
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
    public void refreshBorrowerListCache(){
        List<CacheBorrowerDto> dtoList = borrowerRepository.findAllWithFeeVerification();
        Map<String, CacheBorrowerDto> map = dtoList.stream()
                .collect(Collectors.toMap(
                        CacheBorrowerDto::getId,
                        dto -> dto));
        borrowerCache.putAll(map);
        log.info("사용자 캐시 갱신 완료. 총 {}명", map.size());

    }

    public CacheBorrowerDto refreshBorrowerCache(String borrowerId){
        CacheBorrowerDto dto = borrowerRepository.findByIdWithFeeVerification(borrowerId);
        borrowerCache.put(borrowerId,dto);
        return dto;
    }
}

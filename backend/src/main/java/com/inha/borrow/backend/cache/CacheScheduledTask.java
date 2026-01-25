package com.inha.borrow.backend.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.inha.borrow.backend.model.entity.user.Borrower;
import com.inha.borrow.backend.repository.BorrowerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class CacheScheduledTask {
    private final BorrowerRepository borrowerRepository;
    private final Cache<String, Borrower> borrowerCache;
    private static final int ONE_HOUR = 3_600_000;

    // 시간단위는 ms임, 한시간마다 동작되는 메서드
    @Scheduled(fixedRate = ONE_HOUR)
    public void cleanExpiredCache() {
        log.info("[Scheduled] 캐시 정리작업 수행");
        borrowerCache.cleanUp();
    }

    @Scheduled(fixedRate = ONE_HOUR)
    public void refreshBorrowerCache(){
        log.info("사용자 캐시 갱신 시작...");
        List<Borrower> borrower = borrowerRepository.findAll();
        Map<String, Borrower> borrowerMap = borrower.stream()
                .collect(Collectors.toMap(Borrower::getId, Function.identity()));
        borrowerCache.putAll(borrowerMap);
        log.info("사용자 캐시 갱신 완료. 총 {}명", borrowerMap.size());

    }
}

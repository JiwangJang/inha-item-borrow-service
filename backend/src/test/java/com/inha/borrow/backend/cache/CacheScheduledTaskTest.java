package com.inha.borrow.backend.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.inha.borrow.backend.model.dto.user.borrower.CacheBorrowerDto;
import com.inha.borrow.backend.repository.BorrowerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CacheScheduledTaskTest {

    private CacheScheduledTask cacheScheduledTask;

    @Mock
    private BorrowerRepository borrowerRepository;

    @Mock
    private Cache<String, CacheBorrowerDto> borrowerCache;

    @BeforeEach
    void setUp() {
        // @InjectMocks 대신 직접 생성자 주입을 사용하여 Mock 객체가 정확히 들어가도록 보장
        cacheScheduledTask = new CacheScheduledTask(borrowerRepository, borrowerCache);
    }

    @Test
    @DisplayName("cleanExpiredCache: 캐시의 cleanUp 메서드가 호출되어야 한다")
    void cleanExpiredCache_Success() {
        // when
        cacheScheduledTask.cleanExpiredCache();

        // then
        verify(borrowerCache, times(1)).cleanUp();
    }

    @Test
    @DisplayName("refreshBorrowerCache: DB에서 데이터를 가져와 Map으로 변환 후 캐시에 저장해야 한다")
    void refreshBorrowerCache_Success() {
        // given
        // 1. 테스트용 더미 데이터 생성 (Builder 패턴 가정)
        CacheBorrowerDto user1 = CacheBorrowerDto.builder().id("12121212").name("김인하").build();
        CacheBorrowerDto user2 = CacheBorrowerDto.builder().id("20202020").name("이인하").build();
        List<CacheBorrowerDto> dtoList = Arrays.asList(user1, user2);

        // 2. Repository가 더미 데이터를 반환하도록 설정
        given(borrowerRepository.findAllWithFeeVerification()).willReturn(dtoList);

        // when
        cacheScheduledTask.refreshBorrowerCache();

        // then
        // 1. Repository 메서드가 호출되었는지 확인
        verify(borrowerRepository, times(1)).findAllWithFeeVerification();

        // 2. Cache.putAll()에 전달된 Map 데이터를 캡쳐해서 검증
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, CacheBorrowerDto>> captor = ArgumentCaptor.forClass(Map.class);

        verify(borrowerCache, times(1)).putAll(captor.capture());

        Map<String, CacheBorrowerDto> capturedMap = captor.getValue();

        // 3. 검증 수행
        assertThat(capturedMap).hasSize(2); // 데이터 개수 확인
        assertThat(capturedMap.get("12121212")).isEqualTo(user1); // Key-Value 매핑 확인
        assertThat(capturedMap.get("20202020")).isEqualTo(user2);
    }
}
package com.medibook.modules.schedule.cache;

import com.medibook.modules.schedule.cache.ScheduleCacheService;
import com.medibook.modules.schedule.dto.response.SlotResponse;
import com.medibook.modules.schedule.service.SlotGenerationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class ScheduleCacheServiceTest {

    @Autowired
    private ScheduleCacheService scheduleCacheService;

    @MockBean
    private SlotGenerationService slotGenerationService;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        // Clear the cache before each test to ensure fresh state
        if (cacheManager.getCache("doctorSlots") != null) {
            cacheManager.getCache("doctorSlots").clear();
        }
        Mockito.reset(slotGenerationService);
    }

    @Test
    void testGetSlots_ShouldCacheResult() {
        Long doctorId = 1L;
        LocalDate date = LocalDate.of(2026, 7, 20);
        List<SlotResponse> mockSlots = Collections.emptyList();

        // When
        when(slotGenerationService.generate(any())).thenReturn(mockSlots);

        // First call - should invoke the underlying service (cache miss)
        List<SlotResponse> slots1 = scheduleCacheService.getSlots(doctorId, date);
        
        // Second call - should return cached value (cache hit, underlying service not called again)
        List<SlotResponse> slots2 = scheduleCacheService.getSlots(doctorId, date);

        // Then
        assertThat(slots1).isNotNull();
        verify(slotGenerationService, times(1)).generate(any());
    }

    @Test
    void testEvictSlots_ShouldClearCachedResult() {
        Long doctorId = 1L;
        LocalDate date = LocalDate.of(2026, 7, 20);
        List<SlotResponse> mockSlots = Collections.emptyList();

        // When
        when(slotGenerationService.generate(any())).thenReturn(mockSlots);

        // First call - cache miss
        scheduleCacheService.getSlots(doctorId, date);

        // Evict slots
        scheduleCacheService.evictSlots(doctorId, date);

        // Second call - cache miss again (underlying service should be called again)
        scheduleCacheService.getSlots(doctorId, date);

        // Then
        verify(slotGenerationService, times(2)).generate(any());
    }
}

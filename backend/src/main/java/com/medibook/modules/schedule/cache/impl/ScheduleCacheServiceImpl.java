package com.medibook.modules.schedule.cache.impl;

import java.time.LocalDate;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import org.springframework.stereotype.Service;

import com.medibook.modules.schedule.cache.ScheduleCacheService;
import com.medibook.modules.schedule.dto.request.SlotGenerateRequest;
import com.medibook.modules.schedule.dto.response.SlotResponse;
import com.medibook.modules.schedule.service.SlotGenerationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScheduleCacheServiceImpl
        implements ScheduleCacheService {

    private final SlotGenerationService slotGenerationService;

    @Override
    @Cacheable(cacheNames = "doctorSlots", key = "#doctorId + ':' + #date")
    public List<SlotResponse> getSlots(Long doctorId, LocalDate date) {

        SlotGenerateRequest request = SlotGenerateRequest.builder().doctorId(doctorId).date(date).build();

        return slotGenerationService.generate(request);

    }

    @Override
    @CacheEvict(cacheNames = "doctorSlots", key = "#doctorId + ':' + #date")
    public void evictSlots(Long doctorId, LocalDate date) {

    }

    @Override
    @CacheEvict(cacheNames = "doctorSlots", allEntries = true)
    public void clearAllSlots() {

    }

}

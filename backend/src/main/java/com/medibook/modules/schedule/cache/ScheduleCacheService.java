package com.medibook.modules.schedule.cache;

import java.time.LocalDate;
import java.util.List;

import com.medibook.modules.schedule.dto.response.SlotResponse;

public interface ScheduleCacheService {

    List<SlotResponse> getSlots(Long doctorId, LocalDate date);

    void evictSlots(Long doctorId, LocalDate date);

    void clearAllSlots();

}

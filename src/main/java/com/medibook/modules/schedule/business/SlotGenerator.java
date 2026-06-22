package com.medibook.modules.schedule.business;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class SlotGenerator {

    public List<LocalDateTime> generate(LocalDateTime workStart, LocalDateTime workEnd, int slotDuration,
            int bufferDuration) {

        List<LocalDateTime> slots = new ArrayList<>();

        LocalDateTime current = workStart;

        while (current.plusMinutes(slotDuration).isBefore(workEnd.plusSeconds(1))) {

            slots.add(current);

            current = current.plusMinutes(slotDuration + bufferDuration);
        }

        return slots;
    }
}
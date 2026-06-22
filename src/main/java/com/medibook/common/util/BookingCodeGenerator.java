package com.medibook.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public class BookingCodeGenerator {

    public String generate() {

        return "MB-" + LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "-"
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

    }
}

package com.medibook.modules.appointment.repository;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface RevenueTrendProjection {

    LocalDate getDate();

    BigDecimal getTotal();

}

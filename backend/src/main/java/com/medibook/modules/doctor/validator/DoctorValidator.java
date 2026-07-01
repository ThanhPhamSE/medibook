package com.medibook.modules.doctor.validator;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.medibook.common.exception.BadRequestException;
import com.medibook.modules.doctor.dto.request.DoctorSearchRequest;

@Component
public class DoctorValidator {

    public void validateSearchRequest(DoctorSearchRequest request) {

        if (request.getMinExperience() != null && request.getMaxExperience() != null
                && request.getMinExperience() > request.getMaxExperience()) {

            throw new BadRequestException("Min experience cannot be greater than max experience");
        }

        if (request.getMinFee() != null && request.getMaxFee() != null
                && request.getMinFee().compareTo(request.getMaxFee()) > 0) {

            throw new BadRequestException("Min fee cannot be greater than max fee");
        }

        if (request.getMinExperience() != null
                && request.getMinExperience() < 0) {

            throw new BadRequestException(
                    "Min experience cannot be negative");
        }

        if (request.getMaxExperience() != null
                && request.getMaxExperience() < 0) {

            throw new BadRequestException(
                    "Max experience cannot be negative");
        }

        if (request.getMinFee() != null
                && request.getMinFee().compareTo(BigDecimal.ZERO) < 0) {

            throw new BadRequestException(
                    "Min fee cannot be negative");
        }

        if (request.getMaxFee() != null
                && request.getMaxFee().compareTo(BigDecimal.ZERO) < 0) {

            throw new BadRequestException(
                    "Max fee cannot be negative");
        }

        if (request.getMinRating() != null
                && request.getMinRating().compareTo(BigDecimal.ZERO) < 0) {

            throw new BadRequestException(
                    "Rating cannot be negative");
        }
    }
}

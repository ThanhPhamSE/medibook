package com.medibook.modules.specialty.facade.impl;

import org.springframework.stereotype.Service;

import com.medibook.modules.specialty.entity.Specialty;
import com.medibook.modules.specialty.facade.SpecialtyFacade;
import com.medibook.modules.specialty.service.SpecialtyService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SpecialtyFacadeImpl implements SpecialtyFacade {

    private final SpecialtyService specialtyService;

    @Override
    public Specialty getSpecialtyById(Long id) {
        return specialtyService.getEntityById(id);
    }

}

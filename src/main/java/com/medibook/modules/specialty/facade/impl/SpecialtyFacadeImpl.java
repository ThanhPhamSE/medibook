package com.medibook.modules.specialty.facade.impl;

import org.springframework.stereotype.Service;

import com.medibook.common.exception.ResourceNotFoundException;
import com.medibook.modules.specialty.entity.Specialty;
import com.medibook.modules.specialty.facade.SpecialtyFacade;
import com.medibook.modules.specialty.repository.SpecialtyRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SpecialtyFacadeImpl implements SpecialtyFacade {

    private final SpecialtyRepository specialtyRepository;

    @Override
    public Specialty getSpecialtyById(Long id) {

        return specialtyRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Specialty not found"));
    }

}

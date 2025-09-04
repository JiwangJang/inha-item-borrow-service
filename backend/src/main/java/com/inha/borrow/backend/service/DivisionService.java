package com.inha.borrow.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.inha.borrow.backend.model.dto.division.DivisionDto;
import com.inha.borrow.backend.model.entity.Division;
import com.inha.borrow.backend.repository.DivisionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DivisionService {
    private final DivisionRepository divisionRepository;

    public List<Division> findAllDivisions() {
        return divisionRepository.findAllDivisions();
    }

    public void saveDivision(DivisionDto divisionDto) {
        divisionRepository.saveDivision(divisionDto);
    }

    public void updateDivision(DivisionDto divisionDto) {
        divisionRepository.updateDivision(divisionDto);
    }

    public void deleteDivision(DivisionDto divisionDto) {
        divisionRepository.deleteDivision(divisionDto);
    }

}

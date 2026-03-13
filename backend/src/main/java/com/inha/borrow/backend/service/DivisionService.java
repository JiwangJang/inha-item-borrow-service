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
        Division division = transformDto(divisionDto);
        divisionRepository.saveDivision(division);
    }

    public void updateDivision(DivisionDto divisionDto) {
        Division division = transformDto(divisionDto);
        divisionRepository.updateDivision(division);
    }

    public void deleteDivision(String divisionCode) {
        divisionRepository.deleteDivision(divisionCode);
    }

    public Division transformDto(DivisionDto divisionDto) {
        Division division = Division.builder()
                .name(divisionDto.getName())
                .code(divisionDto.getCode())
                .build();
        return division;
    }

}

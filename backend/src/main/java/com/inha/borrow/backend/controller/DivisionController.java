package com.inha.borrow.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.inha.borrow.backend.model.dto.division.DivisionDto;
import com.inha.borrow.backend.model.dto.response.ApiResponse;
import com.inha.borrow.backend.model.entity.Division;
import com.inha.borrow.backend.service.DivisionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
@RequiredArgsConstructor
@RequestMapping("/divisions")
public class DivisionController {
    private final DivisionService divisionService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Division>>> findAllDivisions() {
        List<Division> divisions = divisionService.findAllDivisions();
        ApiResponse<List<Division>> response = new ApiResponse<>(true, divisions);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Division>> saveDivision(@RequestBody @Valid DivisionDto divisionDto) {
        Division savedDivision = divisionService.saveDivision(divisionDto);
        ApiResponse<Division> response = new ApiResponse<Division>(true, savedDivision);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping
    public ResponseEntity<ApiResponse<Void>> updateDivision(@RequestBody @Valid DivisionDto divisionDto) {
        divisionService.updateDivision(divisionDto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteDivision(@RequestBody @Valid DivisionDto divisionDto) {
        divisionService.deleteDivision(divisionDto);
        return ResponseEntity.noContent().build();
    }
}

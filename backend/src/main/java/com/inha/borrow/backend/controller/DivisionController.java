package com.inha.borrow.backend.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.inha.borrow.backend.model.dto.apiResponse.ApiResponse;
import com.inha.borrow.backend.model.dto.division.DivisionDto;
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
    public ResponseEntity<Void> saveDivision(@RequestBody @Valid DivisionDto divisionDto) {
        divisionService.saveDivision(divisionDto);
        return ResponseEntity.created(URI.create("/divisions")).build();
    }

    @PatchMapping
    public ResponseEntity<Void> updateDivision(@RequestBody @Valid DivisionDto divisionDto) {
        divisionService.updateDivision(divisionDto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteDivision(@RequestParam("division-code") String divisionCode) {
        divisionService.deleteDivision(divisionCode);
        return ResponseEntity.noContent().build();
    }
}

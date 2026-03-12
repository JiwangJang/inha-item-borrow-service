package com.inha.borrow.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.inha.borrow.backend.model.dto.division.DivisionDto;
import com.inha.borrow.backend.model.entity.Division;
import com.inha.borrow.backend.model.exception.InvalidValueException;
import com.inha.borrow.backend.model.exception.ResourceNotFoundException;

@SpringBootTest
@Transactional
public class DivisionServiceTest {
    @Autowired
    private DivisionService divisionService;

    @Test
    @DisplayName("Division 전체 목록 조회 테스트")
    void findAllDivisionsTest() {
        // given
        DivisionDto testDivision = new DivisionDto("TEST", "테스트");
        divisionService.saveDivision(testDivision);
        // when
        List<Division> result = divisionService.findAllDivisions();
        // then
        assertEquals(result.size(), 1);
    }

    @Test
    @DisplayName("Division 저장 테스트(성공)")
    void saveDivisionSuccessTest() {
        // given
        DivisionDto testDivision = new DivisionDto("TEST", "테스트");
        divisionService.saveDivision(testDivision);
        // when
        Division result = divisionService.findAllDivisions().get(0);
        // then
        assertEquals(result.getCode(), "TEST");
        assertEquals(result.getName(), "테스트");
    }

    @Test
    @DisplayName("Division 저장 테스트(실패-중복된 코드 값)")
    void saveDivisionFailForDuplicatedCodeTest() {
        // given
        DivisionDto testDivision = new DivisionDto("TEST", "테스트");
        divisionService.saveDivision(testDivision);
        // when
        // then
        assertThrows(InvalidValueException.class, () -> {
            divisionService.saveDivision(testDivision);
        });
    }

    @Test
    @DisplayName("Division 이름 수정 테스트(성공)")
    void updateDivisionSuccessTest() {
        // given
        DivisionDto originDivision = new DivisionDto("TEST", "테스트");
        DivisionDto revisedDivision = new DivisionDto("TEST", "변경된 부서명");
        divisionService.saveDivision(originDivision);
        // when
        divisionService.updateDivision(revisedDivision);
        Division result = divisionService.findAllDivisions().get(0);
        // then
        assertEquals(result.getName(), revisedDivision.getName());
    }

    @Test
    @DisplayName("Division 이름 수정 테스트(실패-부서 미존재)")
    void updateDivisionFailForNotExistTest() {
        // given
        DivisionDto divisionDto = new DivisionDto("TEST", "수정할거임");
        // when
        // then
        assertThrows(ResourceNotFoundException.class, () -> {
            divisionService.updateDivision(divisionDto);
        });
    }

    @Test
    @DisplayName("Division 이름 삭제 테스트(성공)")
    void deleteDivisionSuccessTest() {
        // given
        DivisionDto divisionDto = DivisionDto.builder()
                .code("TEST")
                .name("수정할거임")
                .build();
        divisionService.saveDivision(divisionDto);
        // when
        divisionService.deleteDivision(divisionDto.getCode());
        List<Division> result = divisionService.findAllDivisions();
        // then
        assertEquals(result.size(), 0);
    }

    @Test
    @DisplayName("Division 이름 삭제 테스트(실패-부서 미존재)")
    void deleteDivisionFailForNotExistTest() {
        // given
        DivisionDto divisionDto = DivisionDto.builder()
                .code("TEST")
                .name("수정할거임")
                .build();
        // when
        // then
        assertThrows(ResourceNotFoundException.class, () -> {
            divisionService.deleteDivision(divisionDto.getCode());
        });
    }
}

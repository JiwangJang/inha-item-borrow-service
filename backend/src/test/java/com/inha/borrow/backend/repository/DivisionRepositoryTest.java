package com.inha.borrow.backend.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.context.annotation.Import;

import com.inha.borrow.backend.model.dto.division.DivisionDto;
import com.inha.borrow.backend.model.entity.Division;
import com.inha.borrow.backend.model.exception.InvalidValueException;
import com.inha.borrow.backend.model.exception.ResourceNotFoundException;

@JdbcTest
@Import(DivisionRepository.class)
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class DivisionRepositoryTest {
    @Autowired
    private DivisionRepository divisionRepository;

    @Test
    @DisplayName("Division 전체 목록 조회 테스트")
    void findAllDivisionsTest() {
        // given
        Division testDivision = new Division("TEST", "테스트");
        divisionRepository.saveDivision(testDivision);
        // when
        List<Division> result = divisionRepository.findAllDivisions();
        // then
        assertEquals(result.size(), 1);
    }

    @Test
    @DisplayName("Division 저장 테스트(성공)")
    void saveDivisionSuccessTest() {
        // given
        Division testDivision = new Division("TEST", "테스트");
        divisionRepository.saveDivision(testDivision);
        // when
        Division result = divisionRepository.findAllDivisions().get(0);
        // then
        assertEquals(result.getCode(), "TEST");
        assertEquals(result.getName(), "테스트");
    }

    @Test
    @DisplayName("Division 저장 테스트(실패-중복된 코드 값)")
    void saveDivisionFailForDuplicatedCodeTest() {
        // given
        Division testDivision = new Division("TEST", "테스트");

        divisionRepository.saveDivision(testDivision);
        // when
        // then
        assertThrows(InvalidValueException.class, () -> {
            divisionRepository.saveDivision(testDivision);
        });
    }

    @Test
    @DisplayName("Division 이름 수정 테스트(성공)")
    void updateDivisionSuccessTest() {
        // given
        Division oringDivision = new Division("TEST", "테스트");
        Division revisedDivision = new Division("TEST", "변경된 부서명");
        divisionRepository.saveDivision(oringDivision);
        // when
        divisionRepository.updateDivision(revisedDivision);
        Division result = divisionRepository.findAllDivisions().get(0);
        // then
        assertEquals(result.getName(), revisedDivision.getName());
    }

    @Test
    @DisplayName("Division 이름 수정 테스트(실패-부서 미존재)")
    void updateDivisionFailForNotExistTest() {
        // given
        Division divisionDto = new Division("TEST", "수정할거임");
        // when
        // then
        assertThrows(ResourceNotFoundException.class, () -> {
            divisionRepository.updateDivision(divisionDto);
        });
    }

    @Test
    @DisplayName("Division 이름 삭제 테스트(성공)")
    void deleteDivisionSuccessTest() {
        // given
        Division division = Division.builder()
                .code("TEST")
                .name("수정할거임")
                .build();
        divisionRepository.saveDivision(division);
        // when
        divisionRepository.deleteDivision(division.getCode());
        List<Division> result = divisionRepository.findAllDivisions();
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
            divisionRepository.deleteDivision(divisionDto.getCode());
        });
    }
}

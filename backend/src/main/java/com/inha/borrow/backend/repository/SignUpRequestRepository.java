package com.inha.borrow.backend.repository;

import com.inha.borrow.backend.enums.ApiErrorCode;
import com.inha.borrow.backend.model.dto.signUpRequest.EvaluationRequestDto;
import com.inha.borrow.backend.model.entity.SignUpForm;
import com.inha.borrow.backend.model.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * signup_request table을 다루는 클래스
 */
@Repository
@RequiredArgsConstructor
public class SignUpRequestRepository {
    private final JdbcTemplate jdbcTemplate;

    private RowMapper<SignUpForm> rowMapper = (rs, rowNum) -> {
        String id = rs.getString("id");
        String password = rs.getString("password");
        String email = rs.getString("email");
        String name = rs.getString("name");
        String phoneNumber = rs.getString("phonenumber");
        String identityPhoto = rs.getString("identity_Photo");
        String StudentCouncilFeePhoto = rs.getString("Student_Council_Fee_Photo");
        String accountNumber = rs.getString("account_number");
        return new SignUpForm(id, password, email, name, phoneNumber, identityPhoto, StudentCouncilFeePhoto,
                accountNumber);
    };

    /**
     * signup_request를 저장하는 메서드
     * 
     * @param signupform
     * @return SignUpForm
     * @author 형민재
     */
    public SignUpForm save(SignUpForm signupform) {
        String sql = "INSERT INTO signup_request(id, password, email, name, phonenumber, " +
                "identity_photo, student_council_fee_photo, account_number) VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(
                sql,
                signupform.getId(),
                signupform.getPassword(),
                signupform.getEmail(),
                signupform.getName(),
                signupform.getPhoneNumber(),
                signupform.getIdentityPhoto(),
                signupform.getStudentCouncilFeePhoto(),
                signupform.getAccountNumber());
        return signupform;
    }

    /**
     * signup_request들을 반환하는 메서드
     *
     * @return SignUpForm
     * @author 형민재
     */

    public List<SignUpForm> findAll() {
        String sql = "SELECT * FROM signup_request";
        return jdbcTemplate.query(sql, rowMapper);
    }

    /**
     * signup_request를 id로 찾는 메서드
     * 
     * @param id
     * @return SignUpForm
     * @author 형민재
     */

    public SignUpForm findById(String id) {
        try {
            String sql = "SELECT * FROM signup_request WHERE id = ?";
            return jdbcTemplate.queryForObject(sql, rowMapper, id);
        } catch (EmptyResultDataAccessException e) {
            ApiErrorCode errorCode = ApiErrorCode.NOT_FOUND;
            throw new ResourceNotFoundException(errorCode.name(), errorCode.getMessage());
        }
    }

    public String findPasswordById(String id){
        try {
            String sql = "SELECT password FROM signup_request WHERE id = ?";
            return jdbcTemplate.queryForObject(sql,String.class,id);
        } catch (EmptyResultDataAccessException e) {
            ApiErrorCode errorCode = ApiErrorCode.NOT_FOUND;
            throw new ResourceNotFoundException(errorCode.name(), errorCode.getMessage());
        }
    }

    /**
     * signup_request의 state,rejectReason을 설정하는 메서드
     *
     * @param evaluationRequest
     * @param id
     * @return int
     * @author 형민재
     */

    public void patchEvaluation(EvaluationRequestDto evaluationRequest, String id) {
        String sql = "UPDATE signup_request SET state = ?, rejectReason = ? WHERE id = ?";
        jdbcTemplate.update(sql, evaluationRequest.getState(), evaluationRequest.getRejectReason(), id);
    }

    /**
     * signup_request를 수정하는 메서드
     *
     * @param signUpForm
     * @param id
     * @return signUpForm
     * @author 형민재
     */

    public void patchSignUpRequest(SignUpForm signUpForm, String id) {
        String sql = "UPDATE signup_request SET id = ? , password = ?, email = ?, name = ?, phonenumber = ?, " +
                "identity_Photo = ?,student_council_fee_photo = ? ,account_number = ? WHERE id =?";
        int affectedRow = jdbcTemplate.update(sql,
                signUpForm.getId(),
                signUpForm.getPassword(),
                signUpForm.getEmail(),
                signUpForm.getName(),
                signUpForm.getPhoneNumber(),
                signUpForm.getIdentityPhoto(),
                signUpForm.getStudentCouncilFeePhoto(),
                signUpForm.getAccountNumber(),
                id);
        if (affectedRow == 0) {
            ApiErrorCode errorCode = ApiErrorCode.NOT_FOUND;
            throw new ResourceNotFoundException(errorCode.name(), errorCode.getMessage());
        }
    }

    /**
     * signup_request를 삭제하는 메서드
     *
     * @param id
     * @return int
     * @author 형민재
     */

    public void deleteSignUpRequest(String id, String password) {
        String sql = "DELETE FROM signup_request WHERE id = ?";
        int affectedRow = jdbcTemplate.update(sql, id);
        if (affectedRow == 0) {
            ApiErrorCode errorCode = ApiErrorCode.NOT_FOUND;
            throw new ResourceNotFoundException(errorCode.name(), errorCode.getMessage());
        }
    }
}

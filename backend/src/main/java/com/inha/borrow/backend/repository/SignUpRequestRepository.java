package com.inha.borrow.backend.repository;

import com.inha.borrow.backend.enums.ApiErrorCode;
import com.inha.borrow.backend.model.dto.EvaluationRequest;
import com.inha.borrow.backend.model.dto.SignUpForm;
import com.inha.borrow.backend.model.exception.InvalidValueException;
import com.inha.borrow.backend.model.exception.ResourceNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import java.util.List;

/**
 * signup_request table을 다루는 클래스
 */
@Repository
@AllArgsConstructor
public class SignUpRequestRepository {
    private final JdbcTemplate jdbcTemplate;

    /**
     * signup_request를 저장하는 메서드
     * 
     * @param signupform
     * @return SignUpForm
     * @author 형민재
     */
    public SignUpForm save(SignUpForm signupform) {
        if (!StringUtils.hasText(signupform.getId()) ||
                !StringUtils.hasText(signupform.getPassword()) ||
                !StringUtils.hasText(signupform.getEmail()) ||
                !StringUtils.hasText(signupform.getName()) ||
                !StringUtils.hasText(signupform.getPhoneNumber()) ||
                !StringUtils.hasText(signupform.getIdentityPhoto()) ||
                !StringUtils.hasText(signupform.getStudentCouncilFeePhoto()) ||
                !StringUtils.hasText(signupform.getAccountNumber())) {
            ApiErrorCode errorCode = ApiErrorCode.NOT_ALLOWED_VALUE;
            throw new InvalidValueException(errorCode.name(), errorCode.getMessage());
        }
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
        return jdbcTemplate.query(sql, rowMapper());
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
            return jdbcTemplate.queryForObject(sql, rowMapper(), id);
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

    public void patchEvaluation(EvaluationRequest evaluationRequest, String id) {
        if (!StringUtils.hasText(evaluationRequest.getState()) ||
                !StringUtils.hasText(evaluationRequest.getRejectReason())) {
            throw new DataIntegrityViolationException("공백값은 넣을 수 없습니다");
        }
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
        if (!StringUtils.hasText(signUpForm.getId()) ||
                !StringUtils.hasText(signUpForm.getPassword()) ||
                !StringUtils.hasText(signUpForm.getEmail()) ||
                !StringUtils.hasText(signUpForm.getName()) ||
                !StringUtils.hasText(signUpForm.getPhoneNumber()) ||
                !StringUtils.hasText(signUpForm.getIdentityPhoto()) ||
                !StringUtils.hasText(signUpForm.getStudentCouncilFeePhoto()) ||
                !StringUtils.hasText(signUpForm.getAccountNumber())) {
            throw new DataIntegrityViolationException("공백값은 넣을 수 없습니다");
        }
        String sql = "UPDATE signup_request SET id = ? , password = ?, email = ?, name = ?, phonenumber = ?, " +
                "identity_Photo = ?,student_council_fee_photo = ? ,account_number = ? WHERE id =?";
        jdbcTemplate.update(sql,
                signUpForm.getId(),
                signUpForm.getPassword(),
                signUpForm.getEmail(),
                signUpForm.getName(),
                signUpForm.getPhoneNumber(),
                signUpForm.getIdentityPhoto(),
                signUpForm.getStudentCouncilFeePhoto(),
                signUpForm.getAccountNumber(),
                id);
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
        jdbcTemplate.update(sql, id);
    }

    public RowMapper<SignUpForm> rowMapper() {
        return (rs, rowNum) -> {
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
    }

}

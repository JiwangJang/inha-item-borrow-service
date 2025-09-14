package com.inha.borrow.backend.repository;

import com.inha.borrow.backend.enums.ApiErrorCode;
import com.inha.borrow.backend.enums.SignUpRequestState;
import com.inha.borrow.backend.model.dto.signUpRequest.EvaluationRequestDto;
import com.inha.borrow.backend.model.entity.SignUpForm;
import com.inha.borrow.backend.model.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

/**
 * signup_request table을 다루는 클래스
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class SignUpRequestRepository {
    private final JdbcTemplate jdbcTemplate;

    private RowMapper<SignUpForm> rowMapper = (rs, rowNum) -> {
        String id = rs.getString("id");
        String password = rs.getString("password");
        String email = rs.getString("email");
        String name = rs.getString("name");
        String phonenumber = rs.getString("phonenumber");
        String identityPhoto = rs.getString("identity_photo");
        String studentCouncilFeePhoto = rs.getString("student_council_fee_photo");
        String accountNumber = rs.getString("account_number");
        Timestamp created_at = rs.getTimestamp("created_at");
        SignUpRequestState signUpRequestState = SignUpRequestState.valueOf(rs.getString("state"));
        String rejectReason = rs.getString("reject_reason");

        return SignUpForm.builder()
                .id(id)
                .password(password)
                .email(email)
                .name(name)
                .phonenumber(phonenumber)
                .identityPhoto(identityPhoto)
                .studentCouncilFeePhoto(studentCouncilFeePhoto)
                .accountNumber(accountNumber)
                .created_at(created_at)
                .state(signUpRequestState)
                .rejectReason(rejectReason)
                .build();
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
        log.info(signupform.getEmail());
        jdbcTemplate.update(
                sql,
                signupform.getId(),
                signupform.getPassword(),
                signupform.getEmail(),
                signupform.getName(),
                signupform.getPhonenumber(),
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
            ApiErrorCode errorCode = ApiErrorCode.NOT_FOUND_SIGN_UP_REQUEST;
            throw new ResourceNotFoundException(errorCode.name(), errorCode.getMessage());
        }
    }

    /**
     * password검증을 하는 메서드
     *
     * @param id
     * @return SignUpForm
     * @author 형민재
     */

    public String findPasswordById(String id) {
        try {
            String sql = "SELECT password FROM signup_request WHERE id = ?";
            return jdbcTemplate.queryForObject(sql, String.class, id);
        } catch (EmptyResultDataAccessException e) {
            ApiErrorCode errorCode = ApiErrorCode.NOT_FOUND_SIGN_UP_REQUEST;
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
        String sql = "UPDATE signup_request SET state = ?, reject_reason = ? WHERE id = ?";
        int result = jdbcTemplate.update(sql, evaluationRequest.getState().name(), evaluationRequest.getRejectReason(), id);
        if(result==0){
            ApiErrorCode errorCode = ApiErrorCode.NOT_FOUND_SIGN_UP_REQUEST;
            throw new ResourceNotFoundException(errorCode.name(), errorCode.getMessage());
        }
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
                signUpForm.getPhonenumber(),
                signUpForm.getIdentityPhoto(),
                signUpForm.getStudentCouncilFeePhoto(),
                signUpForm.getAccountNumber(),
                id);
        if (affectedRow == 0) {
            ApiErrorCode errorCode = ApiErrorCode.NOT_FOUND_SIGN_UP_REQUEST;
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
            ApiErrorCode errorCode = ApiErrorCode.NOT_FOUND_SIGN_UP_REQUEST;
            throw new ResourceNotFoundException(errorCode.name(), errorCode.getMessage());
        }
    }

    /**
     * test 코드에 사용하기 위한 메서드
     */
    public void deleteALL() {
        String sql = "DELETE FROM signup_request";
        jdbcTemplate.update(sql);
    }

    /**
     * test 코드에 사용하기 위한 메서드
     */
    public EvaluationRequestDto findStateAndReject(String id) {
        String sql = "SELECT state,reject_reason FROM signup_request WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            SignUpRequestState state = SignUpRequestState.valueOf(rs.getString("state"));
            String rejectReason = rs.getString("reject_reason");
            return new EvaluationRequestDto(state, rejectReason);
        }, id);
    }
}

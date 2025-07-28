package com.inha.borrow.backend.repository;

import com.inha.borrow.backend.model.dto.EvaluationRequest;
import com.inha.borrow.backend.model.dto.SignUpForm;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * signup_request table을 다루는 클래스
 */
@Repository
@AllArgsConstructor
public class SignUpRequestRepository {
    private JdbcTemplate jdbcTemplate;

/**
 * signup_request를 저장하는 메서드
 *
 * @param "id signup_request 아이디
 * @return int
 * @throws org.springframework.dao.DataAccessException
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
        return findById(signupform.getId());
    }
    /**
     * signup_request를 갖는 메서드
     *
     * @param "id signup_request 아이디
     * @return List<SignUpForm>
     * @throws org.springframework.dao.DataAccessException
     * @author 형민재
     */

    public List<SignUpForm> findAll() {
        String sql = "SELECT * FROM signup_request";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            SignUpForm signUpForm = new SignUpForm();
            signUpForm.setId(rs.getString("id"));
            signUpForm.setPassword(rs.getString("password"));
            signUpForm.setEmail(rs.getString("email"));
            signUpForm.setName(rs.getString("name"));
            signUpForm.setPhoneNumber(rs.getString("phonenumber"));
            signUpForm.setIdentityPhoto(rs.getString("identity_photo"));
            signUpForm.setStudentCouncilFeePhoto(rs.getString("student_council_fee_photo"));
            signUpForm.setAccountNumber(rs.getString("account_number"));
            return signUpForm;
        });
    }
    /**
     * signup_request를 id로 갖는 메서드
     *
     * @param id signup_request 아이디
     * @return SignUpForm
     * @throws org.springframework.dao.DataAccessException
     * @author 형민재
     */

    public SignUpForm findById(String id) {
        String sql = "SELECT * FROM signup_request WHERE ID = ?";
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            SignUpForm signUpForm = new SignUpForm();
            signUpForm.setId(rs.getString("id"));
            signUpForm.setPassword(rs.getString("password"));
            signUpForm.setEmail(rs.getString("email"));
            signUpForm.setName(rs.getString("name"));
            signUpForm.setPhoneNumber(rs.getString("phonenumber"));
            signUpForm.setIdentityPhoto(rs.getString("identity_photo"));
            signUpForm.setStudentCouncilFeePhoto(rs.getString("student_council_fee_photo"));
            signUpForm.setAccountNumber(rs.getString("account_number"));
            return signUpForm;
        }, id);
    }
    /**
     * signup_request의 state,rejectReason을 설정하는 메서드
     *
     * @param id signup_request 아이디
     * @return int
     * @throws org.springframework.dao.DataAccessException
     * @author 형민재
     */

    public SignUpForm patchEvaluation(EvaluationRequest evaluationRequest, String id) {
        String sql = "UPDATE signup_request SET state = ?, rejectReason = ? WHERE id = ?";
        jdbcTemplate.update(sql, evaluationRequest.getState(),evaluationRequest.getRejectReason(), id);
        return findById(id);
    }
    /**
     * signup_request를 수정하는 메서드
     *
     * @param id signup_request 아이디
     * @return int
     * @throws org.springframework.dao.DataAccessException
     * @author 형민재
     */

    public SignUpForm patchSignUpRequest(SignUpForm signUpForm, String id) {
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
        return findById(id);
    }
    /**
     * signup_request를 삭제하는 메서드
     *
     * @param id signup_request 아이디
     * @return int
     * @throws org.springframework.dao.DataAccessException
     * @author 형민재
     */

    public SignUpForm deleteSignUpRequest(String id) {
        String sql = "DELETE FROM signup_request WHERE id = ?";
        jdbcTemplate.update(sql, id);
        return findById(id);
    }

}

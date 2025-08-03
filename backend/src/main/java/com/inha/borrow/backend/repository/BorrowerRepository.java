package com.inha.borrow.backend.repository;
import java.util.List;
import com.inha.borrow.backend.model.dto.BorrowerDto;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.access.method.P;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
import com.inha.borrow.backend.model.user.Borrower;
import lombok.AllArgsConstructor;
import org.springframework.util.StringUtils;

/**
 * borrower table을 다루는 클래스
 */
@Repository
@AllArgsConstructor
public class BorrowerRepository {

    private JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    /**
     * 아이디로 대여자 정보를 가져오는 메서드
     * 
     * @param id 대여자 아이디
     * @return Borrower
     * @throws DataAccessException
     * @author 장지왕
     */
    public Borrower findById(String id) {
        String sql = "SELECT * FROM borrower WHERE id=?;";
        return jdbcTemplate.queryForObject(sql, borrowerRowMapper(), id);
        }

    /**
     * 대여자 리스트를 반환하는 메서드
     * @return List<Borrower>
     * @author 형민재
     */
    public List<Borrower> findAll(){
        String sql = "SELECT * FROM borrower";
        return jdbcTemplate.query(sql, borrowerRowMapper());
    }

    public void save(BorrowerDto borrower){
        if(!StringUtils.hasText(borrower.getId())||
            !StringUtils.hasText(borrower.getPassword())||
            !StringUtils.hasText(borrower.getEmail())||
            !StringUtils.hasText(borrower.getName())||
            !StringUtils.hasText(borrower.getPhonenumber())||
            !StringUtils.hasText(borrower.getStudentNumber())||
            !StringUtils.hasText(borrower.getAccountNumber())){
            throw new DataIntegrityViolationException("공백값은 넣을 수 없습니다");
        }
        String encodedPassword = passwordEncoder.encode(borrower.getPassword());
        String sql = "INSERT INTO borrower(id, password, email, name, phonenumber, " +
                "student_number, account_number) VALUES(?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                borrower.getId(),
                encodedPassword,
                borrower.getEmail(),
                borrower.getName(),
                borrower.getPhonenumber(),
                borrower.getStudentNumber(),
                borrower.getAccountNumber());
    }
    /**
     * 아이디로 대여자 정보 수정 메서드
     * @param id 대여자 아이디
     * @return int
     * @throws DataAccessException
     * @author 형민재
     */
    public void patchPassword(String password, String id){
        if(!StringUtils.hasText(password)){
            throw new DataIntegrityViolationException("공백값은 널을 수 없습니다.");
        }
        String encodedPassword = passwordEncoder.encode(password);
        String sql ="UPDATE borrower SET password = ? WHERE id = ?";
        jdbcTemplate.update(sql,encodedPassword, id);
    }

    public void patchEmail(String email, String id){
        if(!StringUtils.hasText(email)){
            throw new DataIntegrityViolationException("공백값은 널을 수 없습니다.");
        }
        String sql = " UPDATE borrower SET email = ? WHERE id = ?";
        jdbcTemplate.update(sql, email, id);
    }
    public void patchName(String name, String id){
        if(!StringUtils.hasText(name)){
            throw new DataIntegrityViolationException("공백값은 널을 수 없습니다.");
        }
        String sql = " UPDATE borrower SET name = ? WHERE id = ?";
        jdbcTemplate.update(sql, name, id);
    }
    public void patchPhoneNumber(String phoneNumber, String id) {
        if(!StringUtils.hasText(phoneNumber)){
            throw new DataIntegrityViolationException("공백값은 널을 수 없습니다.");
        }
        String sql = " UPDATE borrower SET phonenumber = ? WHERE id = ?";
        jdbcTemplate.update(sql, phoneNumber, id);
    }
    public void patchStudentNumber(String studentNumber, String id){
        if(!StringUtils.hasText(studentNumber)){
            throw new DataIntegrityViolationException("공백값은 널을 수 없습니다.");
        }
        String sql = " UPDATE borrower SET student_number = ? WHERE id = ?";
        jdbcTemplate.update(sql, studentNumber, id);
    }
    public void patchAccountNumber(String accountNumber, String id){
        if(!StringUtils.hasText(accountNumber)){
            throw new DataIntegrityViolationException("공백값은 널을 수 없습니다.");
        }
        String sql = " UPDATE borrower SET account_number = ? WHERE id = ?";
        jdbcTemplate.update(sql, accountNumber, id);
    }
    public void patchWithDrawal(Boolean withDrawal, String id) {
        if(withDrawal==null){
            throw new DataIntegrityViolationException("공백값은 널을 수 없습니다.");
        }
        String sql = " UPDATE borrower SET withdrawal = ? WHERE id = ?";
        jdbcTemplate.update(sql, withDrawal, id);
    }
    public void patchBan(Boolean ban, String id){
        if(ban==null){
            throw new DataIntegrityViolationException("공백값은 널을 수 없습니다.");
        }
        String sql = " UPDATE borrower SET ban = ? WHERE id = ?";
        jdbcTemplate.update(sql, ban, id);
    }
    /**
     * 토큰부여 메소드
     *
     * @param id
     * @author 형민재
     */

    public void patchRefreshToken(String token, String id){
        String sql = "UPDATE borrower SET refresh_token = ?  WHERE ID =?";
        jdbcTemplate.update(sql,token,id);
    }
    /**
     * test 코드에 사용하기 위한 메서드
     */
    public void delete(){
        String sql = "DELETE FROM borrower";
        jdbcTemplate.update(sql);
    }


    private RowMapper<Borrower> borrowerRowMapper() {
        return (rs, rowNum) -> {
            String id = rs.getString("id");
            String password = rs.getString("password");
            String email = rs.getString("email");
            String name = rs.getString("name");
            String phonenumber = rs.getString("phonenumber");
            String studentNumber = rs.getString("student_number");
            String accountNumber = rs.getString("account_number");
            boolean ban = rs.getBoolean("ban");

            List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("BORROWER"));

            return new Borrower(id, password, email, name, phonenumber, authorities, ban, studentNumber, accountNumber);
        };

    }
}

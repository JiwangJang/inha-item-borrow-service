package com.inha.borrow.backend.service;

import com.inha.borrow.backend.model.user.Borrower;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.inha.borrow.backend.repository.BorrowerRepository;
import lombok.AllArgsConstructor;
import java.util.List;

/**
 * 대여자와 관련된 작업을 하는 클래스
 * 
 */
@Service
@AllArgsConstructor
public class BorrowerService implements UserDetailsService {
    private BorrowerRepository borrowerRepository;

    /**
     * 대여자 계정 정보를 가져오는 메서드
     * 대여자 인증과정에서 사용됨
     *
     * @param id 대여자 아이디
     * @return UserDetails 대여자 정보
     * @author 장지왕
     */
    @Override
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
            return borrowerRepository.findById(id);
    }

    /**
     *대여자를 id로 찾는 메서드
     *
     * @param id
     * @return 대여자 정보
     * @author 형민재
     */

    public Borrower findById(String id ){
        return borrowerRepository.findById(id);
    }

    /**
     *대여자들의 정보를 반환하는 메서드
     *
     * @return 대여자 정보
     * @author 형민재
     */

    public List<Borrower> findAll(){
        return borrowerRepository.findAll();
    }

    /**
     *대여자의 정보를 수정하는 메서드
     *
     * @param email
     * @param id
     * @author 형민재
     */
    public void patchEmail(String email, String id) {
        borrowerRepository.patchEmail(email, id);
    }

    /**
     *대여자의 정보를 수정하는 메서드
     *
     * @param name
     * @param id
     * @author 형민재
     */
    public void patchName(String name, String id) {
        borrowerRepository.patchName(name, id);
    }

    /**
     *대여자의 정보를 수정하는 메서드
     *
     * @param phoneNumber
     * @param id
     * @author 형민재
     */
    public void patchPhoneNumber(String phoneNumber, String id) {
        borrowerRepository.patchPhoneNumber(phoneNumber, id);
    }

    /**
     *대여자의 정보를 수정하는 메서드
     *
     * @param studentNumber
     * @param id
     * @author 형민재
     */
    public void patchStudentNumber(String studentNumber, String id) {
       borrowerRepository.patchStudentNumber(studentNumber, id);
    }

    /**
     *대여자의 정보를 수정하는 메서드
     *
     * @param accountNumber
     * @param id
     * @author 형민재
     */
    public void patchAccountNumber(String accountNumber, String id) {
         borrowerRepository.patchAccountNumber(accountNumber, id);
    }

    /**
     *대여자의 정보를 수정하는 메서드
     *
     * @param withDrawal
     * @param id
     * @author 형민재
     */
    public void patchWithDrawal(int withDrawal, String id) {
         borrowerRepository.pathcWithDrawal(withDrawal, id);
    }

    /**
     *대여자의 정보를 수정하는 메서드
     *
     * @param ban
     * @param id
     * @author 형민재
     */
    public void patchBan(int ban, String id) {
        borrowerRepository.patchBan(ban, id);
    }

    /**
     *대여자의 정보를 수정하는 메서드
     *
     * @param password
     * @param id
     * @author 형민재
     */
    public void patchPassword(String password, String id) {
        borrowerRepository.patchPassword(password, id);
    }
}
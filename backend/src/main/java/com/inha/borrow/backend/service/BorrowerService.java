package com.inha.borrow.backend.service;

import com.inha.borrow.backend.model.user.Borrower;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
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
        try {
            return borrowerRepository.findById(id);
        } catch (Exception e) {
            if (e.getClass() == IncorrectResultSizeDataAccessException.class) {
                throw new UsernameNotFoundException("등록되지 않은 사용자입니다.");
            } else {
                throw e;
            }
        }
    }

    public class BorrowerNotFoundException extends RuntimeException {
        public BorrowerNotFoundException(String message) {
            super(message);
        }
    }

    public Borrower findById(String id ){
        Borrower find = borrowerRepository.findById(id);
        if(find==null){
            throw new BorrowerNotFoundException("해당 ID를 갖는 대여자를 찾을 수 없습니다");
        }
        return  find;
    }

    public List<Borrower> findAll(){
        return borrowerRepository.findAll();
    }

    public Borrower patchEmail(String email, String id) {
        Borrower patch = borrowerRepository.patchEmail(email, id);
        if (patch == null) {
            throw new BorrowerNotFoundException("해당 ID를 갖는 대여자를 찾을 수 없습니다");
        }
        return patch;
    }

    public Borrower patchName(String name, String id) {
        Borrower patch = borrowerRepository.patchName(name, id);
        if (patch == null) {
            throw new BorrowerNotFoundException("해당 ID를 갖는 대여자를 찾을 수 없습니다");
        }
        return patch;

    }
    public Borrower patchPhoneNumber(String phoneNumber, String id) {
        Borrower patch = borrowerRepository.patchPhoneNumber(phoneNumber, id);
        if (patch == null) {
            throw new BorrowerNotFoundException("해당 ID를 갖는 대여자를 찾을 수 없습니다");
        }
        return patch;

    }
    public Borrower patchStudentNumber(String studentNumber, String id) {
        Borrower patch = borrowerRepository.patchStudentNumber(studentNumber, id);
        if (patch == null) {
            throw new BorrowerNotFoundException("해당 ID를 갖는 대여자를 찾을 수 없습니다");
        }
        return patch;

    }
    public Borrower patchAccountNumber(String accountNumber, String id) {
        Borrower patch = borrowerRepository.patchAccountNumber(accountNumber, id);
        if (patch == null) {
            throw new BorrowerNotFoundException("해당 ID를 갖는 대여자를 찾을 수 없습니다");
        }
        return patch;

    }
    public Borrower patchWithDrawal(int withDrawal, String id) {
        Borrower patch = borrowerRepository.pathcWithDrawal(withDrawal, id);
        if (patch == null) {
            throw new BorrowerNotFoundException("해당 ID를 갖는 대여자를 찾을 수 없습니다");
        }
        return patch;

    }
    public Borrower patchBan(int ban, String id) {
        Borrower patch = borrowerRepository.patchBan(ban, id);
        if (patch == null) {
            throw new BorrowerNotFoundException("해당 ID를 갖는 대여자를 찾을 수 없습니다");
        }
        return patch;

    }
    public Borrower patchPassword(String password, String id) {
        Borrower patch = borrowerRepository.patchPassword(password, id);
        if (patch == null) {
            throw new BorrowerNotFoundException("해당 ID를 갖는 대여자를 찾을 수 없습니다");
        }
        return patch;

    }

}
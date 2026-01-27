package com.inha.borrow.backend.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.inha.borrow.backend.config.CacheConfig;
import com.inha.borrow.backend.model.dto.user.borrower.CacheBorrowerDto;
import com.inha.borrow.backend.model.dto.user.borrower.PatchPhonenumberDto;

import com.inha.borrow.backend.model.entity.user.Borrower;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.inha.borrow.backend.repository.BorrowerRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 대여자와 관련된 작업을 하는 클래스
 * 
 */
@Service
@RequiredArgsConstructor
public class BorrowerService implements UserDetailsService{
    private final BorrowerRepository borrowerRepository;
    private final PasswordEncoder passwordEncoder;

    // i-class 연동부분을 여기서 구현하는게 좋을듯 함
    // 따로 하는게 좋아보이는데 반환타입이 걸리기도 하고 인자도 하나밖에 못받음
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return null;
    }


    /**
     * 대여자를 id로 찾는 메서드
     *
     * @param id
     * @return 대여자 정보
     * @author 형민재
     */

    public Borrower findById(String id) {
        return borrowerRepository.findById(id);
    }

    /**
     * 대여자들의 정보를 반환하는 메서드
     *
     * @return 대여자 정보
     * @author 형민재
     */

    public List<Borrower> findAll() {
        return borrowerRepository.findAll();
    }

    /**
     * 대여자의 정보를 수정하는 메서드
     *
     * @param name
     * @param id
     * @author 형민재
     */
    public void patchName(String name, String id) {
        borrowerRepository.patchName(name, id);
    }

    /**
     * 대여자의 정보를 수정하는 메서드
     *
     * @param borrowerId
     * @param dto
     * @author 형민재
     */
    public void patchPhoneNumber(String borrowerId, PatchPhonenumberDto dto) {
        String newPhonenumber = dto.getNewPhonenumber();
        borrowerRepository.patchPhoneNumber(newPhonenumber, borrowerId);
    }

    /**
     * 대여자의 정보를 수정하는 메서드
     *
     * @param accountNumber
     * @param id
     * @author 형민재
     */
    public void patchAccountNumber(String accountNumber, String id) {
        borrowerRepository.patchAccountNumber(accountNumber, id);
    }

    /**
     * 대여자의 정보를 수정하는 메서드
     *
     * @param ban
     * @param id
     * @author 형민재
     */
    public void patchBan(boolean ban, String id) {
        borrowerRepository.patchBan(ban, id);
    }
}

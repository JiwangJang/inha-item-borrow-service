package com.inha.borrow.backend.model.user;

import java.util.List;

import org.springframework.security.core.GrantedAuthority;

/**
 * 대여자(Borrower) 유저를 나타내는 클래스
 * <p>
 * User 추상클래스를 상속함. authroity는 BORROWER
 * 
 * @author 장지왕
 */
public class Borrower extends User {
    /**
     * 대여 정지 사용자인지 표시
     */
    boolean ban;
    /**
     * 학번
     */
    String studentNumber;
    /**
     * 보증금 반환계좌
     */
    String accountNumber;

    /**
     * 대여자 객체를 생성
     * <p>
     * authroity는 BORROWER
     * 
     * @param id            대여자 ID
     * @param password      대여자 비밀번호
     * @param email         대여자 이메일
     * @param name          대여자 이름
     * @param phonenumber   대여자 전화번호
     * @param authorities   대여자 권한(BORROWER 고정)
     * @param ban           대여 정지 사용자 여부
     * @param studentNumber 대여자 학번
     * @param accountNumber 대여자 보증금 반환 계좌
     */
    public Borrower(String id, String password, String email, String name, String phonenumber,
            List<GrantedAuthority> authorities, boolean ban, String studentNumber, String accountNumber) {
        super(id, password, email, name, phonenumber, authorities);
        this.ban = ban;
        this.studentNumber = studentNumber;
        this.accountNumber = accountNumber;
    }
}

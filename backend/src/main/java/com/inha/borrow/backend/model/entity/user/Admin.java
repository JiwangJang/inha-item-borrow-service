package com.inha.borrow.backend.model.entity.user;

import java.util.List;

import org.springframework.security.core.GrantedAuthority;

import com.inha.borrow.backend.enums.Role;

import lombok.Getter;
import lombok.Setter;

/**
 * 관리자(Admin) 유저를 나타내는 클래스
 * <p>
 * 일반 사용자(User)를 상속하며, 역할(Role)에 따라 직책(position)을 부여.
 * 
 * @author 장지왕
 */
@Getter
@Setter
public class Admin extends User {
    /**
     * 관리자 직책. 예: "학생회장", "부학생회장", "사업국장" 등
     * <ul>
     * <li>PRESIDENT : 학생회장
     * <li>VICE_PRESIDENT : 부학생회장
     * <li>DIVISION_HEAD : 사업국장
     * <li>DIVISION_MEMBER : 사업국원
     * </ul>
     */
    String position;

    /**
     * 관리자 객체를 생성
     * 
     * @param id          관리자 ID
     * @param password    관리자 비밀번호
     * @param email       관리자 이메일
     * @param name        관리자 이름
     * @param phonenumber 관리자 전화번호
     * @param authorities 관리자 권한 목록(무조건 한개 담겨있음)
     */
    public Admin(String id, String password, String email, String name, String phonenumber,
            List<GrantedAuthority> authorities, String refreshToken) {
        super(id, password, email, name, phonenumber, refreshToken, authorities);

        Role role = Role.valueOf(authorities.get(0).getAuthority());

        switch (role) {
            case PRESIDENT:
                this.position = "학생회장";
                break;
            case VICE_PRESIDENT:
                this.position = "부학생회장";
                break;
            case DIVISION_HEAD:
                this.position = "사업국장";
                break;
            default:
                this.position = "사업국원";
                break;
        }
    }
}
package com.inha.borrow.backend.enums;

public enum Role {
    PRESIDENT(4),
    VICE_PRESIDENT(3),
    DIVISION_HEAD(2),
    DIVISION_MEMBER(1),
    BORROWER(0);

    private final int level;

    private Role(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }
}

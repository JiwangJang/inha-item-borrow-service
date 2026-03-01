package com.inha.borrow.backend.util;

public class ServiceUtils {
    static final int TIME = 600000;

    static public long getTtl() {
        return System.currentTimeMillis() + TIME;
    }
}

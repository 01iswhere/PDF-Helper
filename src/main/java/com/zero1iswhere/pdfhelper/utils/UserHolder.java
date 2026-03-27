package com.zero1iswhere.pdfhelper.utils;

public class UserHolder {
    private static final ThreadLocal<String> tl = new ThreadLocal<>();

    public static void saveUser(String userName) {
        tl.set(userName);
    }

    public static String getUser() {
        return tl.get();
    }

    public static void removeUser() {
        tl.remove();
    }
}

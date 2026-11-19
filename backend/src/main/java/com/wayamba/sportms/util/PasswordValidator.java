package com.wayamba.sportms.util;

import java.util.regex.Pattern;

public class PasswordValidator {
    // Minimum 8 characters, at least one lowercase, one uppercase, one digit, one special character
    private static final Pattern PATTERN =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$");

    public static boolean isValid(String password) {
        if (password == null) {
            return false;
        }
        return PATTERN.matcher(password).matches();
    }
}


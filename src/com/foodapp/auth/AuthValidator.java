package com.foodapp.auth;

public class AuthValidator {
    public static boolean isValidEmail(String email) {
        return email != null && email.matches("^[\\w.+-]+@[\\w-]+\\.[\\w.]+$");
    }
    public static boolean isValidPhone(String phone) {
        return phone != null && phone.matches("\\d{10}");
    }
    public static boolean isValidPassword(String pw) {
        return pw != null && pw.length() >= 6;
    }
    public static boolean isValidEmailOrPhone(String s) {
        return isValidEmail(s) || isValidPhone(s);
    }
    public static boolean isNotEmpty(String s) {
        return s != null && !s.trim().isEmpty();
    }
}

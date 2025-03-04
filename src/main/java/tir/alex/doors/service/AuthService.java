package tir.alex.doors.service;

import org.springframework.http.ResponseEntity;

public interface AuthService {
    ResponseEntity<String> sendCode(String phone);
    ResponseEntity<String> verifyCode(String phone, String code);
}

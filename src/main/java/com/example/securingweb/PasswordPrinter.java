package com.example.securingweb;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordPrinter {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {
        // Sinh hash cho mật khẩu "123456"
        String hashed = passwordEncoder.encode("123456");
        System.out.println("BCrypt hash: " + hashed);
    }
}
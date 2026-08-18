package com.mall;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class PasswordHashGeneratorTest {

    @Test
    void generatePassword() {
        BCryptPasswordEncoder encoder =
                new BCryptPasswordEncoder();

        String encodedPassword =
                encoder.encode("123456");

        System.out.println(encodedPassword);
    }
}
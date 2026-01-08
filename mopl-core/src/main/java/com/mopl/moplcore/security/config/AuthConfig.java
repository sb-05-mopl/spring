package com.mopl.moplcore.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AuthConfig {

    @Bean
    @Profile({"local","dev"})
    public PasswordEncoder passwordEncoder(){
        return new PlainTextPasswordEncoder();
    }

    @Bean
    @Profile("prod")
    public PasswordEncoder devPasswordEncoder(){
        return new BCryptPasswordEncoder();
    }

    public static class PlainTextPasswordEncoder implements PasswordEncoder {

        @Override
        public String encode(CharSequence rawPassword) {
            return rawPassword == null ? null : rawPassword.toString();
        }

        @Override
        public boolean matches(CharSequence rawPassword, String encodedPassword) {
            if (rawPassword == null || encodedPassword == null) {
                return false;
            }
            return rawPassword.toString().equals(encodedPassword);
        }
    }
}

package com.mopl.moplwebsocketsse.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.mopl.moplwebsocketsse.security.mock.MockAuthenticationFilter;

@Configuration
@EnableWebSecurity
// 로컬 테스트 인증 통과용 임시 코드
public class SecurityConfigLocal {

	@Bean
	public SecurityFilterChain localFilterChain(HttpSecurity http) throws Exception {
		http
			.csrf(csrf -> csrf.disable())
			.formLogin(form -> form.disable())
			.httpBasic(basic -> basic.disable())
			.logout(logout -> logout.disable())
			.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/actuator/**").permitAll()
				.requestMatchers("/api/**").permitAll()
				.anyRequest().permitAll()
			)

			.addFilterBefore(new MockAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}
}

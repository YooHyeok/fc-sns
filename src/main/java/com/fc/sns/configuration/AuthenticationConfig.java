package com.fc.sns.configuration;

import com.fc.sns.configuration.filter.JwtTokenFilter;
import com.fc.sns.exception.CustomAuthenticationEntryPoint;
import com.fc.sns.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security 관련 설정 (인증)
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class AuthenticationConfig extends WebSecurityConfigurerAdapter {

    private final UserService userService;

    @Value("${jwt.secret-key}")
    private String key;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeRequests()
                .antMatchers("/api/*/users/join", "/api/*/users/login").permitAll() // 회원가입, 로그인은 회원 정보를 가지고 로그인하는 것이므로 어떠한 경우에도 허용
                .antMatchers("/api/**").authenticated() // 그 외는 모두 인증받아야함
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Session을 사용하지 않는 방식 (JWT사용)
                // TODO - 인증 도중 exception 발생시 지정한 Entrypoint로 매핑
                .and()
                .addFilterBefore(new JwtTokenFilter(key, userService), UsernamePasswordAuthenticationFilter.class)
                /* 에러 발생시 핸들링 */
                .exceptionHandling()
                .authenticationEntryPoint(new CustomAuthenticationEntryPoint())
        ;
    }
}

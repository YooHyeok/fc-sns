package com.fc.sns.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

/**
 * Security 관련 설정 (인증)
 */
@Configuration
@EnableWebSecurity
public class AuthenticationConfig extends WebSecurityConfigurerAdapter {

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
//                .and()
//                .exceptionHandling()
//                .authenticationEntryPoint()
        ;
    }
}

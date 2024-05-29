package com.fc.sns.configuration;

import com.fc.sns.configuration.filter.JwtTokenFilter;
import com.fc.sns.exception.CustomAuthenticationEntryPoint;
import com.fc.sns.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
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

    /**
     * 리소스 인가 처리
     * SecurityFilterChain의 앞단에서 작동하여 해당 요청 리소스 경로만 보안 검사를 처리하도록 지정한다.
     * 지정하지 않은 url은 Security의 보안 검사대상에서 제외된다.
     * ex) /favicon.js 리소스에 대한 요청이 들어올 경우 HttpSercurity를 매개변수로 갖는 configure()에 걸리지 않음
     * @param web
     * @throws Exception
     */
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().regexMatchers("^(?!/api/).*")
                .antMatchers(HttpMethod.POST, "/api/*/users/join", "/api/*/users/login"); // 회원가입, 로그인은 회원 정보를 가지고 로그인하는 것이므로 어떠한 경우에도 허용

    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeRequests()
                .antMatchers("/api/**").authenticated() // 모두 인증받아야함
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

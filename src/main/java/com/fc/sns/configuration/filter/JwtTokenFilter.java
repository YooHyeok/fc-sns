package com.fc.sns.configuration.filter;

import com.fc.sns.model.User;
import com.fc.sns.service.UserService;
import com.fc.sns.util.JwtTokenUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

    private final String key;
    private final UserService userService;

    /**
     * 요청이 들어올 때 Request 객체를 가지고 인증을 수행할 수 있는 작업이 가능해짐.
     * 로그인 할 때 토큰값을 Response로 반환한다.
     * 해당 Token을 Header에 넣어 서버에 요청을 하면 필터에서 헤더값을 보고 토큰을통해 인증을 수행한다.
     * @param request
     * @param response
     * @param filterChain
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // get Header
        final String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            log.error("Error occurs while getting header. header is null or invalid");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String token = header.split(" ")[1].trim();

            /* 토큰 유효 검증 */
            // TODO: check token is valid
            if (JwtTokenUtils.isExpired(token, key)) {
                log.error("Key is expired");
                filterChain.doFilter(request, response);
                return;
            }

            /* 토큰으로 부터 username 획득 */
            // TODO: get userName from token
            String userName = JwtTokenUtils.getUserName(token, key);

            /* username을 통해 회원 엔티티 조회 */
            // TODO: check the userName is valid
            User user = userService.loadByUsername(userName);

            /* 회원 정보와 Role 각각 Security의 Pirncipal과 Authorities에 저장 */
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(
                    //princlipal, credential, authorities
//                    user, null, List.of(new SimpleGrantedAuthority(user.getUserRole().toString()))
                    user, null, user.getAuthorities()
            );

            /* Detail 초기화 - request 정보를 함께 넣어준다. */
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        } catch (RuntimeException e) {
            log.error("Error occurs while validating. {}", e.toString());
            filterChain.doFilter(request, response);
            return;
        }

        filterChain.doFilter(request, response); // 종료 후 다음 Filter로 request와 response를 넘겨준다.
    }
}

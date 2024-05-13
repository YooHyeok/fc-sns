package com.fc.sns.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

public class JwtTokenUtils {

    public static String getUserName(String token, String key) {
        return extractClaims(token, key).get("userName", String.class);
    }

    /**
     * 토큰 만료 여부
     * @param token
     * @return
     */
    public static boolean isExpired(String token, String key) {
        Date expiredDate = extractClaims(token, key).getExpiration();
        return expiredDate.before(new Date()); // 현재 시간보다 이전인지
    }

    /**
     * Token정보와 Key값을 가지고 Claim 추출
     * @param token
     * @param key
     * @return
     */
    private static Claims extractClaims(String token, String key) {
        return Jwts.parserBuilder().setSigningKey(getKey(key))
                .build().parseClaimsJws(token).getBody();
    }


    /**
     * 토큰 생성
     */
    public static String generateToken(String userName, String key, long expiredTimeMs) {
        Claims claims = Jwts.claims();
        claims.put("userName", userName);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiredTimeMs))
                .signWith(getKey(key), SignatureAlgorithm.HS256) // 서명 key 암호화 - Hash 256 알고리즘
                .compact(); // String으로 반환
    }

    /**
     * setExpiration에 의해 Jwt 서명시 필요한 Key 반환 <br/>
     * 문자열 타입의 Key를 Security 패키지의 Key 타입으로 변환한다. <br/>
     */
    public static Key getKey(String key) {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

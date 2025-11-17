package project.backend.kakaoLogin;

import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.Base64Codec;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtTokenProvider {

	@Value("${jwt.secret-key}")
    private String secretKey;

    // 유효기간 설정
    private final long accessTokenValidity = 60 * 60 * 1000L; // 1시간
    private final long refreshTokenValidity = 7 * 24 * 60 * 60 * 1000L; // 7일

    // Access Token 생성
    public String createAccessToken(String userId, String role) {
        return createToken(userId, role, accessTokenValidity);
    }

    // Refresh Token 생성
    public String createRefreshToken(String userId, String role) {
        return createToken(userId, role, refreshTokenValidity);
    }

    // 공통 토큰 생성 로직
    private String createToken(String userId, String role, long validity) {
        Claims claims = Jwts.claims().setSubject(userId);
        claims.put("role", role);

        Date now = new Date();
        Date expiration = new Date(now.getTime() + validity);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(SignatureAlgorithm.HS256, secretKey.getBytes())
                .compact();
    }

    // 토큰에서 사용자 ID 추출
    public String getUserIdFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey.getBytes())
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secretKey.getBytes()).parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT 검증 실패: {}", e.getMessage());
            return false;
        }
    }

    // Claims 추출
    public Claims getClaims(String token) {
        return Jwts.parser()
                .setSigningKey(Base64Codec.BASE64.encode(secretKey))
                .parseClaimsJws(token)
                .getBody();
    }
}
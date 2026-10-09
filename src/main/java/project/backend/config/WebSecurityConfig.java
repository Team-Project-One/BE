package project.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {

    private static final String[] SWAGGER_URLS = {
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/webjars/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // csrf 비활성화
                .csrf(csrf -> csrf.disable())
                // CORS 설정 허용
                .cors(Customizer.withDefaults())
                // 요청별 인가 규칙
                .authorizeHttpRequests(auth -> auth
                        // 테스트를 위해 "/api/**" 및 "/ai/**"를 추가함, 추후 보안 고려 시 세밀하게 관리하도록 수정하는 게 좋음
                        .requestMatchers(
                                "/auth/**",
                                "/login/**",
                                "/api/**",
                                "/ai/**",
                                "/oauth2/**",
                                "/uploads/**",
                                "/ws/chat/**",
                                "/users/signup",
                                "/users/status/**",
                                "/my-page/**",
                                "/fortune/**",
                                "/auth/test-login",
                                "/matching/**"
                        ).permitAll()
                        .requestMatchers(SWAGGER_URLS).permitAll()
                        .anyRequest().authenticated()
                )
                // OAuth2 로그인 설정
                .oauth2Login(oauth -> oauth
                        .defaultSuccessUrl("/auth/kakao/callback", true)
                )
                // JWT 사용 시 기본 폼 로그인 사용하지 않음
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                // 세션 비활성화 (JWT 사용 시)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
package org.example.emmm.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final AuthService authService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. 요청 URL과 메서드 확인
        System.out.println("=== [필터 시작] 요청: " + request.getMethod() + " " + request.getRequestURI());

        if ("OPTIONS".equals(request.getMethod())) {
            System.out.println("=== [필터 통과] OPTIONS 요청입니다.");
            filterChain.doFilter(request, response);
            return;
        }

        // 2. 헤더 값 확인 (여기가 핵심! ⭐)
        String header = request.getHeader("Authorization");
        System.out.println("=== [헤더 확인] Authorization: " + header);

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7).trim();
            System.out.println("=== [토큰 추출] token: " + token.substring(0, Math.min(token.length(), 10)) + "..."); // 앞 10자리만 출력

            try {
                Long userId = authService.verifyAccessToken(token);
                System.out.println("=== [검증 성공] userId: " + userId);

                UserPrincipal principal = new UserPrincipal(userId);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(principal, null, List.of());

                SecurityContextHolder.getContext().setAuthentication(authentication);
                System.out.println("=== [컨텍스트 저장 완료]");

            } catch (Exception e) {
                System.out.println("=== [검증 실패] 에러: " + e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        } else {
            // 3. 토큰이 없거나 형식이 틀린 경우
            System.out.println("=== [인증 실패] 헤더가 없거나 'Bearer '로 시작하지 않음");
        }

        filterChain.doFilter(request, response);
        System.out.println("=== [필터 종료] 다음 필터/컨트롤러로 이동");
    }

}

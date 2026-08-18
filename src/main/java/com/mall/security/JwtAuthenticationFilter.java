package com.mall.security;

import com.mall.common.enums.PrincipalType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;

    private final MemberDetailsService memberDetailsService;

    private final AdminDetailsService adminDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authorization =
                request.getHeader("Authorization");

        if (authorization == null
                || !authorization.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.substring(7);

        if (!jwtTokenService.validateToken(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (SecurityContextHolder.getContext()
                .getAuthentication() == null) {

            try {
                String username =
                        jwtTokenService.getUsername(token);

                PrincipalType principalType =
                        jwtTokenService
                                .getPrincipalType(token);

                UserDetails userDetails =
                        loadUserDetails(
                                username,
                                principalType
                        );

                if (userDetails.isEnabled()) {
                    UsernamePasswordAuthenticationToken
                            authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    SecurityContextHolder.getContext()
                            .setAuthentication(authentication);
                }

            } catch (UsernameNotFoundException exception) {
                // Token 中的用户已被删除，
                // 保持未认证状态，由 Security 返回 401
            }
        }

        filterChain.doFilter(request, response);
    }

    private UserDetails loadUserDetails(
            String username,
            PrincipalType principalType) {

        return switch (principalType) {
            case MEMBER ->
                    memberDetailsService
                            .loadUserByUsername(username);

            case ADMIN ->
                    adminDetailsService
                            .loadUserByUsername(username);
        };
    }
}
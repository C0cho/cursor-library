package com.cc.library.filter;

import com.cc.library.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.debug("Authorization header missing or does not start with Bearer");
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);
        logger.debug("Extracted JWT: {}", jwt);

        try {
            username = jwtUtil.extractUsername(jwt);
            logger.debug("Extracted username: {}", username);
        } catch (Exception e) {
            logger.error("Error extracting username from JWT", e);
            // Allow filter chain to continue, will result in unauthenticated
            filterChain.doFilter(request, response);
            return;
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = null;
            try {
                 userDetails = this.userDetailsService.loadUserByUsername(username);
                 logger.debug("Loaded UserDetails for user {}: {}", username, userDetails != null);
            } catch (Exception e) {
                 logger.error("Error loading user details for user {}", username, e);
                 // Allow filter chain to continue
                 filterChain.doFilter(request, response);
                 return;
            }

            if (userDetails != null && jwtUtil.validateToken(jwt, userDetails)) {
                logger.debug("JWT token validated for user: {}", username);
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);
                logger.debug("Authentication set in SecurityContext for user: {}", username);
            } else {
                 logger.debug("JWT token validation failed for user: {} or userDetails is null", username);
            }
        }

        filterChain.doFilter(request, response);
    }
} 
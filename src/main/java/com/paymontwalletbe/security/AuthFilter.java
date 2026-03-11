package com.paymontwalletbe.security;

import com.paymontwalletbe.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String email = request.getHeader("X-User-Email");
        Optional<com.paymontwalletbe.model.entities.User> userOptional =
                email == null ? Optional.empty() : userRepository.findByEmail(email);

        if (email == null) {
            log.warn("Unauthorized request: missing X-User-Email header");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized: missing or invalid X-User-Email");
            return;
        }

        if (userOptional.isEmpty()) {
            log.warn("Unauthorized request: user not found for email={}", email);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized: missing or invalid X-User-Email");
            return;
        }

        userOptional.ifPresent(user -> {
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            List.of(new SimpleGrantedAuthority("USER"))
                    );
            auth.setDetails(user);
            SecurityContextHolder.getContext().setAuthentication(auth);
            log.debug("Authenticated request for userId={} email={}", user.getId(), user.getEmail());
        });

        filterChain.doFilter(request, response);
    }
}

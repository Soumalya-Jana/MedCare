package com.hospital.management.config;

import com.hospital.management.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authenticationProvider(authenticationProvider())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/images/**", "/fonts/**").permitAll()
                .requestMatchers("/login", "/login/**", "/register", "/register/**", "/error").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler((request, response, authentication) -> {
                    String portalRole = request.getParameter("portalRole");
                    boolean hasAdmin = authentication.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                    boolean hasDoctor = authentication.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_DOCTOR"));
                    boolean hasReceptionist = authentication.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_RECEPTIONIST"));

                    // Enforce portal-specific role isolation if accessed from a dedicated portal
                    if ("ADMIN".equalsIgnoreCase(portalRole) && !hasAdmin) {
                        org.springframework.security.core.context.SecurityContextHolder.clearContext();
                        request.getSession().invalidate();
                        response.sendRedirect(request.getContextPath() + "/login/admin?roleMismatch=true");
                        return;
                    }
                    if ("DOCTOR".equalsIgnoreCase(portalRole) && !hasDoctor) {
                        org.springframework.security.core.context.SecurityContextHolder.clearContext();
                        request.getSession().invalidate();
                        response.sendRedirect(request.getContextPath() + "/login/doctor?roleMismatch=true");
                        return;
                    }
                    if ("RECEPTIONIST".equalsIgnoreCase(portalRole) && !hasReceptionist) {
                        org.springframework.security.core.context.SecurityContextHolder.clearContext();
                        request.getSession().invalidate();
                        response.sendRedirect(request.getContextPath() + "/login/receptionist?roleMismatch=true");
                        return;
                    }

                    // Direct to respective role dashboard
                    if (hasDoctor) {
                        response.sendRedirect(request.getContextPath() + "/dashboard/doctor");
                    } else if (hasReceptionist) {
                        response.sendRedirect(request.getContextPath() + "/dashboard/receptionist");
                    } else {
                        response.sendRedirect(request.getContextPath() + "/dashboard/admin");
                    }
                })
                .failureHandler((request, response, exception) -> {
                    String portalRole = request.getParameter("portalRole");
                    if ("DOCTOR".equalsIgnoreCase(portalRole)) {
                        response.sendRedirect(request.getContextPath() + "/login/doctor?error=true");
                    } else if ("RECEPTIONIST".equalsIgnoreCase(portalRole)) {
                        response.sendRedirect(request.getContextPath() + "/login/receptionist?error=true");
                    } else if ("ADMIN".equalsIgnoreCase(portalRole)) {
                        response.sendRedirect(request.getContextPath() + "/login/admin?error=true");
                    } else {
                        response.sendRedirect(request.getContextPath() + "/login?error=true");
                    }
                })
                .permitAll()
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/access-denied")
            );

        return http.build();
    }
}

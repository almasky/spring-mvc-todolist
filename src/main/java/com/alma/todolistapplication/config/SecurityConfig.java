package com.alma.todolistapplication.config;

import com.alma.todolistapplication.security.UserDetailsServiceImpl; // Your UserDetailsService
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher; // For logout

@Configuration
@EnableWebSecurity // Enables Spring Security's web security support
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;

    @Autowired
    public SecurityConfig(UserDetailsServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Use BCrypt for strong, salted password hashing
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        // Configure AuthenticationManagerBuilder to use our UserDetailsService and PasswordEncoder
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
        return authenticationManagerBuilder.build();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Configure authorization rules
                .authorizeHttpRequests(authz -> authz
                        // Allow access to static resources (CSS, JS, images) without authentication
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                        // Allow access to the registration page and the registration processing URL
                        .requestMatchers("/register", "/perform-register").permitAll()
                        // Allow access to the login page (Spring Security handles /login POST internally)
                        .requestMatchers("/login").permitAll()
                        // H2 Console access (only for development, remove or secure for production)
                        // .requestMatchers("/h2-console/**").permitAll() // Uncomment if you were using H2
                        // All other requests must be authenticated
                        .anyRequest().authenticated()
                )
                // Configure form-based login
                .formLogin(form -> form
                        .loginPage("/login")             // The URL of our custom login page
                        .loginProcessingUrl("/perform_login") // The URL Spring Security will intercept for login submissions
                        .defaultSuccessUrl("/", true)   // Redirect to home page after successful login
                        .failureUrl("/login?error=true")   // Redirect to login page with error if login fails
                        .permitAll()                     // Allow access to the login page and processing URL
                )
                // Configure logout
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/perform_logout")) // The URL to trigger logout
                        .logoutSuccessUrl("/login?logout=true") // Redirect to login page with logout message
                        .invalidateHttpSession(true)        // Invalidate the HTTP session
                        .deleteCookies("JSESSIONID")      // Delete the session cookie
                        .permitAll()                      // Allow access to the logout URL
                );

        // For H2 console frame display (only if using H2 and its console)
        // http.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));
        // http.csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**")); // Disable CSRF for H2 console

        return http.build();
    }
}
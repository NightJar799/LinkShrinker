package miniLu.demo.config;

import miniLu.demo.Repository.UserRepository;
import miniLu.demo.entity.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import java.util.Optional;

@Configuration
public class SecurityConfig {

    private final UserRepository userService;
    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    public SecurityConfig(UserRepository userService) {
        this.userService = userService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                )
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers("/auth", "/auth/**", "/reg", "/reg/**")
                        .permitAll()
                         .requestMatchers("/favicon.ico")
                        .permitAll()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/auth")
                        .loginProcessingUrl("/auth")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .successHandler(authenticationSuccessHandler())
                        .failureUrl("/auth?error=true")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/auth?logout")
                        .permitAll())
                .exceptionHandling(exceptions -> exceptions
                        .accessDeniedHandler(accessDeniedHandler()))
                .build();
    }

   @Bean
public AuthenticationSuccessHandler authenticationSuccessHandler() {
    return (request, response, authentication) -> {
        log.info("=== AUTHENTICATION SUCCESS ===");
        log.info("User: {}", authentication.getName());
        log.info("Authorities: {}", authentication.getAuthorities());

        Optional<User> userOptional = userService.findUserByEmail(authentication.getName());
        userOptional.ifPresent(user -> 
            request.getSession().setAttribute("user", user)
        );

        response.sendRedirect("/");
    };
}

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            log.error("=== ACCESS DENIED ===");
            //request.getSession().setAttribute("AuthError", true);
            response.sendRedirect("/auth");
        };
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepo) {
        log.info("Creating UserDetailsService bean...");

        return username -> {
            log.info("=== LOADING USER DETAILS ===");
            log.info("Username: {}", username);

            User user = userRepo.findUserByEmail(username).get();

            if (user != null) {
                log.info("User found in database: {}", user.getName());
                log.info("User password hash: {}", user.getPassword());
                log.info("=== END LOADING USER DETAILS ===");
                return user;
            }

            log.warn("User NOT FOUND in database: {}", username);
            log.info("=== END LOADING USER DETAILS ===");
            throw new UsernameNotFoundException("User " + username + " doesn't exist");
        };
    }
}


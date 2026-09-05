package miniLu.demo.config;

import miniLu.demo.Repository.UserRepository;
import miniLu.demo.entity.User;
import miniLu.demo.util.Mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
// import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import java.util.Optional;

@Configuration
public class SecurityConfig {

    private final UserRepository userRepository;
    private final Mapper mapper;
    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    public SecurityConfig(UserRepository userRepository, Mapper mapper) {
        this.userRepository = userRepository;
        this.mapper = mapper;
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
                        // .successHandler(authenticationSuccessHandler())
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

//    @Bean
// public AuthenticationSuccessHandler authenticationSuccessHandler() {
//     return (request, response, authentication) -> {
//         log.info("=== AUTHENTICATION SUCCESS ===");
//         log.info("User: {}", authentication.getName());
//         log.info("Authorities: {}", authentication.getAuthorities());

//         //Optional<User> userOptional = userRepository.findUserByEmail(authentication.getName());
//         //UserDetails userdetails = (UserDetails)authentication.getPrincipal();
//         //log.info("USER DETAILS: {}", authentication.toString());

//         // UserDto dto = UserDto.builder().email(authentication.get)
//         // log.info(dto.toString());
//         // request.getSession().setAttribute("user", dto);

//         response.sendRedirect("/");
//     };
// }

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

        return email -> {
            log.info("=== LOADING USER DETAILS ===");
            log.info("Username: {}", email);

            Optional<User> userOptinal = userRepository.findUserByEmail(email);
            if (userOptinal.isPresent()) {
                log.info("User found in database: {}", userOptinal.get().getName());
                log.info("User password hash: {}", userOptinal.get().getPassword());
                log.info("=== END LOADING USER DETAILS ===");
                return userOptinal.get();
            }

            log.warn("User NOT FOUND in database: {}", email);
            log.info("=== END LOADING USER DETAILS ===");
            throw new UsernameNotFoundException("User " + email + " doesn't exist");
        };
    }
}


package com.drzig.taskmanager.config;

import com.drzig.taskmanager.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final UserService userService;
    private final LoginSuccessHandler loginSuccessHandler;
    private final LoggingAccessDeniedHandler accessDeniedHandler;

    public SecurityConfig(UserService userService, LoginSuccessHandler loginSuccessHandler, LoggingAccessDeniedHandler accessDeniedHandler) {
        this.userService = userService;
        this.loginSuccessHandler = loginSuccessHandler;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/css/**", "/h2-console/**").permitAll()
                    .requestMatchers("/admin/**").hasRole("ADMIN")   // admin only
                    .requestMatchers("/change-password").authenticated()
                    // /register is NO LONGER permitted
                    .anyRequest().authenticated()
            )
            .formLogin(form -> form
                    .loginPage("/login")
                    .successHandler(loginSuccessHandler)   // custom handler checks the flag
                    .permitAll()
            )
            .logout(logout -> logout
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/login?logout")
                    .permitAll()
            )
            .exceptionHandling(ex -> ex.accessDeniedHandler(accessDeniedHandler))
            .headers(h -> h.frameOptions(fo -> fo.sameOrigin()))
            .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"));

        return http.build();
    }
}

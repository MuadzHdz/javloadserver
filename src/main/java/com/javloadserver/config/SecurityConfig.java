package com.javloadserver.config;

import com.javloadserver.ServerConfig;
import com.javloadserver.UploadServerApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final ServerConfig serverConfig;

    public SecurityConfig() {
        this.serverConfig = UploadServerApplication.getServerConfig();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/login", "/static/**", "/css/**", "/js/**").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )
            .sessionManagement(session -> session
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
            );

        if (!serverConfig.hasPassword()) {
            http
                .authorizeHttpRequests(authz -> authz
                    .anyRequest().permitAll()
                )
                .formLogin(form -> form
                    .disable()
                )
                .csrf(csrf -> csrf
                    .disable()
                );
        }

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        if (serverConfig.hasPassword()) {
            UserDetails user = User.builder()
                .username("user")
                .password(serverConfig.getPassword())
                .passwordEncoder(passwordEncoder()::encode)
                .roles("USER")
                .build();
            return new InMemoryUserDetailsManager(user);
        } else {
            return new InMemoryUserDetailsManager();
        }
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }
}
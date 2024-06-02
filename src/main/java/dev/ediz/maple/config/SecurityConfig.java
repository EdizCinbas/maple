package dev.ediz.maple.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.boot.autoconfigure.security.servlet.PathRequest.toH2Console;
import static org.springframework.security.config.Customizer.withDefaults;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http

                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(("/h2-console/**")).permitAll();
                    auth.requestMatchers("/").permitAll();
                    auth.requestMatchers("/posts/**").permitAll();
                    auth.anyRequest().authenticated();
                })
                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
                .headers(header -> header.frameOptions(frame -> frame.disable()))
                .oauth2Login(withDefaults())
                .formLogin(withDefaults())
                .build();
    }

}
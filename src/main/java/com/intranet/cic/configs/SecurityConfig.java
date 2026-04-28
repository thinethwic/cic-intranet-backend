package com.intranet.cic.configs;

import com.intranet.cic.security.AuthenticationFilter;
import com.intranet.cic.security.IntranetAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthenticationFilter authenticationFilter;

    private final IntranetAuthenticationEntryPoint intranetAuthenticationEntryPoint;
    private final CorsConfigurationSource corsConfigurationSource;

    //TODO: handle unauthorized error 403

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(intranetAuthenticationEntryPoint)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/public/**",
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/webjars/**",
                                "/swagger-resources/**"
                        ).permitAll()
                        .requestMatchers("/uploads/images/**").permitAll()       // ✅ images
                        .requestMatchers("/uploads/documents/**").permitAll()
                        // Public read access to mentors from home page
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/events",
                                "/api/v1/members",
                                "/api/v1/videos",
                                "/api/v1/images",
                                "/api/v1/documents",
                                "/api/v1/documents/*/view",      // ✅ add
                                "/api/v1/documents/*/download",
                                "/api/v1/news",
                                "/api/v1/news/*",
                                "/api/v1/news/*/image",
                                "/api/v1/users").permitAll()

                        .requestMatchers(HttpMethod.PUT,
                                "/api/v1/events",
                                "/api/v1/members",
                                "/api/v1/videos",
                                "/api/v1/images",
                                "/api/v1/documents",
                                "/api/v1/news",
                                "/api/v1/news/*",
                                "/api/v1/news/*/image",
                                "/api/v1/users").permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/events",
                                "/api/v1/members",
                                "/api/v1/videos",
                                "/api/v1/images",
                                "/api/v1/documents",
                                "/api/v1/news",
                                "/api/v1/users").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new InMemoryUserDetailsManager();
    }
}

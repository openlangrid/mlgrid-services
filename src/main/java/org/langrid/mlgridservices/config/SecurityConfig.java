package org.langrid.mlgridservices.config;

import java.util.ArrayList;
import java.util.List;

import org.langrid.mlgridservices.ApplicationYaml;
import org.langrid.mlgridservices.config.auth.ApiKeyAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        var origins = new ArrayList<String>();
        if(yaml.getAllowedOrigines() != null) {
            origins.addAll(List.of(yaml.getAllowedOrigines()));
        }
        System.out.println("allowed origins: " + origins);
        http.csrf(csrf -> csrf.disable())
            .cors(cors-> cors.configurationSource(request -> {
                CorsConfiguration c = new CorsConfiguration();
                c.setAllowedOriginPatterns(origins);
                c.setAllowedMethods(List.of("*"));
                c.setAllowedHeaders(List.of("*"));
                return c;
            }))
//            .httpBasic(Customizer.withDefaults())
            .sessionManagement(s->s
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(
                new ApiKeyAuthenticationFilter(yaml.getApiKeys(), contextPath, "/services", "/ws"),
                UsernamePasswordAuthenticationFilter.class)
            ;
        return http.build();
    }

    @Autowired
    private ApplicationYaml yaml;
    @Value("${server.servlet.context-path:}")
    private String contextPath;
}

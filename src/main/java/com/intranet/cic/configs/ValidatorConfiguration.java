package com.intranet.cic.configs;

import com.intranet.cic.security.IntranetJwtValidator;
import com.intranet.cic.security.TokenValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class ValidatorConfiguration {
    @Bean
    @ConditionalOnProperty(name = "auth.validator.type", havingValue = "stem-link")
    public TokenValidator jwtTokenValidator(@Value("${jwt.secret:my-secret-key-must-be-at-least-32-characters-long-for-HS256}") String jwtSecret) {
        log.info("JWT validator configured as primary TokenValidator");
        return new IntranetJwtValidator(jwtSecret);
    }
}

package spc.cloud;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Class to configure AWS Cognito as an OAuth 2.0 authorizer with Spring Security.
 * In this configuration, we specify our OAuth Client.
 * We also declare that all requests must come from an authenticated user.
 * Finally, we configure our logout handler.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private final CognitoAuthSuccessHandler cognitoAuthSuccessHandler;
    private final CognitoLogoutHandler cognitoLogoutHandler;

    SecurityConfiguration(CognitoAuthSuccessHandler cognitoAuthSuccessHandler, CognitoLogoutHandler cognitoLogoutHandler) {
        this.cognitoAuthSuccessHandler = cognitoAuthSuccessHandler;
        this.cognitoLogoutHandler = cognitoLogoutHandler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/").permitAll()
                        .anyRequest().authenticated())
                .oauth2Login(oath2 -> oath2
                        .successHandler(cognitoAuthSuccessHandler))
                .logout(logout -> logout
                        .logoutSuccessHandler(cognitoLogoutHandler));
        return http.build();
    }
}
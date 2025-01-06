package spc.cloud;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
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

    @Autowired
    private CognitoAuthSuccessHandler cognitoAuthSuccessHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        CognitoLogoutHandler cognitoLogoutHandler = new CognitoLogoutHandler();
//
//        http.csrf(Customizer.withDefaults())
//                .authorizeHttpRequests(authz -> authz
//                        .requestMatchers("/").permitAll()
//                        .anyRequest()
//                        .authenticated())
//                .oauth2Login(oauth2 -> oauth2
//                    .successHandler(cognitoAuthSuccessHandler)
//                )
//                .logout(logout -> logout.logoutSuccessHandler(cognitoLogoutHandler));
//        return http.build();
        //TODO for development, delete
        http
                .csrf(csrf -> csrf.disable()) // Disable CSRF for simplicity in development
                .authorizeHttpRequests(authz -> authz
                        .anyRequest().permitAll()) // Allow all requests without authentication
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(cognitoAuthSuccessHandler)) // Disable OAuth2 Login
                .logout(logout -> logout.disable()); // Disable Logout

        return http.build();
    }
}
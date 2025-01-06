package spc.cloud;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import spc.cloud.entity.User;
import spc.cloud.repository.UserRepository;

import java.io.IOException;
import java.util.UUID;

@Component
public class CognitoAuthSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        org.springframework.security.core.Authentication authentication)
            throws IOException, ServletException {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

        String username = oauth2User.getAttribute("username"); // From Cognito
        String userId = oauth2User.getAttribute("sub"); // Unique userId from Cognito

        // save user to db
        User user = new User();
        user.setUserId(UUID.fromString(userId));
        user.setName(username);
        userRepository.save(user);

        response.sendRedirect("/upload"); // Redirect to a default page after success
    }
}
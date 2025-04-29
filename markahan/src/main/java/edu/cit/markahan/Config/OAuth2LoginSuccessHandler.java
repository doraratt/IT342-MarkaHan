package edu.cit.markahan.Config;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2LoginSuccessHandler.class);

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                       Authentication authentication) throws IOException, ServletException {
        try {
            OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
            
            // Log all attributes for debugging
            logger.info("OAuth2 login success. Authentication: {}", authentication);
            logger.info("OAuth2 user attributes:");
            oauthUser.getAttributes().forEach((key, value) -> 
                logger.info("  {} = {}", key, value));
            
            // Redirect to frontend
            response.sendRedirect("http://localhost:5173/oauth2/redirect");
        } catch (Exception e) {
            logger.error("Error in OAuth2LoginSuccessHandler", e);
            response.sendRedirect("http://localhost:5173/login?error=" + e.getMessage());
        }
    }
}
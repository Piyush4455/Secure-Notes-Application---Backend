package com.secure.notes.config;

import com.secure.notes.models.AppRole;
import com.secure.notes.models.Role;
import com.secure.notes.models.User;
import com.secure.notes.repositories.RoleRepository;
import com.secure.notes.security.jwt.JwtUtils;
import com.secure.notes.security.services.UserDetailsImpl;
import com.secure.notes.services.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    @Autowired
    private final UserService userService;

    @Autowired
    private final JwtUtils jwtUtils;

    @Autowired
    RoleRepository roleRepository;

    @Value("${frontend.url}")
    private String frontendUrl;

    String username;
    String idAttributeKey;

    @Override
    @Transactional  // Ensure user is saved before method completes
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws ServletException, IOException {
        OAuth2AuthenticationToken oAuth2AuthenticationToken = (OAuth2AuthenticationToken) authentication;

        if ("github".equals(oAuth2AuthenticationToken.getAuthorizedClientRegistrationId()) ||
                "google".equals(oAuth2AuthenticationToken.getAuthorizedClientRegistrationId())) {

            DefaultOAuth2User principal = (DefaultOAuth2User) authentication.getPrincipal();
            Map<String, Object> attributes = principal.getAttributes();
            String email = attributes.getOrDefault("email", "").toString();
            String name = attributes.getOrDefault("name", "").toString();

            if ("github".equals(oAuth2AuthenticationToken.getAuthorizedClientRegistrationId())) {
                username = attributes.getOrDefault("login", "").toString();
                idAttributeKey = "id";
            } else if ("google".equals(oAuth2AuthenticationToken.getAuthorizedClientRegistrationId())) {
                username = email.split("@")[0];
                idAttributeKey = "sub";
            } else {
                username = "";
                idAttributeKey = "id";
            }

            log.info("OAuth2 Login - Email: {}, Username: {}, Provider: {}",
                    email, username, oAuth2AuthenticationToken.getAuthorizedClientRegistrationId());

            // Find or create user
            User user = userService.findByEmail(email)
                    .orElseGet(() -> {
                        log.info("Creating new OAuth2 user: {}", email);
                        User newUser = new User();
                        Optional<Role> userRole = roleRepository.findByRoleName(AppRole.ROLE_USER);
                        if (userRole.isPresent()) {
                            newUser.setRole(userRole.get());
                        } else {
                            throw new RuntimeException("Default role not found");
                        }
                        newUser.setEmail(email);
                        newUser.setUserName(username);
                        newUser.setSignUpMethod(oAuth2AuthenticationToken.getAuthorizedClientRegistrationId());

                        // Save and return the user
                        User savedUser = userService.registerUser(newUser);
                        log.info("Successfully created OAuth2 user with ID: {}", savedUser.getUserId());
                        return savedUser;
                    });

            // Update security context with user's actual role
            DefaultOAuth2User oauthUser = new DefaultOAuth2User(
                    List.of(new SimpleGrantedAuthority(user.getRole().getRoleName().name())),
                    attributes,
                    idAttributeKey
            );
            Authentication securityAuth = new OAuth2AuthenticationToken(
                    oauthUser,
                    List.of(new SimpleGrantedAuthority(user.getRole().getRoleName().name())),
                    oAuth2AuthenticationToken.getAuthorizedClientRegistrationId()
            );
            SecurityContextHolder.getContext().setAuthentication(securityAuth);

            // Generate JWT token
            Set<SimpleGrantedAuthority> authorities = new HashSet<>();
            authorities.add(new SimpleGrantedAuthority(user.getRole().getRoleName().name()));
            authorities.add(new SimpleGrantedAuthority("OAUTH2_USER"));

            // Create UserDetailsImpl instance
            UserDetailsImpl userDetails = new UserDetailsImpl(
                    user.getUserId(),
                    user.getUserName(),
                    user.getEmail(),
                    user.getPassword(),
                    user.isTwoFactorEnabled(),
                    authorities
            );

            String jwtToken = jwtUtils.generateTokenFromUsername(userDetails);
            log.info("Generated JWT token for user: {}", username);

            // Redirect to frontend with JWT token
            String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/oauth2/redirect")
                    .queryParam("token", jwtToken)
                    .build().toUriString();

            this.setAlwaysUseDefaultTargetUrl(true);
            this.setDefaultTargetUrl(targetUrl);
            super.onAuthenticationSuccess(request, response, authentication);
        }
    }
}
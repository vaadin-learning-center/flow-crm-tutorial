package com.example.application.security;

import java.util.Optional;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinServletRequest;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Component;

@Component
public class SecurityService {

    private static final String LOGOUT_SUCCESS_URL = "/";

    public static UserDetails getAuthenticatedUser() {
        SecurityContext context = SecurityContextHolder.getContext();
        Object principal = context.getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return (UserDetails) context.getAuthentication().getPrincipal();
        }
        // Anonymous or no authentication.
        return null;
    }

    public static UserDetails getAnonymousUser() {
        return org.springframework.security.core.userdetails.User
                .withUsername("anonymousUser")
                .password("") // Password is not relevant for anonymous user
                .authorities("ROLE_ANONYMOUS") // Anonymous authority
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }

    public void logout() {
        UI.getCurrent().getPage().setLocation(LOGOUT_SUCCESS_URL);
        SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
        logoutHandler.logout(
                VaadinServletRequest.getCurrent().getHttpServletRequest(), null,
                null);
    }

    public static boolean userInRole(String role) {
        UserDetails authenticatedUser = getAuthenticatedUser();
        if (authenticatedUser == null) {
            return false;
        }
        return authenticatedUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(role));
    }
}

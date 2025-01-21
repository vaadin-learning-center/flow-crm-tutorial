package com.example.application.views;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.example.application.security.SecurityService;
import com.example.application.views.list.ListView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.switchuser.SwitchUserGrantedAuthority;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HighlightConditions;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinServletRequest;

public class MainLayout extends AppLayout {
    private final SecurityService securityService;

    @Autowired
    private InMemoryUserDetailsManager userDetailsService;

    @Autowired
    private SecurityContextHolderStrategy securityContextHolderStrategy;

    private VerticalLayout menuItems = new VerticalLayout();

    private Span helloUser = new Span("Welcome, Anonymous!");

    public MainLayout(SecurityService securityService) {
        this.securityService = securityService;
        createHeader();


        addToDrawer(menuItems);
        updateDrawer();
    }

    private void createHeader() {
        H1 logo = new H1("Vaadin CRM");
        logo.addClassNames("text-l", "m-m");

        Button logout = new Button("Log out", e -> securityService.logout());

        HorizontalLayout header = new HorizontalLayout(new DrawerToggle(), logo, logout);

        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(logo);
        header.setWidth("100%");
        header.addClassNames("py-0", "px-m");

        header.add(helloUser);
        updateUserName(SecurityService.getAuthenticatedUser());

        Button impersonate = new Button("Impersonate user", e -> {
            UserDetails user = userDetailsService.loadUserByUsername("user");
            impersonate(user, (VaadinServletRequest) VaadinRequest.getCurrent());
        });

        impersonate.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        impersonate.setIcon(VaadinIcon.USER.create());

        Button impersonateAnonymous = new Button("Impersonate anonymous", e -> {
            UserDetails anonymous = SecurityService.getAnonymousUser();
            impersonate(anonymous, (VaadinServletRequest) VaadinRequest.getCurrent());
        });

        impersonateAnonymous.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        impersonateAnonymous.setIcon(VaadinIcon.QUESTION.create());

        Button exitImpersonation = new Button("Exit impersonation", e ->
                exitImpersonated());

        exitImpersonation.addThemeVariants(ButtonVariant.LUMO_ERROR);

        header.add(impersonate, impersonateAnonymous, exitImpersonation);


        addToNavbar(header);

    }

    private void updateDrawer() {

        menuItems.removeAll();

        RouterLink publicLink = new RouterLink("Home", PublicView.class);
        menuItems.add(publicLink);

        if (SecurityService.userInRole("ROLE_ADMIN")) {
            RouterLink dashboardLink = new RouterLink("Dashboard", DashboardView.class);
            menuItems.add(dashboardLink);
        }

        if (SecurityService.userInRole("ROLE_USER")) {
            RouterLink listLink = new RouterLink("List", ListView.class);
            listLink.setHighlightCondition(HighlightConditions.sameLocation());
            menuItems.add(listLink);
        }
    }

    private void impersonate(UserDetails targetUser, VaadinServletRequest request) {
        UsernamePasswordAuthenticationToken targetUserRequest =
                createSwitchUserToken(request.getHttpServletRequest(), targetUser);
        // TODO: Before switching, a check should be made on whether the user is already impersonated.
        SecurityContext context = this.securityContextHolderStrategy.createEmptyContext();
        context.setAuthentication(targetUserRequest);
        this.securityContextHolderStrategy.setContext(context);
        // TODO: redirect, context persistence and other stuff should be done in an impersonation listener
        UI.getCurrent().getPage().setLocation("/list");
    }

    private void exitImpersonated() {
        Authentication current = this.securityContextHolderStrategy.getContext().getAuthentication();
        Authentication original = getOriginalAuthentication(current).orElseThrow(
                () -> new RuntimeException("Impersonated anonymous user"));
        SecurityContext context = this.securityContextHolderStrategy.createEmptyContext();
        context.setAuthentication(original);
        this.securityContextHolderStrategy.setContext(context);
        // TODO: redirect, context persistence and other stuff should be done in an impersonation listener
        UI.getCurrent().getPage().setLocation("/");
    }

    private UsernamePasswordAuthenticationToken createSwitchUserToken(HttpServletRequest request,
                                                                      UserDetails targetUser) {
        List<GrantedAuthority> switchUserAuthoritiesList = getMergedGrantedAuthorities(targetUser);
        UsernamePasswordAuthenticationToken targetUserRequest =
                UsernamePasswordAuthenticationToken.authenticated(targetUser, targetUser.getPassword(),
                        switchUserAuthoritiesList);
        WebAuthenticationDetailsSource webAuthenticationDetailsSource = new WebAuthenticationDetailsSource();
        targetUserRequest.setDetails(webAuthenticationDetailsSource.buildDetails(request));
        return targetUserRequest;
    }

    /**
     * Merges the granted authorities of the target user with the current user's granted authorities.
     * @param targetUser the target user to impersonate
     * @return a list of merged granted authorities
     */
    private List<GrantedAuthority> getMergedGrantedAuthorities(UserDetails targetUser) {
        Authentication currentAuthentication = this.securityContextHolderStrategy.getContext().getAuthentication();
        if (currentAuthentication == null) {
            throw new RuntimeException("Trying to impersonate without an existing authentication");
        }
        Collection<? extends GrantedAuthority> targetUserAuthorities = targetUser.getAuthorities();
        List<GrantedAuthority> switchUserAuthoritiesList = new ArrayList<>(targetUserAuthorities);
        switchUserAuthoritiesList.addAll(getOriginalAuthorities(currentAuthentication));
        return switchUserAuthoritiesList;
    }

    private Collection<GrantedAuthority> getOriginalAuthorities(Authentication currentAuthentication) {
//        List<GrantedAuthority> currentAuthoritiesList = new ArrayList<>();
//        for (GrantedAuthority authority : currentAuthentication.getAuthorities()) {
//            GrantedAuthority currentUserGrantedAuthority = new SwitchUserGrantedAuthority(authority.getAuthority(),
//                    currentAuthentication);
//            currentAuthoritiesList.add(currentUserGrantedAuthority);
//        }
//        return currentAuthoritiesList;

        // by default this adds some non-existing role,
        // so that the original authorities are not taken into account, i.e.
        // a user cannot act as admin after impersonation.
        // This however can be changed by adding all current authorities.
        return Collections.singleton(new SwitchUserGrantedAuthority(
                "ROLE_PREVIOUS_ADMINISTRATOR", currentAuthentication));
    }

    private Optional<Authentication> getOriginalAuthentication(Authentication currentAuthentication) {
        // iterate over granted authorities and find the 'switch user' authentications
        Collection<? extends GrantedAuthority> authenticationAuthorities = currentAuthentication.getAuthorities();
        for (GrantedAuthority grantedAuthority : authenticationAuthorities) {
            // return original Authentication that is mapped to any SwitchUserGrantedAuthority
            // (expected that all SwitchUserGrantedAuthority are mapped to a single Authentication)
            if (grantedAuthority instanceof SwitchUserGrantedAuthority) {
                return Optional.of(((SwitchUserGrantedAuthority) grantedAuthority).getSource());
            }
        }
        return Optional.empty();
    }

    private void updateUserName(UserDetails targetUser) {
        if (targetUser != null) {
            helloUser.setText("Welcome, " + targetUser.getUsername() + "!");
        } else {
            helloUser.setText("Welcome, Anonymous!");
        }
    }
}

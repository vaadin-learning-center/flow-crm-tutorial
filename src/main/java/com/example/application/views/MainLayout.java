package com.example.application.views;

import javax.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
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

        Button exitImpersonation = new Button("Exit impersonation", e ->
                exitImpersonated());

        header.add(impersonate, exitImpersonation);


        addToNavbar(header);

    }

    private void updateDrawer() {

        menuItems.removeAll();

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
        SecurityContext context = this.securityContextHolderStrategy.createEmptyContext();
        context.setAuthentication(targetUserRequest);
        this.securityContextHolderStrategy.setContext(context);
        UI.getCurrent().getPage().setLocation("/");
    }

    private void exitImpersonated() {
        Authentication current = this.securityContextHolderStrategy.getContext().getAuthentication();
        if (current == null) {
            throw new RuntimeException("Cannot exit impersonation: no current user");
        }
        Authentication original = getOriginalAuthentication(current);
        SecurityContext context = this.securityContextHolderStrategy.createEmptyContext();
        context.setAuthentication(original);
        this.securityContextHolderStrategy.setContext(context);
        UI.getCurrent().getPage().setLocation("/dashboard");
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

        Collection<? extends GrantedAuthority> switchUserAuthorities = targetUser.getAuthorities();
        List<GrantedAuthority> switchUserAuthoritiesList = new ArrayList<>(switchUserAuthorities);
        for (GrantedAuthority authority : currentAuthentication.getAuthorities()) {
            GrantedAuthority currentUserAuthorities = new SwitchUserGrantedAuthority(authority.getAuthority(),
                    currentAuthentication);
            switchUserAuthoritiesList.add(currentUserAuthorities);
        }
        return switchUserAuthoritiesList;
    }

    private void updateUserName(UserDetails targetUser) {
        if (targetUser != null) {
            helloUser.setText("Welcome, " + targetUser.getUsername() + "!");
        } else {
            helloUser.setText("Welcome, Anonymous!");
        }
    }

    private Authentication getOriginalAuthentication(Authentication currentAuthentication) {
        Authentication original = null;
        // iterate over granted authorities and find the 'switch user' authentications
        Collection<? extends GrantedAuthority> authenticationAuthorities = currentAuthentication.getAuthorities();
        for (GrantedAuthority grantedAuthority : authenticationAuthorities) {
            // return original Authentication that is mapped to any SwitchUserGrantedAuthority
            // (expected that all SwitchUserGrantedAuthority are mapped to a single Authentication)
            if (grantedAuthority instanceof SwitchUserGrantedAuthority) {
                return ((SwitchUserGrantedAuthority) grantedAuthority).getSource();
            }
        }
        return null;
    }
}

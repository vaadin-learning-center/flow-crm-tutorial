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

    private Span helloUser = new Span("Welcome, Anonymous!");

    public MainLayout(SecurityService securityService) {
        this.securityService = securityService;
        createHeader();
        createDrawer();
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
        updateUserName(securityService.getAuthenticatedUser());

        Button impersonate = new Button("Impersonate user", e -> {
            UserDetails user = userDetailsService.loadUserByUsername("user");
            e.getSource().getUI().ifPresent(ui -> {
                impersonate(user, (VaadinServletRequest) VaadinRequest.getCurrent());
            });
        });

        header.add(impersonate);


        addToNavbar(header);

    }

    private void createDrawer() {
        RouterLink listLink = new RouterLink("List", ListView.class);
        listLink.setHighlightCondition(HighlightConditions.sameLocation());

        addToDrawer(new VerticalLayout(
            listLink,
            new RouterLink("Dashboard", DashboardView.class)
        ));
    }

    private void impersonate(UserDetails targetUser, VaadinServletRequest request) {
        UsernamePasswordAuthenticationToken targetUserRequest =
                createSwitchUserToken(request.getHttpServletRequest(), targetUser);
        SecurityContext context = this.securityContextHolderStrategy.createEmptyContext();
        context.setAuthentication(targetUserRequest);
        this.securityContextHolderStrategy.setContext(context);
        updateUserName(targetUser);
    }

    private UsernamePasswordAuthenticationToken createSwitchUserToken(HttpServletRequest request,
                                                                      UserDetails targetUser) {
        Authentication authentication = this.securityContextHolderStrategy.getContext().getAuthentication();
        GrantedAuthority switchAuthority = new SwitchUserGrantedAuthority("ADMIN",
                authentication);
        Collection<? extends GrantedAuthority> orig = targetUser.getAuthorities();
        List<GrantedAuthority> newAuths = new ArrayList<>(orig);
        newAuths.add(switchAuthority);
        UsernamePasswordAuthenticationToken targetUserRequest =
                UsernamePasswordAuthenticationToken.authenticated(targetUser, targetUser.getPassword(),
                        newAuths);
        WebAuthenticationDetailsSource webAuthenticationDetailsSource = new WebAuthenticationDetailsSource();
        targetUserRequest.setDetails(webAuthenticationDetailsSource.buildDetails(request));
        return targetUserRequest;
    }

    private void updateUserName(UserDetails targetUser) {
        if (targetUser != null) {
            helloUser.setText("Welcome, " + targetUser.getUsername() + "!");
        } else {
            helloUser.setText("Welcome, Anonymous!");
        }
    }
}

package com.example.application.views;

import jakarta.servlet.http.HttpServletRequest;
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
            getUI().ifPresent(ui -> ui.getPage().setLocation("/impersonate?username=user"));
        });

        impersonate.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        impersonate.setIcon(VaadinIcon.USER.create());


        Button exitImpersonation = new Button("Exit impersonation", e ->
                exitImpersonated());

        exitImpersonation.addThemeVariants(ButtonVariant.LUMO_ERROR);

        header.add(impersonate, exitImpersonation);


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

    private void exitImpersonated() {
        getUI().ifPresent(ui -> ui.getPage().setLocation("/impersonate/exit"));
    }


    private void updateUserName(UserDetails targetUser) {
        if (targetUser != null) {
            helloUser.setText("Welcome, " + targetUser.getUsername() + "!");
        } else {
            helloUser.setText("Welcome, Anonymous!");
        }
    }
}

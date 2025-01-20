package com.example.application.views;

import com.example.application.security.SecurityService;
import com.example.application.views.list.ListView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

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

public class MainLayout extends AppLayout {
    private final SecurityService securityService;

    @Autowired
    private InMemoryUserDetailsManager userDetailsService;

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

        UserDetails authenticatedUser = securityService.getAuthenticatedUser();
        if (authenticatedUser != null) {
            header.add(new Span("Welcome, " + authenticatedUser.getUsername()));

            if (authenticatedUser.getUsername().equals("admin")) {
                Button impersonate = new Button("Impersonate user", e -> {
                    UserDetails user = userDetailsService.loadUserByUsername("user");
                    getUI().ifPresent(ui -> ui.getPage().setLocation("/impersonate?username=" + user.getUsername()));
                });

                header.add(impersonate);
            }
        }

        Button exitButton = new Button("Exit Impersonation", event -> {
            getUI().ifPresent(ui -> ui.getPage().setLocation("/exit-impersonate"));
        });
        header.add(exitButton);

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
}

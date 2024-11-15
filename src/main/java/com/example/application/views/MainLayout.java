package com.example.application.views;

import java.util.Optional;

import com.example.application.data.PushSubscriptionEntity;
import com.example.application.services.CrmService;
import com.example.application.security.SecurityService;
import com.example.application.views.list.ListView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.componentfactory.ToggleButton;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.webpush.WebPushSubscription;
import com.vaadin.flow.theme.lumo.LumoUtility;

public class MainLayout extends AppLayout {
    private final SecurityService securityService;
    private final CrmService crmService;

    private ToggleButton notifications;
    private WebPushSubscription clientSubscription;
    private boolean storeClientSub = false;

    public MainLayout(SecurityService securityService, CrmService crmService) {
        this.securityService = securityService;
        this.crmService = crmService;
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        H1 logo = new H1("Vaadin CRM");
        logo.addClassNames(LumoUtility.FontSize.LARGE,
                LumoUtility.Margin.MEDIUM);

        String userName = securityService.getAuthenticatedUser().getUsername();
        notifications = new ToggleButton("Notifications");
        notifications.addValueChangeListener(event -> {
            if (event.getValue()) {
                // subscribe
                if (storeClientSub) {
                    getLog().debug("Storing existing subscription for user");
                    crmService.addSubscription(userName, clientSubscription);
                } else if(clientSubscription == null) {
                    getLog().debug("Subscribing push for user {}", userName);
                    crmService.getWebPush()
                            .subscribe(event.getSource().getUI().get(),
                                    subscription -> crmService.addSubscription(
                                            userName, subscription));
                }
            } else {
                if (crmService.hasMultipleSubscriptions(userName)) {
                    // unsubscribe
                    getLog().debug("Removing only db subscription");
                    crmService.removeSubscription(userName, clientSubscription);
                } else {
                    getLog().debug("Removing subscription");
                    crmService.getWebPush()
                            .unsubscribe(event.getSource().getUI().get(),
                                    subscription -> crmService.removeSubscription(
                                            userName, subscription));
                }
                // Manually remove sub on browser
                // navigator.serviceWorker.getRegistration().then((reg) => reg.pushManager.getSubscription().then(sub => sub.unsubscribe()));
            }
        });
        // Manually check subscription on client console use:
        // navigator.serviceWorker.getRegistration().then((reg) => reg.pushManager.getSubscription().then(sub => console.log(sub)));

        Button logout = new Button("Log out " + userName,
                e -> securityService.logout()); // <2>

        var header = new HorizontalLayout(new DrawerToggle(), logo,
                notifications, logout);

        header.setDefaultVerticalComponentAlignment(
                FlexComponent.Alignment.CENTER);
        header.expand(logo); // <4>
        header.setWidthFull();
        header.addClassNames(LumoUtility.Padding.Vertical.NONE,
                LumoUtility.Padding.Horizontal.MEDIUM);

        addToNavbar(header);

    }

    private void createDrawer() {
        VerticalLayout links = new VerticalLayout(
                new RouterLink("List", ListView.class),
                new RouterLink("Dashboard", DashboardView.class));
        if (securityService.getAuthenticatedUser().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            links.add(new RouterLink("Message", MessageView.class));
        }
        addToDrawer(links);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        String username = securityService.getAuthenticatedUser().getUsername();
        boolean hasSubscription = crmService.getSubscription(username) != null;

        crmService.getWebPush()
                .subscriptionExists(attachEvent.getUI(), existsOnClient -> {
                    if (!hasSubscription && existsOnClient) {
                        getLog().info(
                                "Found subscription on client, but not in DataBase for {}",
                                username);
                        // get client subscription, but do not activate for wrong user on server.
                        crmService.getWebPush()
                                .fetchExistingSubscription(attachEvent.getUI(),
                                        this::checkSubscriptionMatchForClientSubscription);
                    } else if (hasSubscription && existsOnClient) {
                        getLog().info(
                                "Client and data base subscriptions exist");
                        crmService.getWebPush()
                                .fetchExistingSubscription(attachEvent.getUI(),
                                        this::checkClientAndDBSubscriptionMatch);
                    }
                });
    }

    private void checkSubscriptionMatchForClientSubscription(
            WebPushSubscription subscription) {
        Optional<PushSubscriptionEntity> matchingSubscription = crmService.getAllSubscriptions()
                .stream().filter(sub -> sub.equalsSubscription(subscription))
                .findFirst();
        // If no DB subscription exists for this client subscription register for current user.
        if (!matchingSubscription.isPresent()) {
            storeClientSub = true;
            clientSubscription = subscription;
            notifications.setValue(true);
        }
    }

    private void checkClientAndDBSubscriptionMatch(WebPushSubscription subscription) {
        String username = securityService.getAuthenticatedUser().getUsername();

        // User client subscription if user turns on if it wasn't for the current user.
        clientSubscription = subscription;
        // If the client subscription doesn't match the stored subscription(s) store as new.
        if (crmService.getPushSubscriptions(username).stream()
                .anyMatch(sub -> sub.equalsSubscription(subscription))) {
            // set active as user subscription and existing client subscription
            notifications.setValue(true);
        } else {
            getLog().info(
                    "Data base did not contain client subscription for {}.\nBrowser subscription is for another user.",
                    username);

        }
    }

    private Logger getLog() {
        return LoggerFactory.getLogger("Crm");
    }
}
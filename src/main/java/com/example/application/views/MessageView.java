package com.example.application.views;

import java.util.List;

import com.example.application.data.entity.PushSubscription;
import com.example.application.data.service.CrmService;
import jakarta.annotation.security.RolesAllowed;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.webpush.WebPushMessage;

@RolesAllowed("ADMIN")
@Route(value = "message", layout = MainLayout.class)
@PageTitle("WebPush Message | Vaadin CRM")
public class MessageView extends VerticalLayout {
    private final CrmService service;

    public MessageView(CrmService service) {
        this.service = service;
        addClassName("message-view");
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);

        add(getMessageBuilder());
    }

    private Component getMessageBuilder() {
        List<PushSubscription> allSubscriptions = service.getAllSubscriptions();

        if (allSubscriptions.isEmpty()) {
            // If no subscriptions do not populate message view and inform user.
            return new Span("No web push message subscriptions.");
        }

        MultiSelectComboBox<PushSubscription> registeredUsers = new MultiSelectComboBox<>("Send to");
        registeredUsers.setItems(allSubscriptions);
        registeredUsers.setItemLabelGenerator(PushSubscription::getUserName);

        Button selectAll = new Button("Select all", event -> registeredUsers.select(allSubscriptions));

        TextField title = new TextField("Message title");
        title.setTooltipText("Tip: Add '{}' to include username to title string");
        TextField message = new TextField("Message");

        Button send = new Button("Send", event -> {
            new Thread(() -> {
                for (PushSubscription subscription : registeredUsers.getSelectedItems()) {
                    String titleValue = title.getValue().replace("{}", subscription.getUserName());
                    service.getWebPush().sendNotification(
                            subscription.createSubscription(),
                            new WebPushMessage(titleValue, message.getValue())
                    );
                }
            }).start();
        });
        VerticalLayout layout = new VerticalLayout(registeredUsers, selectAll, title, message, send);


        return layout;
    }
}
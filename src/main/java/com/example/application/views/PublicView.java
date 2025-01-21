package com.example.application.views;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@AnonymousAllowed
@Component
@Scope("prototype")
@Route(value="", layout = MainLayout.class)
@PageTitle("Home | Vaadin CRM")
public class PublicView extends VerticalLayout {

    public PublicView() {
        addClassName("public-view");
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        add(new H1("Welcome to Vaadin CRM"), new Span("Please log in to access the application."));
    }
}

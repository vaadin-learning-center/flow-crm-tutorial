package com.example.application.views;

import com.example.application.data.Contact;
import com.example.application.data.ContactSpecification;
import com.example.application.services.ContactService;
import jakarta.annotation.security.PermitAll;
import org.springframework.context.annotation.Scope;
import org.springframework.data.jpa.domain.Specification;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;

@SpringComponent
@Scope("prototype")
@PermitAll
@Route(value = "crud", layout = MainLayout.class)
@PageTitle("CRUD | Vaadin CRM")
public class PageableCrudView extends VerticalLayout {

    private final ContactService service;
    private final Grid<Contact> grid = new Grid<>(Contact.class);
    private final TextField filterText = new TextField();

    public PageableCrudView(ContactService service) {
        this.service = service;

        filterText.setPlaceholder("Filter by name...");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        filterText.addValueChangeListener(e -> updateList());

        grid.setColumns("firstName", "lastName", "email");
        grid.addColumn(contact -> contact.getStatus().getName()).setHeader("Status");
        grid.addColumn(contact -> contact.getCompany().getName()).setHeader("Company");
        grid.getColumns().forEach(col -> col.setAutoWidth(true));
        updateList();

        add(filterText, grid);

    }

    private void updateList() {
        Specification<Contact> filter = ContactSpecification.containsText(
                filterText.getValue());
        grid.setItemsPageable(pageable -> service.list(pageable, filter));
    }
}

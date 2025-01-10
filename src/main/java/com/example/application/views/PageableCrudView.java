package com.example.application.views;

import java.util.List;

import com.example.application.data.Contact;
import com.example.application.services.ContactService;
import com.google.common.collect.Lists;
import jakarta.annotation.security.PermitAll;
import org.hibernate.query.Page;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.data.filter.Filter;
import com.vaadin.flow.spring.data.filter.OrFilter;
import com.vaadin.flow.spring.data.filter.PropertyStringFilter;

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
        grid.setItems(query -> {
            PropertyStringFilter nameFilter = new PropertyStringFilter();
            nameFilter.setPropertyId("firstName");
            nameFilter.setFilterValue(filterText.getValue());
            nameFilter.setMatcher(PropertyStringFilter.Matcher.CONTAINS);

            PropertyStringFilter lastNameFilter = new PropertyStringFilter();
            lastNameFilter.setPropertyId("lastName");
            lastNameFilter.setFilterValue(filterText.getValue());
            lastNameFilter.setMatcher(PropertyStringFilter.Matcher.CONTAINS);

            PropertyStringFilter emailFilter = new PropertyStringFilter();
            emailFilter.setPropertyId("email");
            emailFilter.setFilterValue(filterText.getValue());
            emailFilter.setMatcher(PropertyStringFilter.Matcher.CONTAINS);

            OrFilter filter = new OrFilter();
            filter.setChildren(Lists.newArrayList(nameFilter, lastNameFilter, emailFilter));

            return service.list(PageRequest.of(query.getPage(), query.getPageSize()),
                    filter).stream();
        });
    }
}

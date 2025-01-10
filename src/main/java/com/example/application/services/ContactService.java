package com.example.application.services;

import java.util.stream.Stream;

import com.example.application.data.Contact;
import com.example.application.data.ContactRepository;
import com.example.application.data.ContactSpecification;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.vaadin.flow.data.provider.Query;

@Service
public class ContactService {

    private final ContactRepository repository;

    public ContactService(ContactRepository repository) {
        this.repository = repository;
    }


    public Stream<Contact> list(Query<Contact, Void> query, String filter) {
        PageRequest pageRequest = PageRequest.of(query.getPage(), query.getPageSize());
        if (filter == null || filter.isEmpty()) {
            return repository.findAll(pageRequest).stream();
        } else {
            return repository.findAll(ContactSpecification.filterByKeyword(
                    filter, "firstName", "lastName", "email"), pageRequest).stream();
        }
    }
}

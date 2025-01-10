package com.example.application.services;

import com.example.application.data.Contact;
import com.example.application.data.ContactRepository;
import org.springframework.stereotype.Service;

import com.vaadin.flow.spring.data.jpa.CrudRepositoryService;

@Service
public class ContactService extends CrudRepositoryService<Contact, Long, ContactRepository> {

    public ContactService(ContactRepository repository) {
        super(repository);
    }
}

package com.example.application.services;

import java.util.List;

import com.example.application.data.Contact;
import com.example.application.data.ContactRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class ContactService {

    private final ContactRepository repository;

    public ContactService(ContactRepository repository) {
        this.repository = repository;
    }

    public List<Contact> list(Pageable pageable, Specification<Contact> filter) {
        return repository.findAll(filter, pageable).toList();
    }
}

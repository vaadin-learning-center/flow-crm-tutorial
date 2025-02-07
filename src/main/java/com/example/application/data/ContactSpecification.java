package com.example.application.data;

import org.springframework.data.jpa.domain.Specification;

public class ContactSpecification {

    public static Specification<Contact> containsText(String text) {
        return (root, query, criteriaBuilder) -> {
            String likePattern = "%" + text.toLowerCase() + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), likePattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), likePattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), likePattern)
            );
        };
    }
}

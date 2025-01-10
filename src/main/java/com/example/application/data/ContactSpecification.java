package com.example.application.data;

import java.util.Arrays;

import org.springframework.data.jpa.domain.Specification;

public class ContactSpecification {

    public static Specification<Contact> filterByKeyword(String keyword, String ... properties) {
        return (root, query, criteriaBuilder) -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                return criteriaBuilder.conjunction(); // No filter
            }

            String containsPattern = "%" + keyword.toLowerCase() + "%";
            return Arrays.stream(properties).map(property ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.get(property)), containsPattern)
            ).reduce(criteriaBuilder::or).orElse(criteriaBuilder.conjunction());
        };
    }
}


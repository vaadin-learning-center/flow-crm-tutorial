package com.example.application.data;

import jakarta.annotation.Nullable;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.Formula;

import java.util.LinkedList;
import java.util.List;

@Entity
public class Company extends AbstractEntity {
    @NotBlank
    private String name;

    @OneToMany(mappedBy = "company")
    @Nullable
    private List<Contact> employees = new LinkedList<>();

    @Formula("(select count(c.id) from Contact c where c.company_id = id)")
    private int employeeCount;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Contact> getEmployees() {
        return employees;
    }

    public void setEmployees(List<Contact> employees) {
        this.employees = employees;
    }

    public int getEmployeeCount(){
        return employeeCount;
    }
}

package com.example.saas.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "orders")
public class OrderEntity {

    @Id
    private Long id;
    private String userId;
    private String customerName;
    private double total;
    private String status;

    public Long getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public double getTotal() {
        return total;
    }

    public String getStatus() {
        return status;
    }
}

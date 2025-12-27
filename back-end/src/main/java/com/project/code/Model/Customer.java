package com.project.code.Model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotNull;

/**
 * Represents a Customer within the system.
 * <p>
 * This entity stores Customer's information and maintains a 
 * one-to-many relationship with {@link OrderDetails}.
 * </p>
 */
@Entity
public class Customer {

    /** Unique Identifier for the Customer. */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The name of the Customer. Must not be null. */
    @NotNull(message = "Name cannot be null")
    private String name;

    /** The email address of the Customer. Must not be null. */
    @NotNull(message = "Email cannot be null")
    private String email;

    /** The phone number of the Customer. Must not be null. */
    @NotNull(message = "Phone cannot be null")
    private String phone;

    /** The list of orders associated with the Customer.
     *  Mapped by the "customer" field in the OrderDetails entity.
     */
    @OneToMany(mappedBy = "customer", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<OrderDetails> orders;

    /**
     * Default constructor required by JPA.
     */
    public Customer() {};

    /**
     * Getter for Customer ID.
     * @return The generated ID of this Customer.
     */
    public Long getId() {
        return id;
    }

    /**
     * Setter for Customer ID.
     * @param id The ID to set for this Customer.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Getter for Customer name.
     * @return The name of this Customer.
     */
    public String getName() {
        return name;
    }

    /**
     * Setter for Customer name.
     * @param name The name to set for this Customer.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter for Customer email.
     * @return The email of this Customer.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Setter for Customer email.
     * @param email The email to set for this Customer.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Getter for Customer phone number.
     * @return The phone number of this Customer.
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Setter for Customer phone number.
     * @param phone The phone number to set for this Customer
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Retrieves all orders linked to this Customer.
     * @return A list of {@link OrderDetails}
     */
    public List<OrderDetails> getOrders() {
        return orders;
    }

    /**
     * Associates a list of {@link OrderDetails} with this Customer.
     * @param orders The list of {@link OrderDetails} to link to this Customer.
     */
    public void setOrders(List<OrderDetails> orders) {
        this.orders = orders;
    }

}
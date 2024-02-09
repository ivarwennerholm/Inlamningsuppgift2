package Models;

import java.time.LocalDate;

public class Order {
    private int id;
    private LocalDate date;
    private Customer customer;

    public Order(int id, LocalDate date, Customer customer) {
        this.id = id;
        this.date = date;
        this.customer = customer;
    }

    public int getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public Customer getCustomer() {
        return customer;
    }

    @Override
    public String toString() {
        return String.format("%-6s%-14s%-12s", id, date, customer.getId());
    }

}
package Models;

import java.time.LocalDate;

public class Order {
    private final int id;
    private final LocalDate date;
    private final Customer customer;

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

}

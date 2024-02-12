package Models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private final int id;
    private final LocalDate date;
    private final Customer customer;
    private List<Shoe> shoes = new ArrayList<>();
    private List<Integer> numberOrdered = new ArrayList<>();
    private int totalValue;

    public Order(int id, LocalDate date, Customer customer) {
        this.id = id;
        this.date = date;
        this.customer = customer;
    }
    public List<Shoe> getShoes() {
        return shoes;
    }

    public void setShoes(List<Shoe> shoes) {
        this.shoes = shoes;
    }

    public List<Integer> getNumberOrdered() {
        return numberOrdered;
    }

    public void setNumberOrdered(List<Integer> numberOrdered) {
        this.numberOrdered = numberOrdered;
    }

    public int getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(int totalValue) {
        this.totalValue = totalValue;
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

    public boolean isShoeInOrder(int shoeID) {
        return this.shoes.stream().filter(c -> c.getId() == shoeID).findAny().isPresent();
    }

    public int getOrderedNumberForShoe(int shoeID) {
        int numberedOrdered = 0;
        for (int i = 0; i < shoes.size(); i++) {
            if (shoes.get(i).getId() == shoeID)
                numberedOrdered = numberOrdered.get(i);
        }
        return numberedOrdered;
    }


}

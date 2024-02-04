package Models;

import Models.Shoe;

public class OrderContent {

    private Order order;
    private Shoe shoe;
    private int antal;

    public OrderContent(Order order, Shoe shoe, int antal) {
        this.order = order;
        this.shoe = shoe;
        this.antal = antal;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Shoe getSko() {
        return shoe;
    }

    public void setSko(Shoe shoe) {
        this.shoe = shoe;
    }

    public int getAntal() {
        return antal;
    }

    public void setAntal(int antal) {
        this.antal = antal;
    }
}

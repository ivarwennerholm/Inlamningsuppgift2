package Models;

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

    public Shoe getSko() {
        return shoe;
    }

    public int getAntal() {
        return antal;
    }

}
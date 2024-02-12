package Models;

public class OrderContent {
    private final Order order;
    private final Shoe shoe;
    private final int number;

    public OrderContent(Order order, Shoe shoe, int num) {
        this.order = order;
        this.shoe = shoe;
        this.number = num;
    }

    public Order getOrder() {
        return order;
    }

    public Shoe getShoe() {
        return shoe;
    }

    public int getNumber() {
        return number;
    }

}
package Models;

public class Shoe {
    private int id, size, inventory, price;
    private String brand, color;

    public Shoe(int id, int size, int inventory, int price, String brand, String color) {
        this.id = id;
        this.size = size;
        this.inventory = inventory;
        this.price = price;
        this.brand = brand;
        this.color = color;
    }

    public int getId() {
        return id;
    }

    public int getSize() {
        return size;
    }

    public int getInventory() {
        return inventory;
    }

    public int getPrice() {
        return price;
    }

    public String getBrand() {
        return brand;
    }

    public String getColor() {
        return color;
    }

    @Override
    public String toString() {
        return String.format("%-12s%-8s%-10s%-10s%-8s", brand, color, size, price, inventory);
    }

}
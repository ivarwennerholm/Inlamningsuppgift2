package Models;

public class Customer {
    private int id, zipCode;
    private String firstName, lastName, address, city;

    public Customer(int id, int zipCode, String firstName, String lastName, String address, String city) {
        this.id = id;
        this.zipCode = zipCode;
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.city = city;
    }

    public int getId() {
        return id;
    }

    public int getZipCode() {
        return zipCode;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }

    @Override
    public String toString() {
        return String.format("%-6s%-12s%-18s%-24s%-12s%-12s", id, firstName, lastName, address, zipCode, city);
    }

}
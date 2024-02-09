package Models;

public class Customer {
    private final int id, zipCode;
    private final String firstName, lastName, address, city;

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

}

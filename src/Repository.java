import Models.*;

import java.io.FileInputStream;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Repository {

    Connection con = null;
    Statement stmt = null;
    PreparedStatement pstmt = null;
    CallableStatement cstmt = null;
    ResultSet rs = null;
    Properties props = new Properties();
    String url, user, pw;
    List<Shoe> shoes = new ArrayList<>();
    List<Category> categories = new ArrayList<>();
    List<CategoryMapping> categoryMappings = new ArrayList<>();

    public Repository() throws Exception {
        try {
            props.load(new FileInputStream("src/props.properties"));
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Error reading properties files", e);
        }
        url = props.getProperty("url");
        user = props.getProperty("user");
        pw = props.getProperty("pw");
    }

    public List<Shoe> getShoes() throws SQLException {
        int id, size, inventory;
        double prize;
        String brand, color;
        Shoe shoe;
        try {
            con = DriverManager.getConnection(url, user, pw);
            stmt = con.createStatement();
            rs = stmt.executeQuery("SELECT * FROM Shoes");
            while (rs.next()) {
                id = rs.getInt("id");
                brand = rs.getString("brand");
                color = rs.getString("color");
                prize = rs.getDouble("price");
                size = rs.getInt("shoesize");
                inventory = rs.getInt("inventory");
                shoe = new Shoe(id, size, inventory, prize, brand, color);
                shoes.add(shoe);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Error fetching Shoes", e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return shoes.stream().filter(s -> s.getInventory() > 0).toList();
    }

    public Customer loginCustomer(String username, String password) throws SQLException {
        Customer customer = null;
        try {
            con = DriverManager.getConnection(url, user, this.pw);
            pstmt = con.prepareStatement("SELECT * FROM Customers WHERE username = ? AND pw = ?");
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("id");
                String firstName = rs.getString("firstname");
                String lastName = rs.getString("lastname");
                String address = rs.getString("address");
                int zipCode = rs.getInt("zipcode");
                String city = rs.getString("city");
                String userName = rs.getString("username");
                String pw = rs.getString("pw");
                customer = new Customer(id, zipCode, firstName, lastName, address, city, userName, pw);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Error fetching table 'Customers' from database", e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return customer;
    }

    public List<Category> getCategories() throws SQLException {
        int id;
        String name;
        try {
            con = DriverManager.getConnection(url, user, pw);
            stmt = con.createStatement();
            rs = stmt.executeQuery("SELECT * FROM Categories");
            while (rs.next()) {
                id = rs.getInt("id");
                name = rs.getString("name");
                categories.add(new Category(id, name));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Error fetching Categories", e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return categories;
    }

    public List<CategoryMapping> getCategoryMappings() throws SQLException {
        int categoryID, shoeID;
        Category category = null;
        Shoe shoe = null;
        try {
            con = DriverManager.getConnection(url, user, pw);
            stmt = con.createStatement();
            rs = stmt.executeQuery("SELECT * FROM CategoriesMapping");
            while (rs.next()) {
                categoryID = rs.getInt("categoryID");
                shoeID = rs.getInt("shoeID");
                for (Category cat : categories) {
                    if (cat.getId() == categoryID)
                        category = cat;
                }
                for (Shoe shoetemp : shoes) {
                    if (shoetemp.getId() == shoeID)
                        shoe = shoetemp;
                }
                categoryMappings.add(new CategoryMapping(category, shoe));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Error fetching CategoriesMapping", e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return categoryMappings;
    }

    public int getLastOrderID() throws SQLException {
        int lastOrderID = 0;
        try {
            con = DriverManager.getConnection(url, user, pw);
            stmt = con.createStatement();
            rs = stmt.executeQuery("SELECT MAX(id) AS ID FROM Orders");
            while (rs.next()) {
                lastOrderID = rs.getInt("ID");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Error fetching last Order ID from table 'Orders'", e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return lastOrderID;
    }

    public void placeOrder(int orderID, int customerID, int shoeID, int number) throws SQLException {
        int lastOrderID = 0;
        try {
            con = DriverManager.getConnection(url, user, pw);
            pstmt = con.prepareCall("CALL AddToCart(?, ?, ?, ?)");
            pstmt.setInt(1, orderID);
            pstmt.setInt(2, customerID);
            pstmt.setInt(3, shoeID);
            pstmt.setInt(4, number);
            pstmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Error placing order with orderID = " + orderID + ", customerID = " + customerID +
                    ", shoeID = " + shoeID + " and number = " + number, e);
        } finally {
            try {
                if (cstmt != null) cstmt.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // TODO: BEHÖVS INTE LÄNGRE?
    /************************
     public List<Customer> getCustomers() throws Exception {
     int id, zipCode;
     String firstName, lastName, address, city, userName, pw;
     Customer customer;
     try {
     con = DriverManager.getConnection(url, user, this.pw);
     stmt = con.createStatement();
     rs = stmt.executeQuery("SELECT * FROM Customers");
     while (rs.next()) {
     id = rs.getInt("id");
     firstName = rs.getString("firstname");
     lastName = rs.getString("lastname");
     address = rs.getString("address");
     zipCode = rs.getInt("zipcode");
     city = rs.getString("city");
     userName = rs.getString("username");
     pw = rs.getString("pw");
     customer = new Customer(id, zipCode, firstName, lastName, address, city, userName, pw);
     customers.add(customer);
     }
     } catch (SQLException e) {
     e.printStackTrace();
     throw new Exception("Error fetching Customers", e);
     } finally {
     try {
     if (rs != null) rs.close();
     if (stmt != null) stmt.close();
     if (con != null) con.close();
     } catch (SQLException e) {
     e.printStackTrace();
     }
     }
     return customers;
     }

     public List<Order> getOrders() throws Exception {
     int id, customerID, year, month, day;
     String dateString;
     LocalDate date;
     Customer customer = null;
     Order order;
     try {
     con = DriverManager.getConnection(url, user, pw);
     stmt = con.createStatement();
     rs = stmt.executeQuery("SELECT * FROM Orders");
     while (rs.next()) {
     id = rs.getInt("id");
     dateString = rs.getString("orddate");
     customerID = rs.getInt("customerID");
     year = Integer.parseInt(dateString.substring(0, 4));
     month = Integer.parseInt(dateString.substring(5, 7));
     day = Integer.parseInt(dateString.substring(8, 10));
     date = LocalDate.of(year, month, day);
     //System.out.println(date); // TEST
     for (Customer cust : customers) {
     if (cust.getId() == customerID)
     customer = cust;
     }
     order = new Order(id, date, customer);
     orders.add(order);
     }
     } catch (SQLException e) {
     e.printStackTrace();
     throw new Exception("Error fetching Orders", e);
     } finally {
     try {
     if (rs != null) rs.close();
     if (stmt != null) stmt.close();
     if (con != null) con.close();
     } catch (SQLException e) {
     e.printStackTrace();
     }
     }
     return orders;
     }
     *************************/


}

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
        int id, size, inventory, price;
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
                price = rs.getInt("price");
                size = rs.getInt("shoesize");
                inventory = rs.getInt("inventory");
                shoe = new Shoe(id, size, inventory, price, brand, color);
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


    public String[] getOrderedTypes(String type) throws SQLException {
        List<String> list = new ArrayList<>();
        String inject = "";
        if (type.equals("brands"))
            inject = "Shoes.brand";
        if (type.equals("colors"))
            inject = "Shoes.color";
        if (type.equals("sizes"))
            inject = "Shoes.shoesize";
        String query = "SELECT DISTINCT " +
                inject +
                " AS types\n" +
                "FROM OrderContent\n" +
                "JOIN Shoes ON OrderContent.shoeID = Shoes.id\n" +
                "ORDER BY types";
        try {
            con = DriverManager.getConnection(url, user, pw);
            stmt = con.createStatement();
            rs = stmt.executeQuery(query);
            while (rs.next()) {
                list.add(rs.getString("types"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Error fetching ordered types in getOrderedTypes()", e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return list.toArray(new String[0]);
    }

    public List<String> getListQueryOne() throws SQLException {
        List<String> output = new ArrayList<>();
        StringBuilder str = new StringBuilder();
        String query = "SELECT Customers.firstname, Customers.lastname, Shoes.brand, Shoes.color, Shoes.shoesize\n" +
                "FROM Customers\n" +
                "JOIN Orders ON Customers.id = Orders.customerID\n" +
                "JOIN OrderContent ON Orders.id = OrderContent.orderID\n" +
                "JOIN Shoes ON OrderContent.shoeID = Shoes.id;";
        try {
            con = DriverManager.getConnection(url, user, pw);
            stmt = con.createStatement();

            rs = stmt.executeQuery(query);
            while (rs.next()) {
                str.append("firstname=" + rs.getString("firstname"));
                str.append(" lastname=" + rs.getString("lastname"));
                str.append(" brand=" + rs.getString("brand"));
                str.append(" color=" + rs.getString("color"));
                str.append(" size=" + rs.getInt("shoesize"));
                output.add(str.toString());
                str.setLength(0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Error fetching list for query #1'", e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return output;
    }


}

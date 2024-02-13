import Models.*;

import java.io.FileInputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


public class Repository {
    final Properties props = new Properties();
    final String url, user, pw;
    final Report report;
    Connection con;
    Statement stmt;
    PreparedStatement pstmt;
    CallableStatement cstmt;
    ResultSet rs = null;
    Customer thisCustomer;

    public Repository() throws Exception {
        try {
            props.load(new FileInputStream("src/props.properties"));
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Error reading properties file", e);
        }
        url = props.getProperty("url");
        user = props.getProperty("user");
        pw = props.getProperty("pw");

        List<Shoe> shoes = getShoesFromDB();
        List<Customer> customers = getCustomersFromDB();
        List<Order> orders = getOrdersFromDB(customers);
        List <OrderContent> orderContents = getOrderContentsFromDB(orders, shoes);
        orders = completeOrderList(orders, orderContents);
        report = new Report(shoes, customers, orders);
    }

    public List<Shoe> getShoesFromDB() throws SQLException {
        List<Shoe> output = new ArrayList<>();
        try {
            con = DriverManager.getConnection(url, user, pw);
            stmt = con.createStatement();
            rs = stmt.executeQuery("SELECT * FROM Shoes");
            while (rs.next()) {
                Shoe shoe = new Shoe(rs.getInt("id"),
                        rs.getInt("shoesize"),
                        rs.getInt("inventory"),
                        rs.getInt("price"),
                        rs.getString("brand"),
                        rs.getString("color"));
                output.add(shoe);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Error fetching 'Shoes' from database", e);
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

    public List<Customer> getCustomersFromDB() throws SQLException {
        List<Customer> output = new ArrayList<>();
        try {
            con = DriverManager.getConnection(url, user, pw);
            stmt = con.createStatement();
            rs = stmt.executeQuery("SELECT id, zipcode, firstname, lastname, address, city FROM Customers");
            while (rs.next()) {
                output.add(new Customer(rs.getInt("id"),
                        rs.getInt("zipcode"),
                        rs.getString("firstname"),
                        rs.getString("lastname"),
                        rs.getString("address"),
                        rs.getString("city")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Error fetching 'Customers' from database in getCustomers()", e);
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

    public List<OrderContent> getOrderContentsFromDB(List<Order> ordersInput, List<Shoe> shoesInput) throws SQLException {
        List<OrderContent> output = new ArrayList<>();
        try {
            con = DriverManager.getConnection(url, user, pw);
            stmt = con.createStatement();
            rs = stmt.executeQuery("SELECT * FROM OrderContent");
            while (rs.next()) {
                int orderID = rs.getInt("orderID");
                int shoeID = rs.getInt("shoeID");
                int number = rs.getInt("num");
                Order order = ordersInput.stream().
                        filter(o -> o.getId() == orderID).
                        findFirst().
                        orElse(null);
                Shoe shoe = shoesInput.stream().
                        filter(s -> s.getId() == shoeID).
                        findFirst().
                        orElse(null);
                output.add(new OrderContent(order, shoe, number));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Error fetching 'OrderContents' from database", e);
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

    public List<Order> getOrdersFromDB(List<Customer> customersInput) throws SQLException {
        List<Order> output = new ArrayList<>();
        try {
            con = DriverManager.getConnection(url, user, pw);
            stmt = con.createStatement();
            rs = stmt.executeQuery("SELECT * FROM Orders");
            while (rs.next()) {
                int id = rs.getInt("id");
                int customerID = rs.getInt("customerID");
                Customer customer = customersInput.stream().
                        filter(c -> c.getId() == customerID).
                        findFirst().
                        orElse(null);
                output.add(new Order(id, customer));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Error fetching 'Orders' from database", e);
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

    public List<Order> completeOrderList(List<Order> ordersInput, List<OrderContent> orderContentsInput) {
        for (Order order : ordersInput) {
            int totalValue = 0;
            List<OrderContent> contentList = orderContentsInput.stream().
                    filter(oc -> oc.getOrder().getId() == order.getId()).
                    toList();
            List<Shoe> shoeList = contentList.stream().
                    map(OrderContent::getShoe).
                    toList();
            List<Integer> numberList = contentList.stream().
                    map(OrderContent::getNumber).
                    toList();
            for (int i = 0; i < shoeList.size(); i++) {
                totalValue += shoeList.get(i).getPrice() * numberList.get(i);
            }
            order.setShoes(shoeList);
            order.setNumberOrdered(numberList);
            order.setTotalValue(totalValue);
        }
        return ordersInput;
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
                customer = new Customer(rs.getInt("id"),
                        rs.getInt("zipcode"),
                        rs.getString("firstname"),
                        rs.getString("lastname"),
                        rs.getString("address"),
                        rs.getString("city"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Error fetching 'Customers' from database in loginCustomer()", e);
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

    public String[] getOrderedTypesForJComboBox(String type) throws SQLException {
        List<String> list = new ArrayList<>();
        String inject = "";
        if (type.equals("brands"))
            inject = "Shoes.brand";
        if (type.equals("colors"))
            inject = "Shoes.color";
        if (type.equals("sizes"))
            inject = "Shoes.shoesize";
        String query = "SELECT DISTINCT " + inject + " " +
                "AS types " +
                "FROM OrderContent " +
                "JOIN Shoes ON OrderContent.shoeID = Shoes.id " +
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

}
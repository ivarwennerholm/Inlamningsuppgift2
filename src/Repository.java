import Models.*;

import java.io.FileInputStream;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class Repository {
    Connection con = null;
    Statement stmt = null;
    PreparedStatement pstmt = null;
    CallableStatement cstmt = null;
    ResultSet rs = null;
    Properties props = new Properties();
    String url, user, pw;
    List<Shoe> shoes = new ArrayList<>();
    List<Customer> customers = new ArrayList<>();
    List<Category> categories = new ArrayList<>();
    List<CategoryMapping> categoryMappings = new ArrayList<>();
    Customer thisCustomer;
    Comparator<String[]> comp = Comparator.comparingInt(arr -> Integer.parseInt(arr[arr.length - 1]));

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

        shoes = getShoes();
        customers = getCustomers();
        categories = getCategories();
        categoryMappings = getCategoryMappings();
    }

    public List<Shoe> getShoes() throws SQLException {
        shoes.clear();
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
                shoes.add(shoe);
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
        return shoes.stream().filter(s -> s.getInventory() > 0).toList();
    }

    public List<Customer> getCustomers() throws SQLException {
        customers.clear();
        try {
            con = DriverManager.getConnection(url, user, pw);
            stmt = con.createStatement();
            rs = stmt.executeQuery("SELECT id, zipcode, firstname, lastname, address, city FROM Customers");
            while (rs.next()) {
                customers.add(new Customer(rs.getInt("id"),
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
        return customers;
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

    public List<Category> getCategories() throws SQLException {
        categories.clear();
        try {
            con = DriverManager.getConnection(url, user, pw);
            stmt = con.createStatement();
            rs = stmt.executeQuery("SELECT * FROM Categories");
            while (rs.next()) {
                categories.add(new Category(rs.getInt("id"), rs.getString("name")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Error fetching 'Categories' from database", e);
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
        Category category = null;
        Shoe shoe = null;
        try {
            con = DriverManager.getConnection(url, user, pw);
            stmt = con.createStatement();
            rs = stmt.executeQuery("SELECT * FROM CategoriesMapping");
            while (rs.next()) {
                for (Category c : categories) {
                    if (c.getId() == rs.getInt("categoryID"))
                        category = c;
                }
                for (Shoe s : shoes) {
                    if (s.getId() == rs.getInt("shoeID"))
                        shoe = s;
                }
                categoryMappings.add(new CategoryMapping(category, shoe));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Error fetching 'CategoriesMapping' from database", e);
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
        List<String[]> report = new ArrayList<>();
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

    public List<String[]> getListQueryOne(String type, String target) throws SQLException {
        List<String> list = new ArrayList<>();
        StringBuilder str = new StringBuilder();
        List<String[]> report = new ArrayList<>();
        String query = "SELECT CONCAT(Customers.firstname, \" \", Customers.lastname) AS name, Shoes.brand, Shoes.color, Shoes.shoesize " +
                "FROM Customers " +
                "JOIN Orders ON Customers.id = Orders.customerID " +
                "JOIN OrderContent ON Orders.id = OrderContent.orderID " +
                "JOIN Shoes ON OrderContent.shoeID = Shoes.id;";
        try {
            con = DriverManager.getConnection(url, user, pw);
            stmt = con.createStatement();

            rs = stmt.executeQuery(query);
            while (rs.next()) {
                str.append("name=" + rs.getString("name") + ":");
                str.append("brand=" + rs.getString("brand") + ":");
                str.append("color=" + rs.getString("color") + ":");
                str.append("size=" + rs.getInt("shoesize"));
                list.add(str.toString());
                str.setLength(0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Error fetching list for query #1", e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (type.equals("brand")) {
            list = list.stream()
                    .filter(s -> s.contains("brand=" + target))
                    .toList();
        } else if (type.equals("color")) {
            list = list.stream()
                    .filter(s -> s.contains("color=" + target))
                    .toList();
        } else {
            list = list.stream()
                    .filter(s -> s.contains("size=" + target))
                    .toList();
        }
        list = list.stream().
                map(s -> s.replace("name=", "")).
                map(s -> s.replace("brand=", "")).
                map(s -> s.replace("color=", "")).
                map(s -> s.replace("size=", "")).
                toList();
        for (String s : list)
            report.add(s.split(":"));
        return report;
    }

    public List<String[]> getListQueryTwo() throws SQLException {
        List<String> list = new ArrayList<>();
        List<String[]> report = new ArrayList<>();
        StringBuilder str = new StringBuilder();
        String query = "SELECT Customers.id AS customerID, " +
                "CONCAT(Customers.firstname, \" \", Customers.lastname) AS name, Orders.id AS orderID " +
                "FROM Customers " +
                "LEFT JOIN Orders ON Customers.id = Orders.customerID " +
                "ORDER BY name;";
        try {
            con = DriverManager.getConnection(url, user, pw);
            stmt = con.createStatement();
            rs = stmt.executeQuery(query);
            while (rs.next()) {
                str.append("customerID=" + rs.getString("customerID"));
                str.append(" name=" + rs.getString("name"));
                str.append(" orderID=" + rs.getString("orderID"));
                list.add(str.toString());
                str.setLength(0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Error fetching list for query #2", e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        for (Customer cust : customers) {
            long occ = list.stream().filter(s -> s.contains("customerID=" + cust.getId())).count();
            report.add(new String[]{cust.getFirstName() + " " + cust.getLastName(), String.valueOf(occ)});
        }
        report = getSortedList(comp, report);
        for (String[] arr : report)
            arr[arr.length - 1] = arr[arr.length - 1] + "st";
        return report;
    }

    public List<String[]> getListQueryThree() throws SQLException {
        List<String> list = new ArrayList<>();
        StringBuilder str = new StringBuilder();
        List<String[]> report = new ArrayList<>();
        String query = "SELECT Customers.id AS customerID, OrderContent.num AS num, Shoes.price as price " +
                "FROM Customers " +
                "JOIN Orders ON Customers.id = Orders.customerID " +
                "JOIN OrderContent ON Orders.id = OrderContent.orderID " +
                "JOIN Shoes ON OrderContent.shoeID = Shoes.id " +
                "ORDER BY customerID;";
        try {
            con = DriverManager.getConnection(url, user, pw);
            stmt = con.createStatement();
            rs = stmt.executeQuery(query);
            while (rs.next()) {
                str.append("customerID=" + rs.getInt("customerID"));
                str.append(" sum=" + rs.getInt("num") * rs.getInt("price"));
                list.add(str.toString());
                str.setLength(0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Error fetching list for query #3", e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        for (Customer cust : customers) {
            int total = list.stream().
                    filter(s -> s.contains("customerID=" + cust.getId() + " ")).
                    map(s -> s.substring(s.indexOf("sum=") + 4)).
                    mapToInt(Integer::valueOf).sum();
            report.add(new String[]{cust.getFirstName() + " " + cust.getLastName(), String.valueOf(total)});
        }
        report = getSortedList(comp, report);
        for (String[] arr : report)
            arr[arr.length - 1] = arr[arr.length - 1] + "kr";
        return report;
    }

    public List<String[]> getListQueryFour() throws SQLException {
        List<String> list = new ArrayList<>();
        StringBuilder str = new StringBuilder();
        List<String[]> report = new ArrayList<>();
        Set<String> cities = customers.stream().map(Customer::getCity).collect(Collectors.toSet());
        String query = "SELECT Customers.city AS city, OrderContent.num AS num, Shoes.price as price " +
                "FROM Customers " +
                "JOIN Orders ON Customers.id = Orders.customerID " +
                "JOIN OrderContent ON Orders.id = OrderContent.orderID " +
                "JOIN Shoes ON OrderContent.shoeID = Shoes.id " +
                "ORDER BY city;";
        try {
            con = DriverManager.getConnection(url, user, pw);
            stmt = con.createStatement();
            rs = stmt.executeQuery(query);
            while (rs.next()) {
                str.append("city=" + rs.getString("city"));
                str.append(" sum=" + rs.getInt("num") * rs.getInt("price"));
                list.add(str.toString());
                str.setLength(0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Error fetching list for query #4", e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        for (String city : cities) {
            int total = list.stream().
                    filter(s -> s.contains("city=" + city + " ")).
                    map(s -> s.substring(s.indexOf("sum=") + 4)).
                    mapToInt(Integer::valueOf).sum();
            report.add(new String[]{city, String.valueOf(total)});
        }
        report = getSortedList(comp, report);
        for (String[] arr : report)
            arr[arr.length - 1] = arr[arr.length - 1] + "kr";
        return report;
    }

    public List<String[]> getListQueryFive() throws SQLException {
        List<String> list = new ArrayList<>();
        StringBuilder str = new StringBuilder();
        List<String[]> report = new ArrayList<>();
        String query = "SELECT Shoes.id as id, Shoes.brand as brand, Shoes.color AS color, " +
                "Shoes.shoesize AS size, OrderContent.num AS num " +
                "FROM Shoes " +
                "JOIN OrderContent ON Shoes.id = OrderContent.shoeID " +
                " ORDER BY id;";
        try {
            con = DriverManager.getConnection(url, user, pw);
            stmt = con.createStatement();
            rs = stmt.executeQuery(query);
            while (rs.next()) {
                str.append("id=" + rs.getString("id"));
                str.append(" brand=" + rs.getString("brand"));
                str.append(" color=" + rs.getString("color"));
                str.append(" size=" + rs.getString("size"));
                str.append(" num=" + rs.getInt("num"));
                list.add(str.toString());
                str.setLength(0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Error fetching list for query #5", e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        for (Shoe shoe : shoes) {
            int total = list.stream().
                    filter(s -> s.contains("id=" + shoe.getId() + " ")).
                    map(s -> s.substring(s.indexOf("num=") + 4)).
                    mapToInt(Integer::valueOf).sum();
            report.add(new String[]{shoe.getBrand(), shoe.getColor(), String.valueOf(shoe.getSize()), String.valueOf(total)});
        }
        report = getSortedList(comp, report);
        for (String[] arr : report)
            arr[arr.length - 1] = arr[arr.length - 1] + "st";
        report.subList(5, report.size()).clear();
        return report;
    }

    List<String[]> getSortedList(Comparator<String[]> comp, List<String[]> list) {
        list.sort(comp.reversed());
        return list;
    }

}
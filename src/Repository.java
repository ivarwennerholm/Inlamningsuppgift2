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
    final Properties props = new Properties();
    final String url, user, pw;
    List<Shoe> shoes = new ArrayList<>();
    List<Customer> customers = new ArrayList<>();
    List<OrderContent> orderContents = new ArrayList<>();
    List<Order> orders = new ArrayList<>();
    List<Category> categories = new ArrayList<>();
    List<CategoryMapping> categoryMappings = new ArrayList<>();
    Customer thisCustomer;
    final Comparator<String[]> comp = Comparator.comparingInt(arr -> Integer.parseInt(arr[arr.length - 1]));

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
        orders = getOrders();
        orderContents = getOrderContents();
        completeOrderList();

        /* TEST
        for (Order order: orders) {
            System.out.println("OrderID: " + order.getId() + ", totalt: " + order.getTotalValue() + "kr");
            for (int i = 0; i < order.getShoes().size(); i++) {
                System.out.println("SkoID :" + order.getShoes().get(i).getId() + ", antal: " + order.getNumberOrdered().get(i) + "st");
            }
        }
        */
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
        return shoes;
        //return shoes.stream().filter(s -> s.getInventory() > 0).toList();
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

    public List<OrderContent> getOrderContents() throws SQLException {
        try {
            con = DriverManager.getConnection(url, user, pw);
            stmt = con.createStatement();
            rs = stmt.executeQuery("SELECT * FROM OrderContent");
            while (rs.next()) {
                int orderID = rs.getInt("orderID");
                int shoeID = rs.getInt("shoeID");
                int number = rs.getInt("num");
                Order order = orders.stream().filter(o -> o.getId() == orderID).findFirst().orElse(null);
                Shoe shoe = shoes.stream().filter(s -> s.getId() == shoeID).findFirst().orElse(null);
                orderContents.add(new OrderContent(order, shoe, number));
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

        /* TEST
        for (OrderContent oc : orderContents)
            System.out.println("OrderID = " + oc.getOrder().getId() + ", ShoeID = " + oc.getShoe().getId() + ", Number = " + oc.getNumber());
        */
        return orderContents;
    }

    public List<Order> getOrders() throws SQLException {
        try {
            con = DriverManager.getConnection(url, user, pw);
            stmt = con.createStatement();
            rs = stmt.executeQuery("SELECT * FROM Orders");
            while (rs.next()) {
                int id = rs.getInt("id");
                int customerID = rs.getInt("customerID");
                Customer customer = customers.stream().filter(c -> c.getId() == customerID).findFirst().orElse(null);
                orders.add(new Order(id, null, customer));
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
        return orders;
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

    public void completeOrderList() {
        for (Order order : orders) {
            int totalValue = 0;
            int orderID = order.getId();
            List<OrderContent> ocList = orderContents.stream().filter(oc -> oc.getOrder().getId() == orderID).toList();
            List<Shoe> shList = ocList.stream().map(oc -> oc.getShoe()).toList();
            List<Integer> noList = ocList.stream().map(oc -> oc.getNumber()).toList();
            for (int i = 0; i < shList.size(); i++) {
                totalValue += shList.get(i).getPrice() * noList.get(i);
            }
            /* TEST
            System.out.println("Order " + order.getId() + " ( " + totalValue + "kr ):");
            for (int i = 0; i < shList.size(); i++) {
                System.out.println("SkoID: " + shList.get(i).getId() + ", antal: " + noList.get(i));
            }
            */

            order.setShoes(shList);
            order.setNumberOrdered(noList);
            order.setTotalValue(totalValue);
        }
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
        List<String[]> report = new ArrayList<>();
        for (Customer cust : customers) {
            long occ = orders.stream().filter(o -> o.getCustomer().getId() == cust.getId()).count();
            report.add(new String[]{cust.getFirstName() + " " + cust.getLastName(), String.valueOf(occ)});
        }
        report = getSortedList(comp, report);
        for (String[] arr : report)
            arr[arr.length - 1] = arr[arr.length - 1] + "st";
        return report;
    }

    public List<String[]> getListQueryThree() throws SQLException {
        List<String[]> report = new ArrayList<>();
        for (Customer cust : customers) {
            int total = orders.stream().
                    filter(o -> o.getCustomer().getId() == cust.getId()).
                    map(o -> o.getTotalValue()).
                    mapToInt(Integer::valueOf).sum();
            report.add(new String[]{cust.getFirstName() + " " + cust.getLastName(), String.valueOf(total)});
        }
        report = getSortedList(comp, report);
        for (String[] arr : report)
            arr[arr.length - 1] = arr[arr.length - 1] + "kr";
        return report;
    }

    public List<String[]> getListQueryFour() throws SQLException {
        List<String[]> report = new ArrayList<>();
        Set<String> cities = customers.stream().map(c -> c.getCity()).collect(Collectors.toSet());
        for (String city : cities) {
            int total = orders.stream().
                    filter(o -> o.getCustomer().getCity().equalsIgnoreCase(city)).
                    map(o -> o.getTotalValue()).
                    mapToInt(Integer::valueOf).sum();
            report.add(new String[]{city, String.valueOf(total)});
        }
        report = getSortedList(comp, report);
        for (String[] arr : report)
            arr[arr.length - 1] = arr[arr.length - 1] + "kr";
        return report;
    }


    public List<String[]> getListQueryFive() throws SQLException {
        List<String[]> report = new ArrayList<>();
        for (Shoe shoe : shoes) {
            int total = orders.stream().
                    filter(o -> o.isShoeInOrder(shoe.getId())).
                    map(o -> o.getOrderedNumberForShoe(shoe.getId())).
                    mapToInt(Integer::valueOf).sum();
            report.add(new String[]{shoe.getBrand(), shoe.getColor(), String.valueOf(shoe.getSize()), String.valueOf(total)});
        }
        report = getSortedList(comp, report);
        for (String[] arr : report)
            arr[arr.length - 1] = arr[arr.length - 1] + "st";
        return report;
    }


        /*
        Integer previousValue = clients.get(id);
        if(previousValue == null) previousValue = 0;
        clients.put(id, previousValue + x);
         */
        /*report = getSortedList(comp, report);
        for (String[] arr : report)
            arr[arr.length - 1] = arr[arr.length - 1] + "st";
        report.subList(5, report.size()).clear();
        return report;

         */

    public List<String[]> getListQueryFiveOLD() throws SQLException {
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
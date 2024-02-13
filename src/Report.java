import Models.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Report {
    final List<Shoe> shoes;
    final List<Customer> customers;
    final List<Order> orders;
    final Comparator<String[]> comp = Comparator.comparingInt(arr -> Integer.parseInt(arr[arr.length - 1]));

    public Report(List<Shoe> shoes, List<Customer> customers, List<Order> orders) {
        this.shoes = shoes;
        this.customers = customers;
        this.orders = orders;
    }

    public List<String[]> getListQueryOne(String type, String target) {
        List<String[]> report = new ArrayList<>();
        Predicate<Order> pred = switch (type) {
            case "brand" -> o -> o.doesOrderContainBrand(target);
            case "color" -> o -> o.doesOrderContainColor(target);
            case "size" -> o -> o.doesOrderContainSize(target);
            default -> null;
        };
        for (Customer cust : customers) {
            if (orders.stream().
                    filter(pred).
                    anyMatch(o -> o.getCustomer().getId() == cust.getId()))
                report.add(new String[]{cust.getFirstName() + " " + cust.getLastName(), cust.getAddress(), String.valueOf(cust.getZipCode()), cust.getCity()});
        }
        return report;
    }

    public List<String[]> getListQueryTwo() {
        List<String[]> report = new ArrayList<>();
        for (Customer cust : customers) {
            long occ = orders.stream().
                    filter(o -> o.getCustomer().getId() == cust.getId()).
                    count();
            report.add(new String[]{cust.getFirstName() + " " + cust.getLastName(), String.valueOf(occ)});
        }
        return addSymbolToLastNumber(getSortedList(comp, report), "st");
    }

    public List<String[]> getListQueryThree() {
        List<String[]> report = new ArrayList<>();
        for (Customer cust : customers) {
            int total = orders.stream().
                    filter(o -> o.getCustomer().getId() == cust.getId()).
                    map(Order::getTotalValue).
                    mapToInt(Integer::valueOf).sum();
            report.add(new String[]{cust.getFirstName() + " " + cust.getLastName(), String.valueOf(total)});
        }
        return addSymbolToLastNumber(getSortedList(comp, report), "kr");
    }

    public List<String[]> getListQueryFour() {
        List<String[]> report = new ArrayList<>();
        Set<String> cities = customers.stream().
                map(Customer::getCity).
                collect(Collectors.toSet());
        for (String city : cities) {
            int total = orders.stream().
                    filter(o -> o.getCustomer().getCity().equalsIgnoreCase(city)).
                    map(Order::getTotalValue).
                    mapToInt(Integer::valueOf).sum();
            report.add(new String[]{city, String.valueOf(total)});
        }
        return addSymbolToLastNumber(getSortedList(comp, report), "kr");
    }

    public List<String[]> getListQueryFive() {
        List<String[]> report = new ArrayList<>();
        for (Shoe shoe : shoes) {
            int total = orders.stream().
                    filter(o -> o.isShoeInOrder(shoe.getId())).
                    map(o -> o.getOrderedNumberForShoe(shoe.getId())).
                    mapToInt(Integer::valueOf).sum();
            report.add(new String[]{shoe.getBrand(), shoe.getColor(), String.valueOf(shoe.getSize()), String.valueOf(total)});
        }
        return addSymbolToLastNumber(getSortedList(comp, report), "par").
                stream().
                limit(5).
                toList();
    }

    List<String[]> getSortedList(Comparator<String[]> comp, List<String[]> list) {
        list.sort(comp.reversed());
        return list;
    }

    List<String[]> addSymbolToLastNumber(List<String[]> list, String type) {
        for (String[] arr : list) {
            if (type.equals("st"))
                arr[arr.length - 1] = arr[arr.length - 1] + " st";
            else if (type.equals("kr"))
                arr[arr.length - 1] = arr[arr.length - 1] + " kr";
            else if (type.equals("par"))
                arr[arr.length - 1] = arr[arr.length - 1] + " par";
        }
        return list;
    }

}
import Models.*;

import java.sql.SQLException;
import java.util.List;

public class Main {
    List<Shoe> shoes;
    List<Category> categories;
    List<CategoryMapping> categoryMappings;
    Customer thisCustomer;
    Repository repo = new Repository();

    public Main() throws Exception {
        try {
            shoes = repo.getShoes();
            categories = repo.getCategories();
            categoryMappings = repo.getCategoryMappings();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /* FOR TESTING
    public void printShoesList() {
        System.out.printf(ANSI_YELLOW + "%-12s%-8s%-10s%-10s%-8s\n", "Märke", "Färg", "Storlek", "Pris", "Lagersaldo" + ANSI_RESET);
        shoes.forEach(e -> System.out.println(e));
        System.out.println();
    }

    public void printCategoriesList() {
        System.out.printf(ANSI_YELLOW + "%-8s%-8s\n", "Id", "Namn" + ANSI_RESET);
        categories.forEach(e -> System.out.println(e));
        System.out.println();
    }

    public void printCategoriesMappingList() {
        System.out.printf(ANSI_YELLOW + "%-14s%-10s\n", "CategoryID", "ShoeID" + ANSI_RESET);
        categoryMappings.forEach(e -> System.out.println(e));
        System.out.println();
    }
    */

    public static void main(String[] args) throws Exception {
        Main main = new Main();
        new Gui(main);
        List<String> test = main.repo.getListQueryOne();
        test = test.stream().filter(s -> s.contains("size=46")).toList();

        ListCleaner listCleaner = list ->
                list.stream()
                        .map(s -> s.replace("firstname=", ""))
                        .map(s -> s.replace("lastname=", ""))
                        .map(s -> s.replace("brand=", ""))
                        .map(s -> s.replace("color=", ""))
                        .map(s -> s.replace("size=", ""))
                        .toList();
        test = listCleaner.apply(test);
        //test = test.stream().map(s -> s.replace("firstname=", "")).map(s -> s.replace("lastname=", "")).toList();
        test.forEach(System.out::println);

    }
}
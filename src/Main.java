import Models.*;

import java.sql.SQLException;
import java.util.List;

public class Main {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_RED = "\u001B[31m";
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

    public static void main(String[] args) throws Exception {
        Main main = new Main();

        Gui gui = new Gui(main);

        //main.printShoesList(); // TEST
        //main.printCategoriesList(); // TEST
        //main.printCategoriesMappingList(); // TEST

    }
}
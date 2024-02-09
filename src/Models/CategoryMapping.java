package Models;

public class CategoryMapping {
    private final Category category;
    private final Shoe shoe;

    public CategoryMapping(Category category, Shoe shoe) {
        this.category = category;
        this.shoe = shoe;
    }

    public Category getCategory() {
        return category;
    }

    public Shoe getShoe() {
        return shoe;
    }
}

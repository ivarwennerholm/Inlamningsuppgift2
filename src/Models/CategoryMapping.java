package Models;

public class CategoryMapping {
    private Category category;
    private Shoe shoe;

    public CategoryMapping(Category category, Shoe shoe) {
        this.category = category;
        this.shoe = shoe;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Shoe getShoe() {
        return shoe;
    }

    public void setShoe(Shoe shoe) {
        this.shoe = shoe;
    }

    @Override
    public String toString() {
        return String.format("%-14s%-10s", category.getId(), shoe.getId());
    }

}

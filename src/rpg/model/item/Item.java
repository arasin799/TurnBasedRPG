package rpg.model.item;

public abstract class Item {

    private String name;
    private String description;
    private int buyPrice;
    private int sellPrice;

    public Item(
            String name,
            String description,
            int buyPrice,
            int sellPrice) {

        this.name = name;
        this.description = description;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
    }

    // -------------------------
    // Getters
    // -------------------------

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getBuyPrice() {
        return buyPrice;
    }

    public int getSellPrice() {
        return sellPrice;
    }

    // -------------------------
    // Display
    // -------------------------

    @Override
    public String toString() {
        return name;
    }
}
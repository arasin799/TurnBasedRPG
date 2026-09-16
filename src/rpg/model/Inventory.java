package rpg.model;

import java.util.ArrayList;

import rpg.model.character.player.Player;
import rpg.model.item.Item;
import rpg.model.item.Usable;

public class Inventory {

    private ArrayList<Item> items;

    public Inventory() {
        items = new ArrayList<>();
    }

    // -------------------------
    // Add / Remove
    // -------------------------

    public void addItem(Item item) {

        if (item != null) {
            items.add(item);
        }
    }

    public boolean removeItem(Item item) {

        if (item == null) {
            return false;
        }

        return items.remove(item);
    }

    // -------------------------
    // Getters
    // -------------------------

    public Item getItem(int index) {

        if (index < 0 || index >= items.size()) {
            return null;
        }

        return items.get(index);
    }

    public ArrayList<Item> getItems() {
        return new ArrayList<>(items);
    }

    public int getItemCount() {
        return items.size();
    }

    public boolean containsItem(Item item) {
        return items.contains(item);
    }

    // -------------------------
    // Use Item
    // -------------------------

    public boolean useItem(Item item, Player player) {

        if (item == null || player == null) {
            return false;
        }

        if (!items.contains(item)) {
            return false;
        }

        if (!(item instanceof Usable)) {
            return false;
        }

        Usable usableItem = (Usable) item;

        boolean usedSuccessfully =
                usableItem.use(player);

        if (usedSuccessfully) {
            items.remove(item);
        }

        return usedSuccessfully;
    }
}
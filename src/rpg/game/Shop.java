package rpg.game;

import java.util.ArrayList;

import rpg.exception.NotEnoughGoldException;
import rpg.model.character.player.Player;
import rpg.model.item.Armor;
import rpg.model.item.ArmorType;
import rpg.model.item.HealthPotion;
import rpg.model.item.Item;
import rpg.model.item.ManaPotion;
import rpg.model.item.SmallHealthPotion;
import rpg.model.item.SmallManaPotion;
import rpg.model.item.Weapon;
import rpg.model.item.WeaponType;
import rpg.model.item.Bomb;


public class Shop {

    private ArrayList<Item> itemsForSale;

    public Shop() {

        itemsForSale = new ArrayList<>();

        createDefaultItems();
    }

    // -------------------------
    // Default Shop Items
    // -------------------------


        private void createDefaultItems() {

        // -------------------------
        // Weapons
        // -------------------------

        itemsForSale.add(
                new Weapon(
                        "Dagger",
                        "A lightweight weapon usable by every class.",
                        100,
                        50,
                        5,
                        WeaponType.DAGGER
                )
        );

        itemsForSale.add(
                new Weapon(
                        "Long Sword",
                        "A powerful sword for Knights.",
                        250,
                        125,
                        10,
                        WeaponType.SWORD
                )
        );

        itemsForSale.add(
                new Weapon(
                        "Bow",
                        "A ranged weapon for Archers.",
                        250,
                        125,
                        10,
                        WeaponType.BOW
                )
        );

        itemsForSale.add(
                new Weapon(
                        "Magic Staff",
                        "A magical weapon for Mages.",
                        250,
                        125,
                        10,
                        WeaponType.STAFF
                )
        );

        // -------------------------
        // Armor
        // -------------------------

        itemsForSale.add(
                new Armor(
                        "Wooden Shield",
                        "A simple wooden shield usable by every class.",
                        120,
                        60,
                        7,
                        ArmorType.WOODEN_SHIELD
                )
        );

        itemsForSale.add(
                new Armor(
                        "Iron Shield",
                        "A heavy shield usable by every class.",
                        280,
                        140,
                        12,
                        ArmorType.IRON_SHIELD
                )
        );

        // -------------------------
        // Consumables
        // -------------------------

        itemsForSale.add(new SmallHealthPotion());
        itemsForSale.add(new HealthPotion());

        itemsForSale.add(new SmallManaPotion());
        itemsForSale.add(new ManaPotion());

        itemsForSale.add(new Bomb());
        }


    // -------------------------
    // Getters
    // -------------------------

    public ArrayList<Item> getItemsForSale() {
        return new ArrayList<>(itemsForSale);
    }

    /** At most one weapon of each kind may be owned at a time. */
    public boolean alreadyOwnsWeapon(Player player, Weapon weapon) {
        if (player == null || weapon == null) return false;
        for (Item owned : player.getInventory().getItems()) {
            if (owned instanceof Weapon
                    && ((Weapon) owned).getWeaponType() == weapon.getWeaponType()) {
                return true;
            }
        }
        return false;
    }

    // -------------------------
    // Buy
    // -------------------------

    public void buyItem(Player player, Item item)
            throws NotEnoughGoldException {

        if (player == null || item == null) {
            return;
        }

        if (!itemsForSale.contains(item)) {
            return;
        }

        if (item instanceof Weapon && alreadyOwnsWeapon(player, (Weapon) item)) {
            throw new IllegalStateException(
                    "You already own one " + item.getName()
                    + ". Sell it first if you want to buy another.");
        }

        if (player.getGold()
                < item.getBuyPrice()) {

            throw new NotEnoughGoldException(
                    "Not enough gold to buy "
                            + item.getName()
                            + "."
            );
        }

        Item purchasedItem =
                createItemCopy(item);

        player.spendGold(
                item.getBuyPrice()
        );

        player.getInventory()
                .addItem(
                        purchasedItem
                );
    }

    // -------------------------
    // Sell
    // -------------------------

    public boolean sellItem(
            Player player,
            Item item) {

        if (player == null || item == null) {
            return false;
        }

        if (!player.getInventory()
                .containsItem(item)) {

            return false;
        }

        // Equipped items cannot be sold.
        if (player.isEquipped(item)) {

            return false;
        }

        boolean removed =
                player.getInventory()
                        .removeItem(item);

        if (!removed) {
            return false;
        }

        player.gainGold(
                item.getSellPrice()
        );

        return true;
    }

    // -------------------------
    // CreateItemCopy
    // -------------------------

    private Item createItemCopy(Item item) {

        if (item instanceof SmallHealthPotion) {

            return new SmallHealthPotion();

        } else if (item instanceof SmallManaPotion) {

            return new SmallManaPotion();

        } else if (item instanceof HealthPotion) {

            return new HealthPotion();

        } else if (item instanceof ManaPotion) {

            return new ManaPotion();
            
        } else if (item instanceof Bomb) {

            return new Bomb();

        } else if (item instanceof Weapon) {

            Weapon weapon =
                    (Weapon) item;

            return new Weapon(
                    weapon.getName(),
                    weapon.getDescription(),
                    weapon.getBuyPrice(),
                    weapon.getSellPrice(),
                    weapon.getAttackBonus(),
                    weapon.getWeaponType()
            );

        } else if (item instanceof Armor) {

            Armor armor =
                    (Armor) item;

            return new Armor(
                    armor.getName(),
                    armor.getDescription(),
                    armor.getBuyPrice(),
                    armor.getSellPrice(),
                    armor.getDefenseBonus(),
                    armor.getArmorType()
            );
        }

        throw new IllegalArgumentException(
                "Unsupported item type: "
                        + item.getClass()
                                .getSimpleName()
        );
    }
}
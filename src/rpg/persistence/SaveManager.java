package rpg.persistence;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import rpg.exception.InvalidSaveException;

import rpg.model.character.player.Player;
import rpg.model.character.player.Knight;
import rpg.model.character.player.Archer;
import rpg.model.character.player.Mage;

import rpg.model.item.Item;
import rpg.model.item.HealthPotion;
import rpg.model.item.SmallHealthPotion;
import rpg.model.item.ManaPotion;
import rpg.model.item.SmallManaPotion;
import rpg.model.item.Armor;
import rpg.model.item.ArmorType;
import rpg.model.item.Weapon;
import rpg.model.item.WeaponType;
import rpg.model.item.Bomb;

public class SaveManager {

    private static final String DEFAULT_SAVE_PATH =
            "data/savegame.txt";

    private SaveManager() {
    }

    // -------------------------
    // Save
    // -------------------------

    public static void saveGame(GameData gameData)
            throws IOException {

        saveGame(gameData, DEFAULT_SAVE_PATH);
    }

    public static void saveGame(
            GameData gameData,
            String filePath)
            throws IOException {

        if (gameData == null
                || gameData.getPlayer() == null) {

            throw new IllegalArgumentException(
                    "Game data cannot be null."
            );
        }

        File file = new File(filePath);

        File parent = file.getParentFile();

        if (parent != null && !parent.exists()) {

            if (!parent.mkdirs()) {
                throw new IOException(
                        "Could not create save directory."
                );
            }
        }

        Player player = gameData.getPlayer();

        ArrayList<Item> items =
                player.getInventory().getItems();

        int weaponIndex =
                findWeaponIndex(player, items);

        int armorIndex =
                findArmorIndex(player, items);

        try (BufferedWriter writer =
                     new BufferedWriter(
                             new FileWriter(file))) {

            writer.write(
                    "CLASS="
                            + player.getClass()
                                    .getSimpleName()
            );
            writer.newLine();

            writer.write(
                    "NAME=" + player.getName()
            );
            writer.newLine();

            writer.write(
                    "LEVEL=" + player.getLevel()
            );
            writer.newLine();

            writer.write(
                    "EXP=" + player.getExperience()
            );
            writer.newLine();

            writer.write(
                    "HP=" + player.getHp()
            );
            writer.newLine();

            writer.write(
                    "MP=" + player.getMp()
            );
            writer.newLine();

            writer.write(
                    "GOLD=" + player.getGold()
            );
            writer.newLine();

            writer.write(
                    "STAGE=" + gameData.getStage()
            );
            writer.newLine();

            writer.write(
                    "EQUIPPED_WEAPON_INDEX="
                            + weaponIndex
            );
            writer.newLine();

            writer.write(
                    "EQUIPPED_ARMOR_INDEX="
                            + armorIndex
            );
            writer.newLine();

            for (Item item : items) {

                writer.write(
                        serializeItem(item)
                );

                writer.newLine();
            }
        }
    }

    // -------------------------
    // Load
    // -------------------------

    public static GameData loadGame()
            throws IOException,
            InvalidSaveException {

        return loadGame(DEFAULT_SAVE_PATH);
    }

    public static GameData loadGame(
            String filePath)
            throws IOException,
            InvalidSaveException {

        String className = null;
        String playerName = null;

        Integer level = null;
        Integer experience = null;
        Integer hp = null;
        Integer mp = null;
        Integer gold = null;
        Integer stage = null;

        int weaponIndex = -1;
        int armorIndex = -1;

        ArrayList<String> itemData =
                new ArrayList<>();

        try (BufferedReader reader =
                     new BufferedReader(
                             new FileReader(filePath))) {

            String line;

            while ((line = reader.readLine())
                    != null) {

                if (line.isBlank()) {
                    continue;
                }

                int separator =
                        line.indexOf('=');

                if (separator == -1) {

                    throw new InvalidSaveException(
                            "Invalid line in save file: "
                                    + line
                    );
                }

                String key =
                        line.substring(
                                0,
                                separator
                        );

                String value =
                        line.substring(
                                separator + 1
                        );

                switch (key) {

                    case "CLASS":
                        className = value;
                        break;

                    case "NAME":
                        playerName = value;
                        break;

                    case "LEVEL":
                        level =
                                parseInteger(
                                        value,
                                        "LEVEL"
                                );
                        break;

                    case "EXP":
                        experience =
                                parseInteger(
                                        value,
                                        "EXP"
                                );
                        break;

                    case "HP":
                        hp =
                                parseInteger(
                                        value,
                                        "HP"
                                );
                        break;

                    case "MP":
                        mp =
                                parseInteger(
                                        value,
                                        "MP"
                                );
                        break;

                    case "GOLD":
                        gold =
                                parseInteger(
                                        value,
                                        "GOLD"
                                );
                        break;

                    case "STAGE":
                        stage =
                                parseInteger(
                                        value,
                                        "STAGE"
                                );
                        break;

                    case "EQUIPPED_WEAPON_INDEX":
                        weaponIndex =
                                parseInteger(
                                        value,
                                        key
                                );
                        break;

                    case "EQUIPPED_ARMOR_INDEX":
                        armorIndex =
                                parseInteger(
                                        value,
                                        key
                                );
                        break;

                    case "ITEM":
                        itemData.add(value);
                        break;

                    default:
                        break;
                }
            }
        }

        // Check required data
        if (className == null
                || playerName == null
                || level == null
                || experience == null
                || hp == null
                || mp == null
                || gold == null
                || stage == null) {

            throw new InvalidSaveException(
                    "Save file is missing required data."
            );
        }

        if (level < 1
                || experience < 0
                || gold < 0
                || stage < 0) {

            throw new InvalidSaveException(
                    "Save file contains invalid values."
            );
        }

        Player player =
                createPlayer(
                        className,
                        playerName
                );

        player.restorePlayerState(
                level,
                experience,
                gold,
                hp,
                mp
        );

        // Restore Inventory
        for (String data : itemData) {

            Item item =
                    deserializeItem(data);

            player.getInventory()
                    .addItem(item);
        }

        // Restore Equipment
        ArrayList<Item> loadedItems =
                player.getInventory()
                        .getItems();

        restoreWeapon(
                player,
                loadedItems,
                weaponIndex
        );

        restoreArmor(
                player,
                loadedItems,
                armorIndex
        );

        return new GameData(
                player,
                stage
        );
    }

    // -------------------------
    // Player Creation
    // -------------------------

    private static Player createPlayer(
            String className,
            String name)
            throws InvalidSaveException {

        switch (className) {

            case "Knight":
                return new Knight(name);

            case "Archer":
                return new Archer(name);

            case "Mage":
                return new Mage(name);

            default:
                throw new InvalidSaveException(
                        "Unknown player class: "
                                + className
                );
        }
    }

    // -------------------------
    // Item Save
    // -------------------------

    private static String serializeItem(Item item)
            throws IOException {

        if (item instanceof SmallHealthPotion) {

            return "SMALL_HEALTH_POTION";

        } else if (item instanceof HealthPotion) {

            return "HEALTH_POTION";

        } else if (item instanceof SmallManaPotion) {

            return "SMALL_MANA_POTION";

        } else if (item instanceof ManaPotion) {

            return "MANA_POTION";

        } else if (item instanceof Bomb) {

            return "BOMB";

        } else if (item instanceof Weapon) {

            Weapon weapon =
                    (Weapon) item;

            return "WEAPON"
                    + "|"
                    + weapon.getName()
                    + "|"
                    + weapon.getDescription()
                    + "|"
                    + weapon.getBuyPrice()
                    + "|"
                    + weapon.getSellPrice()
                    + "|"
                    + weapon.getAttackBonus()
                    + "|"
                    + weapon.getWeaponType()
                            .name();

        } else if (item instanceof Armor) {

            Armor armor =
                    (Armor) item;

            return "ARMOR"
                    + "|"
                    + armor.getName()
                    + "|"
                    + armor.getDescription()
                    + "|"
                    + armor.getBuyPrice()
                    + "|"
                    + armor.getSellPrice()
                    + "|"
                    + armor.getDefenseBonus()
                    + "|"
                    + armor.getArmorType()
                            .name();
        }

        throw new IOException(
                "Unsupported item type: "
                        + item.getClass()
                                .getSimpleName()
        );
    }

    // -------------------------
    // Item Load
    // -------------------------

    private static Item deserializeItem(
            String value)
            throws InvalidSaveException {

        if (value == null
                || value.isEmpty()) {

            throw new InvalidSaveException(
                    "Invalid item data."
            );
        }

        if (value.equals(
                "SMALL_HEALTH_POTION")) {

            return new SmallHealthPotion();
        }

        if (value.equals(
                "HEALTH_POTION")) {

            return new HealthPotion();
        }

        if (value.equals(
                "SMALL_MANA_POTION")) {

            return new SmallManaPotion();
        }

        if (value.equals(
                "MANA_POTION")) {

            return new ManaPotion();
        }

        if (value.equals(
                "BOMB")) {

            return new Bomb();
        }

        String[] parts =
                value.split("\\|");

        if (parts.length == 0) {

            throw new InvalidSaveException(
                    "Invalid item data: "
                            + value
            );
        }

        try {

            if (parts[0].equals("WEAPON")) {

                if (parts.length != 7) {

                    throw new InvalidSaveException(
                            "Invalid weapon data: "
                                    + value
                    );
                }

                String name =
                        parts[1];

                String description =
                        parts[2];

                int buyPrice =
                        Integer.parseInt(
                                parts[3]
                        );

                int sellPrice =
                        Integer.parseInt(
                                parts[4]
                        );

                int attackBonus =
                        Integer.parseInt(
                                parts[5]
                        );

                WeaponType weaponType =
                        WeaponType.valueOf(
                                parts[6]
                        );

                return new Weapon(
                        name,
                        description,
                        buyPrice,
                        sellPrice,
                        attackBonus,
                        weaponType
                );
            }

            if (parts[0].equals("ARMOR")) {

                if (parts.length != 7) {

                    throw new InvalidSaveException(
                            "Invalid armor data: "
                                    + value
                    );
                }

                String name =
                        parts[1];

                String description =
                        parts[2];

                int buyPrice =
                        Integer.parseInt(
                                parts[3]
                        );

                int sellPrice =
                        Integer.parseInt(
                                parts[4]
                        );

                int defenseBonus =
                        Integer.parseInt(
                                parts[5]
                        );

                ArmorType armorType =
                        ArmorType.valueOf(
                                parts[6]
                        );

                return new Armor(
                        name,
                        description,
                        buyPrice,
                        sellPrice,
                        defenseBonus,
                        armorType
                );
            }

        } catch (NumberFormatException e) {

            throw new InvalidSaveException(
                    "Invalid number in item data: "
                            + value,
                    e
            );

        } catch (IllegalArgumentException e) {

            throw new InvalidSaveException(
                    "Invalid item type in save data: "
                            + value,
                    e
            );
        }

        throw new InvalidSaveException(
                "Unknown item data: "
                        + value
        );
    }

    // -------------------------
    // Equipment Save
    // -------------------------

    private static int findWeaponIndex(
            Player player,
            ArrayList<Item> items) {

        for (int i = 0;
             i < items.size();
             i++) {

            if (items.get(i)
                    == player.getEquippedWeapon()) {

                return i;
            }
        }

        return -1;
    }

    private static int findArmorIndex(
            Player player,
            ArrayList<Item> items) {

        for (int i = 0;
             i < items.size();
             i++) {

            if (items.get(i)
                    == player.getEquippedArmor()) {

                return i;
            }
        }

        return -1;
    }

    // -------------------------
    // Equipment Load
    // -------------------------

    private static void restoreWeapon(
            Player player,
            ArrayList<Item> items,
            int index)
            throws InvalidSaveException {

        if (index == -1) {
            return;
        }

        if (index < 0
                || index >= items.size()) {

            throw new InvalidSaveException(
                    "Invalid equipped weapon index."
            );
        }

        Item item = items.get(index);

        if (!(item instanceof Weapon)) {

            throw new InvalidSaveException(
                    "Equipped weapon is not a Weapon."
            );
        }

        boolean equipped =
                player.equipWeapon(
                        (Weapon) item
                );

        if (!equipped) {

            throw new InvalidSaveException(
                    "Player cannot equip saved weapon: "
                            + item.getName()
            );
        }
    }

    private static void restoreArmor(
            Player player,
            ArrayList<Item> items,
            int index)
            throws InvalidSaveException {

        if (index == -1) {
            return;
        }

        if (index < 0
                || index >= items.size()) {

            throw new InvalidSaveException(
                    "Invalid equipped armor index."
            );
        }

        Item item = items.get(index);

        if (!(item instanceof Armor)) {

            throw new InvalidSaveException(
                    "Equipped armor is not Armor."
            );
        }

        boolean equipped =
                player.equipArmor(
                        (Armor) item
                );

        if (!equipped) {

            throw new InvalidSaveException(
                    "Player cannot equip saved armor: "
                            + item.getName()
            );
        }
    }

    // -------------------------
    // Parsing
    // -------------------------

    private static int parseInteger(
            String value,
            String fieldName)
            throws InvalidSaveException {

        try {

            return Integer.parseInt(value);

        } catch (NumberFormatException e) {

            throw new InvalidSaveException(
                    "Invalid number for "
                            + fieldName
                            + ": "
                            + value,
                    e
            );
        }
    }
}
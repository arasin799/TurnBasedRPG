package rpg.gui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;

import rpg.game.Game;
import rpg.gui.GameFrame;
import rpg.gui.GameTheme;
import rpg.model.character.player.Player;
import rpg.model.item.Armor;
import rpg.model.item.Bomb;
import rpg.model.item.HealthPotion;
import rpg.model.item.Item;
import rpg.model.item.ManaPotion;
import rpg.model.item.Potion;
import rpg.model.item.SmallHealthPotion;
import rpg.model.item.SmallManaPotion;
import rpg.model.item.SoulShard;
import rpg.model.item.Usable;
import rpg.model.item.Weapon;

/** Pixel-themed Inventory screen. The existing Inventory/Player classes own item logic. */
public class InventoryPanel extends JPanel {

    private static final Color INNER = new Color(20, 29, 49);
    private static final Color HIGHLIGHT = new Color(46, 59, 88);
    private static final Color SUCCESS = new Color(116, 220, 157);
    private static final Font MONO_12 = new Font("Monospaced", Font.BOLD, 12);

    private final Game game;
    private final GameFrame gameFrame;
    private final Map<String, ImageIcon> icons = new HashMap<>();
    // UI-only counts: the actual Inventory still stores individual Item objects.
    private final Map<Class<?>, Integer> displayStackCounts = new HashMap<>();

    private final DefaultListModel<Item> itemListModel = new DefaultListModel<>();
    private JList<Item> itemList;
    private JLabel countLabel;
    private JLabel selectedItemName;
    private JLabel selectedItemCategory;
    private JLabel largeItemIcon;
    private JTextArea itemInfoArea;
    private JLabel weaponLabel;
    private JLabel armorLabel;
    private JLabel ultimateLabel;
    private JButton useButton;
    private JButton equipButton;

    public InventoryPanel(Game game, GameFrame gameFrame) {
        this.game = game;
        this.gameFrame = gameFrame;
        createLayout();
        registerEvents();
        refreshInventory();
    }

    private JLabel label(String text, int size, Color color) {
        JLabel result = new JLabel(text);
        result.setFont(new Font("Monospaced", Font.BOLD, size));
        result.setForeground(color);
        return result;
    }

    private JPanel panel() {
        JPanel result = new JPanel(new BorderLayout(0, 7));
        result.setBackground(GameTheme.PANEL);
        result.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GameTheme.PANEL_BORDER, 2),
                BorderFactory.createEmptyBorder(10, 11, 10, 11)));
        return result;
    }

    private void createLayout() {
        setLayout(new BorderLayout(0, 10));
        setBackground(GameTheme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(11, 17, 12, 17));

        JPanel title = new JPanel(new BorderLayout());
        title.setOpaque(false);
        title.add(label("INVENTORY  /  EQUIPMENT", 19, GameTheme.GOLD), BorderLayout.WEST);
        countLabel = label("0 ITEMS", 11, GameTheme.MUTED);
        title.add(countLabel, BorderLayout.EAST);
        add(title, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(12, 0));
        body.setOpaque(false);

        // Left: actual Item objects, with pixel icons from assets/item.
        JPanel bag = panel();
        bag.setPreferredSize(new Dimension(252, 200));
        bag.add(label("BAG CONTENTS", 12, GameTheme.GOLD), BorderLayout.NORTH);
        itemList = new JList<>(itemListModel);
        itemList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        itemList.setCellRenderer(new ItemRowRenderer());
        itemList.setFixedCellHeight(46);
        itemList.setBackground(INNER);
        itemList.setForeground(GameTheme.TEXT);
        itemList.setSelectionBackground(HIGHLIGHT);
        itemList.setSelectionForeground(GameTheme.TEXT);
        itemList.setFocusable(true);
        JScrollPane itemScroll = new JScrollPane(itemList);
        itemScroll.setBorder(BorderFactory.createLineBorder(GameTheme.PANEL_BORDER));
        itemScroll.getViewport().setBackground(INNER);
        bag.add(itemScroll, BorderLayout.CENTER);
        body.add(bag, BorderLayout.WEST);

        // Right: selected item, stats, equipped items, ultimate charge.
        JPanel details = panel();
        JPanel detailHeading = new JPanel(new BorderLayout(11, 0));
        detailHeading.setOpaque(false);
        largeItemIcon = new JLabel("?", SwingConstants.CENTER);
        largeItemIcon.setForeground(GameTheme.GOLD);
        largeItemIcon.setFont(new Font("Monospaced", Font.BOLD, 24));
        largeItemIcon.setPreferredSize(new Dimension(56, 54));
        largeItemIcon.setOpaque(true);
        largeItemIcon.setBackground(INNER);
        largeItemIcon.setBorder(BorderFactory.createLineBorder(GameTheme.PANEL_BORDER));
        detailHeading.add(largeItemIcon, BorderLayout.WEST);

        JPanel itemTitle = new JPanel(new GridLayout(2, 1, 0, 3));
        itemTitle.setOpaque(false);
        selectedItemName = label("SELECT AN ITEM", 14, GameTheme.TEXT);
        selectedItemCategory = label("Inspect items in your bag", 10, GameTheme.MUTED);
        itemTitle.add(selectedItemName);
        itemTitle.add(selectedItemCategory);
        detailHeading.add(itemTitle, BorderLayout.CENTER);
        details.add(detailHeading, BorderLayout.NORTH);

        itemInfoArea = new JTextArea("Select an item to see its details.");
        itemInfoArea.setEditable(false);
        itemInfoArea.setLineWrap(true);
        itemInfoArea.setWrapStyleWord(true);
        itemInfoArea.setBackground(INNER);
        itemInfoArea.setForeground(GameTheme.TEXT);
        itemInfoArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        itemInfoArea.setMargin(new java.awt.Insets(7, 10, 7, 10));
        JScrollPane detailsScroll = new JScrollPane(itemInfoArea);
        detailsScroll.setBorder(BorderFactory.createLineBorder(GameTheme.PANEL_BORDER));
        detailsScroll.getViewport().setBackground(INNER);
        detailsScroll.getVerticalScrollBar().setUnitIncrement(14);
        details.add(detailsScroll, BorderLayout.CENTER);

        JPanel equipment = new JPanel(new GridLayout(3, 1, 0, 2));
        equipment.setBackground(GameTheme.PANEL);
        equipment.setBorder(BorderFactory.createEmptyBorder(5, 2, 0, 0));
        weaponLabel = label("WEAPON: NONE", 10, GameTheme.MUTED);
        armorLabel = label("ARMOR: NONE", 10, GameTheme.MUTED);
        ultimateLabel = label("ULTIMATE CHARGE: 0/5", 10, GameTheme.GOLD);
        equipment.add(weaponLabel);
        equipment.add(armorLabel);
        equipment.add(ultimateLabel);
        details.add(equipment, BorderLayout.SOUTH);
        body.add(details, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);

        JPanel actions = new JPanel(new GridLayout(1, 2, 12, 0));
        actions.setOpaque(false);
        actions.setPreferredSize(new Dimension(650, 41));
        useButton = new JButton("USE ITEM");
        equipButton = new JButton("EQUIP");
        styleButton(useButton, true);
        styleButton(equipButton, false);
        actions.add(useButton);
        actions.add(equipButton);
        add(actions, BorderLayout.SOUTH);
    }

    private void styleButton(JButton button, boolean primary) {
        GameTheme.styleButton(button, primary);
        button.setFont(MONO_12);
        button.setPreferredSize(new Dimension(130, 40));
        button.setMaximumSize(new Dimension(300, 40));
    }

    private void registerEvents() {
        itemList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateSelectedItem();
            }
        });
        useButton.addActionListener(e -> useSelectedItem());
        equipButton.addActionListener(e -> equipSelectedItem());
    }

    /** Weapons and armor are individual equipment; consumables share one UI row per type. */
    private boolean isStackableForUi(Item item) {
        return !(item instanceof Weapon) && !(item instanceof Armor);
    }

    private int getDisplayStackCount(Item item) {
        return item != null && isStackableForUi(item)
                ? displayStackCounts.getOrDefault(item.getClass(), 1) : 1;
    }

    public void refreshInventory() {
        Item previousSelection = itemList.getSelectedValue();
        itemListModel.clear();
        displayStackCounts.clear();
        Set<Class<?>> shownTypes = new HashSet<>();
        int totalItems = 0;

        // Display the same order as Shop: weapons, armor, Potions, Bomb.
        // Items not sold by the Shop (such as Soul Shard) appear last.
        // Sort a COPY so the underlying Inventory and Save/Load order stay intact.
        Map<String, Integer> shopOrder = new HashMap<>();
        int rank = 0;
        for (Item shopItem : game.getShop().getItemsForSale()) {
            shopOrder.putIfAbsent(shopItem.getName(), rank++);
        }
        ArrayList<Item> sortedItems = game.getPlayer().getInventory().getItems();
        sortedItems.sort((first, second) -> {
            int firstRank = shopOrder.getOrDefault(first.getName(), Integer.MAX_VALUE);
            int secondRank = shopOrder.getOrDefault(second.getName(), Integer.MAX_VALUE);
            if (firstRank != secondRank) {
                return Integer.compare(firstRank, secondRank);
            }
            return first.getName().compareToIgnoreCase(second.getName());
        });

        // First item of each consumable class represents the whole stack.
        // Equipment always has its own row, even if the names are identical.
        for (Item item : sortedItems) {
            totalItems++;
            if (isStackableForUi(item)) {
                Class<?> type = item.getClass();
                displayStackCounts.put(type, displayStackCounts.getOrDefault(type, 0) + 1);
                if (shownTypes.add(type)) {
                    itemListModel.addElement(item);
                }
            } else {
                itemListModel.addElement(item);
            }
        }
        countLabel.setText(totalItems + " ITEMS");
        updateEquipmentInfo();

        // After using a potion, the representative object may have been removed.
        // Select another instance of that SAME consumable type if any remain.
        if (previousSelection != null) {
            int fallbackIndex = -1;
            for (int i = 0; i < itemListModel.size(); i++) {
                Item visible = itemListModel.get(i);
                if (visible == previousSelection) {
                    fallbackIndex = i;
                    break;
                }
                if (isStackableForUi(previousSelection)
                        && visible.getClass() == previousSelection.getClass()) {
                    fallbackIndex = i;
                }
            }
            if (fallbackIndex >= 0) {
                itemList.setSelectedIndex(fallbackIndex);
            }
        }
        itemList.repaint();
        updateSelectedItem();
    }

    private void updateSelectedItem() {
        Item item = itemList.getSelectedValue();
        if (item == null) {
            selectedItemName.setText("SELECT AN ITEM");
            selectedItemCategory.setText("Inspect items in your bag");
            largeItemIcon.setIcon(null);
            largeItemIcon.setText("?");
            itemInfoArea.setText("Choose an item from the list on the left.\n\n"
                    + "You can use Potions and Soul Shards here, or equip weapons and armor.");
            useButton.setEnabled(false);
            equipButton.setEnabled(false);
            equipButton.setText("EQUIP");
            return;
        }

        Player player = game.getPlayer();
        int stackCount = getDisplayStackCount(item);
        selectedItemName.setText(item.getName().toUpperCase()
                + (stackCount > 1 ? " x" + stackCount : ""));
        selectedItemCategory.setText(item instanceof Weapon
                && player.getClassWeaponDamageBonus((Weapon) item) > 0
                ? "WEAPON / +7 CLASS EXTRA DMG" : itemCategory(item));
        ImageIcon picture = iconFor(item, 52);
        largeItemIcon.setIcon(picture);
        largeItemIcon.setText(picture == null ? "?" : "");

        StringBuilder info = new StringBuilder();
        info.append(item.getDescription());
        if (item instanceof Weapon) {
            Weapon weapon = (Weapon) item;
            info.append("\n\nEQUIPPED ATK: +")
                    .append(player.getWeaponAttackBonus(weapon));
            int classBonus = player.getClassWeaponDamageBonus(weapon);
            if (classBonus > 0) {
                info.append("\nCLASS MATCH: +").append(classBonus)
                        .append(" EXTRA DMG WHEN EQUIPPED");
            } else if (weapon.getWeaponType() == rpg.model.item.WeaponType.DAGGER) {
                info.append("\nUNIVERSAL WEAPON - NO EXTRA DMG");
            } else {
                info.append("\nOFF-CLASS: +7 ATK, NO EXTRA DMG");
            }
        } else if (item instanceof Armor) {
            info.append("\n\nDEFENSE BONUS: +")
                    .append(((Armor) item).getDefenseBonus());
        } else if (item instanceof Potion) {
            info.append("\n\nRESTORE AMOUNT: ")
                    .append(((Potion) item).getRestoreAmount());
        } else if (item instanceof SoulShard) {
            info.append("\n\nULTIMATE CHARGE: ")
                    .append(player.getUltimateCharge())
                    .append("/")
                    .append(player.getMaxUltimateCharge());
        } else if (item instanceof Bomb) {
            info.append("\n\nUSABLE IN BATTLE ONLY");
        }
        if (!(item instanceof SoulShard)) {
            info.append("\n\nBUY: ").append(item.getBuyPrice())
                    .append(" GOLD    SELL: ").append(item.getSellPrice()).append(" GOLD");
        } else {
            info.append("\n\nMONSTER DROP - CANNOT BE BOUGHT OR SOLD");
        }
        itemInfoArea.setText(info.toString());
        itemInfoArea.setCaretPosition(0);

        boolean useAvailable = item instanceof Usable;
        if (item instanceof SoulShard) {
            useAvailable = player.getUltimateCharge() < player.getMaxUltimateCharge();
        }
        useButton.setEnabled(useAvailable);

        if (item instanceof Weapon) {
            equipButton.setEnabled(true);
            equipButton.setText(item == player.getEquippedWeapon() ? "UNEQUIP" : "EQUIP");
        } else if (item instanceof Armor) {
            equipButton.setEnabled(true);
            equipButton.setText(item == player.getEquippedArmor() ? "UNEQUIP" : "EQUIP");
        } else {
            equipButton.setEnabled(false);
            equipButton.setText("EQUIP");
        }
    }

    private String itemCategory(Item item) {
        if (item instanceof SoulShard) return "RARE DROP  /  ULTIMATE CHARGE";
        if (item instanceof Weapon) return "WEAPON  /  EQUIPMENT";
        if (item instanceof Armor) return "ARMOR  /  EQUIPMENT";
        if (item instanceof Potion) return "CONSUMABLE  /  POTION";
        if (item instanceof Bomb) return "COMBAT ITEM  /  BOMB";
        return "ITEM";
    }

    private void useSelectedItem() {
        Item item = itemList.getSelectedValue();
        if (item == null) return;
        Player player = game.getPlayer();
        if (!player.getInventory().useItem(item, player)) {
            JOptionPane.showMessageDialog(this,
                    "This item cannot be used right now.", "Use Item",
                    JOptionPane.INFORMATION_MESSAGE);
            updateSelectedItem();
            return;
        }
        JOptionPane.showMessageDialog(this, item.getName() + " used successfully.",
                "Use Item", JOptionPane.INFORMATION_MESSAGE);
        refreshInventory();
        gameFrame.refreshGameInfo();
    }

    private void equipSelectedItem() {
        Item item = itemList.getSelectedValue();
        if (item == null) return;
        Player player = game.getPlayer();
        String message = null;
        if (item instanceof Weapon) {
            Weapon weapon = (Weapon) item;
            if (weapon == player.getEquippedWeapon()) {
                player.unequipWeapon();
                message = weapon.getName() + " unequipped.";
            } else if (player.equipWeapon(weapon)) {
                int extra = player.getEquippedClassWeaponDamageBonus();
                message = weapon.getName() + " equipped."
                        + (extra > 0 ? "\nClass weapon bonus: +" + extra + " Extra Damage!" : "");
            }
        } else if (item instanceof Armor) {
            Armor armor = (Armor) item;
            if (armor == player.getEquippedArmor()) {
                player.unequipArmor();
                message = armor.getName() + " unequipped.";
            } else if (player.equipArmor(armor)) {
                message = armor.getName() + " equipped.";
            }
        }
        if (message == null) {
            JOptionPane.showMessageDialog(this,
                    player.getClass().getSimpleName() + " cannot equip " + item.getName() + ".",
                    "Cannot Equip", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JOptionPane.showMessageDialog(this, message, "Equipment", JOptionPane.INFORMATION_MESSAGE);
        updateEquipmentInfo();
        updateSelectedItem();
        itemList.repaint();
        gameFrame.refreshGameInfo();
    }

    private void updateEquipmentInfo() {
        Player player = game.getPlayer();
        Weapon weapon = player.getEquippedWeapon();
        Armor armor = player.getEquippedArmor();
        weaponLabel.setText(weapon == null ? "WEAPON: NONE"
                : "WEAPON: " + weapon.getName() + "  (+" + player.getWeaponAttackBonus(weapon)
                + " ATK" + (player.getEquippedClassWeaponDamageBonus() > 0
                ? ", +7 DMG" : "") + ")");
        armorLabel.setText(armor == null ? "ARMOR: NONE"
                : "ARMOR: " + armor.getName() + "  (+" + armor.getDefenseBonus() + " DEF)");
        ultimateLabel.setText("ULTIMATE CHARGE: " + player.getUltimateCharge()
                + "/" + player.getMaxUltimateCharge());
    }

    /** Load existing 16x16 icons; preserve sharp pixel edges when scaling. */
    private ImageIcon iconFor(Item item, int targetSize) {
        String filename = spriteFilename(item);
        String key = filename + "@" + targetSize;
        if (icons.containsKey(key)) return icons.get(key);
        BufferedImage original = readItemSprite(filename);
        if (original == null) {
            icons.put(key, null);
            return null;
        }
        BufferedImage scaled = new BufferedImage(targetSize, targetSize,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = scaled.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        graphics.drawImage(original, 0, 0, targetSize, targetSize, null);
        graphics.dispose();
        ImageIcon icon = new ImageIcon(scaled);
        icons.put(key, icon);
        return icon;
    }

    private String spriteFilename(Item item) {
        if (item instanceof SmallHealthPotion) return "health_potion_small.png";
        if (item instanceof HealthPotion) return "health_potion_big.png";
        if (item instanceof SmallManaPotion) return "mana_potion_small.png";
        if (item instanceof ManaPotion) return "mana_potion_big.png";
        if (item instanceof SoulShard) return "soul_shard.png";
        if (item instanceof Bomb) return "bomb.png";
        // Shop's weapons and armor are regular Item objects with canonical names.
        return item.getName().toLowerCase(java.util.Locale.ROOT)
                .replace(' ', '_').replaceAll("[^a-z0-9_.]", "") + ".png";
    }

    private BufferedImage readItemSprite(String filename) {
        String path = "assets/item/" + filename;
        try (InputStream stream = InventoryPanel.class.getClassLoader()
                .getResourceAsStream(path)) {
            if (stream != null) return ImageIO.read(stream);
        } catch (IOException ignored) {
            // File-system fallback for VS Code running from the project directory.
        }
        File file = new File(path);
        if (!file.isFile()) file = new File("TurnBasedRPG", path);
        if (!file.isFile()) return null;
        try {
            return ImageIO.read(file);
        } catch (IOException ignored) {
            return null;
        }
    }

    /** Render items as pixel cards without changing the JList's underlying Item objects. */
    private final class ItemRowRenderer extends JPanel implements ListCellRenderer<Item> {
        private final JLabel itemIcon = new JLabel("?", SwingConstants.CENTER);
        private final JLabel itemName = label("", 12, GameTheme.TEXT);
        private final JLabel itemMeta = label("", 10, GameTheme.MUTED);

        ItemRowRenderer() {
            super(new BorderLayout(8, 0));
            setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
            itemIcon.setPreferredSize(new Dimension(35, 35));
            itemIcon.setForeground(GameTheme.GOLD);
            JPanel text = new JPanel(new GridLayout(2, 1, 0, 1));
            text.setOpaque(false);
            text.add(itemName);
            text.add(itemMeta);
            add(itemIcon, BorderLayout.WEST);
            add(text, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Item> list,
                Item value, int index, boolean isSelected, boolean cellHasFocus) {
            Player player = game.getPlayer();
            boolean equipped = value == player.getEquippedWeapon()
                    || value == player.getEquippedArmor();
            setBackground(isSelected ? HIGHLIGHT : INNER);
            int stackCount = getDisplayStackCount(value);
            itemName.setText(value.getName()
                    + (stackCount > 1 ? " x" + stackCount : ""));
            itemName.setForeground(isSelected ? GameTheme.GOLD : GameTheme.TEXT);
            itemMeta.setText(equipped ? "EQUIPPED" : itemCategory(value).split("  /  ")[0]);
            itemMeta.setForeground(equipped ? SUCCESS : GameTheme.MUTED);
            ImageIcon icon = iconFor(value, 34);
            itemIcon.setIcon(icon);
            itemIcon.setText(icon == null ? "?" : "");
            return this;
        }
    }
}

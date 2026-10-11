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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
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

import rpg.exception.NotEnoughGoldException;
import rpg.game.Game;
import rpg.game.Shop;
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
import rpg.model.item.Weapon;

/** Pixel-RPG shop. Item purchases and sales still use the existing Shop class. */
public class ShopPanel extends JPanel {

    private static final Color INNER = new Color(20, 29, 49);
    private static final Color HIGHLIGHT = new Color(46, 59, 88);
    private static final Color SUCCESS = new Color(116, 220, 157);
    private static final Font MONO_12 = new Font("Monospaced", Font.BOLD, 12);

    private final Game game;
    private final GameFrame gameFrame;
    private final Shop shop;
    private final Map<String, ImageIcon> icons = new HashMap<>();

    private final DefaultListModel<Item> buyModel = new DefaultListModel<>();
    private final DefaultListModel<Item> sellModel = new DefaultListModel<>();
    private JList<Item> buyList;
    private JList<Item> sellList;
    private JLabel goldLabel;
    private JLabel bagCountLabel;
    private JLabel selectedNameLabel;
    private JLabel selectedCategoryLabel;
    private JLabel selectedIconLabel;
    private JTextArea itemInfoArea;
    private JButton buyButton;
    private JButton sellButton;
    private boolean changingSelection;

    public ShopPanel(Game game, GameFrame gameFrame) {
        this.game = game;
        this.gameFrame = gameFrame;
        this.shop = game.getShop();
        createLayout();
        registerEvents();
        refreshShop();
    }

    private JLabel label(String text, int size, Color color) {
        JLabel result = new JLabel(text);
        result.setFont(new Font("Monospaced", Font.BOLD, size));
        result.setForeground(color);
        return result;
    }

    private JPanel framedPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setBackground(GameTheme.PANEL);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GameTheme.PANEL_BORDER, 2),
                BorderFactory.createEmptyBorder(7, 9, 7, 9)));
        return panel;
    }

    private JList<Item> createItemList(DefaultListModel<Item> model, boolean buying) {
        JList<Item> list = new JList<>(model);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer(new ShopRowRenderer(buying));
        list.setFixedCellHeight(46);
        list.setBackground(INNER);
        list.setForeground(GameTheme.TEXT);
        list.setSelectionBackground(HIGHLIGHT);
        return list;
    }

    private JScrollPane listScroll(JList<Item> list) {
        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(BorderFactory.createLineBorder(GameTheme.PANEL_BORDER));
        scroll.getViewport().setBackground(INNER);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        return scroll;
    }

    private void createLayout() {
        setLayout(new BorderLayout(0, 8));
        setBackground(GameTheme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(9, 17, 10, 17));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(label("TRADING POST  /  SHOP", 19, GameTheme.GOLD), BorderLayout.WEST);
        goldLabel = label("GOLD: 0", 12, GameTheme.GOLD);
        header.add(goldLabel, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        JPanel middle = new JPanel(new BorderLayout(0, 8));
        middle.setOpaque(false);

        JPanel inventories = new JPanel(new GridLayout(1, 2, 12, 0));
        inventories.setOpaque(false);

        JPanel buyingPanel = framedPanel();
        buyingPanel.add(label("BUY  /  MERCHANT STOCK", 11, GameTheme.GOLD), BorderLayout.NORTH);
        buyList = createItemList(buyModel, true);
        buyingPanel.add(listScroll(buyList), BorderLayout.CENTER);
        inventories.add(buyingPanel);

        JPanel sellingPanel = framedPanel();
        JPanel sellHeading = new JPanel(new BorderLayout());
        sellHeading.setOpaque(false);
        sellHeading.add(label("SELL  /  YOUR BAG", 11, GameTheme.GOLD), BorderLayout.WEST);
        bagCountLabel = label("0", 10, GameTheme.MUTED);
        sellHeading.add(bagCountLabel, BorderLayout.EAST);
        sellingPanel.add(sellHeading, BorderLayout.NORTH);
        sellList = createItemList(sellModel, false);
        sellingPanel.add(listScroll(sellList), BorderLayout.CENTER);
        inventories.add(sellingPanel);
        middle.add(inventories, BorderLayout.CENTER);

        // Full-width details area: readable descriptions and prices.
        JPanel itemDetails = framedPanel();
        itemDetails.setPreferredSize(new Dimension(675, 111));
        JPanel iconHolder = new JPanel(new BorderLayout());
        iconHolder.setBackground(INNER);
        iconHolder.setBorder(BorderFactory.createLineBorder(GameTheme.PANEL_BORDER));
        iconHolder.setPreferredSize(new Dimension(66, 66));
        selectedIconLabel = new JLabel("?", SwingConstants.CENTER);
        selectedIconLabel.setFont(new Font("Monospaced", Font.BOLD, 22));
        selectedIconLabel.setForeground(GameTheme.GOLD);
        iconHolder.add(selectedIconLabel, BorderLayout.CENTER);
        itemDetails.add(iconHolder, BorderLayout.WEST);

        JPanel textPanel = new JPanel(new BorderLayout(0, 3));
        textPanel.setOpaque(false);
        JPanel detailsHeading = new JPanel(new BorderLayout(6, 0));
        detailsHeading.setOpaque(false);
        selectedNameLabel = label("SELECT AN ITEM", 12, GameTheme.TEXT);
        selectedCategoryLabel = label("Browse items to inspect prices", 10, GameTheme.MUTED);
        detailsHeading.add(selectedNameLabel, BorderLayout.WEST);
        detailsHeading.add(selectedCategoryLabel, BorderLayout.EAST);
        textPanel.add(detailsHeading, BorderLayout.NORTH);

        itemInfoArea = new JTextArea();
        itemInfoArea.setEditable(false);
        itemInfoArea.setLineWrap(true);
        itemInfoArea.setWrapStyleWord(true);
        itemInfoArea.setBackground(INNER);
        itemInfoArea.setForeground(GameTheme.TEXT);
        itemInfoArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        itemInfoArea.setMargin(new java.awt.Insets(4, 7, 4, 7));
        JScrollPane detailsScroll = new JScrollPane(itemInfoArea);
        detailsScroll.setBorder(BorderFactory.createLineBorder(GameTheme.PANEL_BORDER));
        detailsScroll.getViewport().setBackground(INNER);
        textPanel.add(detailsScroll, BorderLayout.CENTER);
        itemDetails.add(textPanel, BorderLayout.CENTER);
        middle.add(itemDetails, BorderLayout.SOUTH);
        add(middle, BorderLayout.CENTER);

        JPanel actions = new JPanel(new GridLayout(1, 2, 12, 0));
        actions.setOpaque(false);
        actions.setPreferredSize(new Dimension(660, 39));
        buyButton = new JButton("BUY ITEM");
        sellButton = new JButton("SELL ITEM");
        styleButton(buyButton, true);
        styleButton(sellButton, false);
        actions.add(buyButton);
        actions.add(sellButton);
        add(actions, BorderLayout.SOUTH);
    }

    private void styleButton(JButton button, boolean primary) {
        GameTheme.styleButton(button, primary);
        button.setFont(MONO_12);
        button.setPreferredSize(new Dimension(130, 39));
    }

    private void registerEvents() {
        buyList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || changingSelection) return;
            Item item = buyList.getSelectedValue();
            if (item != null) {
                changingSelection = true;
                sellList.clearSelection();
                changingSelection = false;
                updateSelectedItem(item, true);
            } else if (sellList.isSelectionEmpty()) {
                showEmptySelection();
            }
        });
        sellList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || changingSelection) return;
            Item item = sellList.getSelectedValue();
            if (item != null) {
                changingSelection = true;
                buyList.clearSelection();
                changingSelection = false;
                updateSelectedItem(item, false);
            } else if (buyList.isSelectionEmpty()) {
                showEmptySelection();
            }
        });
        buyButton.addActionListener(e -> buySelectedItem());
        sellButton.addActionListener(e -> sellSelectedItem());
    }

    /** Refresh after purchases/sales without altering GameFrame's navigation. */
    public void refreshShop() {
        Item oldBuySelection = buyList.getSelectedValue();
        Item oldSellSelection = sellList.getSelectedValue();
        changingSelection = true;
        buyModel.clear();
        sellModel.clear();
        for (Item item : shop.getItemsForSale()) buyModel.addElement(item);
        for (Item item : game.getPlayer().getInventory().getItems()) sellModel.addElement(item);
        bagCountLabel.setText(sellModel.size() + " ITEMS");
        goldLabel.setText("GOLD: " + game.getPlayer().getGold());
        buyList.clearSelection();
        sellList.clearSelection();
        changingSelection = false;

        if (oldBuySelection != null) {
            selectByIdentity(buyList, buyModel, oldBuySelection);
        } else if (oldSellSelection != null) {
            selectByIdentity(sellList, sellModel, oldSellSelection);
        } else {
            showEmptySelection();
        }
        // The item that was sold may no longer exist. Never retain stale details.
        if (buyList.isSelectionEmpty() && sellList.isSelectionEmpty()) showEmptySelection();
        buyList.repaint();
        sellList.repaint();
    }

    private void selectByIdentity(JList<Item> list, DefaultListModel<Item> model, Item old) {
        for (int i = 0; i < model.size(); i++) {
            if (model.get(i) == old) {
                list.setSelectedIndex(i);
                return;
            }
        }
    }

    private void showEmptySelection() {
        selectedNameLabel.setText("SELECT AN ITEM");
        selectedCategoryLabel.setText("BUY OR SELL");
        selectedIconLabel.setIcon(null);
        selectedIconLabel.setText("?");
        itemInfoArea.setText("Select an item above to see its description, equipment bonuses, "
                + "and Gold price.\nSoul Shards cannot be sold.");
        buyButton.setEnabled(false);
        sellButton.setEnabled(false);
    }

    private void updateSelectedItem(Item item, boolean buying) {
        if (item == null) {
            showEmptySelection();
            return;
        }
        Player player = game.getPlayer();
        selectedNameLabel.setText(item.getName().toUpperCase(Locale.ROOT));
        selectedCategoryLabel.setText((buying ? "BUY" : "SELL") + " / "
                + (item instanceof Weapon && player.getClassWeaponDamageBonus((Weapon) item) > 0
                ? "CLASS BONUS +7 DMG" : category(item)));
        ImageIcon picture = iconFor(item, 59);
        selectedIconLabel.setIcon(picture);
        selectedIconLabel.setText(picture == null ? "?" : "");

        StringBuilder info = new StringBuilder(item.getDescription());
        if (item instanceof Weapon) {
            Weapon weapon = (Weapon) item;
            info.append("\nEQUIPPED ATK: +" + player.getWeaponAttackBonus(weapon));
            int classBonus = player.getClassWeaponDamageBonus(weapon);
            if (classBonus > 0) {
                info.append("\nCLASS MATCH: +" + classBonus + " EXTRA DMG WHEN EQUIPPED");
            } else if (weapon.getWeaponType() == rpg.model.item.WeaponType.DAGGER) {
                info.append("\nUNIVERSAL WEAPON - NO EXTRA DMG");
            } else {
                info.append("\nOFF-CLASS: +7 ATK, NO EXTRA DMG");
            }
        } else if (item instanceof Armor) {
            info.append("\n+" + ((Armor) item).getDefenseBonus() + " DEF");
        } else if (item instanceof Potion) {
            info.append("\nRESTORES " + ((Potion) item).getRestoreAmount()
                    + (item instanceof SmallManaPotion || item instanceof ManaPotion ? " MP" : " HP"));
        } else if (item instanceof Bomb) {
            info.append("\nUSABLE IN BATTLE");
        }
        if (item instanceof SoulShard) {
            info.append("\nMONSTER DROP ONLY - NOT FOR SALE");
        } else {
            info.append("\nBUY: ").append(item.getBuyPrice())
                    .append(" GOLD     SELL: ").append(item.getSellPrice()).append(" GOLD");
        }
        if (!buying && player.isEquipped(item)) {
            info.append("\nUNEQUIP IN INVENTORY BEFORE SELLING");
        }
        itemInfoArea.setText(info.toString());
        itemInfoArea.setCaretPosition(0);

        boolean alreadyOwned = buying && item instanceof Weapon
                && shop.alreadyOwnsWeapon(player, (Weapon) item);
        if (alreadyOwned) {
            itemInfoArea.append("\nALREADY OWNED: ONE WEAPON PER TYPE");
        }
        buyButton.setEnabled(buying && !alreadyOwned
                && player.getGold() >= item.getBuyPrice());
        sellButton.setEnabled(!buying && item.getSellPrice() > 0
                && !(item instanceof SoulShard)
                && !player.isEquipped(item));
    }

    private String category(Item item) {
        if (item instanceof SoulShard) return "SOUL SHARD";
        if (item instanceof Weapon) return "WEAPON";
        if (item instanceof Armor) return "ARMOR";
        if (item instanceof Potion) return "POTION";
        if (item instanceof Bomb) return "BOMB";
        return "ITEM";
    }

    private void buySelectedItem() {
        Item item = buyList.getSelectedValue();
        if (item == null) return;
        try {
            shop.buyItem(game.getPlayer(), item);
            JOptionPane.showMessageDialog(this, "Purchased " + item.getName() + ".",
                    "Shop", JOptionPane.INFORMATION_MESSAGE);
            refreshShop();
            gameFrame.refreshGameInfo();
        } catch (NotEnoughGoldException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                    "Not Enough Gold", JOptionPane.WARNING_MESSAGE);
        } catch (IllegalStateException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                    "Already Owned", JOptionPane.INFORMATION_MESSAGE);
            refreshShop();
        }
    }

    private void sellSelectedItem() {
        Item item = sellList.getSelectedValue();
        if (item == null) return;
        Player player = game.getPlayer();
        if (item instanceof SoulShard || item.getSellPrice() <= 0) {
            JOptionPane.showMessageDialog(this, "Soul Shards cannot be sold.",
                    "Cannot Sell", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (player.isEquipped(item)) {
            JOptionPane.showMessageDialog(this,
                    "Unequip this item in Inventory before selling.",
                    "Cannot Sell", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int answer = JOptionPane.showConfirmDialog(this,
                "Sell " + item.getName() + " for " + item.getSellPrice() + " Gold?",
                "Sell Item", JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) return;
        if (!shop.sellItem(player, item)) {
            JOptionPane.showMessageDialog(this, "Could not sell the item.",
                    "Shop", JOptionPane.ERROR_MESSAGE);
            return;
        }
        JOptionPane.showMessageDialog(this,
                item.getName() + " sold for " + item.getSellPrice() + " Gold.",
                "Shop", JOptionPane.INFORMATION_MESSAGE);
        refreshShop();
        gameFrame.refreshGameInfo();
    }

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
        Graphics2D g = scaled.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(original, 0, 0, targetSize, targetSize, null);
        g.dispose();
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
        return item.getName().toLowerCase(Locale.ROOT).replace(' ', '_')
                .replaceAll("[^a-z0-9_.]", "") + ".png";
    }

    private BufferedImage readItemSprite(String filename) {
        String path = "assets/item/" + filename;
        try (InputStream in = ShopPanel.class.getClassLoader().getResourceAsStream(path)) {
            if (in != null) return ImageIO.read(in);
        } catch (IOException ignored) {
            // File-system fallback below.
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

    private final class ShopRowRenderer extends JPanel implements ListCellRenderer<Item> {
        private final boolean buying;
        private final JLabel spriteLabel = new JLabel("?", SwingConstants.CENTER);
        private final JLabel nameLabel = label("", 11, GameTheme.TEXT);
        private final JLabel priceLabel = label("", 10, GameTheme.MUTED);

        ShopRowRenderer(boolean buying) {
            super(new BorderLayout(8, 0));
            this.buying = buying;
            setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 6));
            spriteLabel.setPreferredSize(new Dimension(34, 34));
            spriteLabel.setForeground(GameTheme.GOLD);
            JPanel text = new JPanel(new GridLayout(2, 1, 0, 1));
            text.setOpaque(false);
            text.add(nameLabel);
            text.add(priceLabel);
            add(spriteLabel, BorderLayout.WEST);
            add(text, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Item> list,
                Item value, int index, boolean isSelected, boolean cellHasFocus) {
            setBackground(isSelected ? HIGHLIGHT : INNER);
            nameLabel.setText(value.getName());
            nameLabel.setForeground(isSelected ? GameTheme.GOLD : GameTheme.TEXT);
            boolean equipped = game.getPlayer().isEquipped(value);
            if (buying) {
                boolean owned = value instanceof Weapon
                        && shop.alreadyOwnsWeapon(game.getPlayer(), (Weapon) value);
                priceLabel.setText(owned ? "OWNED / MAX 1" : "BUY  "
                        + value.getBuyPrice() + " GOLD");
                priceLabel.setForeground(owned ? SUCCESS
                        : value.getBuyPrice() > game.getPlayer().getGold()
                        ? new Color(229, 142, 139) : GameTheme.MUTED);
            } else if (equipped) {
                priceLabel.setText("EQUIPPED / CANNOT SELL");
                priceLabel.setForeground(SUCCESS);
            } else if (value instanceof SoulShard || value.getSellPrice() <= 0) {
                priceLabel.setText("NOT FOR SALE");
                priceLabel.setForeground(GameTheme.MUTED);
            } else {
                priceLabel.setText("SELL  " + value.getSellPrice() + " GOLD");
                priceLabel.setForeground(GameTheme.MUTED);
            }
            ImageIcon icon = iconFor(value, 32);
            spriteLabel.setIcon(icon);
            spriteLabel.setText(icon == null ? "?" : "");
            return this;
        }
    }
}

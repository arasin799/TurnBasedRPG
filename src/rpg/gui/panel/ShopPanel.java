package rpg.gui.panel;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;

import rpg.exception.NotEnoughGoldException;
import rpg.game.Game;
import rpg.game.Shop;
import rpg.gui.GameFrame;
import rpg.model.character.player.Player;
import rpg.model.item.Armor;
import rpg.model.item.Item;
import rpg.model.item.Potion;
import rpg.model.item.Weapon;

public class ShopPanel extends JPanel {

    private Game game;
    private GameFrame gameFrame;
    private Shop shop;

    private JLabel goldLabel;

    private DefaultListModel<Item> buyListModel;
    private DefaultListModel<Item> sellListModel;

    private JList<Item> buyList;
    private JList<Item> sellList;

    private JTextArea itemInfoArea;

    private JButton buyButton;
    private JButton sellButton;
    private JButton backButton;

    public ShopPanel(
            Game game,
            GameFrame gameFrame) {

        this.game = game;
        this.gameFrame = gameFrame;
        this.shop = game.getShop();

        createComponents();
        createLayout();
        registerEvents();

        refreshShop();
    }

    // -------------------------
    // Components
    // -------------------------

    private void createComponents() {

        goldLabel =
                new JLabel();

        buyListModel =
                new DefaultListModel<>();

        sellListModel =
                new DefaultListModel<>();

        buyList =
                new JList<>(buyListModel);

        sellList =
                new JList<>(sellListModel);

        buyList.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        sellList.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        itemInfoArea =
                new JTextArea();

        itemInfoArea.setEditable(false);
        itemInfoArea.setLineWrap(true);
        itemInfoArea.setWrapStyleWord(true);

        buyButton =
                new JButton("Buy");

        sellButton =
                new JButton("Sell");

        backButton =
                new JButton("Back");

        buyButton.setEnabled(false);
        sellButton.setEnabled(false);
    }

    // -------------------------
    // Layout
    // -------------------------

    private void createLayout() {

        setLayout(
                new BorderLayout(
                        15,
                        15
                )
        );

        setBorder(
                BorderFactory.createEmptyBorder(
                        15,
                        20,
                        15,
                        20
                )
        );

        // Gold
        JPanel topPanel =
                new JPanel(
                        new BorderLayout()
                );

        topPanel.setBorder(
                BorderFactory.createTitledBorder(
                        "Shop"
                )
        );

        topPanel.add(
                goldLabel,
                BorderLayout.WEST
        );

        // Lists
        JPanel listPanel =
                new JPanel(
                        new GridLayout(
                                1,
                                2,
                                15,
                                0
                        )
                );

        JScrollPane buyScrollPane =
                new JScrollPane(
                        buyList
                );

        buyScrollPane.setBorder(
                BorderFactory.createTitledBorder(
                        "Items For Sale"
                )
        );

        JScrollPane sellScrollPane =
                new JScrollPane(
                        sellList
                );

        sellScrollPane.setBorder(
                BorderFactory.createTitledBorder(
                        "Your Items"
                )
        );

        listPanel.add(
                buyScrollPane
        );

        listPanel.add(
                sellScrollPane
        );

        // Item Info
        JScrollPane infoScrollPane =
                new JScrollPane(
                        itemInfoArea
                );

        infoScrollPane.setBorder(
                BorderFactory.createTitledBorder(
                        "Item Information"
                )
        );

        JPanel centerPanel =
                new JPanel(
                        new GridLayout(
                                2,
                                1,
                                10,
                                10
                        )
                );

        centerPanel.add(
                listPanel
        );

        centerPanel.add(
                infoScrollPane
        );

        // Buttons
        JPanel buttonPanel =
                new JPanel(
                        new GridLayout(
                                1,
                                3,
                                10,
                                0
                        )
                );

        buttonPanel.add(
                buyButton
        );

        buttonPanel.add(
                sellButton
        );

        buttonPanel.add(
                backButton
        );

        add(
                topPanel,
                BorderLayout.NORTH
        );

        add(
                centerPanel,
                BorderLayout.CENTER
        );

        add(
                buttonPanel,
                BorderLayout.SOUTH
        );
    }

    // -------------------------
    // Events
    // -------------------------

    private void registerEvents() {

        buyList.addListSelectionListener(
                e -> {

                    if (!e.getValueIsAdjusting()) {

                        sellList.clearSelection();

                        updateSelectedItem(
                                buyList.getSelectedValue()
                        );
                    }
                }
        );

        sellList.addListSelectionListener(
                e -> {

                    if (!e.getValueIsAdjusting()) {

                        buyList.clearSelection();

                        updateSelectedItem(
                                sellList.getSelectedValue()
                        );
                    }
                }
        );

        buyButton.addActionListener(
                e -> buySelectedItem()
        );

        sellButton.addActionListener(
                e -> sellSelectedItem()
        );

        backButton.addActionListener(
                e -> gameFrame.showHub()
        );
    }

    // -------------------------
    // Refresh
    // -------------------------

    public void refreshShop() {

        Player player =
                game.getPlayer();

        goldLabel.setText(
                "Gold: "
                        + player.getGold()
        );

        buyListModel.clear();

        for (Item item :
                shop.getItemsForSale()) {

            buyListModel.addElement(
                    item
            );
        }

        sellListModel.clear();

        for (Item item :
                player.getInventory()
                        .getItems()) {

            sellListModel.addElement(
                    item
            );
        }

        buyList.clearSelection();
        sellList.clearSelection();

        itemInfoArea.setText(
                "Select an item."
        );

        buyButton.setEnabled(false);
        sellButton.setEnabled(false);
    }

    // -------------------------
    // Selected Item
    // -------------------------

    private void updateSelectedItem(
            Item item) {

        if (item == null) {
            return;
        }

        StringBuilder info =
                new StringBuilder();

        info.append(
                "Name: "
                        + item.getName()
        );

        info.append(
                "\n\nDescription:\n"
                        + item.getDescription()
        );

        info.append(
                "\n\nBuy Price: "
                        + item.getBuyPrice()
        );

        info.append(
                "\nSell Price: "
                        + item.getSellPrice()
        );

        if (item instanceof Weapon) {

            Weapon weapon =
                    (Weapon) item;

            info.append(
                    "\n\nAttack Bonus: +"
                            + weapon.getAttackBonus()
            );

        } else if (item instanceof Armor) {

            Armor armor =
                    (Armor) item;

            info.append(
                    "\n\nDefense Bonus: +"
                            + armor.getDefenseBonus()
            );

        } else if (item instanceof Potion) {

            Potion potion =
                    (Potion) item;

            info.append(
                    "\n\nRestore Amount: "
                            + potion.getRestoreAmount()
            );
        }

        itemInfoArea.setText(
                info.toString()
        );

        boolean buying =
                buyList.getSelectedValue()
                        != null;

        buyButton.setEnabled(
                buying
        );

        sellButton.setEnabled(
                !buying
        );
    }

    // -------------------------
    // Buy
    // -------------------------

    private void buySelectedItem() {

        Item item =
                buyList.getSelectedValue();

        if (item == null) {
            return;
        }

        try {

            shop.buyItem(
                    game.getPlayer(),
                    item
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Purchased "
                            + item.getName()
                            + ".",
                    "Shop",
                    JOptionPane.INFORMATION_MESSAGE
            );

            refreshShop();

            gameFrame.refreshGameInfo();

        } catch (NotEnoughGoldException e) {

            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage(),
                    "Not Enough Gold",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }

    // -------------------------
    // Sell
    // -------------------------

    private void sellSelectedItem() {

        Item item =
                sellList.getSelectedValue();

        if (item == null) {
            return;
        }

        Player player =
                game.getPlayer();

        if (item == player.getEquippedWeapon()
                || item == player.getEquippedArmor()) {

            JOptionPane.showMessageDialog(
                    this,
                    "This item is currently equipped.\n"
                            + "Unequip it before selling.",
                    "Cannot Sell",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        int choice =
                JOptionPane.showConfirmDialog(
                        this,
                        "Sell "
                                + item.getName()
                                + " for "
                                + item.getSellPrice()
                                + " Gold?",
                        "Sell Item",
                        JOptionPane.YES_NO_OPTION
                );

        if (choice
                != JOptionPane.YES_OPTION) {

            return;
        }

        boolean sold =
                shop.sellItem(
                        game.getPlayer(),
                        item
                );

        if (!sold) {

            JOptionPane.showMessageDialog(
                    this,
                    "Could not sell the item.",
                    "Shop",
                    JOptionPane.ERROR_MESSAGE
            );

            return;
        }

        JOptionPane.showMessageDialog(
                this,
                item.getName()
                        + " sold for "
                        + item.getSellPrice()
                        + " Gold.",
                "Shop",
                JOptionPane.INFORMATION_MESSAGE
        );

        refreshShop();

        gameFrame.refreshGameInfo();
    }
}
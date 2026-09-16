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

import rpg.game.Game;
import rpg.gui.GameFrame;
import rpg.model.character.player.Player;
import rpg.model.item.Armor;
import rpg.model.item.Item;
import rpg.model.item.Potion;
import rpg.model.item.Usable;
import rpg.model.item.Weapon;

public class InventoryPanel extends JPanel {

    private Game game;
    private GameFrame gameFrame;

    private DefaultListModel<Item> itemListModel;
    private JList<Item> itemList;

    private JTextArea itemInfoArea;

    private JLabel weaponLabel;
    private JLabel armorLabel;

    private JButton useButton;
    private JButton equipButton;
    private JButton backButton;

    public InventoryPanel(
            Game game,
            GameFrame gameFrame) {

        this.game = game;
        this.gameFrame = gameFrame;

        createComponents();
        createLayout();
        registerEvents();

        refreshInventory();
    }

    // -------------------------
    // Components
    // -------------------------

    private void createComponents() {

        itemListModel =
                new DefaultListModel<>();

        itemList =
                new JList<>(itemListModel);

        itemList.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        itemInfoArea =
                new JTextArea();

        itemInfoArea.setEditable(false);
        itemInfoArea.setLineWrap(true);
        itemInfoArea.setWrapStyleWord(true);

        weaponLabel = new JLabel();
        armorLabel = new JLabel();

        useButton =
                new JButton("Use");

        equipButton =
                new JButton("Equip");

        backButton =
                new JButton("Back");

        useButton.setEnabled(false);
        equipButton.setEnabled(false);
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

        // -------------------------
        // Item List
        // -------------------------

        JScrollPane itemScrollPane =
                new JScrollPane(itemList);

        itemScrollPane.setBorder(
                BorderFactory.createTitledBorder(
                        "Inventory"
                )
        );

        // -------------------------
        // Item Information
        // -------------------------

        JPanel rightPanel =
                new JPanel(
                        new BorderLayout(
                                10,
                                10
                        )
                );

        JScrollPane infoScrollPane =
                new JScrollPane(
                        itemInfoArea
                );

        infoScrollPane.setBorder(
                BorderFactory.createTitledBorder(
                        "Item Information"
                )
        );

        // -------------------------
        // Equipment
        // -------------------------

        JPanel equipmentPanel =
                new JPanel(
                        new GridLayout(
                                2,
                                1,
                                5,
                                5
                        )
                );

        equipmentPanel.setBorder(
                BorderFactory.createTitledBorder(
                        "Equipment"
                )
        );

        equipmentPanel.add(weaponLabel);
        equipmentPanel.add(armorLabel);

        rightPanel.add(
                infoScrollPane,
                BorderLayout.CENTER
        );

        rightPanel.add(
                equipmentPanel,
                BorderLayout.SOUTH
        );

        // -------------------------
        // Buttons
        // -------------------------

        JPanel buttonPanel =
                new JPanel(
                        new GridLayout(
                                1,
                                3,
                                10,
                                0
                        )
                );

        buttonPanel.add(useButton);
        buttonPanel.add(equipButton);
        buttonPanel.add(backButton);

        add(
                itemScrollPane,
                BorderLayout.WEST
        );

        add(
                rightPanel,
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

        itemList.addListSelectionListener(
                e -> {

                    if (!e.getValueIsAdjusting()) {
                        updateSelectedItem();
                    }
                }
        );

        useButton.addActionListener(
                e -> useSelectedItem()
        );

        equipButton.addActionListener(
                e -> equipSelectedItem()
        );

        backButton.addActionListener(
                e -> gameFrame.showHub()
        );
    }

    // -------------------------
    // Refresh
    // -------------------------

    public void refreshInventory() {

        itemListModel.clear();

        Player player =
                game.getPlayer();

        for (Item item :
                player.getInventory()
                        .getItems()) {

            itemListModel.addElement(item);
        }

        updateEquipmentInfo();

        itemInfoArea.setText(
                "Select an item to view its information."
        );

        useButton.setEnabled(false);
        equipButton.setEnabled(false);
    }

    // -------------------------
    // Item Selection
    // -------------------------

    private void updateSelectedItem() {

        Item item =
                itemList.getSelectedValue();

        if (item == null) {

            itemInfoArea.setText(
                    "Select an item to view its information."
            );

            useButton.setEnabled(false);
            equipButton.setEnabled(false);

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

        useButton.setEnabled(
                item instanceof Usable
        );

        Player player =
                game.getPlayer();

        if (item instanceof Weapon) {

            equipButton.setEnabled(true);

            if (item == player.getEquippedWeapon()) {

                equipButton.setText(
                        "Unequip"
                );

            } else {

                equipButton.setText(
                        "Equip"
                );
            }

        } else if (item instanceof Armor) {

            equipButton.setEnabled(true);

            if (item == player.getEquippedArmor()) {

                equipButton.setText(
                        "Unequip"
                );

            } else {

                equipButton.setText(
                        "Equip"
                );
            }

        } else {

            equipButton.setEnabled(false);

            equipButton.setText(
                    "Equip"
            );
        }
    }

    // -------------------------
    // Use Item
    // -------------------------

    private void useSelectedItem() {

        Item item =
                itemList.getSelectedValue();

        if (item == null) {
            return;
        }

        Player player =
                game.getPlayer();

        boolean used =
                player.getInventory()
                        .useItem(
                                item,
                                player
                        );

        if (!used) {

            JOptionPane.showMessageDialog(
                    this,
                    "This item cannot be used right now.",
                    "Use Item",
                    JOptionPane.INFORMATION_MESSAGE
            );

            return;
        }

        JOptionPane.showMessageDialog(
                this,
                item.getName()
                        + " used successfully.",
                "Use Item",
                JOptionPane.INFORMATION_MESSAGE
        );

        refreshInventory();

        gameFrame.refreshGameInfo();
    }

    // -------------------------
    // Equip Item
    // -------------------------

    private void equipSelectedItem() {

        Item item =
                itemList.getSelectedValue();

        if (item == null) {
            return;
        }

        Player player =
                game.getPlayer();

        // -------------------------
        // Weapon
        // -------------------------

        if (item instanceof Weapon) {

            Weapon weapon =
                    (Weapon) item;

            if (weapon
                    == player.getEquippedWeapon()) {

                player.unequipWeapon();

                JOptionPane.showMessageDialog(
                        this,
                        weapon.getName()
                                + " unequipped.",
                        "Equipment",
                        JOptionPane.INFORMATION_MESSAGE
                );

            } else {

                boolean equipped =
                    player.equipWeapon(
                            weapon
                    );

            if (!equipped) {

                JOptionPane.showMessageDialog(
                        this,
                        player.getClass()
                                .getSimpleName()
                                + " cannot equip "
                                + weapon.getName()
                                + ".",
                        "Cannot Equip",
                        JOptionPane.WARNING_MESSAGE
                );

                return;
            }

            JOptionPane.showMessageDialog(
                    this,
                    weapon.getName()
                            + " equipped.",
                    "Equipment",
                    JOptionPane.INFORMATION_MESSAGE
            );
            }

        // -------------------------
        // Armor
        // -------------------------

        } else if (item instanceof Armor) {

            Armor armor =
                    (Armor) item;

            if (armor
                    == player.getEquippedArmor()) {

                player.unequipArmor();

                JOptionPane.showMessageDialog(
                        this,
                        armor.getName()
                                + " unequipped.",
                        "Equipment",
                        JOptionPane.INFORMATION_MESSAGE
                );

            } else {

                boolean equipped =
                    player.equipArmor(
                            armor
                    );

            if (!equipped) {

                JOptionPane.showMessageDialog(
                        this,
                        player.getClass()
                                .getSimpleName()
                                + " cannot equip "
                                + armor.getName()
                                + ".",
                        "Cannot Equip",
                        JOptionPane.WARNING_MESSAGE
                );

                return;
            }

            JOptionPane.showMessageDialog(
                    this,
                    armor.getName()
                            + " equipped.",
                    "Equipment",
                    JOptionPane.INFORMATION_MESSAGE
            );

                JOptionPane.showMessageDialog(
                        this,
                        armor.getName()
                                + " equipped.",
                        "Equipment",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        }

        updateEquipmentInfo();

        updateSelectedItem();

        gameFrame.refreshGameInfo();
    }

    // -------------------------
    // Equipment Information
    // -------------------------

    private void updateEquipmentInfo() {

        Player player =
                game.getPlayer();

        if (player.getEquippedWeapon()
                == null) {

            weaponLabel.setText(
                    "Weapon: None"
            );

        } else {

            weaponLabel.setText(
                    "Weapon: "
                            + player.getEquippedWeapon()
                                    .getName()
                            + " (+"
                            + player.getEquippedWeapon()
                                    .getAttackBonus()
                            + " ATK)"
            );
        }

        if (player.getEquippedArmor()
                == null) {

            armorLabel.setText(
                    "Armor: None"
            );

        } else {

            armorLabel.setText(
                    "Armor: "
                            + player.getEquippedArmor()
                                    .getName()
                            + " (+"
                            + player.getEquippedArmor()
                                    .getDefenseBonus()
                            + " DEF)"
            );
        }
    }
}
package rpg.gui.panel;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import java.util.ArrayList;

import rpg.model.item.Item;
import rpg.model.item.Usable;
import rpg.game.BattleManager;
import rpg.game.Game;
import rpg.gui.GameFrame;
import rpg.model.character.monster.Monster;
import rpg.model.character.player.Player;
import rpg.model.item.BattleUsable;

public class BattlePanel extends JPanel {

    private Game game;
    private GameFrame gameFrame;
    private BattleManager battle;

    private JLabel playerNameLabel;
    private JLabel playerHpLabel;
    private JLabel playerMpLabel;

    private JLabel monsterNameLabel;
    private JLabel monsterHpLabel;

    private JTextArea battleLog;

    private JButton attackButton;
    private JButton skillButton;
    private JButton itemButton;
    private JButton escapeButton;

    public BattlePanel(
            Game game,
            GameFrame gameFrame,
            BattleManager battle) {

        this.game = game;
        this.gameFrame = gameFrame;
        this.battle = battle;

        createComponents();
        createLayout();
        registerEvents();

        refreshBattleInfo();

        addBattleLog(
                "Battle started!\n"
                        + game.getPlayer().getName()
                        + " VS "
                        + battle.getMonster().getName()
        );
    }

    // -------------------------
    // Components
    // -------------------------

    private void createComponents() {

        playerNameLabel =
                new JLabel(
                        "",
                        SwingConstants.CENTER
                );

        playerHpLabel =
                new JLabel(
                        "",
                        SwingConstants.CENTER
                );

        playerMpLabel =
                new JLabel(
                        "",
                        SwingConstants.CENTER
                );

        monsterNameLabel =
                new JLabel(
                        "",
                        SwingConstants.CENTER
                );

        monsterHpLabel =
                new JLabel(
                        "",
                        SwingConstants.CENTER
                );

        battleLog =
                new JTextArea();

        battleLog.setEditable(false);
        battleLog.setLineWrap(true);
        battleLog.setWrapStyleWord(true);

        attackButton =
                new JButton("Attack");

        skillButton =
                new JButton();

        itemButton =
                new JButton("Use Item");

        escapeButton =
                new JButton("Escape");

        itemButton =
                new JButton("Use Item");
    }

    // -------------------------
    // Use Item
    // -------------------------

    private void useItem() {

        if (battle.isBattleOver()) {
            return;
        }

        Player player =
                battle.getPlayer();

        ArrayList<Item> usableItems =
                new ArrayList<>();

        for (Item item :
                player.getInventory()
                        .getItems()) {

            if (item instanceof Usable
                    || item instanceof BattleUsable) {

                usableItems.add(item);
            }
        }

        if (usableItems.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "You do not have any usable items.",
                    "Use Item",
                    JOptionPane.INFORMATION_MESSAGE
            );

            return;
        }

        Item selectedItem =
                (Item) JOptionPane.showInputDialog(
                        this,
                        "Choose an item:",
                        "Use Item",
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        usableItems.toArray(),
                        usableItems.get(0)
                );

        if (selectedItem == null) {
            return;
        }

        String result =
                battle.playerUseItem(
                        selectedItem
                );

        addBattleLog(result);

        refreshBattleInfo();

        checkBattleResult();
    }

    // -------------------------
    // Layout
    // -------------------------

    private void createLayout() {

        setLayout(
                new BorderLayout(
                        10,
                        10
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
        // Character Status
        // -------------------------

        JPanel statusPanel =
                new JPanel(
                        new GridLayout(
                                1,
                                2,
                                20,
                                0
                        )
                );

        JPanel playerPanel =
                new JPanel(
                        new GridLayout(
                                3,
                                1,
                                5,
                                5
                        )
                );

        playerPanel.setBorder(
                BorderFactory.createTitledBorder(
                        "Player"
                )
        );

        playerNameLabel.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        18
                )
        );

        playerPanel.add(playerNameLabel);
        playerPanel.add(playerHpLabel);
        playerPanel.add(playerMpLabel);

        JPanel monsterPanel =
                new JPanel(
                        new GridLayout(
                                2,
                                1,
                                5,
                                5
                        )
                );

        monsterPanel.setBorder(
                BorderFactory.createTitledBorder(
                        "Enemy"
                )
        );

        monsterNameLabel.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        18
                )
        );

        monsterPanel.add(monsterNameLabel);
        monsterPanel.add(monsterHpLabel);

        statusPanel.add(playerPanel);
        statusPanel.add(monsterPanel);

        // -------------------------
        // Battle Log
        // -------------------------

        JScrollPane logScrollPane =
                new JScrollPane(
                        battleLog
                );

        logScrollPane.setBorder(
                BorderFactory.createTitledBorder(
                        "Battle Log"
                )
        );

        // -------------------------
        // Action Buttons
        // -------------------------

        JPanel actionPanel =
                new JPanel(
                        new GridLayout(
                                1,
                                4,
                                10,
                                0
                        )
                );

        actionPanel.add(attackButton);
        actionPanel.add(skillButton);
        actionPanel.add(itemButton);
        actionPanel.add(escapeButton);

        add(
                statusPanel,
                BorderLayout.NORTH
        );

        add(
                logScrollPane,
                BorderLayout.CENTER
        );

        add(
                actionPanel,
                BorderLayout.SOUTH
        );
    }

    // -------------------------
    // Events
    // -------------------------

    private void registerEvents() {

        attackButton.addActionListener(
                e -> attack()
        );

        skillButton.addActionListener(
                e -> useSkill()
        );

        itemButton.addActionListener(
                e -> useItem()
        );

        escapeButton.addActionListener(
                e -> escape()
        );
    }

    // -------------------------
    // Attack
    // -------------------------

    private void attack() {

        if (battle.isBattleOver()) {
            return;
        }

        String result =
                battle.playerAttack();

        addBattleLog(result);

        refreshBattleInfo();

        checkBattleResult();
    }

    // -------------------------
    // Skill
    // -------------------------

    private void useSkill() {

        if (battle.isBattleOver()) {
            return;
        }

        String result =
                battle.playerUseSkill();

        addBattleLog(result);

        refreshBattleInfo();

        checkBattleResult();
    }

    // -------------------------
    // Escape
    // -------------------------

    private void escape() {

        if (battle.isBattleOver()) {
            return;
        }

        String result =
                battle.escape();

        addBattleLog(result);

        refreshBattleInfo();

        if (battle.hasEscaped()) {

            JOptionPane.showMessageDialog(
                    this,
                    "You escaped from the battle.",
                    "Escape",
                    JOptionPane.INFORMATION_MESSAGE
            );

            gameFrame.finishCurrentBattle();
        }
    }

    // -------------------------
    // Battle Result
    // -------------------------

    private void checkBattleResult() {

        if (!battle.isBattleOver()) {
            return;
        }

        disableBattleButtons();

        if (battle.isVictory()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Victory!\n"
                            + battle.getMonster()
                                    .getName()
                            + " has been defeated.",
                    "Victory",
                    JOptionPane.INFORMATION_MESSAGE
            );

            gameFrame.finishCurrentBattle();

        } else if (battle.isDefeat()) {

            JOptionPane.showMessageDialog(
                    this,
                    "You have been defeated.",
                    "Defeat",
                    JOptionPane.ERROR_MESSAGE
            );

            gameFrame.finishCurrentBattle();
        }
    }

    // -------------------------
    // Refresh
    // -------------------------

    private void refreshBattleInfo() {

        Player player =
                battle.getPlayer();

        Monster monster =
                battle.getMonster();

        playerNameLabel.setText(
                player.getName()
                        + " ("
                        + player.getClass()
                                .getSimpleName()
                        + ")"
        );

        playerHpLabel.setText(
                "HP: "
                        + player.getHp()
                        + "/"
                        + player.getMaxHp()
        );

        playerMpLabel.setText(
                "MP: "
                        + player.getMp()
                        + "/"
                        + player.getMaxMp()
        );

        monsterNameLabel.setText(
                monster.getName()
        );

        monsterHpLabel.setText(
                "HP: "
                        + monster.getHp()
                        + "/"
                        + monster.getMaxHp()
        );

        skillButton.setText(
                player.getSkillName()
        );
    }

    // -------------------------
    // Battle Log
    // -------------------------

    private void addBattleLog(
            String message) {

        if (!battleLog.getText().isEmpty()) {

            battleLog.append(
                    "\n\n"
            );
        }

        battleLog.append(message);

        battleLog.setCaretPosition(
                battleLog.getDocument()
                        .getLength()
        );
    }

    private void disableBattleButtons() {

        attackButton.setEnabled(false);
        skillButton.setEnabled(false);
        itemButton.setEnabled(false);
        escapeButton.setEnabled(false);
    }
}
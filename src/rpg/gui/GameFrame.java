package rpg.gui;

import java.awt.CardLayout;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import rpg.game.Game;
import rpg.model.character.player.Player;
import rpg.game.BattleManager;
import rpg.gui.panel.BattlePanel;
import rpg.gui.panel.InventoryPanel;
import rpg.gui.panel.ShopPanel;
import rpg.gui.panel.GameResultPanel;
import rpg.gui.panel.StageVictoryPanel;

public class GameFrame extends JFrame {

    private Game game;
    private JFrame mainMenuFrame;

    private JLabel nameLabel;
    private JLabel classLabel;
    private JLabel levelLabel;
    private JLabel goldLabel;

    private JLabel hpLabel;
    private JLabel mpLabel;
    private JLabel expLabel;
    private JLabel attackLabel;
    private JLabel defenseLabel;

    private JLabel stageLabel;
    private JLabel monsterLabel;

    private JButton battleButton;
    private JButton selectStageButton;
    private JButton inventoryButton;
    private JButton shopButton;
    private JButton saveButton;
    private JButton mainMenuButton;

    private CardLayout centerLayout;
    private JPanel centerPanel;
    private JPanel hubPanel;
    private JPanel bottomNavigationPanel;

    // Remove old battle panels before showing a new challenge.
    private BattlePanel activeBattlePanel;
    private StageVictoryPanel activeVictoryPanel;

    // Stage selected on the hub; does not change unlocked progress.
    private int selectedStage;

    public GameFrame(
            Game game,
            JFrame mainMenuFrame) {

        this.game = game;
        this.mainMenuFrame = mainMenuFrame;
        this.selectedStage = Math.min(game.getStage(), game.getStageCount() - 1);

        initializeFrame();
        createComponents();
        createLayout();
        registerEvents();

        refreshGameInfo();
    }

    // -------------------------
    // Frame Setup
    // -------------------------

    private void initializeFrame() {

        setTitle("Turn-Based RPG");

        setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE
        );

        setSize(750, 520);

        setMinimumSize(
                new Dimension(750, 520)
        );

        setLocationRelativeTo(null);

        setResizable(false);
    }

    // -------------------------
    // Components
    // -------------------------

    private void createComponents() {

        nameLabel = new JLabel();
        classLabel = new JLabel();
        levelLabel = new JLabel();
        goldLabel = new JLabel();

        hpLabel = new JLabel();
        mpLabel = new JLabel();
        expLabel = new JLabel();
        attackLabel = new JLabel();
        defenseLabel = new JLabel();

        stageLabel = new JLabel(
                "",
                SwingConstants.CENTER
        );

        monsterLabel = new JLabel(
                "",
                SwingConstants.CENTER
        );

        battleButton =
                new JButton("Battle");

        selectStageButton =
                new JButton("Select Stage");

        inventoryButton =
                new JButton("Inventory");

        shopButton =
                new JButton("Shop");

        saveButton =
                new JButton("Save Game");

        mainMenuButton =
                new JButton("Main Menu");
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

        // -------------------------
        // Player Status
        // -------------------------

        JPanel statusPanel =
                new JPanel(
                        new GridLayout(
                                2,
                                1,
                                5,
                                5
                        )
                );

        statusPanel.setBorder(
                BorderFactory.createTitledBorder(
                        "Player Status"
                )
        );

        JPanel topStatus =
                new JPanel(
                        new GridLayout(
                                1,
                                4,
                                10,
                                0
                        )
                );

        topStatus.add(nameLabel);
        topStatus.add(classLabel);
        topStatus.add(levelLabel);
        topStatus.add(goldLabel);

        JPanel bottomStatus =
                new JPanel(
                        new GridLayout(
                                1,
                                5,
                                10,
                                0
                        )
                );

        bottomStatus.add(hpLabel);
        bottomStatus.add(mpLabel);
        bottomStatus.add(attackLabel);
        bottomStatus.add(defenseLabel);
        bottomStatus.add(expLabel);

        statusPanel.add(topStatus);
        statusPanel.add(bottomStatus);

        // -------------------------
        // Stage Area
        // -------------------------

        hubPanel =
                new JPanel(
                        new GridLayout(
                                2,
                                1,
                                10,
                                10
                        )
                );

        hubPanel.setBorder(
                BorderFactory.createEmptyBorder(
                        60,
                        100,
                        60,
                        100
                )
        );

        stageLabel.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        28
                )
        );

        monsterLabel.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        22
                )
        );

        hubPanel.add(stageLabel);
        hubPanel.add(monsterLabel);

        centerLayout =
                new CardLayout();

        centerPanel =
                new JPanel(
                        centerLayout
                );

        centerPanel.add(
                hubPanel,
                "HUB"
        );

        // -------------------------
        // Action Buttons
        // -------------------------

        JPanel actionPanel =
                new JPanel(
                        new GridLayout(
                                1,
                                5,
                                10,
                                0
                        )
                );

        actionPanel.add(battleButton);
        actionPanel.add(selectStageButton);
        actionPanel.add(inventoryButton);
        actionPanel.add(shopButton);
        actionPanel.add(saveButton);

        JPanel southPanel =
                new JPanel(
                        new BorderLayout(
                                10,
                                10
                        )
                );

        southPanel.setBorder(
                BorderFactory.createEmptyBorder(
                        10,
                        30,
                        25,
                        30
                )
        );

        southPanel.add(
                actionPanel,
                BorderLayout.CENTER
        );

        southPanel.add(
                mainMenuButton,
                BorderLayout.SOUTH
        );

        bottomNavigationPanel = southPanel;

        // -------------------------
        // Add to Frame
        // -------------------------

        add(
                statusPanel,
                BorderLayout.NORTH
        );

        add(
                centerPanel,
                BorderLayout.CENTER
        );

        add(
                southPanel,
                BorderLayout.SOUTH
        );
    }

    // -------------------------
    // Events
    // -------------------------

    private void registerEvents() {

        battleButton.addActionListener(
                e -> openBattle()
        );

        selectStageButton.addActionListener(
                e -> selectStage()
        );

        inventoryButton.addActionListener(
                e -> openInventory()
        );

        shopButton.addActionListener(
                e -> openShop()
        );

        saveButton.addActionListener(
                e -> saveGame()
        );

        mainMenuButton.addActionListener(
                e -> returnToMainMenu()
        );
    }

    // -------------------------
    // Refresh
    // -------------------------

    public void refreshGameInfo() {

        Player player =
                game.getPlayer();

        if (player == null) {
            return;
        }

        nameLabel.setText(
                "Name: "
                        + player.getName()
        );

        classLabel.setText(
                "Class: "
                        + player.getClass()
                                .getSimpleName()
        );

        levelLabel.setText(
                "Level: "
                        + player.getLevel()
        );

        goldLabel.setText(
                "Gold: "
                        + player.getGold()
        );

        hpLabel.setText(
                "HP: "
                        + player.getHp()
                        + "/"
                        + player.getMaxHp()
        );

        mpLabel.setText(
                "MP: "
                        + player.getMp()
                        + "/"
                        + player.getMaxMp()
        );

        attackLabel.setText(
                "ATK: "
                        + player.getAttack()
        );

        defenseLabel.setText(
                "DEF: "
                        + player.getDefense()
        );

        expLabel.setText(
                "EXP: "
                        + player.getExperience()
                        + "/"
                        + player.getExpToNextLevel()
        );

        int unlockedCount = game.getUnlockedStageCount();
        if (selectedStage < 0 || selectedStage >= unlockedCount) {
            selectedStage = unlockedCount - 1;
        }

        stageLabel.setText("Selected Stage " + (selectedStage + 1));
        String status = selectedStage < game.getStage()
                ? " [Cleared - Replay]" : " [New]";
        monsterLabel.setText("Enemy: " + game.getStageName(selectedStage) + status);

        battleButton.setEnabled(player.isAlive());
        selectStageButton.setEnabled(player.isAlive());
    }

    // -------------------------
    // Battle
    // -------------------------

    private void setNavigationEnabled(
            boolean enabled) {

        battleButton.setEnabled(enabled);
        selectStageButton.setEnabled(enabled);
        inventoryButton.setEnabled(enabled);
        shopButton.setEnabled(enabled);
        saveButton.setEnabled(enabled);
        mainMenuButton.setEnabled(enabled);
    }

    // Start the stage previously selected on the hub.
    private void openBattle() {
        openBattleAtStage(selectedStage);
    }

    // Choose a stage without immediately entering combat.
    private void selectStage() {
        int unlockedCount = game.getUnlockedStageCount();
        String[] choices = new String[unlockedCount];

        for (int i = 0; i < unlockedCount; i++) {
            String status = i < game.getStage() ? " [Cleared]" : " [New]";
            choices[i] = "Stage " + (i + 1) + " - "
                    + game.getStageName(i) + status;
        }

        String selected = (String) JOptionPane.showInputDialog(
                this,
                "Choose a stage (press Battle to start):",
                "Select Stage",
                JOptionPane.PLAIN_MESSAGE,
                null,
                choices,
                choices[selectedStage]
        );

        if (selected == null) {
            return;
        }

        for (int i = 0; i < choices.length; i++) {
            if (choices[i].equals(selected)) {
                selectedStage = i;
                refreshGameInfo();
                return;
            }
        }
    }

    private void openBattleAtStage(int stageIndex) {
        try {
            BattleManager battle = game.startBattle(stageIndex);
            bottomNavigationPanel.setVisible(true);
            selectedStage = stageIndex;

            if (activeBattlePanel != null) {
                centerPanel.remove(activeBattlePanel);
            }

            activeBattlePanel = new BattlePanel(game, this, battle);
            centerPanel.add(activeBattlePanel, "BATTLE");
            centerLayout.show(centerPanel, "BATTLE");
            centerPanel.revalidate();
            centerPanel.repaint();
            setNavigationEnabled(false);
        } catch (IllegalStateException e) {
            JOptionPane.showMessageDialog(
                    this, e.getMessage(), "Battle Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    public void finishCurrentBattle() {
        BattleManager battle = game.getCurrentBattle();
        if (battle == null || !battle.isBattleOver()) {
            return;
        }

        // Capture before finishBattle() resets the current battle.
        int completedStage = game.getCurrentBattleStage();
        boolean victory = battle.isVictory();
        boolean defeat = battle.isDefeat();
        String monsterName = battle.getMonster().getName();
        int expReward = battle.getMonster().getExpReward();
        int goldReward = battle.getMonster().getGoldReward();

        game.finishBattle();
        refreshGameInfo();

        if (victory) {
            showVictoryScreen(completedStage, monsterName, expReward, goldReward);
        } else if (defeat) {
            showGameResult(false);
        } else {
            // Escaped: unlocked progress does not change.
            showHub();
        }
    }

    private void showVictoryScreen(
            int completedStage,
            String monsterName,
            int expReward,
            int goldReward) {

        boolean hasNextStage = completedStage + 1 < game.getStageCount()
                && completedStage + 1 <= game.getStage();

        StageVictoryPanel victoryPanel = new StageVictoryPanel(
                this, completedStage, monsterName,
                expReward, goldReward, hasNextStage);

        // Prevent old victory cards accumulating during repeated farming.
        if (activeVictoryPanel != null) {
            centerPanel.remove(activeVictoryPanel);
        }
        activeVictoryPanel = victoryPanel;
        centerPanel.add(activeVictoryPanel, "STAGE_VICTORY");
        centerLayout.show(centerPanel, "STAGE_VICTORY");
        centerPanel.revalidate();
        centerPanel.repaint();
        setNavigationEnabled(false);
        // Victory has its own 3 buttons; hide inactive Hub buttons.
        bottomNavigationPanel.setVisible(false);
        revalidate();
        repaint();
    }

    // Called by the buttons on StageVictoryPanel.
    public void restartChallenge(int stageIndex) {
        openBattleAtStage(stageIndex);
    }

    public void prepareNextStage(int stageIndex) {
        selectedStage = stageIndex;
        showHub();
    }

    public void openStageSelection() {
        showHub();
        selectStage();
    }

    public void viewEnding() {
        showGameResult(true);
    }

    private void showGameResult(
            boolean victory) {

        GameResultPanel resultPanel =
                new GameResultPanel(
                        this,
                        victory
                );

        centerPanel.add(
                resultPanel,
                "RESULT"
        );

        centerLayout.show(
                centerPanel,
                "RESULT"
        );

        setNavigationEnabled(false);
        bottomNavigationPanel.setVisible(false);
        revalidate();
        repaint();
    }

    // -------------------------
    // Inventory
    // -------------------------

    private void openInventory() {

        InventoryPanel inventoryPanel =
                new InventoryPanel(
                        game,
                        this
                );

        centerPanel.add(
                inventoryPanel,
                "INVENTORY"
        );

        centerLayout.show(
                centerPanel,
                "INVENTORY"
        );

        setNavigationEnabled(false);
    }

    public void showHub() {

        bottomNavigationPanel.setVisible(true);
        refreshGameInfo();

        centerLayout.show(
                centerPanel,
                "HUB"
        );

        setNavigationEnabled(true);
        battleButton.setEnabled(game.getPlayer().isAlive());
        selectStageButton.setEnabled(game.getPlayer().isAlive());
    }    

    // -------------------------
    // Shop
    // -------------------------

    private void openShop() {

        ShopPanel shopPanel =
                new ShopPanel(
                        game,
                        this
                );

        centerPanel.add(
                shopPanel,
                "SHOP"
        );

        centerLayout.show(
                centerPanel,
                "SHOP"
        );

        setNavigationEnabled(false);
    }

    // -------------------------
    // Save
    // -------------------------

    private void saveGame() {

        try {

            game.saveGame();

            JOptionPane.showMessageDialog(
                    this,
                    "Game saved successfully!",
                    "Save Game",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (IOException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Could not save the game:\n"
                            + e.getMessage(),
                    "Save Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // -------------------------
    // Main Menu
    // -------------------------

    private void returnToMainMenu() {

        int choice =
                JOptionPane.showConfirmDialog(
                        this,
                        "Return to the Main Menu?\n"
                                + "Unsaved progress will be lost.",
                        "Main Menu",
                        JOptionPane.YES_NO_OPTION
                );

        if (choice
                != JOptionPane.YES_OPTION) {
            return;
        }

        showMainMenu();
    }

    public void showMainMenu() {

        mainMenuFrame.setVisible(true);

        dispose();
    }
}
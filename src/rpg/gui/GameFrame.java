package rpg.gui;

import java.awt.CardLayout;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.IOException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.plaf.basic.BasicProgressBarUI;

import rpg.game.Game;
import rpg.model.character.player.Player;
import rpg.game.BattleManager;
import rpg.gui.panel.BattlePanel;
import rpg.gui.panel.InventoryPanel;
import rpg.gui.panel.ShopPanel;
import rpg.gui.panel.GameResultPanel;
import rpg.gui.panel.StageVictoryPanel;
import rpg.gui.panel.StageSelectPanel;
import rpg.gui.panel.StageHubPanel;

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

    private JProgressBar hpBar;
    private JProgressBar mpBar;
    private JProgressBar expBar;

    private JButton battleButton;
    private JButton selectStageButton;
    private JButton inventoryButton;
    private JButton shopButton;
    private JButton saveButton;
    private JButton mainMenuButton;

    private CardLayout centerLayout;
    private JPanel centerPanel;
    private StageHubPanel hubPanel;
    private JPanel bottomNavigationPanel;
    private JPanel subpageNavigationPanel;

    private JButton backToHubButton;
    private JButton subpageSaveButton;
    private JButton subpageMainMenuButton;

    // Remove old battle panels before showing a new challenge.
    private BattlePanel activeBattlePanel;
    private StageVictoryPanel activeVictoryPanel;
    private GameResultPanel activeResultPanel;
    private StageSelectPanel activeStageSelectPanel;

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

        setTitle("Hero's Journey");

        setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE
        );

        setSize(750, 560);

        setMinimumSize(
                new Dimension(750, 560)
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

        backToHubButton = new JButton("Back to Hub");
        subpageSaveButton = new JButton("Save Game");
        subpageMainMenuButton = new JButton("Main Menu");
    }

    // -------------------------
    // Layout
    // -------------------------

    private void createLayout() {
        getContentPane().setBackground(GameTheme.BACKGROUND);
        setLayout(new BorderLayout(0, 0));

        // Persistent HUD: visible in hub, battle, shop and inventory.
        JPanel statusPanel = new JPanel(new BorderLayout(0, 8));
        statusPanel.setBackground(GameTheme.PANEL);
        statusPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, GameTheme.GOLD),
                BorderFactory.createEmptyBorder(11, 18, 10, 18)));

        JPanel topStatus = new JPanel(new GridLayout(1, 4, 10, 0));
        topStatus.setOpaque(false);
        styleStatusLabel(nameLabel, GameTheme.TEXT, 14);
        styleStatusLabel(classLabel, GameTheme.MUTED, 12);
        styleStatusLabel(levelLabel, GameTheme.TEXT, 12);
        styleStatusLabel(goldLabel, GameTheme.GOLD, 12);
        topStatus.add(nameLabel);
        topStatus.add(classLabel);
        topStatus.add(levelLabel);
        topStatus.add(goldLabel);

        hpBar = createProgressBar(new java.awt.Color(205, 91, 108));
        mpBar = createProgressBar(new java.awt.Color(103, 166, 239));
        expBar = createProgressBar(new java.awt.Color(226, 183, 93));

        JPanel bottomStatus = new JPanel(new GridLayout(1, 5, 10, 0));
        bottomStatus.setOpaque(false);
        bottomStatus.add(makeProgressInfo(hpLabel, hpBar));
        bottomStatus.add(makeProgressInfo(mpLabel, mpBar));
        bottomStatus.add(makeStatInfo(attackLabel));
        bottomStatus.add(makeStatInfo(defenseLabel));
        bottomStatus.add(makeProgressInfo(expLabel, expBar));
        statusPanel.add(topStatus, BorderLayout.NORTH);
        statusPanel.add(bottomStatus, BorderLayout.CENTER);

        hubPanel = new StageHubPanel(game);
        centerLayout = new CardLayout();
        centerPanel = new JPanel(centerLayout);
        centerPanel.setBackground(GameTheme.BACKGROUND);
        centerPanel.add(hubPanel, "HUB");

        JPanel actionPanel = new JPanel(new GridLayout(1, 5, 10, 0));
        actionPanel.setOpaque(false);
        styleNav(battleButton, true);
        styleNav(selectStageButton, false);
        styleNav(inventoryButton, false);
        styleNav(shopButton, false);
        styleNav(saveButton, false);
        actionPanel.add(battleButton);
        actionPanel.add(selectStageButton);
        actionPanel.add(inventoryButton);
        actionPanel.add(shopButton);
        actionPanel.add(saveButton);

        JPanel southPanel = new JPanel(new BorderLayout(0, 9));
        southPanel.setBackground(GameTheme.BACKGROUND);
        southPanel.setBorder(BorderFactory.createEmptyBorder(9, 20, 17, 20));
        southPanel.add(actionPanel, BorderLayout.CENTER);
        GameTheme.styleButton(mainMenuButton, false);
        mainMenuButton.setFont(new Font("Monospaced", Font.BOLD, 12));
        mainMenuButton.setPreferredSize(new Dimension(0, 31));
        southPanel.add(mainMenuButton, BorderLayout.SOUTH);
        bottomNavigationPanel = southPanel;

        // Compact navigation used ONLY in Inventory and Shop.
        JPanel subpageActions = new JPanel(new GridLayout(1, 3, 12, 0));
        subpageActions.setOpaque(false);
        styleNav(backToHubButton, true);
        styleNav(subpageSaveButton, false);
        styleNav(subpageMainMenuButton, false);
        subpageActions.add(backToHubButton);
        subpageActions.add(subpageSaveButton);
        subpageActions.add(subpageMainMenuButton);

        subpageNavigationPanel = new JPanel(new BorderLayout());
        subpageNavigationPanel.setBackground(GameTheme.BACKGROUND);
        subpageNavigationPanel.setBorder(
                BorderFactory.createEmptyBorder(8, 20, 14, 20));
        subpageNavigationPanel.add(subpageActions, BorderLayout.CENTER);

        add(statusPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);
    }

    private void styleStatusLabel(JLabel label, java.awt.Color color, int size) {
        label.setFont(new Font("Monospaced", Font.BOLD, size));
        label.setForeground(color);
    }

    private JProgressBar createProgressBar(java.awt.Color color) {
        JProgressBar bar = new JProgressBar();
        bar.setUI(new BasicProgressBarUI());
        bar.setMinimum(0);
        bar.setStringPainted(false);
        bar.setForeground(color);
        bar.setBackground(new java.awt.Color(13, 21, 37));
        bar.setBorder(BorderFactory.createLineBorder(GameTheme.PANEL_BORDER, 1));
        bar.setPreferredSize(new Dimension(60, 10));
        return bar;
    }

    private JPanel makeProgressInfo(JLabel label, JProgressBar bar) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setOpaque(false);
        styleStatusLabel(label, GameTheme.TEXT, 11);
        panel.add(label, BorderLayout.NORTH);
        panel.add(bar, BorderLayout.CENTER);
        return panel;
    }

    private JPanel makeStatInfo(JLabel label) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        styleStatusLabel(label, GameTheme.MUTED, 12);
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    private void styleNav(JButton button, boolean primary) {
        GameTheme.styleButton(button, primary);
        button.setFont(new Font("Monospaced", Font.BOLD, 13));
        button.setPreferredSize(new Dimension(125, 42));
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
        backToHubButton.addActionListener(e -> showHub());
        subpageSaveButton.addActionListener(e -> saveGame());
        subpageMainMenuButton.addActionListener(e -> returnToMainMenu());
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

        hpBar.setMaximum(Math.max(1, player.getMaxHp()));
        hpBar.setValue(player.getHp());
        mpBar.setMaximum(Math.max(1, player.getMaxMp()));
        mpBar.setValue(player.getMp());
        expBar.setMaximum(Math.max(1, player.getExpToNextLevel()));
        expBar.setValue(player.getExperience());

        int unlockedCount = game.getUnlockedStageCount();
        if (selectedStage < 0 || selectedStage >= unlockedCount) {
            selectedStage = unlockedCount - 1;
        }
        hubPanel.setSelectedStage(selectedStage);

        // Refreshing equipment or Gold must NOT re-enable Hub buttons
        // while the player is still viewing Inventory/Shop.
        boolean onHub = bottomNavigationPanel.getParent() == getContentPane();
        battleButton.setEnabled(onHub && player.isAlive());
        selectStageButton.setEnabled(onHub && player.isAlive());
    }

    // -------------------------
    // Battle
    // -------------------------

    /** Swap navigation without reserving blank Hub-footer space on other pages. */
    private void showNavigation(JPanel target) {
        if (bottomNavigationPanel.getParent() == getContentPane()) {
            getContentPane().remove(bottomNavigationPanel);
        }
        if (subpageNavigationPanel.getParent() == getContentPane()) {
            getContentPane().remove(subpageNavigationPanel);
        }
        if (target != null) {
            getContentPane().add(target, BorderLayout.SOUTH);
        }
        getContentPane().revalidate();
        getContentPane().repaint();
    }

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

    // Dedicated stage selection screen. Selecting a stage never starts a battle.
    private void selectStage() {
        if (activeStageSelectPanel != null) {
            centerPanel.remove(activeStageSelectPanel);
        }
        activeStageSelectPanel = new StageSelectPanel(game, this, selectedStage);
        centerPanel.add(activeStageSelectPanel, "STAGE_SELECT");
        centerLayout.show(centerPanel, "STAGE_SELECT");
        showNavigation(null);
        setNavigationEnabled(false);
        centerPanel.revalidate();
        centerPanel.repaint();
        revalidate();
        repaint();
    }

    // Called only when the player confirms a stage on StageSelectPanel.
    public void confirmSelectedStage(int stageIndex) {
        if (stageIndex < 0 || stageIndex >= game.getUnlockedStageCount()) {
            JOptionPane.showMessageDialog(this, "This stage is locked.",
                    "Stage Locked", JOptionPane.WARNING_MESSAGE);
            return;
        }
        selectedStage = stageIndex;
        showHub();
    }

    private void openBattleAtStage(int stageIndex) {
        try {
            BattleManager battle = game.startBattle(stageIndex);
            // Hide inactive Hub buttons while the battle screen is displayed.
            showNavigation(null);
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
        // Show the actual total, including Gold awarded for leveling up.
        int goldReward = battle.getMonster().getGoldReward()
                + battle.getLevelUpGoldEarned();
        List<String> droppedItems = battle.getDroppedItemNames();

        game.finishBattle();
        refreshGameInfo();

        if (victory) {
            // Final boss: show the final VICTORY immediately, without View Ending.
            if (completedStage == game.getStageCount() - 1) {
                showGameResult(true);
            } else {
                showVictoryScreen(completedStage, monsterName, expReward, goldReward, droppedItems);
            }
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
            int goldReward,
            List<String> droppedItems) {

        boolean hasNextStage = completedStage + 1 < game.getStageCount()
                && completedStage + 1 <= game.getStage();

        StageVictoryPanel victoryPanel = new StageVictoryPanel(
                this, completedStage, monsterName,
                expReward, goldReward, hasNextStage, droppedItems);

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
        // Stage Victory has its own action buttons; hide inactive Hub buttons.
        showNavigation(null);
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
        selectStage();
    }

    private void showGameResult(
            boolean victory) {

        // Replace the previous result card when replaying the final boss.
        if (activeResultPanel != null) {
            centerPanel.remove(activeResultPanel);
        }
        activeResultPanel = new GameResultPanel(this, victory);
        centerPanel.add(activeResultPanel, "RESULT");

        centerLayout.show(
                centerPanel,
                "RESULT"
        );
        centerPanel.revalidate();
        centerPanel.repaint();

        setNavigationEnabled(false);
        showNavigation(null);
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

        showNavigation(subpageNavigationPanel);
        setNavigationEnabled(false);
    }

    public void showHub() {

        showNavigation(bottomNavigationPanel);
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

        showNavigation(subpageNavigationPanel);
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
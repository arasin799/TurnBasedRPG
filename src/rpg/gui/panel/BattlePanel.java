package rpg.gui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.function.Supplier;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import rpg.game.BattleManager;
import rpg.game.Game;
import rpg.gui.GameFrame;
import rpg.gui.GameTheme;
import rpg.model.character.monster.Monster;
import rpg.model.character.player.Player;
import rpg.model.item.BattleUsable;
import rpg.model.item.Item;
import rpg.model.item.Usable;
import rpg.model.item.Potion;
import rpg.model.item.HealthPotion;
import rpg.model.item.SmallHealthPotion;
import rpg.model.item.SoulShard;

/** Styled battle screen. Combat rules continue to live only in BattleManager. */
public class BattlePanel extends JPanel {

    private final GameFrame gameFrame;
    private final BattleManager battle;

    private JLabel playerNameLabel;
    private JLabel monsterNameLabel;
    private JLabel stunStatusLabel;
    private JProgressBar playerHpBar;
    private JProgressBar playerMpBar;
    private JProgressBar monsterHpBar;
    private JProgressBar monsterUltimateBar;
    private JTextArea battleLog;
    private BattleArenaPanel arena;
    private static final Pattern DAMAGE_PATTERN =
            Pattern.compile("for (\\d+) damage[.!]");
    private static final Pattern RECOVERY_PATTERN =
            Pattern.compile("Restored (\\d+) (HP|MP)\\.");
    private JButton attackButton;
    private JButton skillButton;
    private JButton ultimateButton;
    private JButton itemButton;
    private JButton escapeButton;

    public BattlePanel(Game game, GameFrame gameFrame, BattleManager battle) {
        this.gameFrame = gameFrame;
        this.battle = battle;
        setLayout(new BorderLayout(0, 8));
        setBackground(GameTheme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(8, 14, 11, 14));
        createLayout();
        registerEvents();
        refreshBattleInfo();
        setActionButtonsEnabled(true);
        addBattleLog("Battle started! " + battle.getPlayer().getName()
                + " VS " + battle.getMonster().getName());
    }

    private JLabel label(String text, int size, Color color) {
        JLabel result = new JLabel(text, SwingConstants.CENTER);
        result.setForeground(color);
        result.setFont(new Font("Monospaced", Font.BOLD, size));
        return result;
    }

    private JProgressBar bar(Color color) {
        JProgressBar bar = new JProgressBar(0, 100);
        bar.setForeground(color);
        bar.setBackground(new Color(14, 22, 38));
        bar.setBorder(BorderFactory.createLineBorder(GameTheme.PANEL_BORDER, 1));
        bar.setBorderPainted(true);
        bar.setStringPainted(true);
        bar.setFont(new Font("Monospaced", Font.BOLD, 11));
        bar.setPreferredSize(new Dimension(100, 20));
        return bar;
    }

    private JPanel card() {
        JPanel panel = new JPanel(new BorderLayout(2, 5));
        panel.setBackground(GameTheme.PANEL);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GameTheme.PANEL_BORDER, 2),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        return panel;
    }

    private void createLayout() {
        JPanel topPanel = new JPanel(new GridLayout(1, 2, 12, 0));
        topPanel.setBackground(GameTheme.BACKGROUND);
        topPanel.setPreferredSize(new Dimension(700, 91));

        JPanel playerCard = card();
        playerNameLabel = label("", 13, GameTheme.GOLD);
        stunStatusLabel = label("", 10, new Color(242, 152, 115));
        JPanel playerTitle = new JPanel(new GridLayout(2, 1, 0, 0));
        playerTitle.setOpaque(false);
        playerTitle.add(playerNameLabel);
        playerTitle.add(stunStatusLabel);
        playerCard.add(playerTitle, BorderLayout.NORTH);

        playerHpBar = bar(new Color(83, 186, 130));
        playerMpBar = bar(new Color(94, 152, 223));
        JPanel playerBars = new JPanel(new GridLayout(2, 1, 0, 5));
        playerBars.setOpaque(false);
        playerBars.add(playerHpBar);
        playerBars.add(playerMpBar);
        playerCard.add(playerBars, BorderLayout.CENTER);

        JPanel monsterCard = card();
        monsterNameLabel = label("", 13, new Color(245, 153, 144));
        monsterCard.add(monsterNameLabel, BorderLayout.NORTH);
        monsterHpBar = bar(new Color(208, 103, 111));
        JPanel monsterBars = new JPanel(new GridLayout(2, 1, 0, 5));
        monsterBars.setOpaque(false);
        monsterBars.add(monsterHpBar);
        monsterUltimateBar = bar(new Color(165, 116, 232));
        monsterBars.add(monsterUltimateBar);
        monsterCard.add(monsterBars, BorderLayout.CENTER);

        topPanel.add(playerCard);
        topPanel.add(monsterCard);
        add(topPanel, BorderLayout.NORTH);

        arena = new BattleArenaPanel(battle);
        JPanel midPanel = new JPanel(new BorderLayout(0, 7));
        midPanel.setOpaque(false);
        midPanel.add(arena, BorderLayout.NORTH);

        battleLog = new JTextArea();
        battleLog.setEditable(false);
        battleLog.setLineWrap(true);
        battleLog.setWrapStyleWord(true);
        battleLog.setFont(new Font("Monospaced", Font.PLAIN, 12));
        battleLog.setForeground(GameTheme.TEXT);
        battleLog.setBackground(new Color(22, 32, 52));
        battleLog.setCaretColor(GameTheme.GOLD);
        battleLog.setMargin(new java.awt.Insets(7, 11, 7, 11));
        JScrollPane logScroll = new JScrollPane(battleLog);
        logScroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(GameTheme.PANEL_BORDER, 2),
                " BATTLE LOG ", 0, 0,
                new Font("Monospaced", Font.BOLD, 11), GameTheme.GOLD));
        logScroll.getViewport().setBackground(GameTheme.PANEL);
        midPanel.add(logScroll, BorderLayout.CENTER);
        add(midPanel, BorderLayout.CENTER);

        JPanel actions = new JPanel(new GridLayout(1, 5, 7, 0));
        actions.setOpaque(false);
        actions.setPreferredSize(new Dimension(700, 45));
        attackButton = new JButton("ATTACK");
        skillButton = new JButton("SKILL");
        ultimateButton = new JButton("ULTIMATE");
        itemButton = new JButton("USE ITEM");
        escapeButton = new JButton("ESCAPE");
        styleAction(attackButton, true);
        styleAction(skillButton, false);
        styleAction(ultimateButton, false);
        styleAction(itemButton, false);
        styleAction(escapeButton, false);
        actions.add(attackButton);
        actions.add(skillButton);
        actions.add(ultimateButton);
        actions.add(itemButton);
        actions.add(escapeButton);
        add(actions, BorderLayout.SOUTH);
    }

    private void styleAction(JButton button, boolean primary) {
        GameTheme.styleButton(button, primary);
        button.setFont(new Font("Monospaced", Font.BOLD, 12));
        button.setPreferredSize(new Dimension(135, 44));
        button.setMaximumSize(new Dimension(135, 44));
    }

    private void registerEvents() {
        attackButton.addActionListener(e -> attack());
        skillButton.addActionListener(e -> useSkill());
        ultimateButton.addActionListener(e -> useUltimate());
        itemButton.addActionListener(e -> useItem());
        escapeButton.addActionListener(e -> escape());
    }

    private void performAction(Supplier<String> action) {
        if (battle.isBattleOver()) {
            return;
        }
        // Prevent double-clicking while one action's visual sequence plays.
        setActionButtonsEnabled(false);
        String result = action.get();
        addBattleLog(result);
        refreshBattleInfo();
        enqueueEffectsFromLog(result);
        arena.playQueuedEffects(() -> {
            if (battle.isBattleOver()) {
                checkBattleResult();
            } else {
                setActionButtonsEnabled(true);
            }
        });
    }

    /** Read battle messages for visuals only. No stats/rewards are changed here. */
    private void enqueueEffectsFromLog(String result) {
        String hero = battle.getPlayer().getName();
        String enemy = battle.getMonster().getName();
        for (String rawLine : result.split("\\R")) {
            String line = rawLine.trim();
            Matcher damage = DAMAGE_PATTERN.matcher(line);
            if (damage.find()) {
                int amount = Integer.parseInt(damage.group(1));
                if (line.startsWith(enemy + " attacks ")
                        || line.startsWith(enemy + " uses ")) {
                    arena.queueDamage(true, amount, line.contains(" uses "));
                } else if (line.startsWith(hero + " attacks ")
                        || line.startsWith(hero + " uses ")) {
                    arena.queueDamage(false, amount, line.contains(" uses "));
                }
            }
            Matcher recovery = RECOVERY_PATTERN.matcher(line);
            if (recovery.find()) {
                arena.queueRecovery(Integer.parseInt(recovery.group(1)),
                        recovery.group(2).equals("MP"));
            }
            if (line.startsWith(hero + " uses ULTIMATE ")) {
                arena.queueStatus(true, "HERO ULTIMATE!");
            } else if (line.startsWith(enemy + " uses ULTIMATE ")) {
                arena.queueStatus(false, "ENEMY ULTIMATE!");
            }
            if (line.contains("is stunned and misses a turn")) {
                arena.queueStatus(true, "TURN SKIPPED");
            } else if (line.contains("is stunned for 1 turn")) {
                arena.queueStatus(true, "STUNNED!");
            } else if (line.contains("fades into mist")) {
                arena.queueStatus(false, "FADE 50%");
            } else if (line.contains("creates a magical barrier")) {
                arena.queueStatus(false, "BARRIER 50%");
            }
        }
    }

    private void setActionButtonsEnabled(boolean enabled) {
        attackButton.setEnabled(enabled);
        skillButton.setEnabled(enabled);
        // Charge, MP and Stun must all permit the Ultimate action.
        ultimateButton.setEnabled(enabled && battle.getPlayer().canUseUltimate()
                && !battle.isPlayerStunned());
        itemButton.setEnabled(enabled);
        escapeButton.setEnabled(enabled && !battle.getMonster().isBoss());
        updateUltimateButtonVisual();
    }

    /** Distinct READY appearance without changing the combat rules. */
    private void updateUltimateButtonVisual() {
        Player player = battle.getPlayer();
        int charge = player.getUltimateCharge();
        int maximum = player.getMaxUltimateCharge();
        int mpCost = player.getUltimateMpCost();
        boolean chargeFull = charge >= maximum;
        boolean mpEnough = player.getMp() >= mpCost;
        boolean readyToClick = ultimateButton.isEnabled()
                && !battle.isPlayerStunned() && !battle.isBattleOver();

        if (readyToClick) {
            // Violet + bright gold outline stands out from the gold ATTACK button.
            ultimateButton.setText("ULTIMATE READY!");
            ultimateButton.setBackground(new Color(103, 61, 180));
            ultimateButton.setForeground(new Color(255, 240, 181));
            ultimateButton.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(255, 216, 105), 3),
                    BorderFactory.createEmptyBorder(5, 3, 5, 3)));
            ultimateButton.setToolTipText("READY! " + player.getUltimateSkillName()
                    + " | Uses " + mpCost + " MP and " + maximum + " charges.");
        } else {
            ultimateButton.setText(chargeFull && !mpEnough
                    ? "ULT MP LOW" : "ULTIMATE " + charge + "/" + maximum);
            ultimateButton.setBackground(GameTheme.PANEL);
            ultimateButton.setForeground(GameTheme.MUTED);
            ultimateButton.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(GameTheme.PANEL_BORDER, 2),
                    BorderFactory.createEmptyBorder(6, 3, 6, 3)));
            if (battle.isPlayerStunned()) {
                ultimateButton.setToolTipText("STUNNED: skip your next turn first.");
            } else if (chargeFull && !mpEnough) {
                ultimateButton.setToolTipText("Need " + (mpCost - player.getMp())
                        + " more MP for " + player.getUltimateSkillName() + ".");
            } else {
                ultimateButton.setToolTipText(player.getUltimateSkillName()
                        + " | Charge " + charge + "/" + maximum
                        + " | Costs " + mpCost + " MP.");
            }
        }
        ultimateButton.setOpaque(true);
        ultimateButton.setContentAreaFilled(true);
        ultimateButton.repaint();
    }

    private void attack() {
        performAction(() -> battle.playerAttack());
    }

    private void useSkill() {
        performAction(() -> battle.playerUseSkill());
    }

    private void useUltimate() {
        performAction(() -> battle.playerUseUltimate());
    }

    private void useItem() {
        if (battle.isBattleOver()) {
            return;
        }
        // Never open the item dialog or spend an item when stunned.
        if (battle.isPlayerStunned()) {
            performAction(() -> battle.skipStunnedTurn());
            return;
        }

        Player player = battle.getPlayer();
        ArrayList<Item> usableItems = new ArrayList<>();
        for (Item item : player.getInventory().getItems()) {
            if (item instanceof Usable || item instanceof BattleUsable) {
                usableItems.add(item);
            }
        }
        if (usableItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "You do not have any usable items.",
                    "Use Item", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Item selected = chooseItemWithDetails(player, usableItems);
        if (selected != null) {
            performAction(() -> battle.playerUseItem(selected));
        }
    }

    /** Preview each item's effect and stock before the player spends a turn. */
    private Item chooseItemWithDetails(Player player, ArrayList<Item> items) {
        Map<String, Item> firstCopy = new LinkedHashMap<>();
        Map<String, Integer> quantities = new LinkedHashMap<>();
        for (Item item : items) {
            firstCopy.putIfAbsent(item.getName(), item);
            quantities.put(item.getName(), quantities.getOrDefault(item.getName(), 0) + 1);
        }

        JComboBox<String> chooser = new JComboBox<>();
        for (String name : firstCopy.keySet()) {
            chooser.addItem(name + "  x" + quantities.get(name));
        }
        chooser.setFont(new Font("Monospaced", Font.BOLD, 13));

        JTextArea description = new JTextArea(5, 32);
        description.setEditable(false);
        description.setFocusable(false);
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        description.setFont(new Font("Monospaced", Font.PLAIN, 12));
        description.setForeground(GameTheme.TEXT);
        description.setBackground(GameTheme.PANEL);
        description.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GameTheme.PANEL_BORDER, 2),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));

        ArrayList<String> names = new ArrayList<>(firstCopy.keySet());
        Runnable update = () -> {
            int index = chooser.getSelectedIndex();
            if (index >= 0) {
                String name = names.get(index);
                description.setText(describeBattleItem(firstCopy.get(name),
                        quantities.get(name), player));
                description.setCaretPosition(0);
            }
        };
        chooser.addActionListener(e -> update.run());
        update.run();

        JPanel dialog = new JPanel(new BorderLayout(0, 10));
        dialog.setPreferredSize(new Dimension(420, 165));
        dialog.add(chooser, BorderLayout.NORTH);
        dialog.add(description, BorderLayout.CENTER);

        int answer = JOptionPane.showConfirmDialog(this, dialog, "Use Item",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        int index = chooser.getSelectedIndex();
        return answer == JOptionPane.OK_OPTION && index >= 0
                ? firstCopy.get(names.get(index)) : null;
    }

    private String describeBattleItem(Item item, int amount, Player player) {
        String info = item.getName() + "  (Owned: " + amount + ")\n"
                + item.getDescription() + "\n";
        if (item instanceof Potion) {
            boolean health = item instanceof HealthPotion
                    || item instanceof SmallHealthPotion;
            int current = health ? player.getHp() : player.getMp();
            int maximum = health ? player.getMaxHp() : player.getMaxMp();
            int restore = ((Potion) item).getRestoreAmount();
            int actual = Math.min(restore, maximum - current);
            info += (health ? "HP " : "MP ") + current + "/" + maximum
                    + " | Actual restore: +" + actual + "\n";
            if (actual == 0) info += "Already full - item will not be consumed.";
        } else if (item instanceof SoulShard) {
            info += "ULT " + player.getUltimateCharge() + "/"
                    + player.getMaxUltimateCharge() + " | Adds +1 charge\n";
            if (player.isUltimateReady()) {
                info += "Charge full - item will not be consumed.";
            }
        } else if (item instanceof BattleUsable) {
            info += "Targets the enemy. Uses one battle turn.";
        }
        return info;
    }

    private void escape() {
        if (battle.isBattleOver()) {
            return;
        }
        setActionButtonsEnabled(false);
        String result = battle.escape();
        addBattleLog(result);
        refreshBattleInfo();
        enqueueEffectsFromLog(result);
        arena.playQueuedEffects(() -> {
            if (battle.hasEscaped()) {
                JOptionPane.showMessageDialog(this, "You escaped from the battle.",
                        "Escape", JOptionPane.INFORMATION_MESSAGE);
                gameFrame.finishCurrentBattle();
            } else if (battle.isBattleOver()) {
                checkBattleResult();
            } else {
                setActionButtonsEnabled(true);
            }
        });
    }

    private void checkBattleResult() {
        if (!battle.isBattleOver()) {
            return;
        }
        disableBattleButtons();
        if (battle.isVictory()) {
            // GameFrame shows the dedicated victory screen with 3 action buttons.
            gameFrame.finishCurrentBattle();
        } else if (battle.isDefeat()) {
            JOptionPane.showMessageDialog(this, "You have been defeated.",
                    "Defeat", JOptionPane.ERROR_MESSAGE);
            gameFrame.finishCurrentBattle();
        }
    }

    private void setBar(JProgressBar bar, int value, int max, String prefix) {
        bar.setMaximum(Math.max(1, max));
        bar.setValue(Math.max(0, value));
        bar.setString(prefix + " " + value + "/" + max);
    }

    private void refreshBattleInfo() {
        Player player = battle.getPlayer();
        Monster monster = battle.getMonster();
        playerNameLabel.setText(player.getName() + " / "
                + player.getClass().getSimpleName().toUpperCase());
        monsterNameLabel.setText((monster.isBoss() ? "BOSS: " : "ENEMY: ")
                + monster.getName().toUpperCase());
        stunStatusLabel.setText(battle.isPlayerStunned()
                ? "STUNNED - NEXT ACTION SKIPPED" : "");
        setBar(playerHpBar, player.getHp(), player.getMaxHp(), "HP");
        setBar(playerMpBar, player.getMp(), player.getMaxMp(), "MP");
        playerMpBar.setString("MP " + player.getMp() + "/" + player.getMaxMp()
                + "   |   ULT " + player.getUltimateCharge()
                + "/" + player.getMaxUltimateCharge());
        setBar(monsterHpBar, monster.getHp(), monster.getMaxHp(), "HP");
        setBar(monsterUltimateBar, monster.getUltimateCharge(),
                monster.getUltimateChargeRequired(), "ULT");
        if (monster.isUltimateReady()) {
            monsterUltimateBar.setString("ULT READY! " + monster.getUltimateCharge()
                    + "/" + monster.getUltimateChargeRequired());
        }
        skillButton.setText(player.getSkillName().toUpperCase());
        skillButton.setToolTipText("Spend MP to use " + player.getSkillName());
        updateUltimateButtonVisual();
        if (monster.isBoss()) {
            escapeButton.setEnabled(false);
            escapeButton.setToolTipText("You cannot escape from a boss battle.");
        }
    }

    private void addBattleLog(String message) {
        if (message == null || message.isEmpty()) {
            return;
        }
        if (!battleLog.getText().isEmpty()) {
            battleLog.append("\n\n");
        }
        battleLog.append(message);
        battleLog.setCaretPosition(battleLog.getDocument().getLength());
    }

    private void disableBattleButtons() {
        attackButton.setEnabled(false);
        skillButton.setEnabled(false);
        ultimateButton.setEnabled(false);
        itemButton.setEnabled(false);
        escapeButton.setEnabled(false);
        updateUltimateButtonVisual();
    }
}

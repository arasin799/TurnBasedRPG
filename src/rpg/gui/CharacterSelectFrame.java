package rpg.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import rpg.game.Game;
import rpg.model.character.player.Archer;
import rpg.model.character.player.Knight;
import rpg.model.character.player.Mage;
import rpg.model.character.player.Player;

/** Step 49.2: Pixel-themed class selection, preserving existing game logic. */
public class CharacterSelectFrame extends JFrame {

    private final Game game;
    private final JFrame previousFrame;

    private JTextField nameField;
    private HeroClassCard knightCard;
    private HeroClassCard archerCard;
    private HeroClassCard mageCard;
    private JButton startButton;
    private JButton backButton;
    private String selectedClass = "Knight";

    public CharacterSelectFrame(Game game, JFrame previousFrame) {
        this.game = game;
        this.previousFrame = previousFrame;

        initializeFrame();
        createLayout();
        registerEvents();
        selectClass("Knight");
    }

    private void initializeFrame() {
        setTitle("Hero's Journey - Choose Your Hero");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(750, 520);
        setMinimumSize(new Dimension(750, 520));
        setResizable(false);
        setLocationRelativeTo(null);
    }

    private JLabel createLabel(String text, int size, Color color) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Monospaced", Font.BOLD, size));
        label.setForeground(color);
        label.setAlignmentX(CENTER_ALIGNMENT);
        return label;
    }

    private void createLayout() {
        JPanel background = GameTheme.createBackdrop();
        background.setLayout(new BorderLayout(0, 13));
        background.setBorder(BorderFactory.createEmptyBorder(18, 32, 20, 32));
        setContentPane(background);

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(createLabel("CHOOSE YOUR HERO", 27, GameTheme.GOLD));
        header.add(Box.createVerticalStrut(5));
        header.add(createLabel("THREE PATHS. ONE JOURNEY.", 12, GameTheme.MUTED));
        background.add(header, BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(1, 3, 13, 0));
        cards.setOpaque(false);
        knightCard = new HeroClassCard("Knight", new Knight("Preview"),
                new Color(151, 192, 231), () -> selectClass("Knight"));
        archerCard = new HeroClassCard("Archer", new Archer("Preview"),
                new Color(126, 216, 153), () -> selectClass("Archer"));
        mageCard = new HeroClassCard("Mage", new Mage("Preview"),
                new Color(192, 154, 247), () -> selectClass("Mage"));
        cards.add(knightCard);
        cards.add(archerCard);
        cards.add(mageCard);
        background.add(cards, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout(0, 10));
        footer.setOpaque(false);

        JPanel nameRow = new JPanel(new BorderLayout(12, 0));
        nameRow.setOpaque(false);
        JLabel nameLabel = createLabel("HERO NAME", 13, GameTheme.TEXT);
        nameRow.add(nameLabel, BorderLayout.WEST);
        nameField = new JTextField();
        nameField.setFont(new Font("Monospaced", Font.BOLD, 15));
        nameField.setForeground(GameTheme.TEXT);
        nameField.setCaretColor(GameTheme.GOLD);
        nameField.setBackground(GameTheme.PANEL);
        nameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GameTheme.PANEL_BORDER, 2),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        nameField.setPreferredSize(new Dimension(0, 36));
        nameRow.add(nameField, BorderLayout.CENTER);
        footer.add(nameRow, BorderLayout.NORTH);

        JPanel buttons = new JPanel(new GridLayout(1, 2, 13, 0));
        buttons.setOpaque(false);
        backButton = new JButton("Back");
        startButton = new JButton("Start Game");
        GameTheme.styleButton(backButton, false);
        GameTheme.styleButton(startButton, true);
        // Width follows the two-column layout, keeping the shared theme.
        buttons.add(backButton);
        buttons.add(startButton);
        footer.add(buttons, BorderLayout.CENTER);
        background.add(footer, BorderLayout.SOUTH);
    }

    private void registerEvents() {
        startButton.addActionListener(e -> startGame());
        backButton.addActionListener(e -> goBack());
        nameField.addActionListener(e -> startGame());
    }

    private void selectClass(String playerClass) {
        selectedClass = playerClass;
        knightCard.setChosen("Knight".equals(playerClass));
        archerCard.setChosen("Archer".equals(playerClass));
        mageCard.setChosen("Mage".equals(playerClass));
    }

    private void startGame() {
        String playerName = nameField.getText().trim();
        if (playerName.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter your character name.",
                    "Invalid Name", JOptionPane.WARNING_MESSAGE);
            nameField.requestFocusInWindow();
            return;
        }

        // Preserve the original character creation and game navigation.
        game.startNewGame(playerName, selectedClass);
        Player player = game.getPlayer();

        JOptionPane.showMessageDialog(this,
                "Character created!\n\n"
                + "Name: " + player.getName()
                + "\nClass: " + player.getClass().getSimpleName()
                + "\nHP: " + player.getHp() + "/" + player.getMaxHp()
                + "\nMP: " + player.getMp() + "/" + player.getMaxMp()
                + "\nSkill: " + player.getSkillName(),
                "New Game", JOptionPane.INFORMATION_MESSAGE);

        GameFrame gameFrame = new GameFrame(game, previousFrame);
        gameFrame.setVisible(true);
        dispose();
    }

    private void goBack() {
        previousFrame.setVisible(true);
        dispose();
    }
}

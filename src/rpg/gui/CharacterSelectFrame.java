package rpg.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.Box;
import javax.swing.BoxLayout;

import rpg.game.Game;
import rpg.model.character.player.Archer;
import rpg.model.character.player.Knight;
import rpg.model.character.player.Mage;
import rpg.model.character.player.Player;

public class CharacterSelectFrame extends JFrame {

    private Game game;
    private JFrame previousFrame;

    private JTextField nameField;

    private JRadioButton knightButton;
    private JRadioButton archerButton;
    private JRadioButton mageButton;

    private JLabel classLabel;
    private JLabel hpLabel;
    private JLabel mpLabel;
    private JLabel attackLabel;
    private JLabel defenseLabel;
    private JLabel skillLabel;

    private JButton startButton;
    private JButton backButton;

    private String selectedClass;

    public CharacterSelectFrame(
            Game game,
            JFrame previousFrame) {

        this.game = game;
        this.previousFrame = previousFrame;

        this.selectedClass = "Knight";

        initializeFrame();
        createComponents();
        createLayout();
        registerEvents();

        updateClassInfo("Knight");
    }

    // -------------------------
    // Frame Setup
    // -------------------------

    private void initializeFrame() {

        setTitle("Create Character");

        setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE
        );

        setSize(600, 520);

        setMinimumSize(
                new Dimension(600, 520)
        );

        setLocationRelativeTo(null);

        setResizable(false);
    }

    // -------------------------
    // Components
    // -------------------------

    private void createComponents() {

        nameField = new JTextField();

        knightButton =
                new JRadioButton(
                        "Knight",
                        true
                );

        archerButton =
                new JRadioButton(
                        "Archer"
                );

        mageButton =
                new JRadioButton(
                        "Mage"
                );

        ButtonGroup classGroup =
                new ButtonGroup();

        classGroup.add(knightButton);
        classGroup.add(archerButton);
        classGroup.add(mageButton);

        classLabel = new JLabel();
        hpLabel = new JLabel();
        mpLabel = new JLabel();
        attackLabel = new JLabel();
        defenseLabel = new JLabel();
        skillLabel = new JLabel();

        startButton =
                new JButton("Start Game");

        backButton =
                new JButton("Back");
    }

    // -------------------------
    // Layout
    // -------------------------

    private void createLayout() {

        setLayout(
                new BorderLayout()
        );

        // Title
        JLabel titleLabel =
                new JLabel(
                        "CREATE CHARACTER",
                        SwingConstants.CENTER
                );

        titleLabel.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        26
                )
        );

        titleLabel.setBorder(
                BorderFactory.createEmptyBorder(
                        30,
                        20,
                        20,
                        20
                )
        );

        // Main Content
        JPanel contentPanel = new JPanel();

        contentPanel.setLayout(
                new BoxLayout(
                        contentPanel,
                        BoxLayout.Y_AXIS
                )
        );

        contentPanel.setBorder(
                BorderFactory.createEmptyBorder(
                        10,
                        80,
                        10,
                        80
                )
        );

        // Name
        JPanel namePanel =
                new JPanel(
                        new BorderLayout(
                                10,
                                0
                        )
                );

        namePanel.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        45
                )
        );

        namePanel.add(
                new JLabel("Name:"),
                BorderLayout.WEST
        );

        namePanel.add(
                nameField,
                BorderLayout.CENTER
        );

        // Class Selection
        JPanel classPanel =
                new JPanel(
                        new GridLayout(
                                1,
                                3,
                                10,
                                0
                        )
                );

        classPanel.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        80
                )
        );

        classPanel.setBorder(
                BorderFactory.createTitledBorder(
                        "Choose Class"
                )
        );

        classPanel.add(knightButton);
        classPanel.add(archerButton);
        classPanel.add(mageButton);

        // Class Information
        JPanel infoPanel =
                new JPanel(
                        new GridLayout(
                                6,
                                1,
                                0,
                                6
                        )
                );

        infoPanel.setPreferredSize(
            new Dimension(400, 180)
        );

        infoPanel.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        180
                )
        );

        infoPanel.setBorder(
                BorderFactory.createTitledBorder(
                        "Class Information"
                )
        );

        infoPanel.add(classLabel);
        infoPanel.add(hpLabel);
        infoPanel.add(mpLabel);
        infoPanel.add(attackLabel);
        infoPanel.add(defenseLabel);
        infoPanel.add(skillLabel);

        // Buttons
        JPanel buttonPanel =
                new JPanel(
                        new GridLayout(
                                1,
                                2,
                                15,
                                0
                        )
                );

        buttonPanel.setMaximumSize(
                new Dimension(
                        Integer.MAX_VALUE,
                        50
                )
        );                

        buttonPanel.add(backButton);
        buttonPanel.add(startButton);

        contentPanel.add(namePanel);

        contentPanel.add(
                Box.createVerticalStrut(15)
        );

        contentPanel.add(classPanel);

        contentPanel.add(
                Box.createVerticalStrut(15)
        );

        contentPanel.add(infoPanel);

        contentPanel.add(
                Box.createVerticalStrut(20)
        );

        contentPanel.add(buttonPanel);

        add(
                titleLabel,
                BorderLayout.NORTH
        );

        add(
                contentPanel,
                BorderLayout.CENTER
        );
    }

    // -------------------------
    // Events
    // -------------------------

    private void registerEvents() {

        knightButton.addActionListener(
                e -> selectClass("Knight")
        );

        archerButton.addActionListener(
                e -> selectClass("Archer")
        );

        mageButton.addActionListener(
                e -> selectClass("Mage")
        );

        startButton.addActionListener(
                e -> startGame()
        );

        backButton.addActionListener(
                e -> goBack()
        );
    }

    // -------------------------
    // Class Selection
    // -------------------------

    private void selectClass(
            String playerClass) {

        selectedClass = playerClass;

        updateClassInfo(playerClass);
    }

    private void updateClassInfo(
            String playerClass) {

        Player previewPlayer;

        if (playerClass.equals("Knight")) {

            previewPlayer =
                    new Knight("Preview");

        } else if (playerClass.equals("Archer")) {

            previewPlayer =
                    new Archer("Preview");

        } else {

            previewPlayer =
                    new Mage("Preview");
        }

        classLabel.setText(
                "Class: "
                        + playerClass
        );

        hpLabel.setText(
                "HP: "
                        + previewPlayer.getMaxHp()
        );

        mpLabel.setText(
                "MP: "
                        + previewPlayer.getMaxMp()
        );

        attackLabel.setText(
                "Attack: "
                        + previewPlayer.getAttack()
        );

        defenseLabel.setText(
                "Defense: "
                        + previewPlayer.getDefense()
        );

        skillLabel.setText(
                "Skill: "
                        + previewPlayer.getSkillName()
        );
    }

    // -------------------------
    // Start Game
    // -------------------------

    private void startGame() {

        String playerName =
                nameField.getText().trim();

        if (playerName.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please enter your character name.",
                    "Invalid Name",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        game.startNewGame(
                playerName,
                selectedClass
        );

        Player player =
                game.getPlayer();

        JOptionPane.showMessageDialog(
                this,
                "Character created!\n\n"
                        + "Name: "
                        + player.getName()
                        + "\nClass: "
                        + player.getClass()
                                .getSimpleName()
                        + "\nHP: "
                        + player.getHp()
                        + "/"
                        + player.getMaxHp()
                        + "\nMP: "
                        + player.getMp()
                        + "/"
                        + player.getMaxMp()
                        + "\nSkill: "
                        + player.getSkillName(),
                "New Game",
                JOptionPane.INFORMATION_MESSAGE
        );

        GameFrame gameFrame =
                new GameFrame(
                        game,
                        previousFrame
                );

        gameFrame.setVisible(true);

        dispose();
    }

    // -------------------------
    // Back
    // -------------------------

    private void goBack() {

        previousFrame.setVisible(true);

        dispose();
    }
}
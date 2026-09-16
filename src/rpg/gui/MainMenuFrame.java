package rpg.gui;

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

import rpg.exception.InvalidSaveException;
import rpg.game.Game;

public class MainMenuFrame extends JFrame {

    private Game game;

    private JButton newGameButton;
    private JButton loadGameButton;
    private JButton exitButton;

    public MainMenuFrame(Game game) {

        this.game = game;

        initializeFrame();
        createComponents();
        createLayout();
        registerEvents();
    }

    // -------------------------
    // Frame Setup
    // -------------------------

    private void initializeFrame() {

        setTitle("Turn-Based RPG");

        setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE
        );

        setSize(500, 400);

        setMinimumSize(
                new Dimension(500, 400)
        );

        setLocationRelativeTo(null);

        setResizable(false);
    }

    // -------------------------
    // Components
    // -------------------------

    private void createComponents() {

        newGameButton =
                new JButton("New Game");

        loadGameButton =
                new JButton("Load Game");

        exitButton =
                new JButton("Exit");
    }

    // -------------------------
    // Layout
    // -------------------------

    private void createLayout() {

        setLayout(
                new BorderLayout()
        );

        JLabel titleLabel =
                new JLabel(
                        "TURN-BASED RPG",
                        SwingConstants.CENTER
                );

        titleLabel.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        28
                )
        );

        titleLabel.setBorder(
                BorderFactory.createEmptyBorder(
                        60,
                        20,
                        30,
                        20
                )
        );

        JPanel buttonPanel =
                new JPanel(
                        new GridLayout(
                                3,
                                1,
                                10,
                                15
                        )
                );

        buttonPanel.setBorder(
                BorderFactory.createEmptyBorder(
                        20,
                        120,
                        70,
                        120
                )
        );

        buttonPanel.add(
                newGameButton
        );

        buttonPanel.add(
                loadGameButton
        );

        buttonPanel.add(
                exitButton
        );

        add(
                titleLabel,
                BorderLayout.NORTH
        );

        add(
                buttonPanel,
                BorderLayout.CENTER
        );
    }

    // -------------------------
    // Events
    // -------------------------

    private void registerEvents() {

        newGameButton.addActionListener(
                e -> openNewGame()
        );

        loadGameButton.addActionListener(
                e -> loadGame()
        );

        exitButton.addActionListener(
                e -> exitGame()
        );
    }

    // -------------------------
    // New Game
    // -------------------------

    private void openNewGame() {

        CharacterSelectFrame characterSelect =
                new CharacterSelectFrame(
                        game,
                        this
                );

        setVisible(false);

        characterSelect.setVisible(true);
    }

    // -------------------------
    // Load Game
    // -------------------------

    private void loadGame() {

        try {

            game.loadGame();

            JOptionPane.showMessageDialog(
                    this,
                    "Game loaded successfully!\n\n"
                            + "Player: "
                            + game.getPlayer()
                                    .getName()
                            + "\nClass: "
                            + game.getPlayer()
                                    .getClass()
                                    .getSimpleName()
                            + "\nLevel: "
                            + game.getPlayer()
                                    .getLevel()
                            + "\nStage: "
                            + game.getStage(),
                    "Load Game",
                    JOptionPane.INFORMATION_MESSAGE
            );

            GameFrame gameFrame =
                new GameFrame(
                        game,
                        this
                );

            setVisible(false);

            gameFrame.setVisible(true);

        } catch (IOException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Save file could not be found or read.",
                    "Load Error",
                    JOptionPane.ERROR_MESSAGE
            );

        } catch (InvalidSaveException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Invalid save file:\n"
                            + e.getMessage(),
                    "Load Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // -------------------------
    // Exit
    // -------------------------

    private void exitGame() {

        int choice =
                JOptionPane.showConfirmDialog(
                        this,
                        "Are you sure you want to exit?",
                        "Exit Game",
                        JOptionPane.YES_NO_OPTION
                );

        if (choice
                == JOptionPane.YES_OPTION) {

            System.exit(0);
        }
    }
}
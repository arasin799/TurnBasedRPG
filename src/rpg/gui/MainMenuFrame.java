package rpg.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
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

        setTitle("Hero's Journey");

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

        newGameButton =
                new JButton("New Game");

        loadGameButton =
                new JButton("Load Game");

        exitButton =
                new JButton("Exit");

        GameTheme.styleButton(newGameButton, true);
        GameTheme.styleButton(loadGameButton, false);
        GameTheme.styleButton(exitButton, false);
    }

    // -------------------------
    // Layout
    // -------------------------

    private void createLayout() {

        JPanel background = GameTheme.createBackdrop();
        background.setLayout(new BorderLayout());

        JPanel menu = new JPanel();
        menu.setOpaque(false);
        menu.setLayout(new javax.swing.BoxLayout(
                menu, javax.swing.BoxLayout.Y_AXIS));
        menu.setBorder(BorderFactory.createEmptyBorder(65, 30, 30, 30));

        JLabel titleLabel = new JLabel("HERO'S JOURNEY", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Monospaced", Font.BOLD, 38));
        titleLabel.setForeground(GameTheme.GOLD);
        titleLabel.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel(
                "A TURN-BASED PIXEL RPG", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Monospaced", Font.PLAIN, 14));
        subtitleLabel.setForeground(GameTheme.MUTED);
        subtitleLabel.setAlignmentX(CENTER_ALIGNMENT);

        newGameButton.setAlignmentX(CENTER_ALIGNMENT);
        loadGameButton.setAlignmentX(CENTER_ALIGNMENT);
        exitButton.setAlignmentX(CENTER_ALIGNMENT);

        JLabel footerLabel = new JLabel(
                "CHOOSE YOUR HERO  -  BEGIN YOUR ADVENTURE",
                SwingConstants.CENTER);
        footerLabel.setFont(new Font("Monospaced", Font.PLAIN, 11));
        footerLabel.setForeground(GameTheme.MUTED);
        footerLabel.setAlignmentX(CENTER_ALIGNMENT);

        menu.add(titleLabel);
        menu.add(javax.swing.Box.createVerticalStrut(10));
        menu.add(subtitleLabel);
        menu.add(javax.swing.Box.createVerticalStrut(49));
        menu.add(newGameButton);
        menu.add(javax.swing.Box.createVerticalStrut(12));
        menu.add(loadGameButton);
        menu.add(javax.swing.Box.createVerticalStrut(12));
        menu.add(exitButton);
        menu.add(javax.swing.Box.createVerticalStrut(40));
        menu.add(footerLabel);

        background.add(menu, BorderLayout.CENTER);
        setContentPane(background);
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
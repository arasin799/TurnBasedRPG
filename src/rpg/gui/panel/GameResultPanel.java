package rpg.gui.panel;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import rpg.gui.GameFrame;

public class GameResultPanel extends JPanel {

    private GameFrame gameFrame;

    private JLabel titleLabel;
    private JLabel messageLabel;

    private JButton mainMenuButton;

    public GameResultPanel(
            GameFrame gameFrame,
            boolean victory) {

        this.gameFrame = gameFrame;

        createComponents(victory);
        createLayout();
        registerEvents();
    }

    // -------------------------
    // Components
    // -------------------------

    private void createComponents(
            boolean victory) {

        titleLabel =
                new JLabel(
                        "",
                        SwingConstants.CENTER
                );

        messageLabel =
                new JLabel(
                        "",
                        SwingConstants.CENTER
                );

        mainMenuButton =
                new JButton(
                        "Return to Main Menu"
                );

        if (victory) {

            titleLabel.setText(
                    "GAME COMPLETED!"
            );

            messageLabel.setText(
                    "<html><div style='text-align:center;'>"
                            + "You defeated the Dark Mage!<br>"
                            + "The kingdom is safe once again."
                            + "</div></html>"
            );

        } else {

            titleLabel.setText(
                    "GAME OVER"
            );

            messageLabel.setText(
                    "<html><div style='text-align:center;'>"
                            + "Your character has been defeated.<br>"
                            + "Return to the Main Menu to load a save "
                            + "or start a new game."
                            + "</div></html>"
            );
        }

        titleLabel.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        36
                )
        );

        messageLabel.setFont(
                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        18
                )
        );
    }

    // -------------------------
    // Layout
    // -------------------------

    private void createLayout() {

        setLayout(
                new BorderLayout(
                        20,
                        20
                )
        );

        setBorder(
                BorderFactory.createEmptyBorder(
                        80,
                        120,
                        80,
                        120
                )
        );

        add(
                titleLabel,
                BorderLayout.NORTH
        );

        add(
                messageLabel,
                BorderLayout.CENTER
        );

        add(
                mainMenuButton,
                BorderLayout.SOUTH
        );
    }

    // -------------------------
    // Events
    // -------------------------

    private void registerEvents() {

        mainMenuButton.addActionListener(
                e -> gameFrame.showMainMenu()
        );
    }
}
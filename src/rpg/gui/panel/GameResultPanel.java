package rpg.gui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import rpg.gui.GameFrame;
import rpg.gui.GameTheme;

/** Minimal ending screen: VICTORY or grayscale GAME OVER with a gray trophy. */
public class GameResultPanel extends JPanel {

    private static final Color GRAY_BG = new Color(37, 40, 46);
    private static final Color GRAY_PANEL = new Color(61, 65, 73);
    private static final Color GRAY_BORDER = new Color(122, 127, 138);
    private static final Color GRAY_TEXT = new Color(216, 218, 224);

    public GameResultPanel(GameFrame frame, boolean victory) {
        setBackground(victory ? GameTheme.BACKGROUND : GRAY_BG);
        setLayout(new BorderLayout(0, 13));
        setBorder(BorderFactory.createEmptyBorder(24, 30, 20, 30));

        JPanel details = new JPanel();
        details.setOpaque(false);
        details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));

        if (!victory) {
            JPanel trophy = new GrayTrophy();
            trophy.setAlignmentX(CENTER_ALIGNMENT);
            trophy.setPreferredSize(new Dimension(93, 92));
            trophy.setMinimumSize(new Dimension(93, 92));
            trophy.setMaximumSize(new Dimension(93, 92));
            details.add(trophy);
            details.add(Box.createVerticalStrut(9));
        }
        details.add(label(victory ? "VICTORY" : "GAME OVER", 31,
                victory ? GameTheme.GOLD : GRAY_TEXT));
        details.add(Box.createVerticalStrut(15));
        details.add(label(victory ? "DARK MAGE DEFEATED" : "YOUR HERO HAS FALLEN",
                14, victory ? GameTheme.TEXT : GRAY_TEXT));
        details.add(Box.createVerticalStrut(9));
        details.add(label(victory ? "The adventure is complete."
                : "Return to Main Menu to load a save or start over.",
                12, victory ? GameTheme.MUTED : new Color(175, 180, 190)));

        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        center.add(details);
        add(center, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new GridLayout(1, victory ? 2 : 1, 12, 0));
        buttons.setOpaque(false);
        if (victory) {
            JButton hub = makeButton("RETURN TO HUB", true, false);
            hub.addActionListener(e -> frame.showHub());
            buttons.add(hub);
        }
        JButton menu = makeButton("MAIN MENU", !victory, !victory);
        menu.addActionListener(e -> frame.showMainMenu());
        buttons.add(menu);
        add(buttons, BorderLayout.SOUTH);
    }

    private JLabel label(String value, int size, Color color) {
        JLabel result = new JLabel(value, SwingConstants.CENTER);
        result.setFont(new Font("Monospaced", Font.BOLD, size));
        result.setForeground(color);
        result.setAlignmentX(CENTER_ALIGNMENT);
        return result;
    }

    private JButton makeButton(String title, boolean primary, boolean grayscale) {
        JButton button = new JButton(title);
        GameTheme.styleButton(button, primary && !grayscale);
        button.setFont(new Font("Monospaced", Font.BOLD, 12));
        button.setPreferredSize(new Dimension(190, 43));
        if (grayscale) {
            button.setBackground(GRAY_PANEL);
            button.setForeground(GRAY_TEXT);
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(GRAY_BORDER, 2),
                    BorderFactory.createEmptyBorder(7, 12, 7, 12)));
        }
        return button;
    }

    /** Draws a small, flat gray pixel trophy without glow or extra assets. */
    private static final class GrayTrophy extends JPanel {
        GrayTrophy() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            int x = (getWidth() - 72) / 2;
            int y = 5;
            g.setColor(GRAY_PANEL);
            g.fillRect(x, y, 72, 79);
            g.setColor(GRAY_BORDER);
            g.drawRect(x, y, 71, 78);

            // Cup and handles, built from aligned blocks to match existing sprites.
            g.setColor(new Color(174, 179, 188));
            g.fillRect(x + 19, y + 14, 34, 26);
            g.fillRect(x + 10, y + 16, 9, 8);
            g.fillRect(x + 7, y + 22, 8, 12);
            g.fillRect(x + 53, y + 16, 9, 8);
            g.fillRect(x + 57, y + 22, 8, 12);
            g.fillRect(x + 26, y + 40, 20, 9);
            g.fillRect(x + 32, y + 49, 8, 11);
            g.fillRect(x + 19, y + 60, 34, 7);
            // Simple gray shadow/shading; no glitter or gold.
            g.setColor(new Color(115, 120, 130));
            g.fillRect(x + 44, y + 18, 9, 20);
            g.fillRect(x + 26, y + 40, 20, 4);
            g.dispose();
        }
    }
}

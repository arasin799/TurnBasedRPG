package rpg.gui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.plaf.basic.BasicButtonUI;

/** Shared colors and components for the pixel-RPG interface. */
public final class GameTheme {

    public static final Color BACKGROUND = new Color(14, 20, 36);
    public static final Color PANEL = new Color(31, 42, 67);
    public static final Color PANEL_BORDER = new Color(84, 100, 135);
    public static final Color GOLD = new Color(238, 197, 107);
    public static final Color TEXT = new Color(243, 237, 221);
    public static final Color MUTED = new Color(161, 176, 198);

    private GameTheme() {
        // Utility class: do not create instances.
    }

    public static void styleButton(JButton button, boolean primary) {
        button.setUI(new BasicButtonUI());
        button.setBackground(primary ? GOLD : PANEL);
        button.setForeground(primary ? BACKGROUND : TEXT);
        button.setFont(new Font("Monospaced", Font.BOLD, 16));
        button.setPreferredSize(new Dimension(280, 46));
        button.setMaximumSize(new Dimension(280, 46));
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(primary ? GOLD : PANEL_BORDER, 2),
                BorderFactory.createEmptyBorder(9, 14, 9, 14)));
    }

    /** Draws a simple pixel-inspired night sky without external image files. */
    public static JPanel createBackdrop() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                Graphics2D g = (Graphics2D) graphics.create();
                int width = getWidth();
                int height = getHeight();

                g.setColor(BACKGROUND);
                g.fillRect(0, 0, width, height);

                // A few stable pixel stars. No random flickering or timers.
                g.setColor(new Color(79, 107, 151));
                for (int x = 35; x < width - 25; x += 49) {
                    for (int y = 26; y < height - 70; y += 48) {
                        if ((x * 3 + y) % 7 < 2) {
                            g.fillRect(x, y, 3, 3);
                        }
                    }
                }

                // Pixel ground silhouettes near the bottom.
                g.setColor(new Color(20, 31, 54));
                for (int x = 0; x < width; x += 32) {
                    int peak = height - 68 - ((x / 32) % 4) * 10;
                    g.fillRect(x, peak, 32, height - peak);
                }
                g.setColor(new Color(28, 41, 66));
                g.fillRect(0, height - 36, width, 36);
                g.dispose();
            }
        };
    }
}

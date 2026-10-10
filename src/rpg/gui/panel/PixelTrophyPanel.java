package rpg.gui.panel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;
import rpg.gui.GameTheme;

/** One flat pixel trophy: gold on Victory, gray on Game Over. No image files. */
public class PixelTrophyPanel extends JPanel {
    private final boolean gold;

    public PixelTrophyPanel(boolean gold) {
        this.gold = gold;
        setOpaque(false);
        setPreferredSize(new Dimension(93, 92));
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        // Integer-size blocks preserve the 16-bit / pixel-art appearance.
        int unit = Math.max(2, Math.min(getWidth(), getHeight()) / 18);
        int x = (getWidth() - unit * 18) / 2;
        int y = (getHeight() - unit * 18) / 2;

        Color outline = gold ? new Color(93, 61, 31) : new Color(93, 97, 104);
        Color body = gold ? GameTheme.GOLD : new Color(174, 179, 188);
        Color shadow = gold ? new Color(191, 127, 57) : new Color(116, 122, 132);
        Color shine = gold ? new Color(255, 224, 145) : new Color(203, 207, 213);
        Color base = gold ? new Color(139, 91, 43) : new Color(85, 90, 100);
        // Trophy silhouette: handles, cup, stem and foot.
        g.setColor(outline);
        block(g, x, y, unit, 4, 3, 10, 2);
        block(g, x, y, unit, 3, 5, 2, 6);
        block(g, x, y, unit, 13, 3, 2, 6);
        block(g, x, y, unit, 5, 2, 8, 11);
        block(g, x, y, unit, 8, 13, 2, 3);
        block(g, x, y, unit, 5, 15, 8, 2);
        g.setColor(body);
        block(g, x, y, unit, 6, 3, 6, 8);
        block(g, x, y, unit, 4, 6, 2, 4);
        block(g, x, y, unit, 12, 6, 2, 4);
        block(g, x, y, unit, 8, 11, 2, 4);
        block(g, x, y, unit, 6, 15, 6, 1);
        g.setColor(shadow);
        block(g, x, y, unit, 10, 4, 2, 7);
        block(g, x, y, unit, 11, 7, 2, 2);
        block(g, x, y, unit, 6, 16, 6, 1);
        g.setColor(shine);
        block(g, x, y, unit, 7, 4, 2, 5);
        block(g, x, y, unit, 5, 6, 1, 3);
        g.setColor(base);
        block(g, x, y, unit, 7, 14, 4, 1);
        g.dispose();
    }

    private void block(Graphics2D g, int x, int y, int u,
                       int cx, int cy, int w, int h) {
        g.fillRect(x + cx * u, y + cy * u, w * u, h * u);
    }
}

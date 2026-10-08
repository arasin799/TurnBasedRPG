package rpg.gui.panel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import rpg.game.Game;
import rpg.gui.GameTheme;

/** Visual hub only. Battle starts separately when the player presses Battle. */
public class StageHubPanel extends JPanel {

    private final Game game;
    private int selectedStage = -1;
    private BufferedImage monsterSprite;

    public StageHubPanel(Game game) {
        this.game = game;
        setOpaque(true);
        setBackground(GameTheme.BACKGROUND);
    }

    public void setSelectedStage(int stage) {
        if (selectedStage != stage) {
            selectedStage = stage;
            monsterSprite = loadMonster(game.getStageName(stage));
        }
        repaint();
    }

    public int getSelectedStage() {
        return selectedStage;
    }

    private BufferedImage loadMonster(String monsterName) {
        String fileName = "assets/monster/"
                + monsterName.toLowerCase().replace(' ', '_') + ".png";
        try (InputStream stream = StageHubPanel.class.getClassLoader()
                .getResourceAsStream(fileName)) {
            if (stream != null) {
                return ImageIO.read(stream);
            }
        } catch (IOException ignored) {
            // Project asset fallback below.
        }
        File file = new File(fileName);
        if (!file.isFile()) {
            file = new File("TurnBasedRPG", fileName);
        }
        try {
            return file.isFile() ? ImageIO.read(file) : null;
        } catch (IOException ignored) {
            return null;
        }
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        int w = getWidth();
        int h = getHeight();

        // Pixel-night backdrop. No animation and no image dependencies.
        g.setColor(GameTheme.BACKGROUND);
        g.fillRect(0, 0, w, h);
        g.setColor(new Color(58, 83, 123));
        for (int i = 0; i < 28; i++) {
            int x = (23 + i * 113) % Math.max(1, w);
            int y = (13 + i * 39) % Math.max(1, h - 45);
            g.fillRect(x, y, 2, 2);
        }
        g.setColor(new Color(24, 36, 60));
        for (int x = 0; x < w; x += 36) {
            int top = h - 35 - (x / 36 % 4) * 9;
            g.fillRect(x, top, 36, h - top);
        }

        int bx = 26;
        int by = 16;
        int bw = Math.max(100, w - 52);
        int bh = Math.max(100, h - 31);
        g.setColor(GameTheme.PANEL);
        g.fillRect(bx, by, bw, bh);
        g.setColor(GameTheme.PANEL_BORDER);
        g.setStroke(new BasicStroke(2));
        g.drawRect(bx, by, bw, bh);
        g.setColor(GameTheme.GOLD);
        g.fillRect(bx, by, 76, 3);
        g.fillRect(bx + bw - 76, by, 76, 3);

        if (selectedStage < 0) {
            g.dispose();
            return;
        }

        int leftX = bx + 27;
        String name = game.getStageName(selectedStage);
        boolean cleared = selectedStage < game.getStage();
        Color statusColor = cleared ? new Color(115, 215, 160) : GameTheme.GOLD;

        text(g, "CURRENT CHALLENGE", leftX, by + 27, 12, GameTheme.MUTED);
        text(g, String.format("STAGE %02d", selectedStage + 1),
                leftX, by + 76, 36, GameTheme.GOLD);
        text(g, name.toUpperCase(), leftX, by + 113,
                name.length() > 8 ? 20 : 26, GameTheme.TEXT);
        text(g, cleared ? "CLEARED  /  REPLAY FOR REWARDS"
                : "NEW STAGE  /  READY TO FIGHT", leftX, by + 139, 11, statusColor);
        text(g, "ENTER SHOP OR INVENTORY TO PREPARE", leftX,
                by + 164, 10, GameTheme.MUTED);

        int spriteSize = Math.min(131, Math.max(65, bh - 64));
        int spriteX = bx + bw - spriteSize - 47;
        int spriteY = by + 20;
        g.setColor(new Color(16, 27, 47));
        g.fillRect(spriteX - 8, spriteY - 4, spriteSize + 16, spriteSize + 11);
        g.setColor(statusColor);
        g.drawRect(spriteX - 8, spriteY - 4, spriteSize + 15, spriteSize + 10);
        if (monsterSprite != null) {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g.drawImage(monsterSprite, spriteX, spriteY, spriteSize, spriteSize, null);
        } else {
            text(g, "ENEMY", spriteX + 9, spriteY + spriteSize / 2,
                    15, GameTheme.MUTED);
        }

        // Stage-progress pips: a filled square is cleared, an outline is locked.
        int marksY = by + bh - 26;
        text(g, "PROGRESS", leftX, marksY + 2, 10, GameTheme.MUTED);
        int marksX = leftX + 100;
        for (int i = 0; i < game.getStageCount(); i++) {
            int x = marksX + i * 33;
            Color c = i < game.getStage()
                    ? new Color(115, 215, 160)
                    : i == selectedStage ? GameTheme.GOLD : GameTheme.PANEL_BORDER;
            g.setColor(c);
            if (i < game.getStage() || i == selectedStage) {
                g.fillRect(x, marksY - 8, 15, 15);
            } else {
                g.drawRect(x, marksY - 8, 14, 14);
            }
        }
        text(g, "PRESS BATTLE TO START", bx + bw - 196,
                marksY + 2, 10, GameTheme.GOLD);
        g.dispose();
    }

    private void text(Graphics2D g, String value, int x, int baseline,
                      int size, Color color) {
        g.setColor(color);
        g.setFont(new Font("Monospaced", Font.BOLD, size));
        g.drawString(value, x, baseline);
    }
}

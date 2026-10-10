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
import rpg.model.character.monster.Monster;
import rpg.gui.GameTheme;

/** Visual hub only. Battle starts separately when the player presses Battle. */
public class StageHubPanel extends JPanel {

    private final Game game;
    private int selectedStage = -1;
    private BufferedImage monsterSprite;
    private Monster previewMonster;

    public StageHubPanel(Game game) {
        this.game = game;
        setOpaque(true);
        setBackground(GameTheme.BACKGROUND);
    }

    public void setSelectedStage(int stage) {
        if (selectedStage != stage) {
            selectedStage = stage;
            monsterSprite = loadMonster(game.getStageName(stage));
            previewMonster = game.previewMonsterForStage(stage);
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

        int leftX = bx + 25;
        String name = game.getStageName(selectedStage);
        boolean cleared = selectedStage < game.getStage();
        Color statusColor = cleared ? new Color(115, 215, 160) : GameTheme.GOLD;

        // Reserve a dedicated footer for progress; never draw stats over it.
        int marksY = by + bh - 24;
        boolean compact = bh < 300;
        text(g, "CURRENT CHALLENGE", leftX, by + 24, 11, GameTheme.MUTED);
        text(g, String.format("STAGE %02d", selectedStage + 1),
                leftX, by + (compact ? 61 : 67), 32, GameTheme.GOLD);
        text(g, name.toUpperCase(), leftX, by + (compact ? 87 : 98),
                name.length() > 8 ? 20 : 24, GameTheme.TEXT);
        text(g, cleared ? "CLEARED  /  REPLAY FOR REWARDS"
                : "NEW STAGE  /  READY TO FIGHT", leftX,
                by + (compact ? 107 : 120), 10, statusColor);

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

        // Use all available space to the LEFT of the monster sprite.
        // The panel grows vertically when GameFrame is taller, but is also
        // safe in the original 750x520 window.
        if (previewMonster != null) {
            int sx = bx + 24;
            int sy = by + (compact ? 115 : 132);
            int sw = Math.max(220, Math.min(470, spriteX - sx - 20));
            int sh = Math.max(87, Math.min(148, marksY - sy - 17));

            g.setColor(new Color(18, 29, 49));
            g.fillRect(sx, sy, sw, sh);
            g.setColor(GameTheme.PANEL_BORDER);
            g.setStroke(new BasicStroke(1));
            g.drawRect(sx, sy, sw - 1, sh - 1);
            g.setColor(GameTheme.GOLD);
            g.fillRect(sx, sy, 69, 2);

            int tx = sx + 13;
            int col2 = sx + Math.min(216, sw / 2 + 7);
            boolean smallBox = sh < 125;
            int headerY = sy + (smallBox ? 16 : 22);
            int firstY = sy + (smallBox ? 32 : 45);
            int rowHeight = smallBox ? 15 : 21;
            int statSize = smallBox ? 11 : 12;
            int detailSize = smallBox ? 10 : 11;

            text(g, "MONSTER STATS", tx, headerY, 12, GameTheme.GOLD);
            text(g, "HP   " + previewMonster.getMaxHp(), tx, firstY,
                    statSize, GameTheme.TEXT);
            text(g, "MP   " + previewMonster.getMaxMp(), col2, firstY,
                    statSize, GameTheme.TEXT);
            text(g, "ATK  " + previewMonster.getAttack(), tx,
                    firstY + rowHeight, statSize, GameTheme.TEXT);
            text(g, "DEF  " + previewMonster.getDefense(), col2,
                    firstY + rowHeight, statSize, GameTheme.TEXT);
            text(g, "EXP  +" + previewMonster.getExpReward(), tx,
                    firstY + rowHeight * 2, detailSize, GameTheme.MUTED);
            text(g, "ENEMY ULT: " + previewMonster.getUltimateSkillName()
                    + "  [" + previewMonster.getUltimateChargeRequired() + "]",
                    tx, firstY + rowHeight * 3, detailSize, GameTheme.TEXT);
        }

        // Stage-progress pips stay below the stats panel at every size.
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

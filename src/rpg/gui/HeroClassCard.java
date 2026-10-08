package rpg.gui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import java.awt.RenderingHints;

import javax.swing.JButton;
import javax.swing.plaf.basic.BasicButtonUI;

import rpg.model.character.player.Player;

/** Clickable class card, with a tiny pixel hero drawn in plain Swing. */
public class HeroClassCard extends JButton {

    private final String heroClass;
    private final Player preview;
    private final Color accent;
    private final Runnable onChoose;
    private boolean chosen;
    private final BufferedImage spriteImage;

    public HeroClassCard(String heroClass, Player preview,
                         Color accent, Runnable onChoose) {
        this.heroClass = heroClass;
        this.preview = preview;
        this.accent = accent;
        this.onChoose = onChoose;
        this.spriteImage = loadSprite("assets/player/"
                + heroClass.toLowerCase(java.util.Locale.ROOT) + ".png");
        setUI(new BasicButtonUI());
        setBorderPainted(false);
        setFocusPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setToolTipText("Choose " + heroClass + " - " + preview.getSkillName());
        getAccessibleContext().setAccessibleDescription(
                heroClass + ", HP " + preview.getMaxHp()
                + ", MP " + preview.getMaxMp()
                + ", Attack " + preview.getAttack()
                + ", Defense " + preview.getDefense());
        addActionListener(e -> this.onChoose.run());
    }

    public void setChosen(boolean chosen) {
        this.chosen = chosen;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        int w = getWidth();
        int h = getHeight();

        g.setColor(GameTheme.PANEL);
        g.fillRect(0, 0, w, h);
        g.setColor(chosen ? GameTheme.GOLD : GameTheme.PANEL_BORDER);
        g.fillRect(0, 0, w, 3);
        g.fillRect(0, h - 3, w, 3);
        g.fillRect(0, 0, 3, h);
        g.fillRect(w - 3, 0, 3, h);

        // Sprite frame deliberately uses sharp corners and crisp pixels.
        int portraitX = (w - 108) / 2;
        int portraitY = 18;
        g.setColor(new Color(18, 27, 46));
        g.fillRect(portraitX, portraitY, 108, 103);
        g.setColor(chosen ? GameTheme.GOLD : accent);
        g.drawRect(portraitX, portraitY, 107, 102);
        // Scale 16x16 sprite to 80x80 with nearest-neighbor pixels.
        if (spriteImage != null) {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g.drawImage(spriteImage, portraitX + 14, portraitY + 11,
                    80, 80, null);
        } else {
            // Keep the original hand-drawn character if an asset is missing.
            drawPixelHero(g, portraitX + 14, portraitY + 10, 5);
        }

        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setFont(new Font("Monospaced", Font.BOLD, 18));
        g.setColor(chosen ? GameTheme.GOLD : GameTheme.TEXT);
        center(g, heroClass.toUpperCase(), w, 147);

        g.setFont(new Font("Monospaced", Font.PLAIN, 11));
        g.setColor(GameTheme.MUTED);
        center(g, roleText(), w, 164);

        g.setColor(GameTheme.PANEL_BORDER);
        g.drawLine(16, 172, w - 16, 172);

        g.setFont(new Font("Monospaced", Font.BOLD, 12));
        g.setColor(GameTheme.TEXT);
        center(g, "HP " + preview.getMaxHp() + "   MP " + preview.getMaxMp(), w, 191);
        center(g, "ATK " + preview.getAttack() + "   DEF " + preview.getDefense(), w, 209);

        g.setFont(new Font("Monospaced", Font.PLAIN, 11));
        g.setColor(accent);
        center(g, preview.getSkillName().toUpperCase(), w, 227);

        g.setFont(new Font("Monospaced", Font.BOLD, 10));
        g.setColor(chosen ? GameTheme.GOLD : GameTheme.MUTED);
        center(g, chosen ? "< SELECTED >" : "CLICK TO CHOOSE", w, h - 11);

        g.dispose();
        super.paintComponent(graphics);
    }

    /** Load sprite from project assets or packaged classpath. */
    private static BufferedImage loadSprite(String path) {
        try (InputStream stream = HeroClassCard.class.getClassLoader()
                .getResourceAsStream(path)) {
            if (stream != null) {
                BufferedImage image = ImageIO.read(stream);
                if (image != null) return image;
            }
        } catch (IOException ignored) {
            // Try file-system paths below.
        }

        File file = new File(path);
        if (!file.isFile()) {
            file = new File("TurnBasedRPG", path);
        }
        if (file.isFile()) {
            try {
                return ImageIO.read(file);
            } catch (IOException ignored) {
                // Fall back to the original built-in pixel drawing.
            }
        }
        return null;
    }

    private String roleText() {
        switch (heroClass) {
            case "Knight": return "GUARDIAN";
            case "Archer": return "RANGER";
            default: return "SPELLCASTER";
        }
    }

    private void center(Graphics2D g, String text, int width, int y) {
        int x = (width - g.getFontMetrics().stringWidth(text)) / 2;
        g.drawString(text, x, y);
    }

    private void tile(Graphics2D g, int ox, int oy, int scale,
                      int x, int y, int width, int height, Color color) {
        g.setColor(color);
        g.fillRect(ox + x * scale, oy + y * scale,
                   width * scale, height * scale);
    }

    /** Three simple 16x16-like silhouettes, no image dependencies. */
    private void drawPixelHero(Graphics2D g, int x, int y, int s) {
        Color outline = new Color(8, 13, 27);
        Color skin = new Color(232, 177, 123);
        Color brown = new Color(111, 70, 52);
        Color shadow = new Color(43, 54, 83);

        // Feet and boots common to all classes.
        tile(g, x, y, s, 5, 13, 3, 2, outline);
        tile(g, x, y, s, 9, 13, 3, 2, outline);
        tile(g, x, y, s, 5, 13, 3, 1, brown);
        tile(g, x, y, s, 9, 13, 3, 1, brown);

        switch (heroClass) {
            case "Knight":
                tile(g, x, y, s, 4, 7, 8, 6, outline);
                tile(g, x, y, s, 5, 8, 6, 5, new Color(62, 103, 160));
                tile(g, x, y, s, 7, 8, 2, 5, new Color(145, 180, 212));
                tile(g, x, y, s, 4, 2, 8, 6, outline);
                tile(g, x, y, s, 5, 2, 6, 4, new Color(160, 179, 197));
                tile(g, x, y, s, 6, 5, 4, 2, skin);
                tile(g, x, y, s, 5, 3, 6, 1, new Color(207, 219, 223));
                tile(g, x, y, s, 7, 0, 2, 3, GameTheme.GOLD);
                tile(g, x, y, s, 7, 6, 1, 1, outline);
                tile(g, x, y, s, 1, 8, 4, 5, outline);
                tile(g, x, y, s, 2, 9, 3, 3, new Color(157, 180, 207));
                tile(g, x, y, s, 3, 10, 1, 2, GameTheme.GOLD);
                tile(g, x, y, s, 13, 2, 1, 12, new Color(203, 213, 224));
                tile(g, x, y, s, 12, 9, 3, 1, GameTheme.GOLD);
                break;
            case "Archer":
                tile(g, x, y, s, 3, 6, 9, 7, outline);
                tile(g, x, y, s, 4, 7, 7, 6, new Color(56, 119, 91));
                tile(g, x, y, s, 4, 2, 8, 6, outline);
                tile(g, x, y, s, 5, 1, 6, 5, new Color(77, 151, 109));
                tile(g, x, y, s, 6, 4, 4, 3, skin);
                tile(g, x, y, s, 7, 5, 1, 1, outline);
                tile(g, x, y, s, 5, 10, 7, 1, brown);
                tile(g, x, y, s, 2, 8, 3, 3, skin);
                // Pixel bow + arrow.
                tile(g, x, y, s, 13, 3, 1, 11, new Color(181, 119, 57));
                tile(g, x, y, s, 12, 2, 2, 1, new Color(181, 119, 57));
                tile(g, x, y, s, 12, 14, 2, 1, new Color(181, 119, 57));
                tile(g, x, y, s, 12, 4, 1, 9, GameTheme.MUTED);
                tile(g, x, y, s, 2, 9, 12, 1, new Color(204, 201, 172));
                tile(g, x, y, s, 1, 8, 2, 3, GameTheme.GOLD);
                break;
            default:
                tile(g, x, y, s, 3, 8, 10, 6, outline);
                tile(g, x, y, s, 4, 8, 8, 6, new Color(108, 76, 172));
                tile(g, x, y, s, 6, 8, 4, 6, new Color(142, 110, 208));
                tile(g, x, y, s, 6, 4, 5, 4, skin);
                tile(g, x, y, s, 4, 4, 8, 1, outline);
                tile(g, x, y, s, 5, 2, 6, 3, new Color(116, 76, 168));
                tile(g, x, y, s, 6, 0, 3, 2, new Color(116, 76, 168));
                tile(g, x, y, s, 3, 5, 10, 1, new Color(159, 122, 204));
                tile(g, x, y, s, 7, 5, 1, 1, outline);
                tile(g, x, y, s, 13, 4, 1, 11, brown);
                tile(g, x, y, s, 12, 2, 3, 3, new Color(108, 226, 231));
                tile(g, x, y, s, 13, 1, 1, 1, GameTheme.GOLD);
                break;
        }
    }
}

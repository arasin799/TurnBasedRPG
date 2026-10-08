package rpg.gui.panel;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.Queue;

import javax.swing.Timer;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import rpg.game.BattleManager;
import rpg.gui.GameTheme;

/** Pixel arena using the existing 16x16 sprites under assets/player and assets/monster. */
public class BattleArenaPanel extends JPanel {

    private static final Color SKY = new Color(16, 25, 46);
    private static final Color FLOOR = new Color(34, 48, 72);
    private static final Color FLOOR_HIGHLIGHT = new Color(57, 75, 101);

    private final BattleManager battle;
    private final BufferedImage playerSprite;
    private final BufferedImage monsterSprite;

    // UI-only effects. The damage itself is calculated by BattleManager.
    private static final class BattleEffect {
        final boolean onPlayer;
        final String text;
        final Color color;
        final boolean skill;

        BattleEffect(boolean onPlayer, String text, Color color, boolean skill) {
            this.onPlayer = onPlayer;
            this.text = text;
            this.color = color;
            this.skill = skill;
        }
    }

    private final Queue<BattleEffect> effectQueue = new ArrayDeque<>();
    private BattleEffect activeEffect;
    private Timer effectTimer;
    private Runnable effectsFinished;
    private int effectTick;

    public void queueDamage(boolean onPlayer, int damage, boolean special) {
        if (damage > 0) {
            effectQueue.add(new BattleEffect(onPlayer, "-" + damage,
                    special ? GameTheme.GOLD : new Color(245, 130, 132), special));
        }
    }

    public void queueRecovery(int amount, boolean mana) {
        if (amount > 0) {
            effectQueue.add(new BattleEffect(true, "+" + amount + (mana ? " MP" : " HP"),
                    mana ? new Color(119, 176, 250) : new Color(105, 220, 153), false));
        }
    }

    public void queueStatus(boolean onPlayer, String label) {
        effectQueue.add(new BattleEffect(onPlayer, label,
                onPlayer ? new Color(255, 171, 110) : new Color(198, 165, 254), true));
    }

    /** Run visual events sequentially. Battle logic is never delayed or altered. */
    public void playQueuedEffects(Runnable onComplete) {
        effectsFinished = onComplete;
        showNextEffect();
    }

    private void showNextEffect() {
        if (effectTimer != null) {
            effectTimer.stop();
            effectTimer = null;
        }
        activeEffect = effectQueue.poll();
        effectTick = 0;
        if (activeEffect == null) {
            repaint();
            Runnable callback = effectsFinished;
            effectsFinished = null;
            if (callback != null) {
                callback.run();
            }
            return;
        }
        effectTimer = new Timer(40, event -> {
            effectTick++;
            repaint();
            if (effectTick >= 13) {
                showNextEffect();
            }
        });
        effectTimer.start();
        repaint();
    }

    @Override
    public void removeNotify() {
        if (effectTimer != null) {
            effectTimer.stop();
            effectTimer = null;
        }
        effectQueue.clear();
        effectsFinished = null;
        super.removeNotify();
    }

    public BattleArenaPanel(BattleManager battle) {
        this.battle = battle;
        String playerType = battle.getPlayer().getClass().getSimpleName().toLowerCase();
        String monsterType = battle.getMonster().getName().toLowerCase().replace(' ', '_');
        playerSprite = loadSprite("assets/player/" + playerType + ".png");
        monsterSprite = loadSprite("assets/monster/" + monsterType + ".png");
        setOpaque(true);
        setPreferredSize(new Dimension(620, 150));
    }

    private BufferedImage loadSprite(String filename) {
        try (InputStream resource = BattleArenaPanel.class.getClassLoader()
                .getResourceAsStream(filename)) {
            if (resource != null) {
                return ImageIO.read(resource);
            }
        } catch (IOException ignored) {
            // Fall back to the project's assets directory below.
        }
        File file = new File(filename);
        if (!file.isFile()) {
            file = new File("TurnBasedRPG", filename);
        }
        if (file.isFile()) {
            try {
                return ImageIO.read(file);
            } catch (IOException ignored) {
                // Keep the interface usable when an image is missing.
            }
        }
        return null;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        int w = getWidth();
        int h = getHeight();

        g.setColor(SKY);
        g.fillRect(0, 0, w, h);
        // Fixed tiny lights - do not animate, so rendering remains smooth in Swing.
        g.setColor(new Color(73, 101, 145));
        for (int i = 0; i < 18; i++) {
            int x = (23 + i * 97) % Math.max(1, w);
            int y = 11 + (i * 29) % Math.max(1, h / 2);
            g.fillRect(x, y, 3, 3);
        }

        int floorY = h - 29;
        g.setColor(FLOOR);
        g.fillRect(0, floorY, w, h - floorY);
        g.setColor(FLOOR_HIGHLIGHT);
        g.fillRect(0, floorY, w, 3);
        for (int i = 0; i < w; i += 34) {
            g.fillRect(i, floorY + 12 + ((i / 34) % 2) * 6, 15, 2);
        }

        // Separate platforms make the two sides readable even without sprite files.
        int leftX = Math.max(15, w / 4);
        int rightX = Math.min(w - 15, (3 * w) / 4);
        drawPlatform(g, leftX, floorY);
        drawPlatform(g, rightX, floorY);

        drawSprite(g, playerSprite, leftX, floorY, new Color(101, 175, 222), "HERO");
        drawSprite(g, monsterSprite, rightX, floorY, new Color(221, 107, 119), "ENEMY");

        g.setColor(GameTheme.GOLD);
        g.setFont(new Font("Monospaced", Font.BOLD, 14));
        String versus = "VS";
        int tw = g.getFontMetrics().stringWidth(versus);
        g.drawString(versus, (w - tw) / 2, Math.max(34, h / 2));
        paintActiveEffect(g, w, h, leftX, rightX);
        g.dispose();
    }

    private void paintActiveEffect(Graphics2D g, int w, int h, int leftX, int rightX) {
        if (activeEffect == null) {
            return;
        }
        int x = activeEffect.onPlayer ? leftX : rightX;
        float alpha = Math.max(0.18f, 1.0f - effectTick / 16.0f);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        // Brief square hit flash: pixel-style rather than smooth circles.
        if (effectTick <= 5) {
            g.setColor(new Color(activeEffect.color.getRed(),
                    activeEffect.color.getGreen(), activeEffect.color.getBlue(), 55));
            int flashSize = 78 + effectTick * 3;
            g.fillRect(x - flashSize / 2, Math.max(6, h - flashSize - 28),
                    flashSize, flashSize);
        }

        String text = activeEffect.text;
        g.setFont(new Font("Monospaced", Font.BOLD, text.length() > 12 ? 15 : 21));
        int textWidth = g.getFontMetrics().stringWidth(text);
        int y = Math.max(30, h - 76 - effectTick * 2);
        int width = textWidth + 16;
        g.setColor(new Color(10, 15, 29, 220));
        g.fillRect(x - width / 2, y - 24, width, 31);
        g.setColor(activeEffect.color);
        g.drawString(text, x - textWidth / 2, y);
        g.setComposite(AlphaComposite.SrcOver);
    }

    private void drawPlatform(Graphics2D g, int x, int y) {
        g.setColor(new Color(10, 17, 31));
        g.fillRect(x - 62, y - 7, 124, 8);
        g.setColor(new Color(92, 104, 119));
        g.fillRect(x - 53, y - 11, 106, 4);
        g.setColor(new Color(56, 74, 97));
        g.fillRect(x - 45, y - 14, 90, 3);
    }

    private void drawSprite(Graphics2D g, BufferedImage sprite, int x, int groundY,
                            Color fallbackColor, String fallbackText) {
        int size = Math.max(55, Math.min(100, getHeight() - 52));
        int top = groundY - size - 12;
        if (sprite != null) {
            Object oldHint = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g.drawImage(sprite, x - size / 2, top, size, size, null);
            if (oldHint != null) {
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, oldHint);
            }
        } else {
            // File-not-found fallback: a clear blocky silhouette rather than a crash.
            g.setColor(new Color(8, 14, 27));
            g.fillRect(x - 25, top + 13, 50, 51);
            g.setColor(fallbackColor);
            g.fillRect(x - 18, top + 18, 36, 24);
            g.fillRect(x - 24, top + 46, 48, 20);
            g.setColor(GameTheme.TEXT);
            g.setFont(new Font("Monospaced", Font.BOLD, 10));
            int sw = g.getFontMetrics().stringWidth(fallbackText);
            g.drawString(fallbackText, x - sw / 2, top + 80);
        }
    }
}

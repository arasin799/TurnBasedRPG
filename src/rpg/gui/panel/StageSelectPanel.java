package rpg.gui.panel;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicButtonUI;

import rpg.game.Game;
import rpg.gui.GameFrame;
import rpg.gui.GameTheme;

/** Stage selection is purely UI: no battle starts until Battle on the hub. */
public class StageSelectPanel extends JPanel {

    private final Game game;
    private final GameFrame gameFrame;
    private final StageCard[] cards;
    private int chosenStage;
    private final JButton confirmButton;
    private final JLabel hint;

    public StageSelectPanel(Game game, GameFrame gameFrame, int initialStage) {
        this.game = game;
        this.gameFrame = gameFrame;
        this.chosenStage = Math.min(Math.max(0, initialStage),
                game.getUnlockedStageCount() - 1);
        this.cards = new StageCard[game.getStageCount()];

        setLayout(new BorderLayout(0, 15));
        setBackground(GameTheme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(17, 25, 20, 25));

        JPanel header = new JPanel(new GridLayout(2, 1, 0, 4));
        header.setOpaque(false);
        JLabel title = createText("SELECT CHALLENGE", 24, GameTheme.GOLD);
        JLabel subtitle = createText("REPLAY CLEARED STAGES TO FARM EXP AND GOLD", 11,
                GameTheme.MUTED);
        header.add(title);
        header.add(subtitle);
        add(header, BorderLayout.NORTH);

        JPanel stages = new JPanel(new GridLayout(1, game.getStageCount(), 8, 0));
        stages.setOpaque(false);
        for (int i = 0; i < cards.length; i++) {
            final int stageIndex = i;
            boolean unlocked = i < game.getUnlockedStageCount();
            boolean cleared = i < game.getStage();
            cards[i] = new StageCard(i, game.getStageName(i),
                    unlocked, cleared, i == chosenStage);
            cards[i].addActionListener(e -> chooseStage(stageIndex));
            stages.add(cards[i]);
        }
        add(stages, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout(0, 9));
        footer.setOpaque(false);
        hint = createText("", 12, GameTheme.MUTED);
        footer.add(hint, BorderLayout.NORTH);

        JPanel actions = new JPanel(new GridLayout(1, 2, 12, 0));
        actions.setOpaque(false);
        JButton backButton = new JButton("BACK TO HUB");
        confirmButton = new JButton("CONFIRM SELECTION");
        GameTheme.styleButton(backButton, false);
        GameTheme.styleButton(confirmButton, true);
        backButton.addActionListener(e -> gameFrame.showHub());
        confirmButton.addActionListener(
                e -> gameFrame.confirmSelectedStage(chosenStage));
        actions.add(backButton);
        actions.add(confirmButton);
        footer.add(actions, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);

        updateHint();
    }

    private JLabel createText(String text, int fontSize, Color color) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Monospaced", Font.BOLD, fontSize));
        label.setForeground(color);
        return label;
    }

    private void chooseStage(int stageIndex) {
        // Do not allow a locked stage to be selected through this UI.
        if (stageIndex < 0 || stageIndex >= game.getUnlockedStageCount()) {
            return;
        }
        chosenStage = stageIndex;
        for (int i = 0; i < cards.length; i++) {
            cards[i].setChosen(i == stageIndex);
        }
        updateHint();
    }

    private void updateHint() {
        String type = chosenStage < game.getStage() ? "CLEARED - REPLAY" : "NEXT CHALLENGE";
        hint.setText("STAGE " + (chosenStage + 1) + "  /  "
                + game.getStageName(chosenStage).toUpperCase() + "  /  "
                + type + "  -  PRESS BATTLE ON THE HUB TO START");
        confirmButton.setEnabled(game.getPlayer() != null
                && game.getPlayer().isAlive());
    }

    /** Stage cards deliberately share the same look as the hero-class cards. */
    private static final class StageCard extends JButton {
        private final int stageIndex;
        private final String monsterName;
        private final boolean unlocked;
        private final boolean cleared;
        private boolean chosen;
        private final BufferedImage sprite;

        StageCard(int stageIndex, String monsterName, boolean unlocked,
                  boolean cleared, boolean chosen) {
            this.stageIndex = stageIndex;
            this.monsterName = monsterName;
            this.unlocked = unlocked;
            this.cleared = cleared;
            this.chosen = chosen;
            this.sprite = loadMonster(monsterName);
            setUI(new BasicButtonUI());
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setEnabled(unlocked);
            setCursor(Cursor.getPredefinedCursor(unlocked
                    ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            setPreferredSize(new Dimension(115, 206));
            setToolTipText(unlocked ? "Choose " + monsterName
                    : "Locked: clear the preceding stage first");
            getAccessibleContext().setAccessibleDescription("Stage "
                    + (stageIndex + 1) + " " + monsterName
                    + (unlocked ? " unlocked" : " locked"));
        }

        void setChosen(boolean value) {
            chosen = value;
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
            g.setStroke(new BasicStroke(chosen ? 3f : 2f));
            g.drawRect(2, 2, w - 5, h - 5);

            centered(g, "STAGE " + (stageIndex + 1), 14, 13,
                    chosen ? GameTheme.GOLD : GameTheme.TEXT);
            int size = Math.min(85, Math.min(w - 20, h - 102));
            int x = (w - size) / 2;
            int y = 36;
            g.setColor(new Color(17, 26, 46));
            g.fillRect(x - 4, y - 4, size + 8, size + 8);
            if (sprite != null) {
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                g.drawImage(sprite, x, y, size, size, null);
            } else {
                centered(g, unlocked ? "?" : "LOCK", y + size / 2,
                        15, GameTheme.MUTED);
            }
            if (!unlocked) {
                g.setColor(new Color(9, 15, 29, 185));
                g.fillRect(x - 4, y - 4, size + 8, size + 8);
                centered(g, "LOCKED", y + size / 2,
                        13, GameTheme.MUTED);
            }

            centered(g, monsterName.toUpperCase(), h - 49,
                    monsterName.length() > 8 ? 11 : 13,
                    unlocked ? GameTheme.TEXT : GameTheme.MUTED);
            String status = !unlocked ? "LOCKED" : cleared ? "CLEARED" : "NEW";
            centered(g, status, h - 29, 11, cleared ? new Color(122, 215, 161)
                    : unlocked ? GameTheme.GOLD : GameTheme.MUTED);
            centered(g, chosen ? "< SELECTED >" : unlocked ? "CLICK TO CHOOSE" : "---",
                    h - 10, 9, chosen ? GameTheme.GOLD : GameTheme.MUTED);
            g.dispose();
            super.paintComponent(graphics);
        }

        private void centered(Graphics2D g, String value, int y,
                              int fontSize, Color color) {
            g.setFont(new Font("Monospaced", Font.BOLD, fontSize));
            g.setColor(color);
            int x = (getWidth() - g.getFontMetrics().stringWidth(value)) / 2;
            g.drawString(value, x, y);
        }

        private static BufferedImage loadMonster(String monsterName) {
            String path = "assets/monster/"
                    + monsterName.toLowerCase().replace(' ', '_') + ".png";
            try (InputStream input = StageSelectPanel.class.getClassLoader()
                    .getResourceAsStream(path)) {
                if (input != null) {
                    return ImageIO.read(input);
                }
            } catch (IOException ignored) {
                // Fall through to the project assets directory.
            }
            File file = new File(path);
            if (!file.isFile()) {
                file = new File("TurnBasedRPG", path);
            }
            try {
                return file.isFile() ? ImageIO.read(file) : null;
            } catch (IOException ignored) {
                return null;
            }
        }
    }
}

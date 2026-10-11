package rpg.gui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import rpg.gui.GameFrame;
import rpg.gui.GameTheme;

/** Simple pixel-RPG victory screen, using the existing EXP / Gold item sprites. */
public class StageVictoryPanel extends JPanel {

    private static final Color GREEN = new Color(122, 215, 157);

    public StageVictoryPanel(GameFrame frame, int stageIndex,
            String monsterName, int expReward, int goldReward,
            boolean hasNextStage, List<String> droppedItems) {

        setBackground(GameTheme.BACKGROUND);
        setLayout(new BorderLayout(0, 6));
        setBorder(BorderFactory.createEmptyBorder(8, 27, 11, 27));

        // Keep the trophy, stage title and rewards in ONE vertical group.
        // In GameFrame the persistent player HUD reduces our available height;
        // centering rewards in BorderLayout.CENTER previously created a huge gap.
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        PixelTrophyPanel trophy = new PixelTrophyPanel(true);
        trophy.setAlignmentX(CENTER_ALIGNMENT);
        trophy.setPreferredSize(new Dimension(63, 56));
        trophy.setMinimumSize(new Dimension(63, 56));
        trophy.setMaximumSize(new Dimension(63, 56));
        heading.add(trophy);
        heading.add(Box.createVerticalStrut(2));
        heading.add(label("VICTORY", 27, GameTheme.GOLD));
        heading.add(Box.createVerticalStrut(3));
        heading.add(label(String.format("STAGE %02d  -  %s", stageIndex + 1,
                monsterName.toUpperCase()), 12, GameTheme.TEXT));
        heading.setAlignmentX(CENTER_ALIGNMENT);
        content.add(heading);
        content.add(Box.createVerticalStrut(12));

        JPanel rewards = new JPanel();
        rewards.setBackground(GameTheme.PANEL);
        rewards.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GameTheme.PANEL_BORDER, 2),
                BorderFactory.createEmptyBorder(9, 17, 10, 17)));
        rewards.setLayout(new BoxLayout(rewards, BoxLayout.Y_AXIS));
        rewards.add(label("REWARDS", 11, GameTheme.MUTED));
        rewards.add(Box.createVerticalStrut(5));

        JPanel rewardRow = new JPanel(new GridLayout(1, 2, 16, 0));
        rewardRow.setOpaque(false);
        rewardRow.setPreferredSize(new Dimension(460, 41));
        rewardRow.setMaximumSize(new Dimension(460, 41));
        rewardRow.setAlignmentX(CENTER_ALIGNMENT);
        rewardRow.add(reward("assets/item/Exp.png", "+" + expReward + " EXP",
                GREEN));
        rewardRow.add(reward("assets/item/coin_gold.png", "+" + goldReward + " GOLD",
                GameTheme.GOLD));
        rewards.add(rewardRow);

        // Only show item rewards actually obtained from BattleManager.
        if (droppedItems != null && !droppedItems.isEmpty()) {
            rewards.add(Box.createVerticalStrut(6));
            rewards.add(label(droppedItems.size() == 1
                    ? "ITEM OBTAINED" : "ITEMS OBTAINED", 10, GREEN));
            rewards.add(Box.createVerticalStrut(3));

            // Group repeated drops so multiple bottles fit beside the trophy.
            Map<String, Integer> counts = new LinkedHashMap<>();
            for (String itemName : droppedItems) {
                counts.put(itemName, counts.getOrDefault(itemName, 0) + 1);
            }
            JPanel dropGrid = new JPanel(new GridLayout(0, 2, 10, 3));
            dropGrid.setOpaque(false);
            for (Map.Entry<String, Integer> entry : counts.entrySet()) {
                dropGrid.add(label(entry.getKey() + " x" + entry.getValue(),
                        11, GameTheme.TEXT));
            }
            if (counts.size() % 2 == 1) {
                dropGrid.add(new JLabel());
            }
            dropGrid.setAlignmentX(CENTER_ALIGNMENT);
            rewards.add(dropGrid);
        }

        // Keep all victory content together, centered vertically in the space
        // between the persistent Player HUD and the action buttons.
        rewards.setAlignmentX(CENTER_ALIGNMENT);
        rewards.setMaximumSize(new Dimension(500, 210));
        content.add(rewards);
        JPanel centeredContent = new JPanel(new GridBagLayout());
        centeredContent.setOpaque(false);
        centeredContent.add(content);
        add(centeredContent, BorderLayout.CENTER);

        JPanel actions = new JPanel(new GridLayout(1, 2, 12, 0));
        actions.setOpaque(false);

        JButton restart = button("RESTART CHALLENGE", false);
        JButton next = button(
                hasNextStage ? "NEXT STAGE" : "VIEW ENDING",
                true
        );

        actions.add(restart);
        actions.add(next);

        add(actions, BorderLayout.SOUTH);

        // Restart: ต่อสู้ด่านเดิมอีกครั้ง
        restart.addActionListener(
                e -> frame.restartChallenge(stageIndex)
        );

        // Next Stage: กลับ Hub ก่อน ไม่เริ่ม Battle ทันที
        next.addActionListener(e -> {
            if (hasNextStage) {
                frame.prepareNextStage(stageIndex + 1);
            } else {
                frame.viewEnding();
            }
        });
        
    }

    private JPanel reward(String path, String amount, Color color) {
        JPanel panel = new JPanel(new BorderLayout(9, 0));
        panel.setBackground(new Color(25, 35, 56));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GameTheme.PANEL_BORDER, 1),
                BorderFactory.createEmptyBorder(7, 10, 7, 10)));
        JLabel iconLabel = new JLabel();
        iconLabel.setPreferredSize(new Dimension(26, 26));
        BufferedImage icon = loadSprite(path);
        if (icon != null) {
            // Nearest-neighbor scaling keeps the same sharp pixels as assets/item.
            BufferedImage scaled = new BufferedImage(26, 26, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = scaled.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g.drawImage(icon, 0, 0, 26, 26, null);
            g.dispose();
            iconLabel.setIcon(new ImageIcon(scaled));
        } else {
            iconLabel.setText("+"); // Don't crash if assets are not found.
            iconLabel.setForeground(color);
        }
        panel.add(iconLabel, BorderLayout.WEST);
        JLabel value = new JLabel(amount);
        value.setForeground(color);
        value.setFont(new Font("Monospaced", Font.BOLD, 14));
        panel.add(value, BorderLayout.CENTER);
        return panel;
    }

    private BufferedImage loadSprite(String path) {
        try (InputStream stream = StageVictoryPanel.class.getClassLoader()
                .getResourceAsStream(path)) {
            if (stream != null) {
                return ImageIO.read(stream);
            }
        } catch (IOException ignored) {
            // Try an unpackaged project directory next.
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

    private JLabel label(String value, int size, Color color) {
        JLabel result = new JLabel(value, SwingConstants.CENTER);
        result.setFont(new Font("Monospaced", Font.BOLD, size));
        result.setForeground(color);
        result.setAlignmentX(CENTER_ALIGNMENT);
        return result;
    }

    private JButton button(String value, boolean primary) {
        JButton result = new JButton(value);
        GameTheme.styleButton(result, primary);
        result.setFont(new Font("Monospaced", Font.BOLD, 11));
        result.setPreferredSize(new Dimension(180, 43));
        return result;
    }
}

package rpg.gui.panel;

import java.awt.BorderLayout;
import java.awt.Font;
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

/** Buttons and reward summary shown after winning a stage. */
public class StageVictoryPanel extends JPanel {

    public StageVictoryPanel(
            GameFrame gameFrame,
            int completedStage,
            String monsterName,
            int expReward,
            int goldReward,
            boolean hasNextStage) {

        // Keep margins small enough for the existing 750x520 game window.
        setLayout(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(16, 28, 18, 28));

        JLabel titleLabel = new JLabel("STAGE CLEARED!", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 30));

        JLabel stageLabel = new JLabel(
                "Stage " + (completedStage + 1) + " - " + monsterName,
                SwingConstants.CENTER);
        stageLabel.setFont(new Font("SansSerif", Font.PLAIN, 19));

        JLabel rewardLabel = new JLabel(
                "Gained " + expReward + " EXP and " + goldReward + " Gold!",
                SwingConstants.CENTER);
        rewardLabel.setFont(new Font("SansSerif", Font.BOLD, 18));

        JLabel hintLabel = new JLabel(
                "Replay cleared stages to farm EXP and Gold.",
                SwingConstants.CENTER);
        hintLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));

        // Use separate labels so Swing never clips the HTML description
        // between BorderLayout.CENTER and the action buttons.
        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        titleLabel.setAlignmentX(CENTER_ALIGNMENT);
        stageLabel.setAlignmentX(CENTER_ALIGNMENT);
        rewardLabel.setAlignmentX(CENTER_ALIGNMENT);
        hintLabel.setAlignmentX(CENTER_ALIGNMENT);

        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(22));
        textPanel.add(stageLabel);
        textPanel.add(Box.createVerticalStrut(18));
        textPanel.add(rewardLabel);
        textPanel.add(Box.createVerticalStrut(10));
        textPanel.add(hintLabel);

        // Center the entire text block, not each line independently.
        JPanel textArea = new JPanel(new GridBagLayout());
        textArea.setOpaque(false);
        textArea.add(textPanel);
        add(textArea, BorderLayout.CENTER);

        JButton restartButton = new JButton("Restart Challenge");
        JButton nextButton = new JButton(
                hasNextStage ? "Next Stage" : "View Ending");
        JButton selectButton = new JButton("Stage Select");

        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 12, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(restartButton);
        buttonPanel.add(nextButton);
        buttonPanel.add(selectButton);
        add(buttonPanel, BorderLayout.SOUTH);

        restartButton.addActionListener(
                e -> gameFrame.restartChallenge(completedStage));
        nextButton.addActionListener(e -> {
            if (hasNextStage) {
                gameFrame.prepareNextStage(completedStage + 1);
            } else {
                gameFrame.viewEnding();
            }
        });
        selectButton.addActionListener(
                e -> gameFrame.openStageSelection());
    }
}

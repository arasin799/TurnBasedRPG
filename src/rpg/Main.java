package rpg;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import rpg.game.Game;
import rpg.gui.MainMenuFrame;

public class Main {

    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName()
            );
        } catch (Exception e) {
            System.out.println(
                    "Could not set system look and feel."
            );
        }

        SwingUtilities.invokeLater(() -> {

            Game game = new Game();

            MainMenuFrame mainMenu =
                    new MainMenuFrame(game);

            mainMenu.setVisible(true);
        });
    }
}
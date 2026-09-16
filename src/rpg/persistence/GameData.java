package rpg.persistence;

import rpg.model.character.player.Player;

public class GameData {

    private Player player;
    private int stage;

    public GameData(Player player, int stage) {

        this.player = player;
        this.stage = stage;
    }

    public Player getPlayer() {
        return player;
    }

    public int getStage() {
        return stage;
    }

    public void setStage(int stage) {

        if (stage >= 0) {
            this.stage = stage;
        }
    }
}
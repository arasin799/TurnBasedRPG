package rpg.model.item;

import rpg.model.character.player.Player;

public interface Usable {

    boolean use(Player player);
}
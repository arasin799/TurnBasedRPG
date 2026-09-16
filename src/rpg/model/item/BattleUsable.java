package rpg.model.item;

import rpg.model.character.Character;

public interface BattleUsable {

    int useOn(Character target);
}
package rpg.model.character.monster;

import rpg.model.item.Item;

public class DarkMage extends Monster {

    private static final int HP = 220;
    private static final int MP = 100;
    private static final int ATTACK = 30;
    private static final int DEFENSE = 15;

    private static final int EXP_REWARD = 300;
    private static final int GOLD_REWARD = 200;

    public DarkMage() {

        super(
                "Dark Mage",
                HP,
                MP,
                ATTACK,
                DEFENSE,
                EXP_REWARD,
                GOLD_REWARD,
                true
        );
    }

    @Override
    public Item generateDrop() {
        return null;
    }
}
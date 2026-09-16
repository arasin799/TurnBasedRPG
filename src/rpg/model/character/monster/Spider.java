package rpg.model.character.monster;

import java.util.Random;

import rpg.model.item.Item;
import rpg.model.item.SmallHealthPotion;
import rpg.model.item.SmallManaPotion;

public class Spider extends Monster {

    private static final int HP = 60;
    private static final int MP = 0;
    private static final int ATTACK = 14;
    private static final int DEFENSE = 4;

    private static final int EXP_REWARD = 50;
    private static final int GOLD_REWARD = 25;

    public Spider() {

        super(
                "Spider",
                HP,
                MP,
                ATTACK,
                DEFENSE,
                EXP_REWARD,
                GOLD_REWARD,
                false
        );
    }

    @Override
    public Item generateDrop() {

        Random random =
                new Random();

        int chance =
                random.nextInt(100);

        if (chance < 25) {

            return new SmallHealthPotion();

        } else if (chance < 40) {

            return new SmallManaPotion();
        }

        return null;
    }
}
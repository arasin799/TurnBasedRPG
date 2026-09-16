package rpg.model.character.monster;

import java.util.Random;

import rpg.model.item.Item;
import rpg.model.item.SmallHealthPotion;
import rpg.model.item.SmallManaPotion;

public class Ghost extends Monster {

    private static final int HP = 85;
    private static final int MP = 20;
    private static final int ATTACK = 18;
    private static final int DEFENSE = 7;

    private static final int EXP_REWARD = 80;
    private static final int GOLD_REWARD = 40;

    public Ghost() {

        super(
                "Ghost",
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

        if (chance < 10) {

            return new SmallHealthPotion();

        } else if (chance < 40) {

            return new SmallManaPotion();
        }

        return null;
    }
}
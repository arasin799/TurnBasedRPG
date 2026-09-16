package rpg.model.character.monster;

import java.util.Random;

import rpg.model.item.Item;
import rpg.model.item.SmallHealthPotion;
import rpg.model.item.SmallManaPotion;

public class Rat extends Monster {

    private static final int HP = 40;
    private static final int MP = 0;
    private static final int ATTACK = 10;
    private static final int DEFENSE = 2;

    private static final int EXP_REWARD = 30;
    private static final int GOLD_REWARD = 15;

    public Rat() {

        super(
                "Rat",
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

        if (chance < 30) {

            return new SmallHealthPotion();

        } else if (chance < 40) {

            return new SmallManaPotion();
        }

        return null;
    }
}
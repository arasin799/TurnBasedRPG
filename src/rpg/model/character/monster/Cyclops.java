package rpg.model.character.monster;

import java.util.Random;

import rpg.model.item.HealthPotion;
import rpg.model.item.Item;
import rpg.model.item.ManaPotion;
import rpg.model.item.SmallHealthPotion;
import rpg.model.item.SmallManaPotion;

public class Cyclops extends Monster {

    private static final int HP = 130;
    private static final int MP = 0;
    private static final int ATTACK = 24;
    private static final int DEFENSE = 12;

    private static final int EXP_REWARD = 130;
    private static final int GOLD_REWARD = 70;

    public Cyclops() {

        super(
                "Cyclops",
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

        if (chance < 20) {

            return new SmallHealthPotion();

        } else if (chance < 30) {

            return new SmallManaPotion();

        } else if (chance < 45) {

            return new HealthPotion();

        } else if (chance < 55) {

            return new ManaPotion();
        }

        return null;
    }
}
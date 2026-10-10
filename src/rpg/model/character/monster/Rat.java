package rpg.model.character.monster;

import java.util.Random;

import rpg.model.item.Item;
import rpg.model.item.SmallHealthPotion;
import rpg.model.item.SmallManaPotion;
import rpg.model.character.player.Player;

public class Rat extends Monster {

    private static final int HP = 100;
    private static final int MP = 0;
    private static final int ATTACK = 10;
    private static final int DEFENSE = 2;

    private static final int EXP_REWARD = 25;
    private static final int GOLD_REWARD = 25;

    public Rat() {

        super(
                "Rat",
                HP,
                MP,
                ATTACK,
                DEFENSE,
                EXP_REWARD,
                rollGold(10, 30),
                false
        );
        configureUltimate("Feral Rush", 3, 0, 10);
    }

    // Soul Shard drop chance from the design spreadsheet.
    @Override
    public double getSoulShardDropChance() {
        return 0.03;
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

    
    private static final int SPECIAL_SKILL_CHANCE = 20;
    private static final int FRENZY_BITE_BONUS = 5;

    @Override
    public int getSpecialSkillChance() {
        return SPECIAL_SKILL_CHANCE;
    }

    @Override
    public MonsterSkillResult useSpecialSkill(Player player) {

        if (player == null || !player.isAlive()) {
            return null;
        }

        int actualDamage = player.takeSpecialDamage(
                getAttack(),
                FRENZY_BITE_BONUS
        );

        String message =
                getName()
                + " uses Frenzy Bite on "
                + player.getName()
                + " for "
                + actualDamage
                + " damage!";

        return new MonsterSkillResult(message);
    }

}
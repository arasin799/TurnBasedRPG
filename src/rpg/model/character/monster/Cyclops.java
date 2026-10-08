package rpg.model.character.monster;

import java.util.Random;

import rpg.model.item.HealthPotion;
import rpg.model.item.Item;
import rpg.model.item.ManaPotion;
import rpg.model.item.SmallHealthPotion;
import rpg.model.item.SmallManaPotion;
import rpg.model.character.player.Player;

public class Cyclops extends Monster {

    private static final int HP = 310;
    private static final int MP = 40;
    private static final int ATTACK = 22;
    private static final int DEFENSE = 12;

    private static final int EXP_REWARD = 100;
    private static final int GOLD_REWARD = 100;

    public Cyclops() {

        super(
                "Cyclops",
                HP,
                MP,
                ATTACK,
                DEFENSE,
                EXP_REWARD,
                rollGold(80, 120),
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

    
    private static final int SPECIAL_SKILL_CHANCE = 25;
    private static final int HEAVY_SMASH_BONUS = 11;
    
    @Override
    public int getSpecialSkillChance() {
        return SPECIAL_SKILL_CHANCE;
    }

    @Override
    public MonsterSkillResult useSpecialSkill(Player player) {

        if (player == null
                || !player.isAlive()
                || !isAlive()) {
            return null;
        }

        // Heavy Smash deals bonus damage
        int actualDamage = player.takeSpecialDamage(
                getAttack(),
                HEAVY_SMASH_BONUS
        );

        String message =
                getName()
                + " uses Heavy Smash on "
                + player.getName()
                + " for "
                + actualDamage
                + " damage!"
                + "\nA devastating blow!";

        return new MonsterSkillResult(message);
    }

}
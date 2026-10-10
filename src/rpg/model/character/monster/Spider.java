package rpg.model.character.monster;

import java.util.Random;

import rpg.model.item.Item;
import rpg.model.item.SmallHealthPotion;
import rpg.model.item.SmallManaPotion;
import rpg.model.character.player.Player;

public class Spider extends Monster {

    private static final int HP = 180;
    private static final int MP = 10;
    private static final int ATTACK = 14;
    private static final int DEFENSE = 4;

    private static final int EXP_REWARD = 75;
    private static final int GOLD_REWARD = 40;

    public Spider() {

        super(
                "Spider",
                HP,
                MP,
                ATTACK,
                DEFENSE,
                EXP_REWARD,
                rollGold(45, 75),
                false
        );
        configureUltimate("Venom Burst", 4, 5, 12);
    }

    // Higher Soul Shard chance for more rewarding stage farming.
    @Override
    public double getSoulShardDropChance() {
        return 0.15;
    }

    @Override
    public Item generateDrop() {

        Random random =
                new Random();

        int chance =
                random.nextInt(100);

        if (chance < 35) {

            return new SmallHealthPotion();

        } else if (chance < 70) {

            return new SmallManaPotion();
        }

        return null;
    }

    private static final int SPECIAL_SKILL_CHANCE = 20;
    private static final int WEB_TRAP_BONUS = 6;

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
                WEB_TRAP_BONUS
        );

        String message =
                getName()
                + " uses Web Trap on "
                + player.getName()
                + " for "
                + actualDamage
                + " damage!";

        return new MonsterSkillResult(message, true);
    }

}
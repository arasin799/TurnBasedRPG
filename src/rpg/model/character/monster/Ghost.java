package rpg.model.character.monster;

import java.util.Random;

import rpg.model.item.HealthPotion;
import rpg.model.item.ManaPotion;
import rpg.model.item.Item;
import rpg.model.item.SmallHealthPotion;
import rpg.model.item.SmallManaPotion;
import rpg.model.character.player.Player;

public class Ghost extends Monster {

    private static final int HP = 250;
    private static final int MP = 20;
    private static final int ATTACK = 18;
    private static final int DEFENSE = 7;

    private static final int EXP_REWARD = 110;
    private static final int GOLD_REWARD = 60;

    public Ghost() {

        super(
                "Ghost",
                HP,
                MP,
                ATTACK,
                DEFENSE,
                EXP_REWARD,
                rollGold(75, 115),
                false
        );
        configureUltimate("Phantom Surge", 4, 10, 16);
    }

    // Higher Soul Shard chance for more rewarding stage farming.
    @Override
    public double getSoulShardDropChance() {
        return 0.25;
    }

    @Override
    public Item generateDrop() {

        Random random =
                new Random();

        int chance =
                random.nextInt(100);

        if (chance < 20) {

            return new SmallHealthPotion();

        } else if (chance < 40) {

            return new SmallManaPotion();

        } else if (chance < 60) {

            return new HealthPotion();

        } else if (chance < 75) {

            return new ManaPotion();
        }

        return null;
    }

    
    private static final int SPECIAL_SKILL_CHANCE = 20;
    private static final int FADE_DAMAGE_BONUS = 8;

    private boolean fadeShieldActive = false;
        
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

        // Attack with bonus damage
        int actualDamage = player.takeSpecialDamage(
                getAttack(),
                FADE_DAMAGE_BONUS
        );

        // Activate Fade shield
        fadeShieldActive = true;

        String message =
                getName()
                + " uses Fade on "
                + player.getName()
                + " for "
                + actualDamage
                + " damage!"
                + "\nGhost fades into mist!"
                + "\nNext incoming damage is reduced by 50%.";

        return new MonsterSkillResult(message);
    }


    @Override
    protected int modifyFinalDamage(int damage) {

        // No shield: receive normal damage
        if (!fadeShieldActive) {
            return damage;
        }

        // Shield blocks only one incoming hit
        fadeShieldActive = false;

        // Reduce damage by 50%, rounded up
        return Math.max(1, (damage + 1) / 2);
    }

}
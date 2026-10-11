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
    private static final int ATTACK = 42;
    private static final int DEFENSE = 12;

    private static final int EXP_REWARD = 150;

    public Cyclops() {

        super(
                "Cyclops",
                HP,
                MP,
                ATTACK,
                DEFENSE,
                EXP_REWARD,
                rollGold(120, 170),
                false
        );
        configureUltimate("Earthshaker", 5, 15, 32);
    }

    // Higher Soul Shard chance for more rewarding stage farming.
    @Override
    public double getSoulShardDropChance() {
        return 0.35;
    }

    @Override
    public Item generateDrop() {

        Random random =
                new Random();

        int chance =
                random.nextInt(100);

        if (chance < 15) {

            return new SmallHealthPotion();

        } else if (chance < 25) {

            return new SmallManaPotion();

        } else if (chance < 55) {

            return new HealthPotion();

        } else if (chance < 85) {

            return new ManaPotion();
        }

        return null;
    }

    
    // Additional rolls: small HP, big HP, small MP, big MP, second bottle.
    @Override
    protected java.util.List<Item> generateAdditionalDrops() {
        return rollExtraPotions(55, 45, 50, 45, 30);
    }

    private static final int SPECIAL_SKILL_CHANCE = 25;
    private static final int HEAVY_SMASH_BONUS = 18;
    
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
package rpg.model.character.monster;

import rpg.model.item.HealthPotion;
import rpg.model.item.ManaPotion;
import rpg.model.item.Item;
import rpg.model.character.player.Player;

public class DarkMage extends Monster {

    private static final int HP = 350;
    private static final int MP = 100;
    private static final int ATTACK = 52;
    private static final int DEFENSE = 18;

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
                rollGold(220, 300),
                true
        );
        configureUltimate("Abyss Nova", 5, 25, 40);
    }

    // Soul Shard drop chance from the design spreadsheet.
    @Override
    public double getSoulShardDropChance() {
        return 1.00;
    }

    @Override
    public Item generateDrop() {
        // Guaranteed large Potion in addition to the boss's guaranteed Soul Shard.
        return java.util.concurrent.ThreadLocalRandom.current().nextBoolean()
                ? new HealthPotion() : new ManaPotion();
    }

    // Additional rolls: small HP, big HP, small MP, big MP, second bottle.
    @Override
    protected java.util.List<Item> generateAdditionalDrops() {
        return rollExtraPotions(65, 70, 65, 70, 35);
    }

    private static final int SPECIAL_SKILL_CHANCE = 20;
    private static final int DARK_BARRIER_BONUS = 22;

    private boolean darkBarrierActive = false;

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

        // Deal bonus damage to player
        int actualDamage = player.takeSpecialDamage(
                getAttack(),
                DARK_BARRIER_BONUS
        );

        // Activate protective barrier
        darkBarrierActive = true;

        String message =
                getName()
                + " uses Dark Barrier on "
                + player.getName()
                + " for "
                + actualDamage
                + " damage!"
                + "\nDark Mage creates a magical barrier!"
                + "\nThe next incoming hit is reduced by 50%.";

        return new MonsterSkillResult(message);
    }

    @Override
    protected int modifyFinalDamage(int damage) {

        // No barrier: take normal damage
        if (!darkBarrierActive) {
            return damage;
        }

        // Consume barrier after one hit
        darkBarrierActive = false;

        // Reduce incoming damage by 50%
        int reducedDamage = (int) Math.ceil(
                damage * 0.50
        );

        return Math.max(1, reducedDamage);
    }

}
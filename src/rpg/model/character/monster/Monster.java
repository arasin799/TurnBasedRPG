package rpg.model.character.monster;

import rpg.model.character.Character;
import rpg.model.item.Item;
import rpg.model.item.SmallHealthPotion;
import rpg.model.item.HealthPotion;
import rpg.model.item.SmallManaPotion;
import rpg.model.item.ManaPotion;
import rpg.model.character.player.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

public abstract class Monster extends Character {

    private int expReward;
    private int goldReward;
    private boolean boss;

    // Each monster keeps its own charge during the current encounter.
    private String ultimateSkillName = "Ultimate";
    private int ultimateChargeRequired = 5;
    private int ultimateMpCost;
    private int ultimateDamageBonus;
    private int ultimateCharge;

    public abstract Item generateDrop();

    /** Keep the existing primary drop; roll additional kinds independently. */
    public List<Item> generateDrops() {
        List<Item> result = new ArrayList<>();
        Item mainDrop = generateDrop();
        if (mainDrop != null) {
            result.add(mainDrop);
        }
        result.addAll(generateAdditionalDrops());
        return result;
    }

    /** Subclasses adjust bonus drop chances without changing their old drops. */
    protected List<Item> generateAdditionalDrops() {
        return new ArrayList<>();
    }

    /** Each kind may drop independently, with a chance for a second bottle. */
    protected final List<Item> rollExtraPotions(int smallHp, int bigHp,
            int smallMp, int bigMp, int secondBottle) {
        List<Item> result = new ArrayList<>();
        addPotions(result, smallHp, secondBottle, SmallHealthPotion::new);
        addPotions(result, bigHp, secondBottle, HealthPotion::new);
        addPotions(result, smallMp, secondBottle, SmallManaPotion::new);
        addPotions(result, bigMp, secondBottle, ManaPotion::new);
        return result;
    }

    private static void addPotions(List<Item> items, int dropChance,
            int secondBottleChance, Supplier<Item> factory) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        if (random.nextInt(100) < dropChance) {
            items.add(factory.get());
            if (random.nextInt(100) < secondBottleChance) {
                items.add(factory.get());
            }
        }
    }

    public Monster(
            String name,
            int maxHp,
            int maxMp,
            int attack,
            int defense,
            int expReward,
            int goldReward,
            boolean boss) {

        super(
                name,
                maxHp,
                maxMp,
                attack,
                defense
        );

        this.expReward = expReward;
        this.goldReward = goldReward;
        this.boss = boss;
    }

    // Each encounter rolls Gold once; getters then return a stable reward.
    protected static int rollGold(int minGold, int maxGold) {
        if (minGold < 0 || maxGold < minGold) {
            throw new IllegalArgumentException("Invalid Gold reward range");
        }

        return java.util.concurrent.ThreadLocalRandom.current()
                .nextInt(minGold, maxGold + 1);
    }

    // -------------------------
    // Reward Getters
    // -------------------------

    public int getExpReward() {
        return expReward;
    }

    public int getGoldReward() {
        return goldReward;
    }

    /** Override to specify independent Soul Shard drop chance (0.0 to 1.0). */
    public double getSoulShardDropChance() {
        return 0.0;
    }

    // -------------------------
    // Monster Ultimate
    // -------------------------

    protected final void configureUltimate(
            String skillName, int chargeRequired, int mpCost, int bonusDamage) {
        if (skillName == null || skillName.isBlank() || chargeRequired < 1
                || mpCost < 0 || bonusDamage < 0) {
            throw new IllegalArgumentException("Invalid monster Ultimate configuration");
        }
        ultimateSkillName = skillName;
        ultimateChargeRequired = chargeRequired;
        ultimateMpCost = mpCost;
        ultimateDamageBonus = bonusDamage;
    }

    public int getUltimateCharge() {
        return ultimateCharge;
    }

    public int getUltimateChargeRequired() {
        return ultimateChargeRequired;
    }

    public String getUltimateSkillName() {
        return ultimateSkillName;
    }

    public boolean isUltimateReady() {
        return isAlive() && ultimateCharge >= ultimateChargeRequired
                && getMp() >= ultimateMpCost;
    }

    // Normal and special attacks earn one charge, but Ultimate does not.
    public void gainUltimateCharge() {
        if (isAlive() && ultimateCharge < ultimateChargeRequired) {
            ultimateCharge++;
        }
    }

    /** -1 means the action could not be performed; no resources are consumed. */
    public int useUltimate(Player player) {
        if (player == null || !player.isAlive() || !isUltimateReady()) {
            return -1;
        }
        // Rat's Ultimate costs 0 MP; Character.consumeMp(0) returns false.
        if (ultimateMpCost > 0 && !consumeMp(ultimateMpCost)) {
            return -1;
        }
        ultimateCharge = 0;
        return player.takeSpecialDamage(getAttack(), ultimateDamageBonus);
    }

    // -------------------------
    // Boss
    // -------------------------

    public boolean isBoss() {
        return boss;
    }

    public boolean canEscape() {
        return !boss;
    }

    public int getSpecialSkillChance() {
        return 0;
    }

    public MonsterSkillResult useSpecialSkill(Player player) {
        return null;
    }

}


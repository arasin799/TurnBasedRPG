package rpg.model.character.monster;

import rpg.model.character.Character;
import rpg.model.item.Item;
import rpg.model.character.player.Player;

public abstract class Monster extends Character {

    private int expReward;
    private int goldReward;
    private boolean boss;

    public abstract Item generateDrop();

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


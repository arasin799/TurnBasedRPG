package rpg.model.character.monster;

import rpg.model.character.Character;
import rpg.model.item.Item;

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
}


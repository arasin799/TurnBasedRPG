package rpg.model.character;

public abstract class Character {

    private String name;

    private int hp;
    private int maxHp;

    private int mp;
    private int maxMp;

    private int attack;
    private int defense;

    public Character(
            String name,
            int maxHp,
            int maxMp,
            int attack,
            int defense) {

        this.name = name;

        this.maxHp = maxHp;
        this.hp = maxHp;

        this.maxMp = maxMp;
        this.mp = maxMp;

        this.attack = attack;
        this.defense = defense;
    }

    // -------------------------
    // Getters
    // -------------------------

    public String getName() {
        return name;
    }

    public int getHp() {
        return hp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public int getMp() {
        return mp;
    }

    public int getMaxMp() {
        return maxMp;
    }

    public int getAttack() {
        return attack;
    }

    public int getDefense() {
        return defense;
    }

    // -------------------------
    // Combat Methods
    // -------------------------

    public int attack(Character target) {

        if (target == null) {
            return 0;
        }

        return target.takeDamage(getAttack());
    }

    // -------------------------
    // HP Methods
    // -------------------------

    public int takeDamage(int incomingDamage) {

        if (incomingDamage <= 0) {
            return 0;
        }

        int actualDamage = incomingDamage - getDefense();

        if (actualDamage < 1) {
            actualDamage = 1;
        }

        hp -= actualDamage;

        if (hp < 0) {
            hp = 0;
        }

        return actualDamage;
    }

    public void heal(int amount) {

        if (amount <= 0) {
            return;
        }

        hp += amount;

        if (hp > maxHp) {
            hp = maxHp;
        }
    }

    // -------------------------
    // MP Methods
    // -------------------------

    public void restoreMp(int amount) {

        if (amount <= 0) {
            return;
        }

        mp += amount;

        if (mp > maxMp) {
            mp = maxMp;
        }
    }

    public boolean consumeMp(int amount) {

        if (amount <= 0) {
            return false;
        }

        if (mp < amount) {
            return false;
        }

        mp -= amount;

        return true;
    }

    // -------------------------
    // Character State
    // -------------------------

    public boolean isAlive() {
        return hp > 0;
    }

    // -------------------------
    // Protected Stat Methods
    // -------------------------

    protected void increaseMaxHp(int amount) {
        if (amount > 0) {
            maxHp += amount;
            hp += amount;
        }
    }

    protected void increaseMaxMp(int amount) {
        if (amount > 0) {
            maxMp += amount;
            mp += amount;
        }
    }

    protected void increaseAttack(int amount) {
        if (amount > 0) {
            attack += amount;
        }
    }

    protected void increaseDefense(int amount) {
        if (amount > 0) {
            defense += amount;
        }
    }

    // -------------------------
    // Save / Load State
    // -------------------------

    protected void restoreCurrentHp(int hp) {

        if (hp < 0) {
            this.hp = 0;
        } else if (hp > maxHp) {
            this.hp = maxHp;
        } else {
            this.hp = hp;
        }
    }

    protected void restoreCurrentMp(int mp) {

        if (mp < 0) {
            this.mp = 0;
        } else if (mp > maxMp) {
            this.mp = maxMp;
        } else {
            this.mp = mp;
        }
    }
}
package rpg.model.character.player;

import rpg.model.Inventory;
import rpg.model.character.Character;
import rpg.model.item.Weapon;
import rpg.model.item.Armor;

public abstract class Player extends Character {

    private int level;
    private int experience;
    private int gold;

    private Weapon equippedWeapon;
    private Armor equippedArmor;

    private Inventory inventory;

    public Player(
            String name,
            int maxHp,
            int maxMp,
            int attack,
            int defense) {

        super(name, maxHp, maxMp, attack, defense);

        this.level = 1;
        this.experience = 0;
        this.gold = 100;

        this.equippedWeapon = null;
        this.equippedArmor = null;

        this.inventory = new Inventory();
    }

    // -------------------------
    // Getters
    // -------------------------

    public int getLevel() {
        return level;
    }

    public int getExperience() {
        return experience;
    }

    public int getGold() {
        return gold;
    }

    public Weapon getEquippedWeapon() {
        return equippedWeapon;
    }

    public Armor getEquippedArmor() {
        return equippedArmor;
    }

    public Inventory getInventory() {
        return inventory;
    }

    // -------------------------
    // Equipment
    // -------------------------

    public boolean equipWeapon(
            Weapon weapon) {

        if (weapon == null) {
            return false;
        }

        if (!canEquipWeapon(weapon)) {
            return false;
        }

        this.equippedWeapon = weapon;

        return true;
    }

    public abstract boolean canEquipWeapon(
        Weapon weapon);

    public boolean equipArmor(
        Armor armor) {

        if (armor == null) {
            return false;
        }

        if (!canEquipArmor(armor)) {
            return false;
        }

        this.equippedArmor = armor;

        return true;
    }

    public abstract boolean canEquipArmor(
        Armor armor);

    public void unequipWeapon() {
        this.equippedWeapon = null;
    }

    public void unequipArmor() {
        this.equippedArmor = null;
    }

    @Override
    public int getAttack() {

        int totalAttack = super.getAttack();

        if (equippedWeapon != null) {
            totalAttack += equippedWeapon.getAttackBonus();
        }

        return totalAttack;
    }

    @Override
    public int getDefense() {

        int totalDefense = super.getDefense();

        if (equippedArmor != null) {
            totalDefense += equippedArmor.getDefenseBonus();
        }

        return totalDefense;
    }

    // -------------------------
    // Experience / Level
    // -------------------------

    public int getExpToNextLevel() {
        return level * 100;
    }

    public void gainExperience(int amount) {

        if (amount <= 0) {
            return;
        }

        experience += amount;

        while (experience >= getExpToNextLevel()) {

            experience -= getExpToNextLevel();

            levelUp();
        }
    }


    private void levelUp() {

        level++;

        // Increase stats based on player class
        applyLevelUpStats();

        // Fully restore HP and MP
        heal(getMaxHp());
        restoreMp(getMaxMp());
    }

    protected abstract void applyLevelUpStats();

    // -------------------------
    // Gold
    // -------------------------

    public void gainGold(int amount) {

        if (amount > 0) {
            gold += amount;
        }
    }

    public boolean spendGold(int amount) {

        if (amount <= 0) {
            return false;
        }

        if (gold < amount) {
            return false;
        }

        gold -= amount;

        return true;
    }

    // -------------------------
    // Skill
    // -------------------------

    public abstract int useSkill(Character target);

    public abstract String getSkillName();

    // -------------------------
    // PlayerState
    // -------------------------

    public void restorePlayerState(
            int savedLevel,
            int experience,
            int gold,
            int hp,
            int mp) {

        if (savedLevel < 1) {
            savedLevel = 1;
        }

        if (experience < 0) {
            experience = 0;
        }

        if (gold < 0) {
            gold = 0;
        }

        // Rebuild level-up stats
        while (this.level < savedLevel) {
            this.level++;
            applyLevelUpStats();
        }

        this.experience = experience;
        this.gold = gold;

        restoreCurrentHp(hp);
        restoreCurrentMp(mp);
    }
}
package rpg.model.character.player;

import rpg.model.Inventory;
import rpg.model.character.Character;
import rpg.model.item.Weapon;
import rpg.model.item.WeaponType;
import rpg.model.item.Armor;

public abstract class Player extends Character {

    private int level;
    private int experience;
    private int gold;

    // Charge persists across battles and is spent by Ultimate skills.
    private static final int MAX_ULTIMATE_CHARGE = 5;
    private static final int CLASS_WEAPON_EXTRA_DAMAGE = 7;
    private static final int CLASS_WEAPON_ATTACK_BONUS = 10;
    private static final int OFF_CLASS_WEAPON_ATTACK_BONUS = 7;

    // Experience curve and extra Gold paid only for actual level-ups.
    private static final int FIRST_LEVEL_EXP = 80;
    private static final int EXP_GROWTH_PER_LEVEL = 70;
    private static final int LEVEL_UP_GOLD_BASE = 50;
    private static final int LEVEL_UP_GOLD_PER_LEVEL = 25;
    private int ultimateCharge = 0;

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

    public int getUltimateCharge() {
        return ultimateCharge;
    }

    public int getMaxUltimateCharge() {
        return MAX_ULTIMATE_CHARGE;
    }

    // Returns false when already full; Soul Shard is then not consumed.
    public boolean addUltimateCharge(int amount) {
        if (amount <= 0 || ultimateCharge >= MAX_ULTIMATE_CHARGE) {
            return false;
        }
        ultimateCharge = Math.min(MAX_ULTIMATE_CHARGE, ultimateCharge + amount);
        return true;
    }

    public void restoreUltimateCharge(int savedCharge) {
        ultimateCharge = Math.max(0, Math.min(MAX_ULTIMATE_CHARGE, savedCharge));
    }

    public boolean isUltimateReady() {
        return ultimateCharge == MAX_ULTIMATE_CHARGE;
    }

    public boolean canUseUltimate() {
        return isAlive() && isUltimateReady() && getMp() >= getUltimateMpCost();
    }

    // One Ultimate consumes all five charges and the class-specific MP cost.
    // Invalid attempts consume nothing. Ultimate itself gives no new charge.
    public final int useUltimate(Character target) {
        if (target == null || !target.isAlive() || !canUseUltimate()) {
            return -1;
        }
        if (!consumeMp(getUltimateMpCost())) {
            return -1;
        }
        ultimateCharge = 0;
        return target.takeSpecialDamage(getAttack(),
                getUltimateDamageBonus() + getEquippedClassWeaponDamageBonus());
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
            totalAttack += getWeaponAttackBonus(equippedWeapon);
        }

        return totalAttack;
    }

    /** The dedicated class weapon grants +7 extra damage (not extra ATK). */
    public int getClassWeaponDamageBonus(Weapon weapon) {
        if (weapon == null) return 0;
        WeaponType type = weapon.getWeaponType();
        boolean matches = (this instanceof Knight && type == WeaponType.SWORD)
                || (this instanceof Archer && type == WeaponType.BOW)
                || (this instanceof Mage && type == WeaponType.STAFF);
        return matches ? CLASS_WEAPON_EXTRA_DAMAGE : 0;
    }

    /** Effective attack from one equipped weapon; no weapon stacking. */
    public int getWeaponAttackBonus(Weapon weapon) {
        if (weapon == null) return 0;
        // Dagger is a universal starter weapon (+5 ATK as before).
        if (weapon.getWeaponType() == WeaponType.DAGGER) {
            return weapon.getAttackBonus();
        }
        // Dedicated weapon: +10 ATK for the matching class, +7 otherwise.
        return getClassWeaponDamageBonus(weapon) > 0
                ? CLASS_WEAPON_ATTACK_BONUS : OFF_CLASS_WEAPON_ATTACK_BONUS;
    }

    public int getEquippedClassWeaponDamageBonus() {
        return getClassWeaponDamageBonus(equippedWeapon);
    }

    /** Apply the class-match bonus after regular defense calculations. */
    protected final int dealWeaponBonusDamage(Character target, int baseDamage) {
        return target == null ? 0
                : target.takeSpecialDamage(baseDamage, getEquippedClassWeaponDamageBonus());
    }

    @Override
    public int attack(Character target) {
        return dealWeaponBonusDamage(target, getAttack());
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
        return FIRST_LEVEL_EXP + (level - 1) * EXP_GROWTH_PER_LEVEL;
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

        // Fully restore HP and MP.
        heal(getMaxHp());
        restoreMp(getMaxMp());

        // Level 2 gets +100 Gold, Level 3 +125, Level 4 +150, ...
        // restorePlayerState does not call levelUp(), so loading grants no Gold.
        gainGold(LEVEL_UP_GOLD_BASE + level * LEVEL_UP_GOLD_PER_LEVEL);
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

    public abstract String getUltimateSkillName();

    public abstract int getUltimateMpCost();

    protected abstract int getUltimateDamageBonus();

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
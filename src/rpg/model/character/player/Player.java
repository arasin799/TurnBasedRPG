package rpg.model.character.player;

import java.util.ArrayList;

import rpg.model.Inventory;
import rpg.model.item.Item;
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

    /** Class-specific status produced by the hero's Skill or Ultimate. */
    public enum CombatEffect { NONE, FORTIFY, BLEED, BURN, WARD }

    // Reduces one actual incoming hit, including monster Skills and Ultimates.
    private int nextHitReductionPercent;

    // One piece per equipment type, with bonuses stacking across different types.
    private final ArrayList<Weapon> equippedWeapons = new ArrayList<>();
    private final ArrayList<Armor> equippedArmors = new ArrayList<>();

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
        return target.takeSpecialDamage(
                getAttack(), getUltimateDamageBonus() + getEquippedClassWeaponDamageBonus());
    }

    public ArrayList<Weapon> getEquippedWeapons() {
        return new ArrayList<>(equippedWeapons);
    }

    public ArrayList<Armor> getEquippedArmors() {
        return new ArrayList<>(equippedArmors);
    }

    /** Backward-compatible accessor; new code should use getEquippedWeapons(). */
    public Weapon getEquippedWeapon() {
        return equippedWeapons.isEmpty() ? null : equippedWeapons.get(0);
    }

    /** Backward-compatible accessor; new code should use getEquippedArmors(). */
    public Armor getEquippedArmor() {
        return equippedArmors.isEmpty() ? null : equippedArmors.get(0);
    }

    public boolean isEquipped(Item item) {
        return item != null && (equippedWeapons.contains(item) || equippedArmors.contains(item));
    }

    public Inventory getInventory() {
        return inventory;
    }

    // Each weapon/armor type is one slot; different types can be equipped together.
    public boolean equipWeapon(Weapon weapon) {
        if (weapon == null || !canEquipWeapon(weapon)) return false;
        for (Weapon equipped : equippedWeapons) {
            if (equipped.getWeaponType() == weapon.getWeaponType()) return false;
        }
        return equippedWeapons.add(weapon);
    }

    public abstract boolean canEquipWeapon(Weapon weapon);

    public boolean equipArmor(Armor armor) {
        if (armor == null || !canEquipArmor(armor)) return false;
        for (Armor equipped : equippedArmors) {
            if (equipped.getArmorType() == armor.getArmorType()) return false;
        }
        return equippedArmors.add(armor);
    }

    public abstract boolean canEquipArmor(Armor armor);

    public boolean unequipWeapon(Weapon weapon) {
        return equippedWeapons.remove(weapon);
    }

    public boolean unequipArmor(Armor armor) {
        return equippedArmors.remove(armor);
    }

    /** Legacy bulk unequip methods. */
    public void unequipWeapon() {
        equippedWeapons.clear();
    }

    public void unequipArmor() {
        equippedArmors.clear();
    }

    public int getTotalEquippedWeaponAttackBonus() {
        int total = 0;
        for (Weapon weapon : equippedWeapons) {
            total += getWeaponAttackBonus(weapon);
        }
        return total;
    }

    public int getTotalEquippedArmorDefenseBonus() {
        int total = 0;
        for (Armor armor : equippedArmors) {
            total += armor.getDefenseBonus();
        }
        return total;
    }

    @Override
    public int getAttack() {

        return super.getAttack() + getTotalEquippedWeaponAttackBonus();
    }

    /** Extra TRUE damage for the weapon dedicated to this hero class.
     *  Dagger is a shared weapon and never receives this class-match bonus.
     *  Used by Shop/Inventory previews; it does not require already equipping.
     */
    public int getClassWeaponDamageBonus(Weapon weapon) {
        if (weapon == null) return 0;
        WeaponType type = weapon.getWeaponType();
        boolean matches = (this instanceof Knight && type == WeaponType.SWORD)
                || (this instanceof Archer && type == WeaponType.BOW)
                || (this instanceof Mage && type == WeaponType.STAFF);
        return matches ? CLASS_WEAPON_EXTRA_DAMAGE : 0;
    }

    /** Effective attack bonus for one equipped weapon. */
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
        // The one matching class weapon grants +7 true damage once.
        for (Weapon weapon : equippedWeapons) {
            if (getClassWeaponDamageBonus(weapon) > 0) {
                return CLASS_WEAPON_EXTRA_DAMAGE;
            }
        }
        return 0;
    }

    /** The class weapon adds +7 damage AFTER normal defense calculation. */
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

        return super.getDefense() + getTotalEquippedArmorDefenseBonus();
    }

    /** A simple extensible hook: each hero class chooses its own effects. */
    public CombatEffect getSkillCombatEffect() {
        return CombatEffect.NONE;
    }

    public CombatEffect getUltimateCombatEffect() {
        return CombatEffect.NONE;
    }

    /** Reduce the next hit; not immune. Stronger protection replaces weaker. */
    public void protectNextHit(int percent) {
        int safe = Math.max(0, Math.min(90, percent));
        nextHitReductionPercent = Math.max(nextHitReductionPercent, safe);
    }

    public int getNextHitReductionPercent() {
        return nextHitReductionPercent;
    }

    public void clearHitReduction() {
        nextHitReductionPercent = 0;
    }

    @Override
    protected int modifyFinalDamage(int damage) {
        if (nextHitReductionPercent <= 0) return damage;
        int reduction = nextHitReductionPercent;
        nextHitReductionPercent = 0; // Consume this guard on the first hit only.
        return Math.max(1, (int) Math.ceil(damage * (100 - reduction) / 100.0));
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

    /** MP required by this hero class's normal Skill. */
    public abstract int getSkillMpCost();

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
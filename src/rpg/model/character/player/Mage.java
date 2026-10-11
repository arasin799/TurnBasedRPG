package rpg.model.character.player;

import rpg.model.character.Character;
import rpg.model.item.Armor;
import rpg.model.item.Weapon;

public class Mage extends Player {

    private static final int STARTING_HP = 80;
    private static final int STARTING_MP = 70;
    private static final int STARTING_ATTACK = 28;
    private static final int STARTING_DEFENSE = 7;

    private static final int SKILL_MP_COST = 20;
    private static final int SKILL_DAMAGE_BONUS = 22;

    public Mage(String name) {

        super(
                name,
                STARTING_HP,
                STARTING_MP,
                STARTING_ATTACK,
                STARTING_DEFENSE
        );
    }

    @Override
    public int getSkillMpCost() {
        return SKILL_MP_COST;
    }

    @Override
    public int useSkill(Character target) {

        if (!consumeMp(SKILL_MP_COST)) {
            return -1;
        }

        int skillDamage = getAttack() + SKILL_DAMAGE_BONUS;

        return dealWeaponBonusDamage(target, skillDamage);
    }

    @Override
    public CombatEffect getSkillCombatEffect() {
        return CombatEffect.BURN;
    }

    @Override
    public CombatEffect getUltimateCombatEffect() {
        return CombatEffect.WARD;
    }

    @Override
    public String getSkillName() {
        return "Fireball";
    }

    @Override
    public String getUltimateSkillName() {
        return "Meteor Burst";
    }

    @Override
    public int getUltimateMpCost() {
        return 30;
    }

    @Override
    protected int getUltimateDamageBonus() {
        return 60;
    }

    @Override
    protected void applyLevelUpStats() {

        increaseMaxHp(7);
        increaseMaxMp(10);
        increaseAttack(5);
        increaseDefense(1);
    }

    @Override
    public boolean canEquipWeapon(Weapon weapon) {
        // Any hero may equip any weapon; attack benefits depend on class.
        return weapon != null;
    }

    @Override
    public boolean canEquipArmor(
            Armor armor) {

        return armor != null;
    }
}
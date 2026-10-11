package rpg.model.character.player;

import rpg.model.character.Character;
import rpg.model.item.Weapon;
import rpg.model.item.WeaponType;
import rpg.model.item.Armor;

public class Knight extends Player {

    private static final int STARTING_HP = 120;
    private static final int STARTING_MP = 30;
    private static final int STARTING_ATTACK = 20;
    private static final int STARTING_DEFENSE = 30;

    private static final int SKILL_MP_COST = 8;
    private static final int SKILL_DAMAGE_BONUS = 10;

    public Knight(String name) {

        super(
                name,
                STARTING_HP,
                STARTING_MP,
                STARTING_ATTACK,
                STARTING_DEFENSE
        );
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
        return CombatEffect.FORTIFY;
    }

    @Override
    public CombatEffect getUltimateCombatEffect() {
        return CombatEffect.FORTIFY;
    }

    @Override
    public String getSkillName() {
        return "Power Strike";
    }

    @Override
    public String getUltimateSkillName() {
        return "Divine Slash";
    }

    @Override
    public int getUltimateMpCost() {
        return 15;
    }

    @Override
    protected int getUltimateDamageBonus() {
        return 35;
    }

    @Override
    protected void applyLevelUpStats() {

        increaseMaxHp(15);
        increaseMaxMp(3);
        increaseAttack(3);
        increaseDefense(4);
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
package rpg.model.character.player;

import rpg.model.character.Character;
import rpg.model.item.Weapon;
import rpg.model.item.WeaponType;
import rpg.model.item.Armor;
import rpg.model.item.ArmorType;

public class Archer extends Player {

    private static final int STARTING_HP = 100;
    private static final int STARTING_MP = 40;
    private static final int STARTING_ATTACK = 24;
    private static final int STARTING_DEFENSE = 10;

    private static final int SKILL_MP_COST = 7;
    private static final int SKILL_DAMAGE_BONUS = 15;

    public Archer(String name) {

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

        return target.takeDamage(skillDamage);
    }

    @Override
    public String getSkillName() {
        return "Power Shot";
    }

    @Override
    protected void applyLevelUpStats() {

        increaseMaxHp(10);
        increaseMaxMp(5);
        increaseAttack(4);
        increaseDefense(2);
    }

    @Override
    public boolean canEquipWeapon(
            Weapon weapon) {

        WeaponType type =
                weapon.getWeaponType();

        return type == WeaponType.BOW
                || type == WeaponType.DAGGER;
    }

    @Override
    public boolean canEquipArmor(
            Armor armor) {

        return armor.getArmorType()
                != ArmorType.IRON_SHIELD;
    }    
}
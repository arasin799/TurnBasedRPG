package rpg.model.character.player;

import rpg.model.character.Character;
import rpg.model.item.Armor;
import rpg.model.item.ArmorType;
import rpg.model.item.Weapon;
import rpg.model.item.WeaponType;

public class Mage extends Player {

    private static final int STARTING_HP = 80;
    private static final int STARTING_MP = 70;
    private static final int STARTING_ATTACK = 28;
    private static final int STARTING_DEFENSE = 7;

    private static final int SKILL_MP_COST = 12;
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
    public int useSkill(Character target) {

        if (!consumeMp(SKILL_MP_COST)) {
            return -1;
        }

        int skillDamage = getAttack() + SKILL_DAMAGE_BONUS;

        return target.takeDamage(skillDamage);
    }

    @Override
    public String getSkillName() {
        return "Fireball";
    }

    @Override
    protected void applyLevelUpStats() {

        increaseMaxHp(7);
        increaseMaxMp(10);
        increaseAttack(5);
        increaseDefense(1);
    }

    @Override
    public boolean canEquipWeapon(
            Weapon weapon) {

        WeaponType type =
                weapon.getWeaponType();

        return type == WeaponType.STAFF
                || type == WeaponType.DAGGER;
    }

    @Override
    public boolean canEquipArmor(
            Armor armor) {

        return armor.getArmorType()
                != ArmorType.IRON_SHIELD;
    }
}
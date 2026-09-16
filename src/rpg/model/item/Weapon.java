package rpg.model.item;

public class Weapon extends Item {

    private int attackBonus;
    private WeaponType weaponType;

    public Weapon(
            String name,
            String description,
            int buyPrice,
            int sellPrice,
            int attackBonus,
            WeaponType weaponType) {

        super(
                name,
                description,
                buyPrice,
                sellPrice
        );

        this.attackBonus = attackBonus;
        this.weaponType = weaponType;
    }

    public int getAttackBonus() {
        return attackBonus;
    }

    public WeaponType getWeaponType() {
        return weaponType;
    }
}
package rpg.model.item;

public class Armor extends Item {

    private int defenseBonus;
    private ArmorType armorType;

    public Armor(
            String name,
            String description,
            int buyPrice,
            int sellPrice,
            int defenseBonus,
            ArmorType armorType) {

        super(
                name,
                description,
                buyPrice,
                sellPrice
        );

        this.defenseBonus = defenseBonus;
        this.armorType = armorType;
    }

    public int getDefenseBonus() {
        return defenseBonus;
    }

    public ArmorType getArmorType() {
        return armorType;
    }
}
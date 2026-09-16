package rpg.model.item;

import rpg.model.character.Character;

public class Bomb extends Item
        implements BattleUsable {

    private static final int DAMAGE = 35;
    private static final int BUY_PRICE = 80;
    private static final int SELL_PRICE = 40;

    public Bomb() {

        super(
                "Bomb",
                "Deals 35 damage before enemy defense.",
                BUY_PRICE,
                SELL_PRICE
        );
    }

    public int getDamage() {
        return DAMAGE;
    }

    @Override
    public int useOn(Character target) {

        if (target == null
                || !target.isAlive()) {

            return 0;
        }

        return target.takeDamage(
                DAMAGE
        );
    }
}
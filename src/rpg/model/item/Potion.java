package rpg.model.item;

import rpg.model.character.player.Player;

public abstract class Potion extends Item implements Usable {

    private int restoreAmount;

    public Potion(
            String name,
            String description,
            int buyPrice,
            int sellPrice,
            int restoreAmount) {

        super(
                name,
                description,
                buyPrice,
                sellPrice
        );

        this.restoreAmount = restoreAmount;
    }

    public int getRestoreAmount() {
        return restoreAmount;
    }

    @Override
    public abstract boolean use(Player player);
}
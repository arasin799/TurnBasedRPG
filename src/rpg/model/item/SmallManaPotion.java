package rpg.model.item;

import rpg.model.character.player.Player;

public class SmallManaPotion extends Potion {

    private static final int RESTORE_AMOUNT = 5;
    private static final int BUY_PRICE = 35;
    private static final int SELL_PRICE = 17;

    public SmallManaPotion() {

        super(
                "Small Mana Potion",
                "Restores 5 MP.",
                BUY_PRICE,
                SELL_PRICE,
                RESTORE_AMOUNT
        );
    }

    @Override
    public boolean use(Player player) {

        if (player == null) {
            return false;
        }

        if (player.getMp()
                >= player.getMaxMp()) {

            return false;
        }

        player.restoreMp(
                getRestoreAmount()
        );

        return true;
    }
}
package rpg.model.item;

import rpg.model.character.player.Player;

public class ManaPotion extends Potion {

    private static final int RESTORE_AMOUNT = 10;
    private static final int BUY_PRICE = 20;
    private static final int SELL_PRICE = 10;

    public ManaPotion() {

        super(
                "Mana Potion",
                "Restores 10 MP",
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

        if (player.getMp() >= player.getMaxMp()) {
            return false;
        }

        player.restoreMp(getRestoreAmount());

        return true;
    }
}
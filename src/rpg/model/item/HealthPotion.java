package rpg.model.item;

import rpg.model.character.player.Player;

public class HealthPotion extends Potion {

    private static final int RESTORE_AMOUNT = 25;
    private static final int BUY_PRICE = 30;
    private static final int SELL_PRICE = 16;

    public HealthPotion() {

        super(
                "Health Potion",
                "Restores 25 HP",
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

        if (player.getHp() >= player.getMaxHp()) {
            return false;
        }

        player.heal(getRestoreAmount());

        return true;
    }
}
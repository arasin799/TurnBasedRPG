package rpg.model.item;

import rpg.model.character.player.Player;

/** Monster-only loot: one shard adds one Ultimate charge. */
public class SoulShard extends Item implements Usable {

    public SoulShard() {
        // Not sold in Shop, and cannot be sold for Gold.
        super("Soul Shard", "Adds 1 Ultimate charge (maximum 5).", 0, 0);
    }

    @Override
    public boolean use(Player player) {
        return player != null && player.addUltimateCharge(1);
    }
}

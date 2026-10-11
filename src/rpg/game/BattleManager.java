package rpg.game;

import rpg.model.character.player.Player;
import rpg.model.character.monster.Monster;
import rpg.model.item.Item;
import rpg.model.item.BattleUsable;
import rpg.model.item.SoulShard;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;
import rpg.model.character.monster.MonsterSkillResult;

public class BattleManager {

    private Player player;
    private Monster monster;

    private boolean battleOver;
    private boolean escaped;
    private boolean rewardGiven;
    private int levelUpGoldEarned;
    private final Random random = new Random();
    private int playerStunTurns = 0;
    // Rewards actually granted by this encounter (no additional drop rolls).
    private final List<String> droppedItemNames = new ArrayList<>();

    public BattleManager(Player player, Monster monster) {

        this.player = player;
        this.monster = monster;

        this.battleOver = false;
        this.escaped = false;
        this.rewardGiven = false;
    }

    // -------------------------
    // Getters
    // -------------------------

    public Player getPlayer() {
        return player;
    }

    public Monster getMonster() {
        return monster;
    }

    public boolean isBattleOver() {
        return battleOver;
    }

    public boolean hasEscaped() {
        return escaped;
    }

    public boolean isVictory() {
        return battleOver
                && !escaped
                && player.isAlive()
                && !monster.isAlive();
    }

    public boolean isDefeat() {
        return battleOver
                && !player.isAlive();
    }

    /** Item names awarded on victory. Returns a copy to protect battle state. */
    public List<String> getDroppedItemNames() {
        return new ArrayList<>(droppedItemNames);
    }

    /** Gold from level-ups in this battle, separate from the monster reward. */
    public int getLevelUpGoldEarned() {
        return levelUpGoldEarned;
    }

    // -------------------------
    // Stun Handling
    // -------------------------

    public boolean isPlayerStunned() {
        return !battleOver && playerStunTurns > 0;
    }

    public String skipStunnedTurn() {
        if (battleOver) {
            return "The battle is already over.";
        }

        if (!isPlayerStunned()) {
            return "Player is not stunned.";
        }

        playerStunTurns--;

        String message = player.getName()
                + " is stunned and misses a turn.";

        // Normal attack only: avoid immediate stun chaining.
        message += "\n" + monsterTurn(false);

        return message;
    }

    // -------------------------
    // Player Attack
    // -------------------------

    public String playerAttack() {

        if (battleOver) {
            return "The battle is already over.";
        }

        if (isPlayerStunned()) {
            return skipStunnedTurn();
        }

        int damage = player.attack(monster);
        if (damage > 0) {
            player.addUltimateCharge(1);
        }

        String message =
                player.getName()
                + " attacks "
                + monster.getName()
                + " for "
                + damage
                + " damage.";

        if (!monster.isAlive()) {

            message += "\n" + handleVictory();

            return message;
        }

        message += "\n" + monsterTurn();

        return message;
    }

    // -------------------------
    // Player Skill
    // -------------------------

    public String playerUseSkill() {

        if (battleOver) {
            return "The battle is already over.";
        }

        if (isPlayerStunned()) {
            return skipStunnedTurn();
        }

        int damage = player.useSkill(monster);

        if (damage == -1) {
            return "Not enough MP to use "
                    + player.getSkillName()
                    + ".";
        }

        // A successful offensive skill also earns 1 Ultimate charge.
        if (damage > 0) {
            player.addUltimateCharge(1);
        }

        String message =
                player.getName()
                + " uses "
                + player.getSkillName()
                + " on "
                + monster.getName()
                + " for "
                + damage
                + " damage.";

        if (!monster.isAlive()) {

            message += "\n" + handleVictory();

            return message;
        }

        message += "\n" + monsterTurn();

        return message;
    }

    // -------------------------
    // Player Ultimate
    // -------------------------

    public String playerUseUltimate() {
        if (battleOver) {
            return "The battle is already over.";
        }
        if (isPlayerStunned()) {
            return skipStunnedTurn();
        }
        if (!player.isUltimateReady()) {
            return "Not enough Ultimate charge (" + player.getUltimateCharge()
                    + "/" + player.getMaxUltimateCharge() + ").";
        }
        if (player.getMp() < player.getUltimateMpCost()) {
            return "Not enough MP to use " + player.getUltimateSkillName() + ".";
        }

        int damage = player.useUltimate(monster);
        if (damage < 0) {
            return "Could not use Ultimate.";
        }

        String message = player.getName() + " uses ULTIMATE "
                + player.getUltimateSkillName() + " on "
                + monster.getName() + " for " + damage + " damage!";

        if (!monster.isAlive()) {
            return message + "\n" + handleVictory();
        }
        return message + "\n" + monsterTurn();
    }

    // -------------------------
    // Player Use Item
    // -------------------------

    public String playerUseItem(Item item) {

        if (battleOver) {
            return "The battle is already over.";
        }

        if (isPlayerStunned()) {
            return skipStunnedTurn();
        }

        if (item == null) {
            return "No item selected.";
        }

        if (!player.getInventory()
                .containsItem(item)) {

            return "Item not found in inventory.";
        }

        // -------------------------
        // Battle Item
        // -------------------------

        if (item instanceof BattleUsable) {

            BattleUsable battleItem =
                    (BattleUsable) item;

            int damage =
                    battleItem.useOn(monster);

            if (damage <= 0) {

                return "Could not use "
                        + item.getName()
                        + ".";
            }

            player.getInventory()
                    .removeItem(item);

            String message =
                    player.getName()
                            + " uses "
                            + item.getName()
                            + " on "
                            + monster.getName()
                            + " for "
                            + damage
                            + " damage.";

            if (!monster.isAlive()) {

                message += "\n"
                        + handleVictory();

                return message;
            }

            message += "\n"
                    + monsterTurn();

            return message;
        }

        // -------------------------
        // Potion / Normal Usable
        // -------------------------

        int hpBefore =
                player.getHp();

        int mpBefore =
                player.getMp();

        boolean used =
                player.getInventory()
                        .useItem(
                                item,
                                player
                        );

        if (!used) {

            return "Could not use "
                    + item.getName()
                    + ".";
        }

        int hpRestored =
                player.getHp()
                        - hpBefore;

        int mpRestored =
                player.getMp()
                        - mpBefore;

        String message =
                player.getName()
                        + " uses "
                        + item.getName()
                        + ".";

        if (hpRestored > 0) {

            message += "\nRestored "
                    + hpRestored
                    + " HP.";
        }

        if (mpRestored > 0) {

            message += "\nRestored "
                    + mpRestored
                    + " MP.";
        }

        if (monster.isAlive()) {

            message += "\n"
                    + monsterTurn();
        }

        return message;
    }

    // -------------------------
    // Monster Turn
    // -------------------------


    private String monsterTurn() {
        return monsterTurn(true);
    }

    private String monsterTurn(boolean allowSpecialSkill) {

        if (battleOver || !monster.isAlive()) {
            return "";
        }

        int playerHpBefore = player.getHp();

        // A full enemy charge takes priority over random Special Skills.
        // During Spider's skipped/stunned turn (allowSpecialSkill=false),
        // the monster makes a normal attack rather than using an Ultimate.
        if (allowSpecialSkill && monster.isUltimateReady()) {
            int damage = monster.useUltimate(player);
            if (damage >= 0) {
                String ultimateMessage = monster.getName()
                        + " uses ULTIMATE " + monster.getUltimateSkillName()
                        + " on " + player.getName()
                        + " for " + damage + " damage!";
                if (!player.isAlive()) {
                    battleOver = true;
                    ultimateMessage += "\n" + player.getName() + " has been defeated.";
                }
                return ultimateMessage;
            }
        }

        String message;

        MonsterSkillResult skillResult = null;

        // Try Special Skill
        if (allowSpecialSkill
                && random.nextInt(100)
                        < monster.getSpecialSkillChance()) {

            skillResult = monster.useSpecialSkill(player);
        }

        if (skillResult != null) {

            message = skillResult.getMessage();

            // Apply Stun only if player survives.
            if (skillResult.isPlayerStunned()
                    && player.isAlive()) {

                playerStunTurns = 1;

                message += "\n"
                        + player.getName()
                        + " is stunned for 1 turn!";
            }

        } else {

            // Normal Attack
            int damage = monster.attack(player);

            message =
                    monster.getName()
                    + " attacks "
                    + player.getName()
                    + " for "
                    + damage
                    + " damage.";
        }

        // Monster gains charge only when a normal or Special hit lands.
        if (player.getHp() < playerHpBefore) {
            monster.gainUltimateCharge();
        }

        if (!player.isAlive()) {

            battleOver = true;

            message += "\n"
                    + player.getName()
                    + " has been defeated.";
        }

        return message;
    }



    // -------------------------
    // Escape
    // -------------------------

    public String escape() {

        if (battleOver) {
            return "The battle is already over.";
        }

        if (isPlayerStunned()) {
            return skipStunnedTurn();
        }

        if (!monster.canEscape()) {

            return "You cannot escape from "
                    + monster.getName()
                    + "!";
        }

        escaped = true;
        battleOver = true;

        return player.getName()
                + " escaped from "
                + monster.getName()
                + ".";
    }

    // -------------------------
    // Victory / Rewards
    // -------------------------

    private String handleVictory() {

        battleOver = true;

        if (rewardGiven) {
            return monster.getName() + " has been defeated.";
        }

        int oldLevel = player.getLevel();

        int expReward = monster.getExpReward();
        int goldReward = monster.getGoldReward();
        

        int goldBeforeExperience = player.getGold();
        player.gainExperience(expReward);
        levelUpGoldEarned = player.getGold() - goldBeforeExperience;
        player.gainGold(goldReward);

        rewardGiven = true;

        String message =
                monster.getName()
                + " defeated!"
                + "\nGained "
                + expReward
                + " EXP."
                + "\nGained "
                + goldReward
                + " Gold.";

        Item droppedItem =
                monster.generateDrop();

        if (droppedItem != null) {

            player.getInventory()
                    .addItem(droppedItem);
            droppedItemNames.add(droppedItem.getName());

            message += "\nItem dropped: "
                    + droppedItem.getName()
                    + "!";
        }

        // Soul Shard is an independent bonus drop; potions may also drop.
        // Roll only once per defeated monster (handleVictory grants rewards once).
        if (random.nextDouble() < monster.getSoulShardDropChance()) {
            SoulShard shard = new SoulShard();
            player.getInventory().addItem(shard);
            droppedItemNames.add(shard.getName());
            message += "\nItem dropped: " + shard.getName() + "!";
        }

        if (player.getLevel() > oldLevel) {

            message += "\nLevel Up! Level "
                    + player.getLevel()
                    + "."
                    + "\nLevel Up Bonus: +" + levelUpGoldEarned + " Gold.";
        }

        return message;
    }
}
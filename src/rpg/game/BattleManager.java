package rpg.game;
import rpg.model.character.player.Player;
import rpg.model.character.monster.Monster;
import rpg.model.character.monster.MonsterSkillResult;
import rpg.model.item.Item;
import rpg.model.item.BattleUsable;
import rpg.model.item.SoulShard;
import rpg.model.item.Potion;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
public class BattleManager {
    private Player player;
    private Monster monster;
    private boolean battleOver;
    private boolean escaped;
    private boolean rewardGiven;
    private int levelUpGoldEarned;
    private final Random random = new Random();
    private int playerStunTurns = 0;
    // One free Potion BEFORE the main action. Offensive items end the turn.
    private boolean potionUsedThisTurn;
    private String damageOverTimeName = "";
    private int damageOverTimeDamage;
    private int damageOverTimeTurns;
    private final List<String> droppedItemNames = new ArrayList<>();
    public BattleManager(Player p,Monster m){player=p;monster=m;}
    public Player getPlayer(){return player;}
    public Monster getMonster(){return monster;}
    public boolean isBattleOver(){return battleOver;}
    public boolean isDefeat(){return battleOver&&!player.isAlive();}
    public boolean isVictory(){return battleOver&&!escaped&&player.isAlive()&&!monster.isAlive();}
    public boolean hasEscaped(){return escaped;}
    public boolean isPlayerStunned(){return !battleOver&&playerStunTurns>0;}
    public List<String> getDroppedItemNames(){return new ArrayList<>(droppedItemNames);}
    public int getLevelUpGoldEarned(){return levelUpGoldEarned;}
    public boolean isPotionUsedThisTurn() {
        return potionUsedThisTurn;
    }

    // -------------------------
    // Stun Handling
    // -------------------------
    public String skipStunnedTurn() {
        if (battleOver) return "The battle is already over.";
        if (!isPlayerStunned()) return "Player is not stunned.";
        playerStunTurns--;
        potionUsedThisTurn = false;
        String message = player.getName() + " is stunned and misses a turn.";
        message += "\n" + monsterTurn(false);
        return message;
    }

    // -------------------------
    // Player Attack
    // -------------------------
    public String playerAttack() {
        if (battleOver) return "The battle is already over.";
        if (isPlayerStunned()) return skipStunnedTurn();
        int damage = player.attack(monster);
        if (damage > 0) player.addUltimateCharge(1);
        String message = player.getName() + " attacks " + monster.getName()
                + " for " + damage + " damage.";
        if (!monster.isAlive()) return message + "\n" + handleVictory();
        return finishPlayerAction(message);
    }

    // -------------------------
    // Player Skill
    // -------------------------
    public String playerUseSkill() {
        if (battleOver) return "The battle is already over.";
        if (isPlayerStunned()) return skipStunnedTurn();
        int damage = player.useSkill(monster);
        if (damage == -1) return "Not enough MP to use " + player.getSkillName() + ".";
        if (damage > 0) player.addUltimateCharge(1);
        String message = player.getName() + " uses " + player.getSkillName()
                + " on " + monster.getName() + " for " + damage + " damage.";
        if (!monster.isAlive()) return message + "\n" + handleVictory();
        message += applyHeroEffect(player.getSkillCombatEffect(), false);
        return finishPlayerAction(message);
    }

    // -------------------------
    // Player Ultimate
    // -------------------------
    public String playerUseUltimate() {
        if (battleOver) return "The battle is already over.";
        if (isPlayerStunned()) return skipStunnedTurn();
        if (!player.isUltimateReady()) {
            return "Not enough Ultimate charge (" + player.getUltimateCharge()
                    + "/" + player.getMaxUltimateCharge() + ").";
        }
        if (player.getMp() < player.getUltimateMpCost()) {
            return "Not enough MP to use " + player.getUltimateSkillName() + ".";
        }
        int damage = player.useUltimate(monster);
        if (damage < 0) return "Could not use Ultimate.";
        String message = player.getName() + " uses ULTIMATE "
                + player.getUltimateSkillName() + " on " + monster.getName()
                + " for " + damage + " damage!";
        if (!monster.isAlive()) return message + "\n" + handleVictory();
        message += applyHeroEffect(player.getUltimateCombatEffect(), true);
        return finishPlayerAction(message);
    }

    // -------------------------
    // Player Use Item
    // -------------------------
    public String playerUseItem(Item item) {
        if (battleOver) return "The battle is already over.";
        if (isPlayerStunned()) return skipStunnedTurn();
        if (item == null) return "No item selected.";
        if (!player.getInventory().containsItem(item)) return "Item not found in inventory.";
        if (potionUsedThisTurn) return "Already used a Potion. Choose Attack, Skill, Ultimate or Defend.";

        // Bombs consume the action and trigger the enemy turn immediately.
        if (item instanceof BattleUsable) {
            int damage = ((BattleUsable) item).useOn(monster);
            if (damage <= 0) return "Could not use " + item.getName() + ".";
            player.getInventory().removeItem(item);
            String message = player.getName() + " uses " + item.getName()
                    + " on " + monster.getName() + " for " + damage + " damage.";
            if (!monster.isAlive()) return message + "\n" + handleVictory();
            return finishPlayerAction(message);
        }

        int oldHp = player.getHp();
        int oldMp = player.getMp();
        boolean used = player.getInventory().useItem(item, player);
        if (!used) return "Could not use " + item.getName() + ".";
        String message = player.getName() + " uses " + item.getName() + ".";
        int hpGained = player.getHp() - oldHp;
        int mpGained = player.getMp() - oldMp;
        if (hpGained > 0) message += "\nRestored " + hpGained + " HP.";
        if (mpGained > 0) message += "\nRestored " + mpGained + " MP.";

        if (item instanceof Potion) {
            potionUsedThisTurn = true;
            // No monster reaction yet. The player can still choose ONE main action.
            return message + "\nPotion used (1/1). Choose your action.";
        }
        // Soul Shard or any other non-Potion utility item uses the whole turn.
        return finishPlayerAction(message);
    }

    /** A defensive action: the next real hit is reduced by 65%, at least 1 remains. */
    public String playerDefend() {
        if (battleOver) return "The battle is already over.";
        if (isPlayerStunned()) return skipStunnedTurn();
        player.protectNextHit(65);
        return finishPlayerAction(player.getName()
                + " uses DEFEND! Next hit is reduced by 65%.");
    }

    private String finishPlayerAction(String message) {
        potionUsedThisTurn = false;
        return message + "\n" + monsterTurn();
    }

    /** Skill/Ultimate status: individual classes select the effect polymorphically. */
    private String applyHeroEffect(Player.CombatEffect effect, boolean ultimate) {
        switch (effect) {
            case FORTIFY:
                int guardPercent = ultimate ? 55 : 35;
                player.protectNextHit(guardPercent);
                return "\n" + player.getName() + " gains FORTIFY " + guardPercent
                        + "% for the next enemy hit.";
            case BLEED:
                damageOverTimeName = "BLEED";
                damageOverTimeDamage = ultimate ? 12 : 7;
                damageOverTimeTurns = ultimate ? 3 : 2;
                return "\n" + monster.getName() + " is bleeding for "
                        + damageOverTimeTurns + " ticks.";
            case BURN:
                damageOverTimeName = "BURN";
                damageOverTimeDamage = ultimate ? 12 : 9;
                damageOverTimeTurns = ultimate ? 3 : 2;
                return "\n" + monster.getName() + " is burning for "
                        + damageOverTimeTurns + " ticks.";
            case WARD:
                player.protectNextHit(55);
                return "\n" + player.getName()
                        + " gains MAGIC WARD (55% less damage on next hit).";
            default:
                return "";
        }
    }

    /** Process damage-over-time once before each enemy action. */
    private String tickDamageOverTime() {
        if (damageOverTimeTurns <= 0 || !monster.isAlive()) return "";
        // One point of regular damage + (N-1) true bonus damage; the monster's
        // own one-hit barrier can still mitigate the tick.
        int actual = monster.takeSpecialDamage(1, Math.max(0, damageOverTimeDamage - 1));
        damageOverTimeTurns--;
        String message = monster.getName() + " suffers " + damageOverTimeName
                + " for " + actual + " damage.";
        if (!monster.isAlive()) return message + "\n" + handleVictory();
        return message;
    }

    // -------------------------
    // Monster Turn
    // -------------------------
    private String monsterTurn(){return monsterTurn(true);}
    private String monsterTurn(boolean allowSpecialSkill) {
        if (battleOver || !monster.isAlive()) return "";
        String tickMessage = tickDamageOverTime();
        if (battleOver || !monster.isAlive()) return tickMessage;
        String prefix = tickMessage.isEmpty() ? "" : tickMessage + "\n";
        int playerHpBefore = player.getHp();

        // Full enemy charge takes priority, except during a stunned turn.
        if (allowSpecialSkill && monster.isUltimateReady()) {
            int damage = monster.useUltimate(player);
            if (damage >= 0) {
                String message = monster.getName() + " uses ULTIMATE "
                        + monster.getUltimateSkillName() + " on " + player.getName()
                        + " for " + damage + " damage!";
                if (!player.isAlive()) {
                    battleOver = true;
                    message += "\n" + player.getName() + " has been defeated.";
                }
                return prefix + message;
            }
        }

        MonsterSkillResult result = null;
        if (allowSpecialSkill
                && random.nextInt(100) < monster.getSpecialSkillChance()) {
            result = monster.useSpecialSkill(player);
        }
        String message;
        if (result != null) {
            message = result.getMessage();
            if (result.isPlayerStunned() && player.isAlive()) {
                playerStunTurns = 1;
                message += "\n" + player.getName() + " is stunned for 1 turn!";
            }
        } else {
            int damage = monster.attack(player);
            message = monster.getName() + " attacks " + player.getName()
                    + " for " + damage + " damage.";
        }
        if (player.getHp() < playerHpBefore) monster.gainUltimateCharge();
        if (!player.isAlive()) {
            battleOver = true;
            message += "\n" + player.getName() + " has been defeated.";
        }
        return prefix + message;
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
            return "You cannot escape from " + monster.getName() + "!";
        }
        escaped = true;
        battleOver = true;
        potionUsedThisTurn = false;
        player.clearHitReduction();
        return player.getName() + " escaped from " + monster.getName() + ".";
    }

    // -------------------------
    // Victory / Rewards
    // -------------------------
    private String handleVictory() {
        battleOver = true;
        potionUsedThisTurn = false;
        player.clearHitReduction();

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

        String message = monster.getName() + " defeated!"
                + "\nGained " + expReward + " EXP."
                + "\nGained " + goldReward + " Gold.";

        // Keep the independent multi-Potion loot and Soul Shard rewards.
        for (Item droppedItem : monster.generateDrops()) {
            player.getInventory().addItem(droppedItem);
            droppedItemNames.add(droppedItem.getName());
            message += "\nItem dropped: " + droppedItem.getName() + "!";
        }
        if (random.nextDouble() < monster.getSoulShardDropChance()) {
            SoulShard shard = new SoulShard();
            player.getInventory().addItem(shard);
            droppedItemNames.add(shard.getName());
            message += "\nItem dropped: " + shard.getName() + "!";
        }
        if (player.getLevel() > oldLevel) {
            message += "\nLevel Up! Level " + player.getLevel() + "."
                    + "\nLevel Up Bonus: +" + levelUpGoldEarned + " Gold.";
        }
        return message;
    }
}

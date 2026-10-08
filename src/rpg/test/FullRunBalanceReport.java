package rpg.test;

import java.util.ArrayList;
import rpg.exception.NotEnoughGoldException;
import rpg.game.BattleManager;
import rpg.game.Game;
import rpg.game.Shop;
import rpg.model.character.player.Player;
import rpg.model.item.Armor;
import rpg.model.item.Item;
import rpg.model.item.Weapon;

/**
 * Step 48.5: Tests real progression, replay rewards, shop and potions.
 * No saves or GUI are touched. This is a model of ONE simple play strategy,
 * not a prediction of how every human player will perform.
 */
public class FullRunBalanceReport {

    private static final int TRIALS = 180;
    private static final int MAX_ACTIONS_PER_FIGHT = 200;
    private static final int MAX_REPLAYS_PER_RUN = 25;
    private static final String[] CLASSES = {"Knight", "Archer", "Mage"};

    private enum Plan { DIRECT, SHOP, FARM }

    private static class Result {
        int clearedStages;
        int level;
        int gold;
        int replays;
        int healthPotions;
        int manaPotions;
        boolean completed;
    }

    private static class Totals {
        int completed;
        int[] stageClears = new int[5];
        long totalLevel;
        long totalGold;
        long totalReplays;
        long totalHealth;
        long totalMana;

        void add(Result result) {
            if (result.completed) completed++;
            for (int i = 0; i < result.clearedStages; i++) stageClears[i]++;
            totalLevel += result.level;
            totalGold += result.gold;
            totalReplays += result.replays;
            totalHealth += result.healthPotions;
            totalMana += result.manaPotions;
        }
    }

    public static void main(String[] args) {
        System.out.println("STEP 48.5 — FULL RUN BALANCE REPORT");
        System.out.println("Uses real Game / BattleManager / Shop / Player and Monster classes.");
        System.out.println("HP/MP persist across battles, except FULL recovery on level-up.");
        System.out.println("Replay unlocks no extra story stages; every victory grants XP/Gold.");
        System.out.println("Policy: use skills when possible. SHOP/FARM heal at Hub, buy gear/potions when affordable.");
        System.out.println("Farming: replay latest cleared stage until Lv 2/3/4/5, max "
                + MAX_REPLAYS_PER_RUN + " repeats. No cheats, no automatic regeneration.");
        System.out.println("Runs per class/plan: " + TRIALS);
        System.out.printf("%-7s %-7s %6s %6s %6s %6s %6s %7s %7s %7s %7s %7s%n",
                "Class", "Plan", "Rat", "Spider", "Ghost", "Cycl.", "Boss", "Full%", "AvgLv", "AvgG", "Replay", "HPpot");

        for (String className : CLASSES) {
            for (Plan plan : Plan.values()) {
                Totals t = new Totals();
                for (int trial = 0; trial < TRIALS; trial++) {
                    t.add(run(className, plan));
                }
                System.out.printf("%-7s %-7s %5.1f%% %5.1f%% %5.1f%% %5.1f%% %5.1f%% %6.1f%% %7.2f %7.1f %7.1f %7.1f%n",
                        className, plan,
                        pct(t.stageClears[0]), pct(t.stageClears[1]),
                        pct(t.stageClears[2]), pct(t.stageClears[3]), pct(t.stageClears[4]),
                        pct(t.completed), avg(t.totalLevel), avg(t.totalGold),
                        avg(t.totalReplays), avg(t.totalHealth));
            }
        }
        System.out.println("\nLegend: DIRECT=no shop/potions/farm; SHOP=shop, no farm; FARM=shop + replay for levels.");
        System.out.println("Shown stages are cleared in a single ongoing run; failures stop the run.");
        System.out.println("This simulator uses a basic fixed policy, not optimal player tactics.");
    }

    private static double pct(int count) {
        return count * 100.0 / TRIALS;
    }

    private static double avg(long value) {
        return value / (double) TRIALS;
    }

    private static Result run(String className, Plan plan) {
        Game game = new Game();
        game.startNewGame("Tester", className);
        Player player = game.getPlayer();
        Result result = new Result();

        for (int stage = 0; stage < game.getStageCount(); stage++) {
            // "Stage 2 = level 2" and so on: farm only AFTER unlocking the next stage.
            if (plan == Plan.FARM && stage > 0) {
                int desiredLevel = stage + 1;
                while (player.getLevel() < desiredLevel
                        && result.replays < MAX_REPLAYS_PER_RUN
                        && player.isAlive()) {
                    prepare(game, className, plan, result);
                    if (!player.isAlive() || player.getHp() < 10) break;
                    if (!fight(game, stage - 1, result, plan)) break;
                    result.replays++;
                }
            }

            if (!player.isAlive()) break;
            if (plan != Plan.DIRECT) {
                prepare(game, className, plan, result);
            }
            if (!fight(game, stage, result, plan)) break;
            result.clearedStages++;
        }

        result.completed = result.clearedStages == game.getStageCount();
        result.level = player.getLevel();
        result.gold = player.getGold();
        return result;
    }

    private static boolean fight(Game game, int stage, Result result, Plan plan) {
        final int priorUnlock = game.getStage();
        BattleManager battle = game.startBattle(stage);
        Player player = game.getPlayer();

        for (int turn = 0; turn < MAX_ACTIONS_PER_FIGHT && !battle.isBattleOver(); turn++) {
            if (plan != Plan.DIRECT
                    && player.getHp() * 100 < player.getMaxHp() * 32) {
                Item potion = findInventoryItem(player, "Health Potion");
                if (potion == null) potion = findInventoryItem(player, "Small Health Potion");
                if (potion != null && player.getHp() <= player.getMaxHp() - 10) {
                    int countBefore = player.getInventory().getItemCount();
                    battle.playerUseItem(potion);
                    if (player.getInventory().getItemCount() < countBefore) {
                        result.healthPotions++;
                    }
                    continue;
                }
            }
            String log = battle.playerUseSkill();
            if (log.startsWith("Not enough MP")) {
                battle.playerAttack();
            }
        }

        boolean victory = battle.isVictory();
        if (battle.isBattleOver()) {
            game.finishBattle();
        }
        // Replaying MUST NOT advance unlocked stage beyond its previous value.
        if (stage < priorUnlock && game.getStage() != priorUnlock) {
            throw new IllegalStateException("Replay changed stage progress");
        }
        return victory;
    }

    private static void prepare(Game game, String className, Plan plan, Result result) {
        Player player = game.getPlayer();
        Shop shop = game.getShop();

        // Reuse dropped potions first. This does not advance turns at the hub.
        consumeHubPotions(player, result);

        // Prioritize basic survival. Keep consumables low enough to remain affordable.
        for (int i = 0; i < 8 && player.getHp() * 100 < player.getMaxHp() * 70; i++) {
            if (!buyAndUse(shop, player, "Health Potion", result)) break;
        }

        // Class weapons and armor are bought using actual earned Gold.
        String weaponName = className.equals("Knight") ? "Long Sword"
                : className.equals("Archer") ? "Bow" : "Magic Staff";
        String armorName = className.equals("Knight") ? "Iron Shield" : "Wooden Shield";
        if (player.getEquippedWeapon() == null) {
            buyAndEquip(shop, player, weaponName);
        }
        if (player.getEquippedArmor() == null) {
            buyAndEquip(shop, player, armorName);
        }

        // At the hub, mana potions are free of turn costs (but consume Gold).
        // Preserve some Gold for HP potions so farming is not artificially easy.
        for (int i = 0; i < 6 && player.getMp() < player.getMaxMp() - 9; i++) {
            if (player.getGold() < 45) break;
            if (!buyAndUse(shop, player, "Mana Potion", result)) break;
        }
    }

    private static void consumeHubPotions(Player player, Result result) {
        for (int i = 0; i < 12; i++) {
            Item health = findInventoryItem(player, "Health Potion");
            if (health == null) health = findInventoryItem(player, "Small Health Potion");
            if (health == null || player.getHp() >= player.getMaxHp() - 9) break;
            if (player.getInventory().useItem(health, player)) result.healthPotions++;
            else break;
        }
        for (int i = 0; i < 12; i++) {
            Item mana = findInventoryItem(player, "Mana Potion");
            if (mana == null) mana = findInventoryItem(player, "Small Mana Potion");
            if (mana == null || player.getMp() >= player.getMaxMp() - 4) break;
            if (player.getInventory().useItem(mana, player)) result.manaPotions++;
            else break;
        }
    }

    private static boolean buyAndUse(Shop shop, Player player,
                                      String name, Result result) {
        Item itemForSale = findShopItem(shop, name);
        if (itemForSale == null || player.getGold() < itemForSale.getBuyPrice()) return false;
        try {
            shop.buyItem(player, itemForSale);
        } catch (NotEnoughGoldException exception) {
            return false;
        }
        Item bought = findInventoryItem(player, name);
        if (bought == null || !player.getInventory().useItem(bought, player)) return false;
        if (name.contains("Health")) result.healthPotions++;
        else result.manaPotions++;
        return true;
    }

    private static void buyAndEquip(Shop shop, Player player, String name) {
        Item offer = findShopItem(shop, name);
        if (offer == null || player.getGold() < offer.getBuyPrice() + 25) return;
        try {
            shop.buyItem(player, offer);
        } catch (NotEnoughGoldException exception) {
            return;
        }
        Item item = findInventoryItem(player, name);
        if (item instanceof Weapon) player.equipWeapon((Weapon) item);
        if (item instanceof Armor) player.equipArmor((Armor) item);
    }

    private static Item findShopItem(Shop shop, String name) {
        for (Item item : shop.getItemsForSale()) {
            if (item.getName().equals(name)) return item;
        }
        return null;
    }

    private static Item findInventoryItem(Player player, String name) {
        ArrayList<Item> items = player.getInventory().getItems();
        for (Item item : items) {
            if (item.getName().equals(name)) return item;
        }
        return null;
    }
}

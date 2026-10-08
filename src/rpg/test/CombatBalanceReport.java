package rpg.test;

import rpg.game.BattleManager;
import rpg.game.Shop;
import rpg.model.character.monster.Cyclops;
import rpg.model.character.monster.DarkMage;
import rpg.model.character.monster.Ghost;
import rpg.model.character.monster.Monster;
import rpg.model.character.monster.Rat;
import rpg.model.character.monster.Spider;
import rpg.model.character.player.Archer;
import rpg.model.character.player.Knight;
import rpg.model.character.player.Mage;
import rpg.model.character.player.Player;
import rpg.model.item.Armor;
import rpg.model.item.Item;
import rpg.model.item.Weapon;

/** Optional diagnostic: does NOT change game state or save files. */
public class CombatBalanceReport {

    private static final int TRIALS = 300;
    private static final int MAX_TURNS = 250;
    private static final String[] CLASSES = {"Knight", "Archer", "Mage"};
    private static final String[] STAGES = {
            "Rat", "Spider", "Ghost", "Cyclops", "Dark Mage"
    };

    public static void main(String[] args) {
        int[] naturalLevels = calculateNaturalLevels();

        System.out.println("STEP 48.2 - COMBAT BALANCE REPORT");
        System.out.println("Each fight starts with full HP/MP.");
        System.out.println("AI uses a player skill when MP is sufficient, otherwise normal attack.");
        System.out.println("No potions; monster special skills and their status effects are active.");
        System.out.println("Each win rate is estimated from " + TRIALS + " random battles.");

        runScenario("NATURAL: No farming, no equipment", naturalLevels, false);
        runScenario("PREPARED: Farmed levels + class weapon + shield", 
                new int[]{1, 2, 3, 4, 5}, true);

        System.out.println("\nNOTE: The prepared scenario assumes equipment has been acquired.");
        System.out.println("These are single-battle results, not full-run survival probabilities.");
        System.out.println("Your actual game keeps lost HP/MP between stages unless you level up.");
    }

    private static void runScenario(
            String title, int[] levels, boolean equipped) {

        System.out.println("\n" + title);
        System.out.printf("%-8s %-10s %4s %7s %8s %10s %8s%n",
                "Class", "Monster", "Lv", "HitOut", "HitIn", "HitsToWin", "WinRate");
        System.out.println("-----------------------------------------------------------------");

        for (String className : CLASSES) {
            for (int stage = 0; stage < STAGES.length; stage++) {
                int level = levels[stage];
                Player player = newPlayer(className, level, equipped);
                Monster monster = newMonster(stage);

                int playerDamage = player.attack(monster);
                // Fresh target prevents previous sample damage affecting this value.
                int monsterDamage = newMonster(stage).attack(
                        newPlayer(className, level, equipped));
                int hitsToWin = (int) Math.ceil(
                        monster.getMaxHp() / (double) playerDamage);

                int victories = 0;
                for (int run = 0; run < TRIALS; run++) {
                    if (simulateBattle(className, level, equipped, stage)) {
                        victories++;
                    }
                }

                double winRate = victories * 100.0 / TRIALS;
                System.out.printf("%-8s %-10s %4d %7d %8d %10d %7.1f%%%n",
                        className, STAGES[stage], level, playerDamage,
                        monsterDamage, hitsToWin, winRate);
            }
        }
    }

    private static boolean simulateBattle(
            String className, int level, boolean equipped, int stage) {

        Player player = newPlayer(className, level, equipped);
        Monster monster = newMonster(stage);
        BattleManager battle = new BattleManager(player, monster);

        for (int turn = 0; turn < MAX_TURNS && !battle.isBattleOver(); turn++) {
            // Invalid skill due to insufficient MP consumes no turn in current game.
            String log = battle.playerUseSkill();
            if (log.startsWith("Not enough MP")) {
                battle.playerAttack();
            }
        }
        return battle.isVictory();
    }

    private static int[] calculateNaturalLevels() {
        int[] levels = new int[STAGES.length];
        Player player = new Knight("ProgressionTest");
        for (int stage = 0; stage < levels.length; stage++) {
            levels[stage] = player.getLevel();
            player.gainExperience(newMonster(stage).getExpReward());
        }
        return levels;
    }

    private static Player newPlayer(String className, int level, boolean equipped) {
        Player player;
        switch (className) {
            case "Knight": player = new Knight("Tester"); break;
            case "Archer": player = new Archer("Tester"); break;
            case "Mage": player = new Mage("Tester"); break;
            default: throw new IllegalArgumentException("Unknown class: " + className);
        }

        while (player.getLevel() < level) {
            player.gainExperience(
                    player.getExpToNextLevel() - player.getExperience());
        }

        if (equipped) {
            equipBestShopItems(player, className);
        }
        player.heal(player.getMaxHp());
        player.restoreMp(player.getMaxMp());
        return player;
    }

    private static void equipBestShopItems(Player player, String className) {
        String weaponName;
        if (className.equals("Knight")) {
            weaponName = "Long Sword";
        } else if (className.equals("Archer")) {
            weaponName = "Bow";
        } else {
            weaponName = "Magic Staff";
        }
        String armorName = className.equals("Knight")
                ? "Iron Shield" : "Wooden Shield";

        for (Item item : new Shop().getItemsForSale()) {
            if (item instanceof Weapon && item.getName().equals(weaponName)) {
                player.equipWeapon((Weapon) item);
            }
            if (item instanceof Armor && item.getName().equals(armorName)) {
                player.equipArmor((Armor) item);
            }
        }
    }

    private static Monster newMonster(int stage) {
        switch (stage) {
            case 0: return new Rat();
            case 1: return new Spider();
            case 2: return new Ghost();
            case 3: return new Cyclops();
            case 4: return new DarkMage();
            default: throw new IllegalArgumentException("Invalid stage: " + stage);
        }
    }
}

package rpg.test;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import rpg.game.BattleManager;
import rpg.game.Game;
import rpg.game.Shop;
import rpg.model.character.player.Archer;
import rpg.model.character.player.Knight;
import rpg.model.character.player.Mage;
import rpg.model.character.player.Player;
import rpg.model.character.monster.Cyclops;
import rpg.model.character.monster.DarkMage;
import rpg.model.character.monster.Rat;
import rpg.model.item.Armor;
import rpg.model.item.Item;
import rpg.model.item.SoulShard;
import rpg.model.item.Weapon;
import rpg.persistence.GameData;
import rpg.persistence.SaveManager;

/**
 * Step 51.4: Headless smoke test of Hero's Journey gameplay and persistence.
 * Does NOT modify data/savegame.txt, does NOT change production classes.
 */
public final class GameIntegrationSmokeTest {
    private static int assertions = 0;
    private static int scenarios = 0;

    public static void main(String[] args) throws Exception {
        run("Character creation and stage preview", GameIntegrationSmokeTest::testNewGame);
        run("Combat, victory rewards and replay", GameIntegrationSmokeTest::testBattleAndReplay);
        run("Class equipment bonuses and armor", GameIntegrationSmokeTest::testEquipment);
        run("Weapon purchase limit and sell/rebuy", GameIntegrationSmokeTest::testShop);
        run("Soul Shard inventory and full charge", GameIntegrationSmokeTest::testSoulShard);
        run("Player Ultimate and Level Up", GameIntegrationSmokeTest::testUltimateAndLevelUp);
        run("Boss escape restriction", GameIntegrationSmokeTest::testBossEscape);
        run("Save and Load round trip (temporary file)", GameIntegrationSmokeTest::testSaveLoad);
        System.out.println("\nSUCCESS: " + scenarios + " scenarios, " + assertions + " assertions passed.");
        System.out.println("Existing data/savegame.txt was not accessed.");
    }

    private static void testNewGame() {
        for (String className : new String[] {"Knight", "Archer", "Mage"}) {
            Game game = new Game();
            game.startNewGame("Tester", className);
            require(game.getPlayer().getClass().getSimpleName().equals(className), "wrong class");
            require(game.getStage() == 0, "new-game stage is not Rat");
            require(game.getUnlockedStageCount() == 1, "unexpected unlocked stage count");
            require(game.getCurrentBattle() == null, "battle started without clicking Battle");
            for (int stage = 0; stage < game.getStageCount(); stage++) {
                require(game.getStageName(stage) != null, "missing stage name");
                require(game.previewMonsterForStage(stage) != null, "missing monster preview");
                require(game.getCurrentBattle() == null, "preview unexpectedly started battle");
            }
        }
    }

    private static void testBattleAndReplay() {
        Game game = new Game();
        game.startNewGame("Tester", "Knight");
        Player p = game.getPlayer();
        BattleManager battle = game.startBattle(0);
        int exp = battle.getMonster().getExpReward();
        int gold = battle.getMonster().getGoldReward();
        int startingGold = p.getGold();
        require(battle.getMonster() instanceof Rat, "stage one is not Rat");
        battle.playerUseSkill();
        require(p.getMp() < p.getMaxMp(), "Skill did not consume MP");
        completeRat(battle);
        require(battle.isVictory(), "Rat battle did not end in victory");
        require(p.getExperience() == exp, "wrong EXP after victory");
        require(p.getGold() == startingGold + gold, "wrong Gold after victory");
        require(p.getMp() < p.getMaxMp(), "MP was restored without level up");
        require(game.finishBattle(), "Game.finishBattle rejected victory");
        require(game.getStage() == 1, "Spider did not unlock");
        int goldAfter = p.getGold();
        battle.playerAttack();
        require(p.getGold() == goldAfter, "duplicate rewards after battle over");

        BattleManager replay = game.startBattle(0);
        completeRat(replay);
        require(replay.isVictory(), "replayed Rat was not defeated");
        require(game.finishBattle(), "replayed battle did not finish");
        require(game.getStage() == 1, "replay changed unlocked progress");
        require(p.getGold() == goldAfter + replay.getMonster().getGoldReward(),
                "replay did not grant Gold once");
    }

    private static void completeRat(BattleManager battle) {
        for (int turns = 0; !battle.isBattleOver() && turns < 50; turns++) {
            battle.playerAttack();
        }
        require(battle.isBattleOver(), "battle exceeded 50 turns");
        require(!battle.isDefeat(), "Knight was unexpectedly defeated by Rat");
    }

    private static void testEquipment() {
        Shop shop = new Shop();
        String[] expectedClasses = {"Knight", "Archer", "Mage"};
        String[] ownedWeapons = {"Long Sword", "Bow", "Magic Staff"};
        for (int i = 0; i < expectedClasses.length; i++) {
            Player p = createPlayer(expectedClasses[i]);
            int baseAttack = p.getAttack();
            p.gainGold(1000);
            Weapon matched = (Weapon) storeItem(shop, ownedWeapons[i]);
            Weapon mismatched = (Weapon) storeItem(shop, ownedWeapons[(i + 1) % 3]);
            Armor ironShield = (Armor) storeItem(shop, "Iron Shield");
            require(p.equipWeapon(matched), "matching weapon equip rejected");
            require(p.getAttack() == baseAttack + 10, "matching weapon ATK should be +10");
            require(p.getEquippedClassWeaponDamageBonus() == 7,
                    "matching weapon extra damage should be +7");
            require(p.equipWeapon(mismatched), "off-class weapon equip rejected");
            require(p.getAttack() == baseAttack + 7, "off-class weapon ATK should be +7");
            require(p.getEquippedClassWeaponDamageBonus() == 0,
                    "off-class weapon must not give extra damage");
            require(p.equipArmor(ironShield), "Iron Shield should work for every class");
            require(p.getDefense() >= ironShield.getDefenseBonus(), "armor DEF not applied");
            p.unequipWeapon();
            require(p.getAttack() == baseAttack, "ATK not restored on unequip");
        }
    }

    private static void testShop() throws Exception {
        Shop shop = new Shop();
        Knight p = new Knight("Shopper");
        p.gainGold(200);
        Weapon sword = (Weapon) storeItem(shop, "Long Sword");
        shop.buyItem(p, sword);
        Weapon bought = ownedWeapon(p, "Long Sword");
        require(bought != null, "weapon missing after buying");
        int gold = p.getGold();
        try {
            shop.buyItem(p, sword);
            throw new AssertionError("duplicate weapon purchase was allowed");
        } catch (IllegalStateException expected) {
            assertions++;
        }
        require(p.getGold() == gold, "duplicate purchase spent Gold");
        require(p.equipWeapon(bought), "could not equip purchased weapon");
        require(!shop.sellItem(p, bought), "equipped weapon was sold");
        p.unequipWeapon();
        require(shop.sellItem(p, bought), "unequipped weapon could not be sold");
        require(!shop.alreadyOwnsWeapon(p, sword), "sold weapon still marked owned");
        p.gainGold(250);  // ensure enough Gold to buy again after a sale
        shop.buyItem(p, sword);
        require(shop.alreadyOwnsWeapon(p, sword), "re-buy after selling failed");
    }

    private static void testSoulShard() {
        Knight p = new Knight("Shard Tester");
        SoulShard shard = new SoulShard();
        p.getInventory().addItem(shard);
        require(p.getInventory().useItem(shard, p), "Soul Shard could not be used");
        require(p.getUltimateCharge() == 1, "charge did not increase by one");
        require(!p.getInventory().containsItem(shard), "used Soul Shard remained in bag");
        p.restoreUltimateCharge(p.getMaxUltimateCharge());
        SoulShard extraShard = new SoulShard();
        p.getInventory().addItem(extraShard);
        require(!p.getInventory().useItem(extraShard, p), "full-charge shard was consumed");
        require(p.getInventory().containsItem(extraShard), "full-charge shard disappeared");
    }

    private static void testUltimateAndLevelUp() {
        for (Player p : new Player[] {new Knight("A"), new Archer("B"), new Mage("C")}) {
            p.restoreUltimateCharge(5);
            require(p.isUltimateReady(), "charge at 5 should be ready");
            int mp = p.getMp();
            int damage = p.useUltimate(new Cyclops());
            require(damage > 0, "Ultimate did no damage");
            require(p.getUltimateCharge() == 0, "Ultimate did not spend charge");
            require(p.getMp() == mp - p.getUltimateMpCost(), "Ultimate MP mismatch");
            int remainingMp = p.getMp();
            require(p.useUltimate(new Cyclops()) == -1, "Ultimate worked without charge");
            require(p.getMp() == remainingMp, "failed Ultimate spent MP");
        }
        Knight p = new Knight("Leveler");
        p.takeDamage(70);
        require(p.consumeMp(10), "test setup could not consume MP");
        p.gainExperience(p.getExpToNextLevel());
        require(p.getLevel() == 2, "Level Up failed");
        require(p.getHp() == p.getMaxHp(), "Level Up did not restore HP");
        require(p.getMp() == p.getMaxMp(), "Level Up did not restore MP");
    }

    private static void testBossEscape() {
        BattleManager battle = new BattleManager(new Knight("Runner"), new DarkMage());
        String message = battle.escape();
        require(message.toLowerCase().contains("cannot escape"), "boss escape was not blocked");
        require(!battle.isBattleOver(), "failed escape ended boss battle");
    }

    private static void testSaveLoad() throws Exception {
        File temporarySave = Files.createTempFile("heros-journey-integration-", ".txt").toFile();
        try {
            Knight p = new Knight("Save Tester");
            p.gainGold(1000);
            Shop shop = new Shop();
            shop.buyItem(p, storeItem(shop, "Long Sword"));
            shop.buyItem(p, storeItem(shop, "Iron Shield"));
            Weapon weapon = ownedWeapon(p, "Long Sword");
            Armor armor = null;
            for (Item item : p.getInventory().getItems()) {
                if (item instanceof Armor) armor = (Armor) item;
            }
            require(weapon != null && armor != null, "test equipment purchase failed");
            require(p.equipWeapon(weapon) && p.equipArmor(armor), "test equipment equip failed");
            p.getInventory().addItem(new SoulShard());
            p.restoreUltimateCharge(3);
            p.takeDamage(40);
            require(p.consumeMp(10), "test MP setup failed");
            int atk = p.getAttack(), def = p.getDefense();
            int hp = p.getHp(), mp = p.getMp(), gold = p.getGold();
            int items = p.getInventory().getItemCount();
            SaveManager.saveGame(new GameData(p, 2), temporarySave.getAbsolutePath());
            GameData restored = SaveManager.loadGame(temporarySave.getAbsolutePath());
            Player q = restored.getPlayer();
            require(restored.getStage() == 2, "stage lost in save/load");
            require(q.getClass() == Knight.class, "class lost in save/load");
            require(q.getName().equals("Save Tester"), "name lost in save/load");
            require(q.getHp() == hp && q.getMp() == mp, "HP or MP lost in save/load");
            require(q.getGold() == gold, "Gold lost in save/load");
            require(q.getUltimateCharge() == 3, "Ultimate charge lost in save/load");
            require(q.getInventory().getItemCount() == items, "inventory count lost");
            require(q.getEquippedWeapon() != null && q.getEquippedArmor() != null,
                    "equipped item lost in save/load");
            require(q.getAttack() == atk && q.getDefense() == def,
                    "equipment stats changed after save/load");
            boolean shardRestored = q.getInventory().getItems().stream()
                    .anyMatch(item -> item instanceof SoulShard);
            require(shardRestored, "Soul Shard lost in save/load");
        } finally {
            Files.deleteIfExists(temporarySave.toPath());
        }
    }

    private static Player createPlayer(String name) {
        switch (name) {
            case "Knight": return new Knight("Equipment Tester");
            case "Archer": return new Archer("Equipment Tester");
            case "Mage": return new Mage("Equipment Tester");
            default: throw new IllegalArgumentException(name);
        }
    }

    private static Item storeItem(Shop shop, String name) {
        for (Item item : shop.getItemsForSale()) {
            if (item.getName().equals(name)) return item;
        }
        throw new IllegalStateException("Shop item not found: " + name);
    }

    private static Weapon ownedWeapon(Player p, String name) {
        for (Item item : p.getInventory().getItems()) {
            if (item instanceof Weapon && item.getName().equals(name)) {
                return (Weapon) item;
            }
        }
        return null;
    }

    private static void require(boolean valid, String message) {
        assertions++;
        if (!valid) throw new AssertionError(message);
    }

    private static void run(String scenario, TestAction action) throws Exception {
        action.execute();
        scenarios++;
        System.out.println("[PASS] " + scenario);
    }

    @FunctionalInterface
    private interface TestAction {
        void execute() throws Exception;
    }
}

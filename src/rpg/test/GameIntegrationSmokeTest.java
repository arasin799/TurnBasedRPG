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
        // A replay may also trigger Level Up (Rat gives 40 EXP; two wins reach 80).
        // The game's level-up bonus is separate from the monster's Gold reward.
        int expectedReplayGold = goldAfter + replay.getMonster().getGoldReward()
                + replay.getLevelUpGoldEarned();
        require(p.getGold() == expectedReplayGold,
                "replay Gold should include monster reward and Level Up bonus");

        // Finishing a battle must never pay its rewards a second time.
        int goldAfterReplay = p.getGold();
        replay.playerAttack();
        require(p.getGold() == goldAfterReplay,
                "replay granted duplicate Gold after battle over");
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
        String[] classes = {"Knight", "Archer", "Mage"};
        String[] classWeapons = {"Long Sword", "Bow", "Magic Staff"};
        for (int i = 0; i < classes.length; i++) {
            Player p = createPlayer(classes[i]);
            int baseAttack = p.getAttack();
            int baseDefense = p.getDefense();
            Weapon matched = (Weapon) storeItem(shop, classWeapons[i]);
            Weapon dagger = (Weapon) storeItem(shop, "Dagger");
            Weapon bow = (Weapon) storeItem(shop, "Bow");
            Weapon staff = (Weapon) storeItem(shop, "Magic Staff");
            Weapon sword = (Weapon) storeItem(shop, "Long Sword");
            Armor wooden = (Armor) storeItem(shop, "Wooden Shield");
            Armor iron = (Armor) storeItem(shop, "Iron Shield");

            for (Weapon weapon : new Weapon[] {sword, bow, staff, dagger}) {
                require(p.equipWeapon(weapon), "could not stack distinct weapon types");
            }
            require(p.getEquippedWeapons().size() == 4, "four weapons should be equipped");
            require(p.getAttack() == baseAttack + 29, "stacked weapons should grant +29 ATK");
            require(p.getEquippedClassWeaponDamageBonus() == 7,
                    "only the matching weapon should grant +7 extra damage");
            require(!p.equipWeapon(matched), "same weapon type equipped twice");
            require(p.getAttack() == baseAttack + 29, "duplicate equip changed ATK");

            require(p.equipArmor(wooden) && p.equipArmor(iron),
                    "two different shield types should stack");
            require(p.getDefense() == baseDefense + 19, "shields should grant +19 DEF");
            require(!p.equipArmor(iron), "same shield type equipped twice");
            require(p.unequipArmor(wooden), "could not unequip Wooden Shield only");
            require(p.getDefense() == baseDefense + 12, "Iron Shield was not kept");
            require(p.unequipWeapon(dagger), "could not unequip Dagger only");
            require(p.getAttack() == baseAttack + 24, "Dagger's +5 ATK was not removed");
            require(p.getEquippedClassWeaponDamageBonus() == 7,
                    "removing Dagger incorrectly removed class extra damage");
            require(p.unequipWeapon(matched), "could not unequip matching weapon");
            require(p.getEquippedClassWeaponDamageBonus() == 0,
                    "class bonus remained after removing class weapon");
            require(p.getAttack() == baseAttack + 14,
                    "off-class weapons should each contribute +7 ATK");
            p.unequipWeapon();
            p.unequipArmor();
            require(p.getAttack() == baseAttack && p.getDefense() == baseDefense,
                    "bulk unequip should reset all equipment bonuses");
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

        // Shield limits use ArmorType, independently of weapon purchases.
        p.gainGold(1000);
        Armor wooden = (Armor) storeItem(shop, "Wooden Shield");
        Armor iron = (Armor) storeItem(shop, "Iron Shield");
        require(!shop.alreadyOwnsArmor(p, wooden), "Wooden Shield falsely marked owned");
        shop.buyItem(p, wooden);
        require(shop.alreadyOwnsArmor(p, wooden), "Wooden Shield ownership not detected");
        int goldBeforeDuplicate = p.getGold();
        int itemsBeforeDuplicate = p.getInventory().getItemCount();
        try {
            shop.buyItem(p, wooden);
            throw new AssertionError("duplicate Wooden Shield purchase was allowed");
        } catch (IllegalStateException expected) {
            assertions++;
        }
        require(p.getGold() == goldBeforeDuplicate, "duplicate shield purchase spent Gold");
        require(p.getInventory().getItemCount() == itemsBeforeDuplicate,
                "duplicate shield purchase added an item");

        // Each shield type can be owned and equipped at the same time.
        shop.buyItem(p, iron);
        require(shop.alreadyOwnsArmor(p, iron), "Iron Shield ownership not detected");
        Armor ownedWooden = null;
        Armor ownedIron = null;
        for (Item item : p.getInventory().getItems()) {
            if (item instanceof Armor) {
                Armor armor = (Armor) item;
                if (armor.getArmorType() == wooden.getArmorType()) ownedWooden = armor;
                if (armor.getArmorType() == iron.getArmorType()) ownedIron = armor;
            }
        }
        require(ownedWooden != null && ownedIron != null, "two shield types not owned");
        require(p.equipArmor(ownedWooden) && p.equipArmor(ownedIron),
                "two different shields should be equippable together");
        require(!shop.sellItem(p, ownedWooden), "equipped Wooden Shield was sold");
        require(p.unequipArmor(ownedWooden), "could not unequip Wooden Shield");
        require(shop.sellItem(p, ownedWooden), "unequipped Wooden Shield could not be sold");
        require(!shop.alreadyOwnsArmor(p, wooden), "sold shield still marked owned");
        require(shop.alreadyOwnsArmor(p, iron), "selling Wooden Shield removed Iron Shield ownership");
        shop.buyItem(p, wooden);
        require(shop.alreadyOwnsArmor(p, wooden), "re-buy of Wooden Shield failed");
        require(p.isEquipped(ownedIron), "Iron Shield was unequipped by shield sale");
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
            p.gainGold(3000);
            Shop shop = new Shop();
            String[] equipmentNames = {"Long Sword", "Bow", "Magic Staff", "Dagger",
                    "Wooden Shield", "Iron Shield"};
            for (String name : equipmentNames) {
                shop.buyItem(p, storeItem(shop, name));
            }
            for (Item item : p.getInventory().getItems()) {
                if (item instanceof Weapon) {
                    require(p.equipWeapon((Weapon) item), "could not equip saved weapon");
                } else if (item instanceof Armor) {
                    require(p.equipArmor((Armor) item), "could not equip saved armor");
                }
            }
            require(p.getEquippedWeapons().size() == 4 && p.getEquippedArmors().size() == 2,
                    "save setup missing stacked equipment");
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
            require(q.getEquippedWeapons().size() == 4 && q.getEquippedArmors().size() == 2,
                    "stacked equipped items lost in save/load");
            require(q.getAttack() == atk && q.getDefense() == def,
                    "equipment stats changed after save/load");
            boolean shardRestored = q.getInventory().getItems().stream()
                    .anyMatch(item -> item instanceof SoulShard);
            require(shardRestored, "Soul Shard lost in save/load");

            // Legacy saves had a single weapon and armor index. They must still load.
            String saved = Files.readString(temporarySave.toPath());
            String legacy = saved.replaceAll("(?m)^EQUIPPED_WEAPON_INDICES=.*$",
                    "EQUIPPED_WEAPON_INDEX=0")
                    .replaceAll("(?m)^EQUIPPED_ARMOR_INDICES=.*$",
                    "EQUIPPED_ARMOR_INDEX=4");
            Files.writeString(temporarySave.toPath(), legacy);
            Player oldSavePlayer = SaveManager.loadGame(temporarySave.getAbsolutePath()).getPlayer();
            require(oldSavePlayer.getEquippedWeapons().size() == 1,
                    "legacy weapon index did not load");
            require(oldSavePlayer.getEquippedArmors().size() == 1,
                    "legacy armor index did not load");
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

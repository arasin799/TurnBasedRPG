package rpg.test;

import java.nio.file.Files;
import java.nio.file.Path;

import javax.swing.JButton;
import javax.swing.SwingUtilities;

import rpg.game.BattleManager;
import rpg.gui.panel.BattlePanel;
import rpg.model.character.monster.Monster;
import rpg.model.character.monster.MonsterSkillResult;
import rpg.model.character.monster.Rat;
import rpg.model.character.player.Archer;
import rpg.model.character.player.Knight;
import rpg.model.character.player.Mage;
import rpg.model.character.player.Player;
import rpg.model.item.Item;
import rpg.persistence.GameData;
import rpg.persistence.SaveManager;

/** Small no-save-overwrite test; run this class separately from Main.java. */
public class UltimateSkillSmokeTest {

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void testClass(Player player) {
        Rat rat = new Rat();
        BattleManager battle = new BattleManager(player, rat);
        int initialHp = rat.getHp();
        int initialMp = player.getMp();

        String blocked = battle.playerUseUltimate();
        check(blocked.startsWith("Not enough Ultimate charge"), "charge check failed");
        check(rat.getHp() == initialHp && player.getMp() == initialMp,
                "blocked Ultimate spent MP or dealt damage");
        check(player.addUltimateCharge(5), "could not fill charge");
        check(player.isUltimateReady(), "charge does not report ready");
        check(player.canUseUltimate(), "full-charge Ultimate should be usable");

        int oldHp = rat.getHp();
        int mpCost = player.getUltimateMpCost();
        String log = battle.playerUseUltimate();
        check(log.contains("uses ULTIMATE "), "missing Ultimate battle log");
        check(rat.getHp() < oldHp, "Ultimate did not damage enemy");
        check(player.getUltimateCharge() == 0, "Ultimate did not consume all charge");
        check(player.getMp() == initialMp - mpCost, "Ultimate MP cost incorrect");
        check(!player.canUseUltimate(), "Ultimate should not be ready immediately again");
        System.out.println("PASS " + player.getClass().getSimpleName()
                + " | " + player.getUltimateSkillName() + " | " + log.split("\\n")[0]);
    }

    private static void testInsufficientMp() {
        Archer player = new Archer("MP-Test");
        player.addUltimateCharge(5);
        int mpToSpend = player.getMp() - player.getUltimateMpCost() + 1;
        check(player.consumeMp(mpToSpend), "could not setup low MP");
        Rat rat = new Rat();
        BattleManager battle = new BattleManager(player, rat);
        int hp = rat.getHp();
        int mp = player.getMp();
        String result = battle.playerUseUltimate();
        check(result.startsWith("Not enough MP"), "low MP should block Ultimate");
        check(rat.getHp() == hp && player.getMp() == mp
                && player.getUltimateCharge() == 5, "blocked Ultimate spent resources");
        System.out.println("PASS insufficient MP leaves charge/turn untouched");
    }

    /** Guaranteed stun source to verify the turn is skipped without spending Ultimate. */
    private static class StunDummy extends Monster {
        StunDummy() { super("Stun Dummy", 1000, 0, 3, 0, 0, 0, false); }
        @Override public Item generateDrop() { return null; }
        @Override public int getSpecialSkillChance() { return 100; }
        @Override public MonsterSkillResult useSpecialSkill(Player player) {
            player.takeSpecialDamage(3, 1);
            return new MonsterSkillResult("Stun Dummy uses Web Trap for 4 damage!", true);
        }
    }

    private static void testStun() {
        Knight player = new Knight("Stun-Test");
        player.addUltimateCharge(5);
        BattleManager battle = new BattleManager(player, new StunDummy());
        battle.playerAttack(); // Dummy applies stun after the hit.
        check(battle.isPlayerStunned(), "stun was not applied");
        int mp = player.getMp();
        String result = battle.playerUseUltimate();
        check(result.contains("stunned and misses a turn"), "Ultimate did not skip stunned turn");
        check(player.getMp() == mp && player.getUltimateCharge() == 5,
                "stunned player incorrectly spent charge/MP");
        check(!battle.isPlayerStunned(), "stun should last just one skipped turn");
        System.out.println("PASS stun skip preserves charge and MP");
    }

    private static void testVictoryAndSave() throws Exception {
        Mage player = new Mage("Save-Test");
        Rat rat = new Rat();
        BattleManager battle = new BattleManager(player, rat);
        rat.takeDamage(90);
        player.addUltimateCharge(5);
        String log = battle.playerUseUltimate();
        check(battle.isVictory(), "finishing Ultimate did not trigger victory");
        check(log.contains("Gained 25 EXP"), "victory rewards missing");
        check(player.getUltimateCharge() == 0, "finisher left charge unspent");
        player.addUltimateCharge(4);
        Path save = Files.createTempFile("heros-ultimate-test-", ".txt");
        try {
            SaveManager.saveGame(new GameData(player, 1), save.toString());
            Player loaded = SaveManager.loadGame(save.toString()).getPlayer();
            check(loaded.getUltimateCharge() == 4, "save/load lost Ultimate charge");
            check(loaded.getMp() == player.getMp(), "save/load changed MP");
        } finally {
            Files.deleteIfExists(save);
        }
        System.out.println("PASS victory and Save/Load");
    }

    private static void testButtonState() throws Exception {
        Knight player = new Knight("UI-Test");
        BattleManager battle = new BattleManager(player, new StunDummy());
        SwingUtilities.invokeAndWait(() -> {
            try {
                BattlePanel panel = new BattlePanel(null, null, battle);
                java.lang.reflect.Field field = BattlePanel.class.getDeclaredField("ultimateButton");
                field.setAccessible(true);
                JButton ultimateButton = (JButton) field.get(panel);
                check(!ultimateButton.isEnabled(), "Ultimate button should start disabled");
                player.addUltimateCharge(5);
                java.lang.reflect.Method refresh = BattlePanel.class
                        .getDeclaredMethod("setActionButtonsEnabled", boolean.class);
                refresh.setAccessible(true);
                refresh.invoke(panel, true);
                check(ultimateButton.isEnabled(), "Ultimate button not enabled when ready");
                check(ultimateButton.getText().contains("0/5"),
                        "button label changed without refresh unexpectedly");
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        });
        System.out.println("PASS Ultimate button enabled/disabled behavior");
    }

    public static void main(String[] args) throws Exception {
        testClass(new Knight("Knight-Test"));
        testClass(new Archer("Archer-Test"));
        testClass(new Mage("Mage-Test"));
        testInsufficientMp();
        testStun();
        testVictoryAndSave();
        testButtonState();
        System.out.println("ALL STEP 50.2 TESTS PASSED");
    }
}

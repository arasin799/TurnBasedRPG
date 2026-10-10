package rpg.test;

import rpg.game.BattleManager;
import rpg.model.character.monster.*;
import rpg.model.character.player.Knight;
import rpg.model.character.player.Player;
import rpg.model.item.Item;

/** Tests individual monster charging, MP rules, and automated Ultimate turns. */
public class MonsterUltimateSmokeTest {
    private static int checks;
    private static void require(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }

    private static class TrainingMonster extends Monster {
        TrainingMonster(int required, boolean stun) {
            super("Training Monster", 600, 20, 3, 0, 0, 0, false);
            configureUltimate("Test Nova", required, 0, 12);
            this.stun = stun;
        }
        private final boolean stun;
        @Override public Item generateDrop() { return null; }
        @Override public int getSpecialSkillChance() { return stun ? 100 : 0; }
        @Override public MonsterSkillResult useSpecialSkill(Player player) {
            int dealt = player.takeSpecialDamage(getAttack(), 2);
            return new MonsterSkillResult(getName() + " uses Web Trap on "
                    + player.getName() + " for " + dealt + " damage!", stun);
        }
    }

    public static void main(String[] args) {
        Monster[] monsters = { new Rat(), new Spider(), new Ghost(),
                new Cyclops(), new DarkMage() };
        int[] costs = {0, 5, 10, 15, 25};
        int[] charges = {3, 4, 4, 5, 5};
        for (int i=0; i<monsters.length; i++) {
            Monster monster = monsters[i];
            Player player = new Knight("Tester");
            require(monster.getUltimateChargeRequired() == charges[i], "Charge cost " + monster.getName());
            require(monster.useUltimate(player) == -1, "Not usable before charge is full");
            int mpInitial = monster.getMp();
            for (int j=0; j<charges[i]+5; j++) monster.gainUltimateCharge();
            require(monster.getUltimateCharge() == charges[i], "Charge cannot exceed cap");
            require(monster.isUltimateReady(), "Ultimate ready " + monster.getName());
            int damage = monster.useUltimate(player);
            require(damage > 0, "Ultimate should deal damage " + monster.getName());
            require(monster.getMp() == mpInitial - costs[i], "Wrong MP cost " + monster.getName());
            require(monster.getUltimateCharge() == 0, "Charge must reset after Ultimate");
            require(!monster.isUltimateReady(), "Not ready after Ultimate");
        }
        Player player = new Knight("Hero");
        TrainingMonster training = new TrainingMonster(2, false);
        BattleManager b = new BattleManager(player, training);
        String first = b.playerAttack();
        require(!first.contains("uses ULTIMATE"), "First attack should not trigger an Ultimate");
        require(training.getUltimateCharge() == 1, "Monster gains 1 charge after normal attack");
        b.playerAttack();
        require(training.getUltimateCharge() == 2, "Charge should reach 2/2");
        String third = b.playerAttack();
        require(third.contains("uses ULTIMATE Test Nova"), "Ultimate must automatically fire when ready");
        require(training.getUltimateCharge() == 0, "Automatic Ultimate consumes charge");
        require(!b.isBattleOver(), "Battle continues after Ultimate");

        Player stunnable = new Knight("Knight");
        TrainingMonster web = new TrainingMonster(1, true);
        BattleManager webBattle = new BattleManager(stunnable, web);
        webBattle.playerAttack(); // Web Trap, stuns the player and fully charges monster.
        require(web.getUltimateCharge() == 1, "Special attack grants Ultimate charge");
        require(webBattle.isPlayerStunned(), "Web Trap stun must be active");
        String skipped = webBattle.playerAttack();
        require(skipped.contains("is stunned and misses a turn"), "Stunned player misses turn");
        require(!skipped.contains("uses ULTIMATE"), "Skipped turn must suppress monster Ultimate");
        require(web.getUltimateCharge() == 1, "Charge remains full during skipped turn");
        String next = webBattle.playerAttack();
        require(next.contains("uses ULTIMATE Test Nova"), "Enemy Ultimate fires after skipped turn");
        System.out.println("PASS: " + checks + " checks - monster ultimate smoke tests");
    }
}

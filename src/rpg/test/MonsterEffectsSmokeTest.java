package rpg.test;

import rpg.game.BattleManager;
import rpg.model.character.monster.Cyclops;
import rpg.model.character.monster.DarkMage;
import rpg.model.character.monster.Ghost;
import rpg.model.character.monster.Monster;
import rpg.model.character.monster.MonsterSkillResult;
import rpg.model.character.monster.Rat;
import rpg.model.character.monster.Spider;
import rpg.model.character.player.Knight;
import rpg.model.character.player.Player;
import rpg.model.item.SmallHealthPotion;

/**
 * Step 51.5.2: deterministic checks for monster skills, shields and turns.
 * No Swing window, file saving, or changes to production classes.
 */
public final class MonsterEffectsSmokeTest {
    private static int assertions;
    private static int scenarios;

    private MonsterEffectsSmokeTest() { }

    private static void check(boolean condition, String explanation) {
        assertions++;
        if (!condition) throw new AssertionError(explanation);
    }

    private static void passed(String message) {
        scenarios++;
        System.out.println("[PASS] " + message);
    }

    /** Force a Special Skill for deterministic testing, with the real Spider ability. */
    private static final class AlwaysWebSpider extends Spider {
        @Override public int getSpecialSkillChance() { return 100; }
    }

    private static final class AlwaysFadeGhost extends Ghost {
        @Override public int getSpecialSkillChance() { return 100; }
    }

    private static final class AlwaysBarrierDarkMage extends DarkMage {
        @Override public int getSpecialSkillChance() { return 100; }
    }

    private static void specialSkillDamage() {
        Monster[] monsters = { new Rat(), new Spider(), new Ghost(),
                new Cyclops(), new DarkMage() };
        String[] skillNames = { "Frenzy Bite", "Web Trap", "Fade", "Heavy Smash", "Dark Barrier" };

        for (int i = 0; i < monsters.length; i++) {
            Monster monster = monsters[i];
            Player normalTarget = new Knight("NormalTarget");
            Player specialTarget = new Knight("SpecialTarget");
            int normalDamage = monster.attack(normalTarget);
            MonsterSkillResult result = monster.useSpecialSkill(specialTarget);
            int specialDamage = specialTarget.getMaxHp() - specialTarget.getHp();
            check(result != null, monster.getName() + " did not use Special Skill");
            check(result.getMessage().contains(skillNames[i]), monster.getName() + " skill name missing");
            check(specialDamage > normalDamage,
                    monster.getName() + " Special Skill should hit harder than normal attack");
            check(result.isPlayerStunned() == (monster instanceof Spider),
                    monster.getName() + " has wrong stun flag");
        }
        passed("All 5 monster Special Skills deal bonus damage");
    }

    private static void shieldOneHitOnly(Monster shieldMonster) {
        shieldMonster.useSpecialSkill(new Knight("Dummy"));
        int expectedNormal = shieldMonster instanceof Ghost
                ? new Ghost().takeDamage(80) : new DarkMage().takeDamage(80);
        int shielded = shieldMonster.takeDamage(80);
        int afterShield = shieldMonster.takeDamage(80);
        check(shielded == Math.max(1, (expectedNormal + 1) / 2),
                shieldMonster.getName() + " must halve the next incoming damage");
        check(afterShield == expectedNormal,
                shieldMonster.getName() + " shield must expire after ONE hit");
    }

    private static void shields() {
        shieldOneHitOnly(new Ghost());
        shieldOneHitOnly(new DarkMage());
        passed("Ghost Fade and Dark Barrier protect exactly one hit");
    }

    private static void spiderStun() {
        Player hero = new Knight("Hero");
        AlwaysWebSpider spider = new AlwaysWebSpider();
        BattleManager battle = new BattleManager(hero, spider);
        SmallHealthPotion potion = new SmallHealthPotion();
        hero.getInventory().addItem(potion);

        String firstTurn = battle.playerAttack();
        check(firstTurn.contains("uses Web Trap"), "Spider must use Web Trap");
        check(firstTurn.contains("is stunned for 1 turn"), "Stun message missing");
        check(battle.isPlayerStunned(), "Spider stun not applied");
        int itemsBefore = hero.getInventory().getItemCount();
        String skipped = battle.playerUseItem(potion);
        check(skipped.contains("is stunned and misses a turn"), "Stun did not skip turn");
        check(!skipped.contains("uses ULTIMATE"), "Monster Ultimate must not trigger on skipped turn");
        check(hero.getInventory().getItemCount() == itemsBefore,
                "Potion was accidentally consumed during stun");
        check(hero.getInventory().containsItem(potion), "Potion reference was removed during stun");
        check(!battle.isPlayerStunned(), "One-turn stun did not expire");
        passed("Spider stun skips one turn; potion safe; no instant Ultimate");
    }

    private static void monsterUltimates() {
        Monster[] monsters = { new Rat(), new Spider(), new Ghost(),
                new Cyclops(), new DarkMage() };
        int[] mpCosts = { 0, 5, 10, 15, 25 };
        int[] chargeRequirements = { 3, 4, 4, 5, 5 };
        for (int i = 0; i < monsters.length; i++) {
            Monster monster = monsters[i];
            Player hero = new Knight("Hero");
            int mpBefore = monster.getMp();
            check(monster.useUltimate(hero) == -1,
                    monster.getName() + " Ultimate used before charging");
            for (int charge = 0; charge < chargeRequirements[i] + 2; charge++) {
                monster.gainUltimateCharge();
            }
            check(monster.getUltimateCharge() == chargeRequirements[i],
                    monster.getName() + " charge exceeded max");
            check(monster.isUltimateReady(), monster.getName() + " Ultimate should be ready");
            int damage = monster.useUltimate(hero);
            check(damage > 0, monster.getName() + " Ultimate failed to hit");
            check(monster.getMp() == mpBefore - mpCosts[i],
                    monster.getName() + " Ultimate MP cost is wrong");
            check(monster.getUltimateCharge() == 0,
                    monster.getName() + " Ultimate did not reset charge");
        }
        passed("All 5 Monster Ultimates consume charge and correct MP");
    }

    private static void automaticUltimateAndLog() {
        Rat rat = new Rat();
        for (int i = 0; i < rat.getUltimateChargeRequired(); i++) rat.gainUltimateCharge();
        BattleManager battle = new BattleManager(new Knight("Hero"), rat);
        String log = battle.playerAttack();
        check(log.contains("uses ULTIMATE Feral Rush"),
                "Monster Ultimate did not activate automatically in its turn");
        check(rat.getUltimateCharge() == 0, "Automatic Ultimate did not reset charge");

        BattleManager ghostBattle = new BattleManager(new Knight("Hero"), new AlwaysFadeGhost());
        String ghostLog = ghostBattle.playerAttack();
        check(ghostLog.contains("fades into mist"), "Ghost Fade effect log missing");
        BattleManager bossBattle = new BattleManager(new Knight("Hero"), new AlwaysBarrierDarkMage());
        String bossLog = bossBattle.playerAttack();
        check(bossLog.contains("creates a magical barrier"), "Dark Barrier effect log missing");
        passed("Automatic Monster Ultimate and battle effect log text");
    }

    public static void main(String[] args) {
        specialSkillDamage();
        shields();
        spiderStun();
        monsterUltimates();
        automaticUltimateAndLog();
        System.out.println("\nSUCCESS: " + scenarios + " scenarios, " + assertions + " assertions passed.");
        System.out.println("No save files or gameplay source files were modified.");
    }
}

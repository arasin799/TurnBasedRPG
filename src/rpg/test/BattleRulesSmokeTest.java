package rpg.test;

import rpg.game.BattleManager;
import rpg.model.character.monster.Monster;
import rpg.model.character.player.Archer;
import rpg.model.character.player.Knight;
import rpg.model.character.player.Mage;
import rpg.model.item.Bomb;
import rpg.model.item.Item;
import rpg.model.item.SmallHealthPotion;
import rpg.model.item.SoulShard;

/** No Swing or save access. Deterministic non-special training monster. */
public class BattleRulesSmokeTest {
    private static int assertions;

    private static class TrainingMonster extends Monster {
        TrainingMonster() {
            super("Training Dummy", 900, 0, 20, 0, 0, 0, false);
            configureUltimate("No Ultimate", 100, 0, 0);
        }
        @Override public Item generateDrop() { return null; }
    }

    private static void check(boolean condition, String message) {
        assertions++;
        if (!condition) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        // Potion uses a pre-action allowance, does not trigger enemy reaction.
        Knight hero = new Knight("PotionTester");
        TrainingMonster enemy = new TrainingMonster();
        BattleManager battle = new BattleManager(hero, enemy);
        hero.takeDamage(65);
        SmallHealthPotion first = new SmallHealthPotion();
        SmallHealthPotion second = new SmallHealthPotion();
        hero.getInventory().addItem(first);
        hero.getInventory().addItem(second);
        int hpBefore = hero.getHp();
        check(battle.playerUseItem(first).contains("Potion used (1/1)"), "Potion allowance");
        check(hero.getHp() > hpBefore, "Potion restores HP");
        check(battle.isPotionUsedThisTurn(), "Turn flag after Potion");
        check(battle.playerUseItem(second).contains("Already used"), "No second Potion");
        check(hero.getInventory().containsItem(second), "Rejected Potion not consumed");
        battle.playerAttack();
        check(!battle.isPotionUsedThisTurn(), "Attack ends the turn");
        check(battle.playerUseItem(second).contains("Potion used"), "Potion available next turn");

        // Bomb is an offensive action: no free follow-up Attack.
        Knight bomber = new Knight("Bomber");
        BattleManager bombBattle = new BattleManager(bomber, new TrainingMonster());
        Bomb bomb = new Bomb();
        bomber.getInventory().addItem(bomb);
        int hp0 = bomber.getHp();
        String bombLog = bombBattle.playerUseItem(bomb);
        check(bombLog.contains("uses Bomb"), "Bomb log");
        check(!bomber.getInventory().containsItem(bomb), "Bomb consumed");
        check(bomber.getHp() < hp0, "Monster counterattacks on Bomb turn");

        // Defend prevents 65% of final damage but never provides immunity.
        Knight plain = new Knight("Plain");
        Knight guarded = new Knight("Guarded");
        BattleManager plainBattle = new BattleManager(plain, new TrainingMonster());
        BattleManager guardedBattle = new BattleManager(guarded, new TrainingMonster());
        int initialHP = plain.getHp();
        plainBattle.playerAttack();
        int regularDamage = initialHP - plain.getHp();
        String defendLog = guardedBattle.playerDefend();
        int guardedDamage = initialHP - guarded.getHp();
        check(guardedDamage >= 1 && guardedDamage < regularDamage, "Defend mitigation");
        check(defendLog.contains("DEFEND"), "Defend log visible");
        check(guarded.getNextHitReductionPercent() == 0, "Guard consumed after hit");

        // Class-specific Skill/Ultimate effects.
        Knight knight = new Knight("Knight");
        BattleManager knightBattle = new BattleManager(knight, new TrainingMonster());
        check(knightBattle.playerUseSkill().contains("FORTIFY"), "Knight skill grants Fortify");
        knight.addUltimateCharge(5);
        check(knightBattle.playerUseUltimate().contains("FORTIFY"), "Knight ultimate grants Fortify");

        Archer archer = new Archer("Archer");
        BattleManager archerBattle = new BattleManager(archer, new TrainingMonster());
        String bleed = archerBattle.playerUseSkill();
        check(bleed.contains("is bleeding") && bleed.contains("suffers BLEED"), "Archer bleed tick");
        archer.addUltimateCharge(5);
        check(archerBattle.playerUseUltimate().contains("is bleeding"), "Archer Ultimate Bleed");

        Mage mage = new Mage("Mage");
        BattleManager mageBattle = new BattleManager(mage, new TrainingMonster());
        String burn = mageBattle.playerUseSkill();
        check(burn.contains("is burning") && burn.contains("suffers BURN"), "Mage Burn tick");
        mage.addUltimateCharge(5);
        check(mageBattle.playerUseUltimate().contains("MAGIC WARD"), "Mage Ultimate Ward");
        check(mage.getNextHitReductionPercent() == 0, "Ward consumed by counterattack");

        Knight soulHero = new Knight("Soul");
        BattleManager soulBattle = new BattleManager(soulHero, new TrainingMonster());
        SoulShard shard = new SoulShard();
        soulHero.getInventory().addItem(shard);
        int soulHP = soulHero.getHp();
        check(soulBattle.playerUseItem(shard).contains("Soul Shard")
                && soulHero.getHp() < soulHP, "Soul Shard is not a free Potion action");
        System.out.println("PASS: " + assertions + " battle-rule assertions");
    }
}

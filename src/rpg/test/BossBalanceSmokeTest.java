package rpg.test;

import rpg.game.Shop;
import rpg.model.character.monster.Cyclops;
import rpg.model.character.monster.DarkMage;
import rpg.model.character.monster.Monster;
import rpg.model.character.monster.MonsterSkillResult;
import rpg.model.character.player.Knight;
import rpg.model.item.Armor;
import rpg.model.item.Item;

/**
 * Step 51.8.6: Boss-damage regression checks against a level-6, fully armored Knight.
 * Headless and deterministic: never uses the actual save file.
 */
public final class BossBalanceSmokeTest {
    private static int checks;

    private BossBalanceSmokeTest() {}

    private static void require(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }

    private static Knight levelSixArmoredKnight() {
        Knight knight = new Knight("Balance Tester");
        while (knight.getLevel() < 6) {
            knight.gainExperience(knight.getExpToNextLevel() - knight.getExperience());
        }
        for (Item item : new Shop().getItemsForSale()) {
            if (item instanceof Armor) {
                require(knight.equipArmor((Armor) item), "Cannot equip both shield types");
            }
        }
        require(knight.getDefense() == 69, "Expected level-6 Knight with +19 shield DEF");
        return knight;
    }

    private static void checkBossDamage(Monster boss,
            int minimumNormal, int minimumSpecial, int minimumUltimate) {
        Knight knight = levelSixArmoredKnight();
        int normal = boss.attack(knight);
        require(normal >= minimumNormal,
                boss.getName() + " normal attack too weak: " + normal);

        int hpBeforeSpecial = knight.getHp();
        MonsterSkillResult special = boss.useSpecialSkill(knight);
        require(special != null, boss.getName() + " special skill missing");
        int specialDamage = hpBeforeSpecial - knight.getHp();
        require(specialDamage >= minimumSpecial,
                boss.getName() + " special attack too weak: " + specialDamage);

        for (int i = 0; i < boss.getUltimateChargeRequired(); i++) {
            boss.gainUltimateCharge();
        }
        require(boss.isUltimateReady(), boss.getName() + " ultimate never became ready");
        int ultimateDamage = boss.useUltimate(knight);
        require(ultimateDamage >= minimumUltimate,
                boss.getName() + " ultimate too weak: " + ultimateDamage);
        require(knight.isAlive(), boss.getName() + " killed armored Knight in just three hits");

        System.out.printf("%s vs 69 DEF - normal: %d, special: %d, ultimate: %d%n",
                boss.getName(), normal, specialDamage, ultimateDamage);
    }

    public static void main(String[] args) {
        checkBossDamage(new Cyclops(), 18, 36, 50);
        checkBossDamage(new DarkMage(), 22, 44, 62);
        System.out.println("PASS: " + checks + " boss balance assertions");
    }
}

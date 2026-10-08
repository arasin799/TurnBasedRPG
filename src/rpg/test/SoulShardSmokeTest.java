package rpg.test;
import java.nio.file.Files;
import java.nio.file.Path;
import rpg.game.BattleManager;
import rpg.model.character.player.Knight;
import rpg.model.character.monster.*;
import rpg.model.item.SoulShard;
import rpg.persistence.GameData;
import rpg.persistence.SaveManager;
public class SoulShardSmokeTest {
    private static void check(boolean ok, String label) {
        if (!ok) throw new AssertionError(label);
        System.out.println("PASS: " + label);
    }
    public static void main(String[] args) throws Exception {
        Knight player = new Knight("Tester");
        check(player.getUltimateCharge() == 0 && player.getMaxUltimateCharge() == 5,"Initial charge 0/5");
        BattleManager battle = new BattleManager(player, new Rat());
        battle.playerAttack();
        check(player.getUltimateCharge() == 1,"Attack earns 1 charge");
        battle.playerUseSkill();
        check(player.getUltimateCharge() == 2,"Skill earns 1 charge");
        SoulShard item = new SoulShard();
        player.getInventory().addItem(item);
        check(player.getInventory().useItem(item,player),"Soul Shard usable");
        check(player.getUltimateCharge() == 3 && !player.getInventory().containsItem(item),"Soul Shard gives 1 charge and is consumed");
        player.addUltimateCharge(99);
        check(player.getUltimateCharge() == 5,"Ultimate charge capped at 5");
        SoulShard fullItem = new SoulShard();
        player.getInventory().addItem(fullItem);
        check(!player.getInventory().useItem(fullItem, player) && player.getInventory().containsItem(fullItem),"Full charge preserves Soul Shard");
        check(new Rat().getSoulShardDropChance() == .03 && new Spider().getSoulShardDropChance() == .05
            && new Ghost().getSoulShardDropChance() == .10 && new Cyclops().getSoulShardDropChance() == .20
            && new DarkMage().getSoulShardDropChance() == 1.0,"Monster drop chances match Excel");
        Path temp = Files.createTempFile("hero-soul", ".txt");
        try {
            SaveManager.saveGame(new GameData(player, 2), temp.toString());
            GameData loaded = SaveManager.loadGame(temp.toString());
            check(loaded.getPlayer().getUltimateCharge()==5,"Save/Load preserves charge");
            check(loaded.getPlayer().getInventory().getItems().stream().anyMatch(i -> i instanceof SoulShard),"Save/Load preserves Soul Shard");
            String content = Files.readString(temp);
            Files.writeString(temp,content.replace("ULTIMATE_CHARGE=5\n", ""));
            check(SaveManager.loadGame(temp.toString()).getPlayer().getUltimateCharge()==0,"Old saves load with charge 0");
        } finally { Files.deleteIfExists(temp); }
        Knight bossPlayer = new Knight("Boss");
        while (bossPlayer.getLevel() < 20) bossPlayer.gainExperience(bossPlayer.getExpToNextLevel()-bossPlayer.getExperience());
        BattleManager boss = new BattleManager(bossPlayer, new DarkMage());
        for(int i=0;i<100 && !boss.isBattleOver();i++) boss.playerAttack();
        check(boss.isVictory(),"Boss defeated in smoke test");
        check(bossPlayer.getInventory().getItems().stream().anyMatch(i -> i instanceof SoulShard),"Dark Mage always drops Soul Shard");
        System.out.println("ALL SOUL SHARD TESTS PASSED");
    }
}

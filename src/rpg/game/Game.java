package rpg.game;

import java.io.IOException;

import rpg.exception.InvalidSaveException;

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

import rpg.persistence.GameData;
import rpg.persistence.SaveManager;

public class Game {

    private static final int FINAL_STAGE = 5;

    private Player player;
    private Shop shop;

    // Highest unlocked stage index (5 means all stages cleared).
    private int stage;
    private int currentBattleStage = -1;

    private BattleManager currentBattle;

    public Game() {

        this.shop = new Shop();

        this.player = null;
        this.stage = 0;
        this.currentBattleStage = -1;
        this.currentBattle = null;
    }

    // -------------------------
    // Getters
    // -------------------------

    public Player getPlayer() {
        return player;
    }

    public Shop getShop() {
        return shop;
    }

    public int getStage() {
        return stage;
    }

    public int getStageCount() {
        return FINAL_STAGE;
    }

    public int getUnlockedStageCount() {
        return Math.min(FINAL_STAGE, stage + 1);
    }

    public int getCurrentBattleStage() {
        return currentBattleStage;
    }

    public String getStageName(int stageIndex) {
        switch (stageIndex) {
            case 0: return "Rat";
            case 1: return "Spider";
            case 2: return "Ghost";
            case 3: return "Cyclops";
            case 4: return "Dark Mage";
            default: throw new IllegalArgumentException("Invalid stage: " + stageIndex);
        }
    }

    public BattleManager getCurrentBattle() {
        return currentBattle;
    }

    public boolean hasGameStarted() {
        return player != null;
    }

    public boolean isGameCompleted() {
        return stage >= FINAL_STAGE;
    }

    // -------------------------
    // New Game
    // -------------------------

    public void startNewGame(
            String playerName,
            String playerClass) {

        this.player =
                createPlayer(
                        playerName,
                        playerClass
                );

        this.stage = 0;
        this.currentBattleStage = -1;
        this.currentBattle = null;
    }

    // -------------------------
    // Player Creation
    // -------------------------

    private Player createPlayer(
            String name,
            String playerClass) {

        switch (playerClass) {

            case "Knight":
                return new Knight(name);

            case "Archer":
                return new Archer(name);

            case "Mage":
                return new Mage(name);

            default:
                throw new IllegalArgumentException(
                        "Unknown player class: "
                                + playerClass
                );
        }
    }

    // -------------------------
    // Battle
    // -------------------------

    public String getCurrentMonsterName() {
        if (isGameCompleted()) {
            return "All stages cleared";
        }
        return getStageName(stage);
    }

    // Kept for compatibility with any older callers.
    public BattleManager startBattle() {
        return startBattle(stage);
    }

    // stageIndex: 0 = Rat, 1 = Spider, ..., 4 = Dark Mage.
    public BattleManager startBattle(int stageIndex) {
        if (player == null) {
            throw new IllegalStateException("No game has been started.");
        }
        if (!player.isAlive()) {
            throw new IllegalStateException("The player has been defeated.");
        }
        if (currentBattle != null && !currentBattle.isBattleOver()) {
            throw new IllegalStateException("A battle is already in progress.");
        }
        if (stageIndex < 0 || stageIndex >= FINAL_STAGE || stageIndex > stage) {
            throw new IllegalStateException("This stage is locked.");
        }

        Monster monster = createMonsterForStage(stageIndex);
        currentBattle = new BattleManager(player, monster);
        currentBattleStage = stageIndex;
        return currentBattle;
    }

    private Monster createMonsterForStage(int stageIndex) {
        switch (stageIndex) {
            case 0: return new Rat();
            case 1: return new Spider();
            case 2: return new Ghost();
            case 3: return new Cyclops();
            case 4: return new DarkMage();
            default: throw new IllegalArgumentException("Invalid stage: " + stageIndex);
        }
    }

    // -------------------------
    // Battle Progress
    // -------------------------

    public boolean finishBattle() {
        if (currentBattle == null || !currentBattle.isBattleOver()) {
            return false;
        }

        boolean won = currentBattle.isVictory();
        if (won) {
            // Replay farming must never reset or skip story progression.
            stage = Math.max(stage, currentBattleStage + 1);
        }

        currentBattle = null;
        currentBattleStage = -1;
        return won;
    }

    // -------------------------
    // Save
    // -------------------------

    public void saveGame()
            throws IOException {

        if (player == null) {

            throw new IllegalStateException(
                    "There is no game to save."
            );
        }

        GameData gameData =
                new GameData(
                        player,
                        stage
                );

        SaveManager.saveGame(gameData);
    }

    // -------------------------
    // Load
    // -------------------------

    public void loadGame()
            throws IOException,
            InvalidSaveException {

        GameData gameData =
                SaveManager.loadGame();

        this.player =
                gameData.getPlayer();

        this.stage =
                Math.min(FINAL_STAGE, Math.max(0, gameData.getStage()));

        this.currentBattleStage = -1;
        this.currentBattle = null;
    }
}
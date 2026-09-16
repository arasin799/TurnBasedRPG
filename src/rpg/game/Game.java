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

    private int stage;

    private BattleManager currentBattle;

    public Game() {

        this.shop = new Shop();

        this.player = null;
        this.stage = 0;
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
            return "None";
        }

        return createMonsterForCurrentStage()
                .getName();
    }

    public BattleManager startBattle() {

        if (player == null) {

            throw new IllegalStateException(
                    "No game has been started."
            );
        }

        if (!player.isAlive()) {
            throw new IllegalStateException(
                    "The player has been defeated."
            );
        }

        if (isGameCompleted()) {

            throw new IllegalStateException(
                    "The game has already been completed."
            );
        }

        Monster monster =
                createMonsterForCurrentStage();

        currentBattle =
                new BattleManager(
                        player,
                        monster
                );

        return currentBattle;
    }

    private Monster createMonsterForCurrentStage() {

        switch (stage) {

            case 0:
                return new Rat();

            case 1:
                return new Spider();

            case 2:
                return new Ghost();

            case 3:
                return new Cyclops();

            case 4:
                return new DarkMage();

            default:
                throw new IllegalStateException(
                        "Invalid game stage: "
                                + stage
                );
        }
    }

    // -------------------------
    // Battle Progress
    // -------------------------

    public boolean finishBattle() {

        if (currentBattle == null) {
            return false;
        }

        if (!currentBattle.isBattleOver()) {
            return false;
        }

        if (currentBattle.isVictory()) {

            stage++;

            currentBattle = null;

            return true;
        }

        currentBattle = null;

        return false;
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
                gameData.getStage();

        this.currentBattle = null;
    }
}
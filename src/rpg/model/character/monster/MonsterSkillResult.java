
package rpg.model.character.monster;

public class MonsterSkillResult {

    private final String message;
    private final boolean stunPlayer;

    public MonsterSkillResult(String message) {
        this(message, false);
    }

    public MonsterSkillResult(
            String message,
            boolean stunPlayer) {

        this.message = message;
        this.stunPlayer = stunPlayer;
    }

    public String getMessage() {
        return message;
    }

    public boolean isPlayerStunned() {
        return stunPlayer;
    }
}

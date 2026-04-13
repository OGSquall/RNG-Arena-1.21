package net.squall.rngarena.game;

/**
 * High-level RNG Arena match lifecycle. Extend with new phases as features land
 * (teleport, combat, win detection, map rotation).
 */
public enum GameState {
	LOBBY,
	COUNTDOWN,
	IN_GAME,
	END
}

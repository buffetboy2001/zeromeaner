package org.zeromeaner.contrib.net.omegaboshi.nullpomino.game.subsystem.randomizer;

import org.zeromeaner.game.component.Piece;

public class History4RollsRandomizer extends LimitedHistoryRandomizer {

	public History4RollsRandomizer() {
		super();
	}

	public History4RollsRandomizer(boolean[] pieceEnable, long seed) {
		super(pieceEnable, seed);

	}

	public void init() {
		super.init();
		history = new int[] {Piece.PIECE_Z, Piece.PIECE_Z, Piece.PIECE_Z, Piece.PIECE_Z};
		numrolls = 4;
	}
}

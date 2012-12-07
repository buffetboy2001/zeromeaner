package org.zeromeaner.contrib.net.omegaboshi.nullpomino.game.subsystem.randomizer;

import org.zeromeaner.game.component.Piece;

public class IRandomizer extends Randomizer {

	public IRandomizer() {
		super();
	}

	@Override
	public int next() {
		return Piece.PIECE_I;
	}


}

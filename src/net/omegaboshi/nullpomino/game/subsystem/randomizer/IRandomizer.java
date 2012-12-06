package net.omegaboshi.nullpomino.game.subsystem.randomizer;

import mu.nu.nullpo.game.component.Piece;

public class IRandomizer extends Randomizer {

	public IRandomizer() {
		super();
	}

	@Override
	public int next() {
		return Piece.PIECE_I;
	}


}

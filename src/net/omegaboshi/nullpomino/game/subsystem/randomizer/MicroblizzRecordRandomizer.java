package net.omegaboshi.nullpomino.game.subsystem.randomizer;

import mu.nu.nullpo.game.component.Piece;

public class MicroblizzRecordRandomizer extends Randomizer {

	public int index = 0;
	
	int[] microblizz = { Piece.PIECE_I, Piece.PIECE_O, Piece.PIECE_S, Piece.PIECE_T, Piece.PIECE_L, Piece.PIECE_J, Piece.PIECE_Z, Piece.PIECE_I, Piece.PIECE_T, Piece.PIECE_O, Piece.PIECE_L, Piece.PIECE_S, Piece.PIECE_J, Piece.PIECE_Z, Piece.PIECE_L, Piece.PIECE_I, Piece.PIECE_S, Piece.PIECE_J, Piece.PIECE_T, Piece.PIECE_O, Piece.PIECE_Z, Piece.PIECE_J, Piece.PIECE_I, Piece.PIECE_Z, Piece.PIECE_T, Piece.PIECE_S, Piece.PIECE_L, Piece.PIECE_O, Piece.PIECE_L, Piece.PIECE_S, Piece.PIECE_J, Piece.PIECE_I, Piece.PIECE_Z, Piece.PIECE_O, Piece.PIECE_T, Piece.PIECE_J, Piece.PIECE_I, Piece.PIECE_S, Piece.PIECE_T, Piece.PIECE_O, Piece.PIECE_L, Piece.PIECE_Z, Piece.PIECE_L, Piece.PIECE_Z, Piece.PIECE_T, Piece.PIECE_S, Piece.PIECE_O, Piece.PIECE_I, Piece.PIECE_J, Piece.PIECE_T, Piece.PIECE_S, Piece.PIECE_I, Piece.PIECE_Z, Piece.PIECE_O, Piece.PIECE_L, Piece.PIECE_J, Piece.PIECE_I, Piece.PIECE_Z, Piece.PIECE_O, Piece.PIECE_S, Piece.PIECE_T, Piece.PIECE_J, Piece.PIECE_L, Piece.PIECE_O, Piece.PIECE_S, Piece.PIECE_I, Piece.PIECE_J, Piece.PIECE_L, Piece.PIECE_T, Piece.PIECE_Z, Piece.PIECE_Z, Piece.PIECE_O, Piece.PIECE_S, Piece.PIECE_L, Piece.PIECE_I, Piece.PIECE_J, Piece.PIECE_T, Piece.PIECE_T, Piece.PIECE_Z, Piece.PIECE_O, Piece.PIECE_S, Piece.PIECE_J, Piece.PIECE_I, Piece.PIECE_L, Piece.PIECE_I, Piece.PIECE_T, Piece.PIECE_J, Piece.PIECE_Z, Piece.PIECE_S, Piece.PIECE_L, Piece.PIECE_O, Piece.PIECE_S, Piece.PIECE_I, Piece.PIECE_Z, Piece.PIECE_J, Piece.PIECE_T, Piece.PIECE_O, Piece.PIECE_L, Piece.PIECE_Z, Piece.PIECE_I, Piece.PIECE_T, Piece.PIECE_J, Piece.PIECE_O, Piece.PIECE_S, Piece.PIECE_L, Piece.PIECE_Z, Piece.PIECE_L, Piece.PIECE_I, };
	
	public MicroblizzRecordRandomizer() {
		super();
	}
	
	public void init() {
		index = 0;
		super.init();
	}

	@Override
	public int next() {
		if (index < microblizz.length){
			int nextPiece =  microblizz[index];
			index++;
			return nextPiece;
		} else return Piece.PIECE_I;
	}


}

package org.zeromeaner.contrib.net.omegaboshi.nullpomino.game.subsystem.randomizer;

import org.zeromeaner.game.component.Piece;

public class MrTSpinRandomizer extends Randomizer {

	public int index = 0;
	
	int[] lapsilap = {
			
			//first tspin
			Piece.PIECE_O,
			Piece.PIECE_I,
			Piece.PIECE_S,
			Piece.PIECE_Z,
			Piece.PIECE_L,
			Piece.PIECE_J,
			Piece.PIECE_T,
			
			//second tspin
			Piece.PIECE_O,
			Piece.PIECE_S,
			Piece.PIECE_Z,
			Piece.PIECE_L,
			Piece.PIECE_I,
			Piece.PIECE_J,
			Piece.PIECE_T,
			
			//perfect clear
			Piece.PIECE_O,
			Piece.PIECE_S,
			Piece.PIECE_Z,
			Piece.PIECE_J,
			Piece.PIECE_T,
			Piece.PIECE_I,
			Piece.PIECE_L,
			
			//further perfect clear
			Piece.PIECE_O,
			Piece.PIECE_S,
			Piece.PIECE_J,
			Piece.PIECE_I,
			Piece.PIECE_Z,
			Piece.PIECE_T,
			Piece.PIECE_L,
			
			Piece.PIECE_Z,
			Piece.PIECE_S,
			Piece.PIECE_J,
			Piece.PIECE_I,
			Piece.PIECE_Z,
			Piece.PIECE_T,
			Piece.PIECE_L,
			Piece.PIECE_O,
			Piece.PIECE_J,
			Piece.PIECE_L,
			Piece.PIECE_Z,
			Piece.PIECE_S,
			Piece.PIECE_O,
			Piece.PIECE_T,
			Piece.PIECE_I,
			Piece.PIECE_L,
			Piece.PIECE_S,
			Piece.PIECE_T,
			Piece.PIECE_J,
			Piece.PIECE_O,
			Piece.PIECE_Z,
			Piece.PIECE_I,
			Piece.PIECE_L,
			Piece.PIECE_J,
			Piece.PIECE_Z,
			Piece.PIECE_S,
			Piece.PIECE_O,
			Piece.PIECE_I,
			Piece.PIECE_T,
			Piece.PIECE_L,
			Piece.PIECE_S,
			Piece.PIECE_Z,
			Piece.PIECE_I,
			Piece.PIECE_T,
			Piece.PIECE_O,
			Piece.PIECE_J,
			Piece.PIECE_T,
			Piece.PIECE_Z,
			Piece.PIECE_J,
			Piece.PIECE_I,
			Piece.PIECE_L,
			Piece.PIECE_S,
			Piece.PIECE_O,
			Piece.PIECE_Z,
			Piece.PIECE_I,
			Piece.PIECE_O,
			Piece.PIECE_S,
			Piece.PIECE_J,
			Piece.PIECE_T,
			Piece.PIECE_L,
			Piece.PIECE_S,
			Piece.PIECE_T,
			Piece.PIECE_J,
			Piece.PIECE_Z,
			Piece.PIECE_I,
			Piece.PIECE_L,
			Piece.PIECE_O,
			Piece.PIECE_Z,
			Piece.PIECE_T,
			Piece.PIECE_O,
			Piece.PIECE_I,
			Piece.PIECE_S,
			Piece.PIECE_L,
			Piece.PIECE_J,
			Piece.PIECE_S,
			Piece.PIECE_Z,
			Piece.PIECE_T,
			Piece.PIECE_O,
			Piece.PIECE_J,
			Piece.PIECE_L,
			Piece.PIECE_I,
			Piece.PIECE_I,
			Piece.PIECE_L,
			Piece.PIECE_J,
			Piece.PIECE_Z,
			Piece.PIECE_S,
			Piece.PIECE_T,
			Piece.PIECE_O,
			Piece.PIECE_I,
			Piece.PIECE_L,
			Piece.PIECE_O,
			Piece.PIECE_Z,
			Piece.PIECE_S,
			Piece.PIECE_T,
			Piece.PIECE_J,
			Piece.PIECE_O
	};
	
	public MrTSpinRandomizer() {
		super();
	}
	
	public void init() {
		index = 0;
		super.init();
	}

	@Override
	public int next() {
		if (index < lapsilap.length){
			int nextPiece =  lapsilap[index];
			index++;
			return nextPiece;
		} else return Piece.PIECE_I;
	}


}

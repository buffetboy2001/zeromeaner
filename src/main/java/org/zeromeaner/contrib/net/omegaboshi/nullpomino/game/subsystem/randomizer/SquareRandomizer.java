package org.zeromeaner.contrib.net.omegaboshi.nullpomino.game.subsystem.randomizer;

import java.util.ArrayList;
import java.util.List;

import org.zeromeaner.game.component.Piece;

public class SquareRandomizer extends Randomizer {

	public List<Combination> combinations = new ArrayList<Combination>();
	
	public boolean started;
	public Combination currentCombination;
	
	public void init(){
		for (Combination combination : combinations){
			combination.currentPieceIndex = 0;
		}
	}
	
	public SquareRandomizer() {
		super();
		
		addCombination(Piece.PIECE_L, Piece.PIECE_S);
		addCombination(Piece.PIECE_J, Piece.PIECE_Z);
		addCombination(Piece.PIECE_L, Piece.PIECE_O);
		addCombination(Piece.PIECE_J, Piece.PIECE_O);
		
		addCombination(Piece.PIECE_L, Piece.PIECE_L);
		addCombination(Piece.PIECE_J, Piece.PIECE_J);
		addCombination(Piece.PIECE_O, Piece.PIECE_O);
		addCombination(Piece.PIECE_I, Piece.PIECE_I);
		
		addCombination(Piece.PIECE_L, Piece.PIECE_T, Piece.PIECE_T);
		addCombination(Piece.PIECE_J, Piece.PIECE_T, Piece.PIECE_T);
		addCombination(Piece.PIECE_J, Piece.PIECE_O, Piece.PIECE_L);
		addCombination(Piece.PIECE_L, Piece.PIECE_O, Piece.PIECE_J);

		addCombination(Piece.PIECE_L, Piece.PIECE_S, Piece.PIECE_L);
		addCombination(Piece.PIECE_J, Piece.PIECE_Z, Piece.PIECE_J);

		addCombination(Piece.PIECE_L, Piece.PIECE_L, Piece.PIECE_O);
		addCombination(Piece.PIECE_J, Piece.PIECE_J, Piece.PIECE_O);
	}

	private void addCombination(int first, int second) {
		Combination combination = new Combination(first, second);
		combinations.add(combination);
	}
	
	private void addCombination(int first, int second, int third) {
		Combination combination = new Combination(first, second, third);
		combinations.add(combination);
	}

	@Override
	public int next() {
		if (currentCombination == null || currentCombination.isFinished()){
			int randomCombination = (int) (Math.random() * combinations.size());
			currentCombination = combinations.get(randomCombination);
			System.out.print("Started combination: " + currentCombination);
		}
		return currentCombination.nextPiece();
	}

	public class Combination {
		
		private List<Integer> pieces = new ArrayList<Integer>();
		private int currentPieceIndex = 0;
		private boolean finished = false;
		
		public Combination(Integer... pieces){
			for (int piece : pieces){
				this.pieces.add(piece);
			}
		}
		
		public Integer nextPiece(){
			if (finished){
				resetCurrentPieceIndex();
				finished = false;
			}
			if (currentPieceIndex == (pieces.size() - 1)){
				finished = true;
			}
			Integer currentPiece = pieces.get(currentPieceIndex);
			currentPieceIndex++;
			return currentPiece;
		}

		private void resetCurrentPieceIndex() {
			currentPieceIndex = 0;
		}
		
		public boolean isFinished(){
			return finished;
		}
		
		public String toString(){
			return "Combination [ " + pieces + "]";
		}
		
	}

}

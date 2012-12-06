package mu.nu.nullpo.util.fumen;

import mu.nu.nullpo.game.component.Block;
import mu.nu.nullpo.game.component.Piece;

public enum FumenPiece {

	I(1),
	L(2),
	O(3),
	Z(4),
	T(5),
	J(6),
	S(7),
	GRAY(8)
	;
	
	int representation = -1;
	
	FumenPiece(int representation){
		this.representation = representation;
	}
	
	public static FumenPiece fromNullpoMinoPiece(int nullpoMinoPiece){
		switch (nullpoMinoPiece){
		 case Piece.PIECE_I : return I;
		 case Piece.PIECE_L : return L;
		 case Piece.PIECE_O : return O;
		 case Piece.PIECE_Z : return Z;
		 case Piece.PIECE_T : return T;
		 case Piece.PIECE_J : return J;
		 case Piece.PIECE_S : return S;
		}
		
		return I;
	}
	
	public static FumenPiece fromNullpoMinoPieceColor(int nullpoMinoPieceColor){
		switch (nullpoMinoPieceColor){
		case Block.BLOCK_COLOR_GRAY: return GRAY;
		case Block.BLOCK_COLOR_RED: return Z;
		case Block.BLOCK_COLOR_ORANGE: return L;
		case Block.BLOCK_COLOR_YELLOW: return O;
		case Block.BLOCK_COLOR_GREEN: return S;
		case Block.BLOCK_COLOR_CYAN: return I;
		case Block.BLOCK_COLOR_BLUE: return J;
		case Block.BLOCK_COLOR_PURPLE: return T;
		}
		return I;
	}
	
	public int getValue(){
		return representation;
	}

	public static FumenPiece fromNullpoMinoPiece(Piece piece) {
		return fromNullpoMinoPiece(piece.id);
	}
	
}

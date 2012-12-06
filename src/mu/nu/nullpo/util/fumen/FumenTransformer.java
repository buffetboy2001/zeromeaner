package mu.nu.nullpo.util.fumen;

import mu.nu.nullpo.game.component.Block;
import mu.nu.nullpo.game.component.Field;
import mu.nu.nullpo.game.component.Piece;

public class FumenTransformer {
	
	
	public static int[] transform(Field field){
		
		int[] fumenFrame = new int[220];
		
		int nrOfColumns = field.getWidth();
		
		for (int row=0; row < field.getHeight(); row++){
			
			for (int column = 0; column < nrOfColumns; column++){
				Block block = field.getBlock(column, row);
				int pieceNumber = block.color;
				if (pieceNumber > 0){
					fumenFrame[(row+1)*10+column] = FumenPiece.fromNullpoMinoPieceColor(pieceNumber).getValue();
				}
			}
		}
		
		return fumenFrame;
	}
	
	public static int[] transformPiece(Piece piece, int row, int column){
		int[] fumenPiece = new int[3];
		fumenPiece[0] = FumenPiece.fromNullpoMinoPiece(piece).getValue();
		fumenPiece[1] = piece.direction;
		fumenPiece[2] = 20 + column;
		return fumenPiece;
	}

}

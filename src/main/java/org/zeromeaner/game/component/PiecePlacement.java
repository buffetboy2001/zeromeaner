package org.zeromeaner.game.component;

public class PiecePlacement {

	private int piece;
	private int x;
	private int y;
	private int direction;
	private int time;
	private int rotations;
	private int moves;
	
	private int framesDelta;
	
	public PiecePlacement() {}

	public PiecePlacement(int piece, int x, int y, int direction) {
		super();
		this.piece = piece;
		this.x = x;
		this.y = y;
		this.direction = direction;
	}

	public PiecePlacement(int piece, int x, int y, int direction, int time) {
		super();
		this.piece = piece;
		this.x = x;
		this.y = y;
		this.direction = direction;
		this.time = time;
	}

	public PiecePlacement(int piece, int x, int y, int direction, int time, int rotations, int moves) {
		super();
		this.piece = piece;
		this.x = x;
		this.y = y;
		this.direction = direction;
		this.time = time;
		this.rotations = rotations;
		this.moves = moves;
	}

	@Override
	public String toString() {
		return "[piece=" + piece + ", x=" + x + ", y=" + y+ ", direction=" + direction + ", time="+ time +", rotations="+ rotations + ", moves=" + moves + "]";
	}
	
	public String getPieceAsString(){
		return Piece.PIECE_NAMES[piece];
	}
	
	public int getPiece() {
		return piece;
	}
	public void setPiece(int piece) {
		this.piece = piece;
	}
	public int getX() {
		return x;
	}
	public void setX(int x) {
		this.x = x;
	}
	public int getY() {
		return y;
	}
	public void setY(int y) {
		this.y = y;
	}
	public int getDirection() {
		return direction;
	}
	public void setDirection(int direction) {
		this.direction = direction;
	}

	public void setTime(int time) {
		this.time = time;
	}

	public int getTime() {
		return time;
	}

	public int getFramesDelta() {
		return framesDelta;
	}

	public void setFramesDelta(int framesDelta) {
		this.framesDelta = framesDelta;
	}

	public int getRotations() {
		return rotations;
	}

	public void setRotations(int rotations) {
		this.rotations = rotations;
	}

	public int getMoves() {
		return moves;
	}

	public void setMoves(int moves) {
		this.moves = moves;
	}
	
}

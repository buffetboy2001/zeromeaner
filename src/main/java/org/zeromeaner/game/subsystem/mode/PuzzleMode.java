package org.zeromeaner.game.subsystem.mode;

import org.zeromeaner.game.component.Block;
import org.zeromeaner.game.component.Field;
import org.zeromeaner.game.component.Piece;
import org.zeromeaner.game.event.EventReceiver;
import org.zeromeaner.game.play.GameEngine;
import org.zeromeaner.util.CustomProperties;

public class PuzzleMode extends AbstractMode {
	
	private EventReceiver receiver;
	private GameEngine engine;
	
	@Override
	public void playerInit(GameEngine engine, int playerID) {
		receiver = engine.owner.receiver;
		this.engine = engine;
	}
	
	@Override
	public void startGame(GameEngine engine, int playerID) {
		loadPuzzle(0);
		loadMap(engine.field, 0);
	}
	
	@Override
	public String getName() {
		return "PUZZLE";
	}
	
	private void loadPuzzle(int puzzleId){
		for (int i = 0; i < engine.nextPieceArraySize; i++) {
			engine.nextPieceArrayID[i] = Piece.PIECE_I;
		}
	}
	
	private void loadMap(Field field, int mapId) {
		
		CustomProperties puzzleMaps = receiver.loadProperties("config/map/puzzle/puzzles.map");
		
		field.reset();
		field.stringToField(puzzleMaps.getProperty("map." + mapId, ""));
		field.setAllAttribute(Block.BLOCK_ATTRIBUTE_VISIBLE, true);
		field.setAllAttribute(Block.BLOCK_ATTRIBUTE_OUTLINE, true);
		field.setAllAttribute(Block.BLOCK_ATTRIBUTE_SELFPLACED, false);
	
	}
	
}

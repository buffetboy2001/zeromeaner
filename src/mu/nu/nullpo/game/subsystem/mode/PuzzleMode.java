package mu.nu.nullpo.game.subsystem.mode;

import mu.nu.nullpo.game.component.Block;
import mu.nu.nullpo.game.component.Field;
import mu.nu.nullpo.game.component.Piece;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.util.CustomProperties;

public class PuzzleMode extends DummyMode {
	
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

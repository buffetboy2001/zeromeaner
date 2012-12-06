package mu.nu.nullpo.game.subsystem.ai;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import mu.nu.nullpo.game.component.PiecePlacement;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.gui.slick.NullpoMinoSlick;
import mu.nu.nullpo.util.CustomProperties;

/**
 * 普通のAI
 */
public class ReplayAI extends DummyAI {
	
	public volatile boolean threadRunning;

	int pieceIndex = 0;

	private CustomProperties propertiesOfReplayFile;
	private ArrayList<PiecePlacement> piecePlacements = new ArrayList<PiecePlacement>();

	public void init(GameEngine engine, int playerID) {
		
		pieceIndex = 0;
	
		propertiesOfReplayFile = new CustomProperties();

		try {
			FileInputStream in = new FileInputStream(NullpoMinoSlick.propGlobal.getProperty("custom.replay.directory", "replay") + "/" + NullpoMinoSlick.propGlobal.getProperty("latest.replay.file"));
			propertiesOfReplayFile.load(in);
			in.close();
		
		
			String pieceSequence = propertiesOfReplayFile.getProperty("linerace.pieceSequence");
			
			if (pieceSequence != null && !pieceSequence.isEmpty()){
			pieceSequence = pieceSequence.substring(1, pieceSequence.length()-1);
			String[] pieces = pieceSequence.split("\\]\\[");
			
			
			for (String piece : pieces){
				String[] pieceAttributes = piece.split(",");
				
				PiecePlacement piecePlacement = new PiecePlacement();
				
				for (String pieceAttribute : pieceAttributes){
					pieceAttribute.trim();
					String[] attributeValuePair = pieceAttribute.split("=");
					if ("piece".equals(attributeValuePair[0].trim())) 
						piecePlacement.setPiece(Integer.parseInt(attributeValuePair[1]));
					else if ("direction".equals(attributeValuePair[0].trim())) 
						piecePlacement.setDirection(Integer.parseInt(attributeValuePair[1]));
					else if ("x".equals(attributeValuePair[0].trim()))
						piecePlacement.setX(Integer.parseInt(attributeValuePair[1]));
					else if ("y".equals(attributeValuePair[0].trim()))
						piecePlacement.setY(Integer.parseInt(attributeValuePair[1]));
				}
				
				piecePlacements.add(piecePlacement);
			}
		}
		
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getName() {
		return "ReplayAI";
	}
	
	public void newPiece(GameEngine engine, int playerID) {
		
		try {
			PiecePlacement currentPiecePlacement = piecePlacements.get(pieceIndex);
			this.bestX = currentPiecePlacement.getX();
			this.bestY = currentPiecePlacement.getY();
			this.bestRt = currentPiecePlacement.getDirection();
		} catch (Exception e){
			//best effort failed
		}
		this.bestHold = false;
		this.forceHold = false;
		engine.aiHintReady = true;
		this.thinkCurrentPieceNo = 1;
		this.thinkLastPieceNo = 1;
		
		//System.out.println("heh i want a new piece!");
		pieceIndex++;
	}
}

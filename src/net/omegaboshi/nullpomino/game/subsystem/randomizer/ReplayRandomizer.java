package net.omegaboshi.nullpomino.game.subsystem.randomizer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import mu.nu.nullpo.game.component.Piece;
import mu.nu.nullpo.gui.slick.NullpoMinoSlick;
import mu.nu.nullpo.util.CustomProperties;

public class ReplayRandomizer extends Randomizer {

	public int index = 0;

	private CustomProperties propertiesOfReplayFile = new CustomProperties();
	private ArrayList<Integer> randomizerPieces = new ArrayList<Integer>();
	
	private boolean[] pieceEnable;
	private long seed;
	private Randomizer delegate = null;
	private long playerId;
	
	public ReplayRandomizer() {
		super();
	}
	
	public ReplayRandomizer(boolean[] pieceEnable, long seed) {
		super(pieceEnable, seed);
		this.pieceEnable = pieceEnable;
	}
	
	public void setState(boolean[] pieceEnable, long seed) {
		setPieceEnable(pieceEnable);
		this.pieceEnable = pieceEnable;
		init();
	}

	public void init() {
		index = 0;
		try {
			loadPieces();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void loadPieces() throws FileNotFoundException, IOException {
		readProperties();
		String pieceSequence = propertiesOfReplayFile.getProperty("linerace.pieceSequence");
		if (pieceSequence != null && !pieceSequence.isEmpty()){
			parsePieceSequence(pieceSequence);
		} else {
			String randomizer = propertiesOfReplayFile.getProperty("0.ruleopt.strRandomizer");
			if (randomizer != null && randomizer.equals("net.omegaboshi.nullpomino.game.subsystem.randomizer.BagNoSZORandomizer")){
				delegate = new BagNoSZORandomizer(pieceEnable, getSeed());
			}
		}
	}

	private long getSeed() {
		if (seed == 0){
			String randSeed = propertiesOfReplayFile.getProperty(getPlayerId() + ".replay.randSeed");
			seed =  Long.parseLong(randSeed, 16);
		}
		reseed(seed);
		return seed;
	}

	private void parsePieceSequence(String pieceSequence) {
		pieceSequence = pieceSequence.substring(1, pieceSequence.length() - 1);
		String[] pieces = pieceSequence.split("\\]\\[");
		for (String piece : pieces) {
			parsePiece(piece);
		}
	}

	private void parsePiece(String piece) {
		String[] pieceAttributes = piece.split(",");
		for (String pieceAttribute : pieceAttributes) {
			pieceAttribute.trim();
			String[] attributeValuePair = pieceAttribute.split("=");
			if ("piece".equals(attributeValuePair[0].trim()))
				randomizerPieces.add(Integer.parseInt(attributeValuePair[1]));
		}
	}

	private void readProperties() throws FileNotFoundException, IOException {
		FileInputStream in = new FileInputStream(
				NullpoMinoSlick.propGlobal.getProperty(
						"custom.replay.directory", "replay")
						+ "/"
						+ NullpoMinoSlick.propGlobal.getProperty("latest.replay.file"));
		propertiesOfReplayFile.load(in);
		in.close();
	}

	@Override
	public int next() {
		if (delegate != null){
			return delegate.next();
		}
		if (index < randomizerPieces.size()) {
			Integer nextPiece = randomizerPieces.get(index);
			index++;
			return nextPiece;
		} else
			return Piece.PIECE_I;
	}
	
	public long getPlayerId() {
		return playerId;
	}

	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}

}

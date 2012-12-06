package org.tetrevil.nullpo;

import mu.nu.nullpo.game.component.Block;
import mu.nu.nullpo.game.component.Piece;
import mu.nu.nullpo.game.play.GameEngine;

import org.tetrevil.Field;
import org.tetrevil.ThreadedMaliciousRandomizer;

public class TNField extends Field {
	
	protected GameEngine engine;
	
	public TNField(GameEngine engine) {
		super(true);
		this.engine = engine;
		ThreadedMaliciousRandomizer r = new ThreadedMaliciousRandomizer(3, 30);
		r.setFair(false);
		r.setRfactor(0.00);
		r.setRandom(engine.random);
		provider = r;
	}
	
	public void update() {
		if(engine.field == null)
			return;
		for(int y = 0; y < BUFFER; y++) {
			for(int x = BUFFER;  x < BUFFER + WIDTH; x++)
				field[y][x] = null;
		}
		for(int y = 0; y < 20; y++) {
			for(int x = 0; x < 10; x++) {
				mu.nu.nullpo.game.component.Block npblock = engine.field.getBlock(x, y);
//				field[y + BUFFER][x + BUFFER] = npblock.color == 0 ? null : Block.values()[npblock.color];
				org.tetrevil.Block b = null;
				switch(npblock.color) {
				case Block.BLOCK_COLOR_NONE: b = null; break;
				case Block.BLOCK_COLOR_YELLOW: b = org.tetrevil.Block.O; break;
				case Block.BLOCK_COLOR_CYAN: b = org.tetrevil.Block.I; break;
				case Block.BLOCK_COLOR_GREEN: b = org.tetrevil.Block.S; break;
				case Block.BLOCK_COLOR_BLUE: b = org.tetrevil.Block.J; break;
				case Block.BLOCK_COLOR_PURPLE: b = org.tetrevil.Block.T; break;
				case Block.BLOCK_COLOR_RED: b = org.tetrevil.Block.Z; break;
				case Block.BLOCK_COLOR_ORANGE: b = org.tetrevil.Block.L; break;
				default:
					b = org.tetrevil.Block._;
				}
				field[y + BUFFER][x + BUFFER] = b;
			}
		}
	}
	
	public Object writeReplace() {
		return copyInto(new Field());
	}
}

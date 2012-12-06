package org.tetrevil.nullpo;

import org.tetrevil.ThreadedMaliciousRandomizer;

import mu.nu.nullpo.game.play.GameEngine;

public class TNAggressiveRandomizer extends TNRandomizer {
	@Override
	public void setEngine(GameEngine engine) {
		super.setEngine(engine);
		ThreadedMaliciousRandomizer t = new ThreadedMaliciousRandomizer(3, 30);
		t.setFair(true);
		t.setRandom(engine.random);
		t.setRfactor(0.05);
		field.setProvider(t);
	}
	
	@Override
	public String getName() {
		return "AGGRESSIVE";
	}
}

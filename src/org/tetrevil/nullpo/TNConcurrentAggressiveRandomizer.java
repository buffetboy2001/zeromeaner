package org.tetrevil.nullpo;

import mu.nu.nullpo.game.play.GameEngine;

import org.tetrevil.ConcurrentShapeProvider;
import org.tetrevil.Shape;
import org.tetrevil.ThreadedMaliciousRandomizer;

public class TNConcurrentAggressiveRandomizer extends TNRandomizer {
	@Override
	public void setEngine(GameEngine engine) {
		super.setEngine(engine);
		ThreadedMaliciousRandomizer t = new ThreadedMaliciousRandomizer(3, 30);
		t.setFair(true);
		t.setRandom(engine.random);
		t.setRfactor(0.05);
		field.setProvider(new ConcurrentShapeProvider(t));
	}
	
	@Override
	public String getName() {
		return "FAST AGGRESSIVE";
	}
	
	@Override
	public synchronized int next() {
		if(regenerate)
			field.setShape(Shape.O_DOWN);
		return super.next();
	}

}

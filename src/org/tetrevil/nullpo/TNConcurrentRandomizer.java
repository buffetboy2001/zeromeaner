package org.tetrevil.nullpo;

import org.tetrevil.ConcurrentShapeProvider;
import org.tetrevil.Shape;
import org.tetrevil.ThreadedMaliciousRandomizer;

import mu.nu.nullpo.game.play.GameEngine;

public class TNConcurrentRandomizer extends TNRandomizer {
	@Override
	public void setEngine(GameEngine engine) {
		super.setEngine(engine);
		ThreadedMaliciousRandomizer r = new ThreadedMaliciousRandomizer(3, 30);
		r.setRfactor(0);
		field.setProvider(new ConcurrentShapeProvider(r));
	}
	
	@Override
	public String getName() {
		return "FAST EVIL";
	}
	
	@Override
	public synchronized int next() {
		if(regenerate)
			field.setShape(Shape.O_DOWN);
		return super.next();
	}
}

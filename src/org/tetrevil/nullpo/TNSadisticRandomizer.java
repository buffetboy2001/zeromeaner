package org.tetrevil.nullpo;

import org.tetrevil.ConcurrentShapeProvider;
import org.tetrevil.RemoteRandomizer;
import org.tetrevil.ThreadedMaliciousRandomizer;

import mu.nu.nullpo.game.play.GameEngine;

public class TNSadisticRandomizer extends TNRandomizer {
	@Override
	public void setEngine(GameEngine engine) {
		super.setEngine(engine);
		RemoteRandomizer rr = new RemoteRandomizer(5, 30);
		rr.setRfactor(0);
		rr.setFair(false);
		rr.setIntermediateNulls(true);
		field.setProvider(new ConcurrentShapeProvider(rr));
	}
}

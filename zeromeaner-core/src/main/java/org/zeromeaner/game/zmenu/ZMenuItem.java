package org.zeromeaner.game.zmenu;

import org.zeromeaner.game.event.EventRenderer;
import org.zeromeaner.game.play.GameManager;
import org.zeromeaner.util.CustomProperties;

public interface ZMenuItem {
	public void reset();
	public void commit();
	public void nextValue();
	public void previousValue();
	
	public String renderName();
	public String renderValue();
	
	public void load(CustomProperties props);
	public void store(CustomProperties props);
}

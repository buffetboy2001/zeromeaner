package org.zeromeaner.game.menu;

import org.zeromeaner.game.event.EventRenderer;
import org.zeromeaner.game.play.GameManager;

public interface ZMenuItem {
	public void reset();
	public void commit();
	public void nextValue();
	public void previousValue();
	
	public String renderName();
	public String renderValue();
}

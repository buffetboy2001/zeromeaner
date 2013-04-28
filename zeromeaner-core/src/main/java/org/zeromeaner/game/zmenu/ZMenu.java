package org.zeromeaner.game.zmenu;

import org.zeromeaner.game.event.EventRenderer;
import org.zeromeaner.game.play.GameManager;
import org.zeromeaner.util.CustomProperties;

public interface ZMenu {
	public ZMenuItem[] getMenuItems();
	public void reset();
	public void commit();
	public void nextItem();
	public void previousItem();
	public void nextValue();
	public void previousValue();
	
	public void render(GameManager m);
	
	public void load(CustomProperties props);
	public void store(CustomProperties props);
}

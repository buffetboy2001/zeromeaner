package org.zeromeaner.game.menu;

import org.zeromeaner.game.event.EventRenderer;
import org.zeromeaner.game.play.GameManager;

public interface ZMenu {
	public ZMenuItem[] getMenuItems();
	public void reset();
	public void commit();
	public void nextItem();
	public void previousItem();
	public void nextValue();
	public void previousValue();
	
	public void render(GameManager m);
}

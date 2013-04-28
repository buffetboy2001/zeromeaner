package org.zeromeaner.game.menu;

import org.zeromeaner.game.event.EventRenderer;

public interface ZMenuItem {
	public void reset();
	public void commit();
	public void nextValue();
	public void previousValue();
	
	public int getNumRows();
	public void renderRow(EventRenderer renderer, int renderPosition, int rowIndex);
}

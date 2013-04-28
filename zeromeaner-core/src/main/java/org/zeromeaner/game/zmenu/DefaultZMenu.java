package org.zeromeaner.game.zmenu;

import org.zeromeaner.game.event.EventRenderer;
import org.zeromeaner.game.play.GameEngine;
import org.zeromeaner.game.play.GameManager;
import org.zeromeaner.util.CustomProperties;

public class DefaultZMenu implements ZMenu {
	
	private ZMenuItem[] menuItems;
	private int cursor;
	
	public DefaultZMenu(ZMenuItem... menuItems) {
		this.menuItems = menuItems;
		cursor = 0;
	}

	@Override
	public ZMenuItem[] getMenuItems() {
		return menuItems;
	}

	@Override
	public void reset() {
		cursor = 0;
		for(ZMenuItem mi : menuItems) {
			mi.reset();
		}
	}

	@Override
	public void commit() {
		for(ZMenuItem mi : menuItems) {
			mi.commit();
		}
	}

	@Override
	public void nextItem() {
		cursor = (cursor + menuItems.length + 1) % menuItems.length;
	}

	@Override
	public void previousItem() {
		cursor = (cursor + menuItems.length - 1) % menuItems.length;
	}

	@Override
	public void nextValue() {
		menuItems[cursor].nextValue();
	}

	@Override
	public void previousValue() {
		menuItems[cursor].previousValue();
	}

	@Override
	public void render(GameManager m) {
		GameEngine engine = m.engine[0];
		int playerID = 0;
		EventRenderer receiver = m.receiver;
		
		int displayOffset = (cursor / 10) * 10;
		for(int i = 0; i < 10; i++) {
			ZMenuItem mi = menuItems[displayOffset + i];
			String name = String.format("%10s", mi.renderName());
			String value = String.format("%-10s", mi.renderValue());
			int y = 2 * (displayOffset + i);
			receiver.drawMenuFont(engine, playerID, 0, y, name, EventRenderer.COLOR_RED);
			receiver.drawMenuFont(engine, playerID, 0, y+1, value, EventRenderer.COLOR_WHITE);
			if(cursor == displayOffset + i)
				receiver.drawMenuFont(engine, playerID, 0, y+1, "b", EventRenderer.COLOR_RED);
		}
		
	}

	@Override
	public void load(CustomProperties props) {
		for(ZMenuItem mi : menuItems) {
			mi.load(props);
		}
	}

	@Override
	public void store(CustomProperties props) {
		for(ZMenuItem mi : menuItems) {
			mi.store(props);
		}
	}

}

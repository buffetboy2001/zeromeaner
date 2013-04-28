package org.zeromeaner.game.menu;

import org.zeromeaner.game.event.EventRenderer;
import org.zeromeaner.game.play.GameEngine;
import org.zeromeaner.game.play.GameManager;

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
			int y = 2 * (displayOffset + i);
			receiver.drawMenuFont(engine, playerID, 0, y, mi.renderName(), EventRenderer.COLOR_RED);
			receiver.drawMenuFont(engine, playerID, 0, y+1, mi.renderValue(), EventRenderer.COLOR_WHITE);
			if(cursor == displayOffset + i)
				receiver.drawMenuFont(engine, playerID, 0, y+1, ">", EventRenderer.COLOR_RED);
		}
		
	}

}

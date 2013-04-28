package org.zeromeaner.game.menu;

import org.zeromeaner.game.event.EventRenderer;

public class DefaultZMenu implements ZMenu {
	
	private ZMenuItem[] menuItems;
	private int menuItemOffset;
	
	public DefaultZMenu(ZMenuItem... menuItems) {
		this.menuItems = menuItems;
		menuItemOffset = 0;
	}

	@Override
	public ZMenuItem[] getMenuItems() {
		return menuItems;
	}

	@Override
	public void reset() {
		menuItemOffset = 0;
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
		menuItemOffset = (menuItemOffset + menuItems.length + 1) % menuItems.length;
	}

	@Override
	public void previousItem() {
		menuItemOffset = (menuItemOffset + menuItems.length - 1) % menuItems.length;
	}

	@Override
	public void nextValue() {
		menuItems[menuItemOffset].nextValue();
	}

	@Override
	public void previousValue() {
		menuItems[menuItemOffset].previousValue();
	}

	@Override
	public void render(EventRenderer renderer) {
		int mio = menuItemOffset;
		ZMenuItem mi = menuItems[mio];
		for(int row = 0; row + mi.getNumRows() < 20;) {
			for(int i = 0; i < mi.getNumRows(); i++) {
				mi.renderRow(renderer, row, i);
				row++;
			}
			mio = (mio + menuItems.length + 1) % menuItems.length;
			mi = menuItems[mio];
		}
	}

}

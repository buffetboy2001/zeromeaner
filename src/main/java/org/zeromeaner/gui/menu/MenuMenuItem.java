package org.zeromeaner.gui.menu;

public class MenuMenuItem extends MenuItem {
	@SuppressWarnings("unused")
	private Menu menu;
	public MenuMenuItem(String name, String description, Menu menu) {
		super(name, description);
		this.menu=menu;
	}

	@Override
	public void changeState(int change) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getState() {
		// TODO Auto-generated method stub
		return 0;
	}

}

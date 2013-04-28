package org.zeromeaner.game.zmenu;

public abstract class OnOffZMenuItem extends AbstractZMenuItem<Boolean> {
	public OnOffZMenuItem(String name, boolean defaultValue) {
		super(name, defaultValue);
	}

	@Override
	public void nextValue() {
		value = !value;
	}

	@Override
	public void previousValue() {
		value = !value;
	}
	
	@Override
	public String renderValue() {
		return value ? "ON" : "OFF";
	}
}

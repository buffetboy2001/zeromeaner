package org.zeromeaner.game.zmenu;

import org.zeromeaner.util.CustomProperties;

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
	
	@Override
	public void load(CustomProperties props) {
		value = props.getProperty(name, defaultValue);
	}
	
	@Override
	public void store(CustomProperties props) {
		props.setProperty(name, value);
	}
}

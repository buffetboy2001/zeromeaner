package org.zeromeaner.game.zmenu;

import java.util.Arrays;

import org.zeromeaner.game.event.EventRenderer;
import org.zeromeaner.game.play.GameEngine;
import org.zeromeaner.game.play.GameManager;

public abstract class AbstractZMenuItem<T> implements ZMenuItem {
	
	protected String name;
	protected T defaultValue;
	protected T value;
	
	protected abstract void commit(T value);

	public AbstractZMenuItem(String name, T defaultValue) {
		this.name = name;
		this.defaultValue = this.value = defaultValue;
	}
	
	public void addTo(ZMenu menu) {
		menu.add(this);
	}
	
	@Override
	public void reset() {
		value = defaultValue;
	}

	@Override
	public void commit() {
		commit(value);
	}

	@Override
	public String renderName() {
		return name.toUpperCase();
	}
	
	@Override
	public String renderValue() {
		return String.valueOf(value).toUpperCase();
	}
}

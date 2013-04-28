package org.zeromeaner.game.zmenu;

import java.util.Arrays;

import org.zeromeaner.game.event.EventRenderer;
import org.zeromeaner.game.play.GameEngine;
import org.zeromeaner.game.play.GameManager;

public abstract class AbstractZMenuItem<T> implements ZMenuItem {
	
	protected String name;
	protected T defaultValue;
	protected T value;
	protected boolean twoLines;
	
	protected abstract void commit(T value);

	public AbstractZMenuItem(String name, T defaultValue, boolean twoLines) {
		this.name = name;
		this.twoLines = twoLines;
		this.defaultValue = this.value = defaultValue;
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
		char[] buf = new char[10];
		Arrays.fill(buf, ' ');
		System.arraycopy(name.toCharArray(), 0, buf, 0, name.length());
		return new String(buf);
	}
	
	@Override
	public String renderValue() {
		char[] buf = new char[10];
		Arrays.fill(buf, ' ');
		String v = String.valueOf(value).toUpperCase();
		System.arraycopy(v.toCharArray(), 0, buf, buf.length - v.length(), v.length());
		return new String(buf);
	}
}

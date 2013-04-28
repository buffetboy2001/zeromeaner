package org.zeromeaner.game.zmenu;

import java.util.Arrays;

public abstract class EnumeratedZMenuItem<T> extends AbstractZMenuItem<T> {
	
	protected T[] values;
	protected int vpos;
	
	public EnumeratedZMenuItem(String name, T defaultValue, T... values) {
		super(name, defaultValue);
		this.values = values;
		vpos = Arrays.asList(values).indexOf(defaultValue);
	}

	@Override
	public void nextValue() {
		if(++vpos >= values.length)
			vpos = 0;
		value = values[vpos];
	}

	@Override
	public void previousValue() {
		if(--vpos < 0)
			vpos = values.length - 1;
		value = values[vpos];
	}

}

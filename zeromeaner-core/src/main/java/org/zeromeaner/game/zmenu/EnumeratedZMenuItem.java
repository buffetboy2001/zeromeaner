package org.zeromeaner.game.zmenu;

import java.util.Arrays;

import org.zeromeaner.util.CustomProperties;

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

	@Override
	public void load(CustomProperties props) {
		value = defaultValue;
		vpos = Arrays.asList(values).indexOf(defaultValue);
		
		String pval = props.getProperty(name);
		for(int i = 0; i < values.length; i++) {
			if(String.valueOf(values[i]).equals(pval)) {
				vpos = i;
				break;
			}
		}
	}
	
	@Override
	public void store(CustomProperties props) {
		props.setProperty(name, String.valueOf(value));
	}
	
}

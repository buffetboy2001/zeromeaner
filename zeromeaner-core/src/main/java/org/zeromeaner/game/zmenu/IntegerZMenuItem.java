package org.zeromeaner.game.zmenu;

import org.zeromeaner.util.CustomProperties;

public abstract class IntegerZMenuItem extends AbstractZMenuItem<Integer> {
	protected int low;
	protected int high;
	
	public IntegerZMenuItem(String name, int defaultValue, int low, int high) {
		super(name, defaultValue);
		this.low = low;
		this.high = high;
	}

	@Override
	public void nextValue() {
		if(++value > high)
			value = low;
	}

	@Override
	public void previousValue() {
		if(--value < low)
			value = high;
	}

	@Override
	public void load(CustomProperties props) {
		value = props.getProperty(name, defaultValue);
		if(value < low)
			value = low;
		if(value > high)
			value = high;
	}
	
	@Override
	public void store(CustomProperties props) {
		props.setProperty(name, value);
	}
}

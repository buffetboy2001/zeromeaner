package org.zeromeaner.game.zmenu;

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

}

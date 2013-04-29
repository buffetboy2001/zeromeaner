package org.zeromeaner.game.zmenu;

public abstract class EnumeratedIntegerZMenuItem extends IntegerZMenuItem {
	
	private String[] values;
	
	public EnumeratedIntegerZMenuItem(String name, String... values) {
		super(name, 0, 0, values.length - 1);
		this.values = values;
	}

	@Override
	public String renderValue() {
		return values[value];
	}
}

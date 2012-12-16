package org.zeromeaner.gui.console;

import org.zeromeaner.game.event.EventRenderer;
import org.zeromeaner.game.play.GameEngine;

import com.googlecode.lanterna.TerminalFacade;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.Terminal.Color;

public class ConsoleRenderer extends EventRenderer {
	private Screen screen;
	
	public ConsoleRenderer() {
		this(TerminalFacade.createScreen());
	}
	
	public ConsoleRenderer(Screen screen) {
		this.screen = screen;
	}
	
	private Color toColor(int nullpoColorInt) {
		switch(nullpoColorInt) {
		case COLOR_WHITE: return Color.WHITE;
		case COLOR_BLUE: return Color.BLUE;
		case COLOR_RED: return Color.RED;
		case COLOR_PINK: return Color.RED;
		case COLOR_GREEN: return Color.GREEN;
		case COLOR_YELLOW: return Color.YELLOW;
		case COLOR_CYAN: return Color.CYAN;
		case COLOR_ORANGE: return Color.YELLOW;
		case COLOR_PURPLE: return Color.MAGENTA;
		case COLOR_DARKBLUE: return Color.BLUE;
		default: return Color.WHITE;
		}
	}
	
	@Override
	public void drawMenuFont(
			GameEngine engine, 
			int playerID, 
			int x, 
			int y,
			String str, 
			int color, 
			float scale) {
		screen.putString(x, y, str, toColor(color), null);
	}
}

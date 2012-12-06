package mu.nu.nullpo.gui.slick;

import javax.swing.JFrame;

import mu.nu.nullpo.gui.user.UserFrame;

import org.apache.log4j.Logger;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

public class StateEnterUserName extends BasicGameState {
	
	static Logger log = Logger.getLogger(StateEnterUserName.class);

	public static final int ID = 20;

	public JFrame enterUserFrame = null;
	protected GameContainer appContainer = null;
	protected String strModeToEnter = "";
	
	private boolean exiting = false;
	
	@Override
	public int getID() {
		return ID;
	}

	public void init(GameContainer container, StateBasedGame game) throws SlickException {
		appContainer = container;
	}
	
	public void exitState(){
		exiting = true;
	}

	@Override
	public void enter(GameContainer container, StateBasedGame game) throws SlickException {
			super.enter(container, game);
			UserFrame userFrame = new UserFrame(this);
			userFrame.setVisible(true);
			exiting = false;
			System.out.println("entering state enter user name");
	}

	@Override
	public void leave(GameContainer container, StateBasedGame game) throws SlickException {
		super.leave(container, game);
//		if(gameManager != null) {
//			gameManager.shutdown();
//			gameManager = null;
//		}
//		if(enterUserFrame != null) {
//			enterUserFrame = null;
//		}
//		container.setClearEachFrame(false);

		// Reload global config (because it can change rules)
		NullpoMinoSlick.loadGlobalConfig();
		
		System.out.println("leaving state enter user name");
	}

	@Override
	public void render(GameContainer arg0, StateBasedGame arg1, Graphics arg2) throws SlickException {
	}

	@Override
	public void update(GameContainer container, StateBasedGame game, int arg2) throws SlickException {
	
		if (exiting){
			// Update key input states
			
			//workaround for bug http://lwjgl.org/forum/index.php?topic=4517.0
			Keyboard.destroy();			
			try {
				Keyboard.create();
			} catch (LWJGLException e) {
				e.printStackTrace();
			}
			
			GameKey.gamekey[0].update(container.getInput());
			//GameKey.gamekey[0].setInputState(GameKey.BUTTON_A, 0);
			game.enterState(StateTitle.ID);
		}
	}

}

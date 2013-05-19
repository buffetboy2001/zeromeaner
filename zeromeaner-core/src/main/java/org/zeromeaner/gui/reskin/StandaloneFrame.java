package org.zeromeaner.gui.reskin;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import org.apache.log4j.Logger;
import org.zeromeaner.game.component.RuleOptions;
import org.zeromeaner.game.play.GameManager;
import org.zeromeaner.game.randomizer.Randomizer;
import org.zeromeaner.game.subsystem.ai.AbstractAI;
import org.zeromeaner.game.subsystem.mode.AbstractNetMode;
import org.zeromeaner.game.subsystem.mode.GameMode;
import org.zeromeaner.game.subsystem.mode.MarathonMode;
import org.zeromeaner.game.subsystem.wallkick.Wallkick;
import org.zeromeaner.gui.knet.KNetPanel;
import org.zeromeaner.gui.knet.KNetPanelAdapter;
import org.zeromeaner.gui.knet.KNetPanelEvent;
import org.zeromeaner.gui.knet.KNetPanelListener;
import org.zeromeaner.util.CustomProperties;
import org.zeromeaner.util.GeneralUtil;
import org.zeromeaner.util.Localization;

public class StandaloneFrame extends JFrame {
	private static final Logger log = Logger.getLogger(StandaloneFrame.class);
	private static final Localization lz = new Localization();
	
	private static Icon icon(String name) {
		URL url = StandaloneFrame.class.getResource(name + ".png");
		return url == null ? null : new ImageIcon(url);
	}
	
	public static final String CARD_PLAY = "toolbar.play";
	public static final String CARD_MODESELECT = "toolbar.modeselect";
	public static final String CARD_NETPLAY = "toolbar.netplay";
	public static final String CARD_OPEN = "toolbar.open";
	public static final String CARD_OPEN_ONLINE = "toolbar.open_online";
	public static final String CARD_RULE_1P = "toolbar.rule_1p";
	public static final String CARD_KEYS_1P = "toolbar.keys_1p";
	public static final String CARD_TUNING_1P = "toolbar.tuning_1p";
	public static final String CARD_AI_1P = "toolbar.ai_1p";
	public static final String CARD_RULE_2P = "toolbar.rule_2p";
	public static final String CARD_KEYS_2P = "toolbar.keys_2p";
	public static final String CARD_TUNING_2P = "toolbar.tuning_2p";
	public static final String CARD_AI_2P = "toolbar.ai_2p";
	public static final String CARD_GENERAL = "toolbar.general";
	public static final String CARD_CLOSE = "toolbar.close";
	
	private JToolBar toolbar;
	private CardLayout contentCards;
	private JPanel content;
	
	private JPanel playCard;
	private JPanel netplayCard;

	KNetPanel netLobby;
	GameManager gameManager;
	private StandaloneGamePanel gamePanel;
	
	public StandaloneFrame() {
		setTitle("0mino");
		setUndecorated(true);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		
		setLayout(new BorderLayout());
		add(toolbar = createToolbar(), BorderLayout.EAST);
		add(content = new JPanel(contentCards = new CardLayout()), BorderLayout.CENTER);

		netLobby = new KNetPanel("none", false);
		netLobby.setPreferredSize(new Dimension(800, 250));
		netLobby.addKNetPanelListener(new KNetPanelAdapter() {
			@Override
			public void knetPanelShutdown(KNetPanelEvent e) {
				content.remove(netLobby);
				content.revalidate();
				content.repaint();
			}
		});
		
		gamePanel = new StandaloneGamePanel(this);
		
		createCards();
	}
	
	private static void add(JToolBar toolbar, ButtonGroup g, AbstractButton b) {
		b.setFocusable(false);
		b.setBorder(null);
		b.setHorizontalAlignment(SwingConstants.RIGHT);
		toolbar.add(b);
		g.add(b);
	}
	
	private void createCards() {
		playCard = new JPanel(new BorderLayout());
		content.add(playCard, CARD_PLAY);
		
		content.add(new StandaloneModeselectPanel(), CARD_MODESELECT);
		
		netplayCard = new JPanel(new BorderLayout());
		netplayCard.add(netLobby, BorderLayout.SOUTH);
		content.add(netplayCard, CARD_NETPLAY);

		StandaloneKeyConfig kc = new StandaloneKeyConfig(this);
		kc.load(0);
		content.add(kc, CARD_KEYS_1P);
		kc = new StandaloneKeyConfig(this);
		kc.load(1);
		content.add(kc, CARD_KEYS_2P);
		
		StandaloneGameTuningPanel gt = new StandaloneGameTuningPanel();
		gt.load(0);
		content.add(gt, CARD_TUNING_1P);
		gt = new StandaloneGameTuningPanel();
		gt.load(1);
		content.add(gt, CARD_TUNING_2P);
		
		StandaloneAISelectPanel ai = new StandaloneAISelectPanel();
		ai.load(0);
		content.add(ai, CARD_AI_1P);
		ai = new StandaloneAISelectPanel();
		ai.load(1);
		content.add(ai, CARD_AI_2P);
	}
	
	private JToolBar createToolbar() {
		JToolBar t = new JToolBar(JToolBar.VERTICAL);
		t.setFloatable(false);
		t.setLayout(new GridLayout(0, 1));
		
		ButtonGroup g = new ButtonGroup();
		
		AbstractButton b;
		
		b = new JToggleButton(new ToolbarAction("toolbar.play") {
			@Override
			public void actionPerformed(ActionEvent e) {
				super.actionPerformed(e);
				playCard.add(gamePanel, BorderLayout.CENTER);
				gamePanel.shutdown();
				try {
					gamePanel.shutdownWait();
				} catch(InterruptedException ie) {
				}
				startNewGame();
				gamePanel.displayWindow();
			}
		});
		add(t, g, b);
		
		b = new JToggleButton(new ToolbarAction("toolbar.modeselect"));
		add(t, g, b);
		
		b = new JToggleButton(new ToolbarAction("toolbar.netplay") {
			@Override
			public void actionPerformed(ActionEvent e) {
				super.actionPerformed(e);
				netplayCard.add(gamePanel, BorderLayout.CENTER);
				gamePanel.shutdown();
				try {
					gamePanel.shutdownWait();
				} catch(InterruptedException ie) {
				}
				enterNewMode(null);
				gamePanel.displayWindow();
			}
		});
		add(t, g, b);
		
		/*
		b = new JToggleButton(new ToolbarAction("toolbar.rule_1p"));
		add(t, g, b);
		*/
		
		b = new JToggleButton(new ToolbarAction("toolbar.ai_1p"));
		add(t, g, b);
		
		b = new JToggleButton(new ToolbarAction("toolbar.keys_1p"));
		add(t, g, b);
		
		b = new JToggleButton(new ToolbarAction("toolbar.tuning_1p"));
		add(t, g, b);
		
		/*
		b = new JToggleButton(new ToolbarAction("toolbar.rule_2p"));
		add(t, g, b);
		
		b = new JToggleButton(new ToolbarAction("toolbar.keys_2p"));
		add(t, g, b);
		
		b = new JToggleButton(new ToolbarAction("toolbar.tuning_2p"));
		add(t, g, b);
		
		b = new JToggleButton(new ToolbarAction("toolbar.ai_2p"));
		add(t, g, b);
		*/
		
		b = new JToggleButton(new ToolbarAction("toolbar.general"));
		add(t, g, b);
		
		b = new JToggleButton(new ToolbarAction("toolbar.open"));
		add(t, g, b);
		
		b = new JToggleButton(new ToolbarAction("toolbar.open_online"));
		add(t, g, b);
		
		b = new JButton(new ToolbarAction("toolbar.close") {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		add(t, g, b);
		
		return t;
	}
	
	@Override
	public void dispose() {
		super.dispose();
		StandaloneMain.saveConfig();
		System.exit(0);
	}
	
	private class ToolbarAction extends AbstractAction {
		private String cardName;
		
		public ToolbarAction(String name) {
			super(lz.s(name), icon(name));
			this.cardName = name;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			remove(netLobby);
			contentCards.show(content, cardName);
			revalidate();
			repaint();
		}
	}

	/**
	 * Enter to a new mode in netplay
	 * @param modeName Mode name
	 */
	public void enterNewMode(String modeName) {
		StandaloneMain.loadGlobalConfig();	// Reload global config file

		if(gameManager == null) {
			StandaloneRenderer rendererSwing = new StandaloneRenderer();
			gameManager = new GameManager(rendererSwing);
		}
		
		GameMode previousMode = gameManager.mode;
		GameMode newModeTemp = (modeName == null) ? new AbstractNetMode() : StandaloneMain.modeManager.get(modeName);

		if(newModeTemp == null) {
			log.error("Cannot find a mode:" + modeName);
		} else if(newModeTemp instanceof AbstractNetMode) {
			log.info("Enter new mode:" + newModeTemp.getName());

			AbstractNetMode newMode = (AbstractNetMode)newModeTemp;

			if(previousMode != null) {
				if(gameManager.engine[0].ai != null) {
					gameManager.engine[0].ai.shutdown(gameManager.engine[0], 0);
					gameManager.engine[0].ai = null;
				}
				previousMode.netplayUnload(netLobby);
			}
			
			newMode.netplayInit(netLobby);
			
			gameManager.mode = newMode;
			gameManager.init();

			// Tuning
			gameManager.engine[0].owRotateButtonDefaultRight = StandaloneMain.propConfig.getProperty(0 + ".tuning.owRotateButtonDefaultRight", -1);
			gameManager.engine[0].owSkin = StandaloneMain.propConfig.getProperty(0 + ".tuning.owSkin", -1);
			gameManager.engine[0].owMinDAS = StandaloneMain.propConfig.getProperty(0 + ".tuning.owMinDAS", -1);
			gameManager.engine[0].owMaxDAS = StandaloneMain.propConfig.getProperty(0 + ".tuning.owMaxDAS", -1);
			gameManager.engine[0].owDasDelay = StandaloneMain.propConfig.getProperty(0 + ".tuning.owDasDelay", -1);
			gameManager.engine[0].owReverseUpDown = StandaloneMain.propConfig.getProperty(0 + ".tuning.owReverseUpDown", false);
			gameManager.engine[0].owMoveDiagonal = StandaloneMain.propConfig.getProperty(0 + ".tuning.owMoveDiagonal", -1);
			gameManager.engine[0].owBlockOutlineType = StandaloneMain.propConfig.getProperty(0 + ".tuning.owBlockOutlineType", -1);
			gameManager.engine[0].owBlockShowOutlineOnly = StandaloneMain.propConfig.getProperty(0 + ".tuning.owBlockShowOutlineOnly", -1);

			// Rule
			RuleOptions ruleopt = null;
			String rulename = StandaloneMain.propConfig.getProperty(0 + ".rule", "");
			if(gameManager.mode.getGameStyle() > 0) {
				rulename = StandaloneMain.propConfig.getProperty(0 + ".rule." + gameManager.mode.getGameStyle(), "");
			}
			if((rulename != null) && (rulename.length() > 0)) {
				log.info("Load rule options from " + rulename);
				ruleopt = GeneralUtil.loadRule(rulename);
			} else {
				log.info("Load rule options from setting file");
				ruleopt = new RuleOptions();
				ruleopt.readProperty(StandaloneMain.propConfig, 0);
			}
			gameManager.engine[0].ruleopt = ruleopt;

			// Randomizer
			if((ruleopt.strRandomizer != null) && (ruleopt.strRandomizer.length() > 0)) {
				Randomizer randomizerObject = GeneralUtil.loadRandomizer(ruleopt.strRandomizer, gameManager.engine[0]);
				gameManager.engine[0].randomizer = randomizerObject;
			}

			// Wallkick
			if((ruleopt.strWallkick != null) && (ruleopt.strWallkick.length() > 0)) {
				Wallkick wallkickObject = GeneralUtil.loadWallkick(ruleopt.strWallkick);
				gameManager.engine[0].wallkick = wallkickObject;
			}

			// AI
			String aiName = StandaloneMain.propConfig.getProperty(0 + ".ai", "");
			if(aiName.length() > 0) {
				AbstractAI aiObj = GeneralUtil.loadAIPlayer(aiName);
				gameManager.engine[0].ai = aiObj;
				gameManager.engine[0].aiMoveDelay = StandaloneMain.propConfig.getProperty(0 + ".aiMoveDelay", 0);
				gameManager.engine[0].aiThinkDelay = StandaloneMain.propConfig.getProperty(0 + ".aiThinkDelay", 0);
				gameManager.engine[0].aiUseThread = StandaloneMain.propConfig.getProperty(0 + ".aiUseThread", true);
				gameManager.engine[0].aiShowHint = StandaloneMain.propConfig.getProperty(0+".aiShowHint", false);
				gameManager.engine[0].aiPrethink = StandaloneMain.propConfig.getProperty(0+".aiPrethink", false);
				gameManager.engine[0].aiShowState = StandaloneMain.propConfig.getProperty(0+".aiShowState", false);
			}
			gameManager.showInput = StandaloneMain.propConfig.getProperty("option.showInput", false);

			// Initialization for each player
			for(int i = 0; i < gameManager.getPlayers(); i++) {
				gameManager.engine[i].init();
			}

			
		} else {
			log.error("This mode does not support netplay:" + modeName);
		}

/*
		if(gameFrame != null) gameFrame.updateTitleBarCaption();
*/
	}


	/**
	 * Start a new game (Rule will be user-selected one))
	 */
	public void startNewGame() {
		startNewGame(null);
	}

	/**
	 * Start a new game
	 * @param strRulePath Rule file path (null if you want to use user-selected one)
	 */
	public void startNewGame(String strRulePath) {
		StandaloneRenderer rendererSwing = new StandaloneRenderer();
		gameManager = new GameManager(rendererSwing);

		// Mode
		String modeName = StandaloneMain.propConfig.getProperty("name.mode", "");
		GameMode modeObj = StandaloneMain.modeManager.get(modeName);
		if(modeObj == null) {
			log.error("Couldn't find mode:" + modeName);
			gameManager.mode = new MarathonMode();
		} else {
			gameManager.mode = modeObj;
		}

		gameManager.init();

		// Initialization for each player
		for(int i = 0; i < gameManager.getPlayers(); i++) {
			// Tuning settings
			gameManager.engine[i].owRotateButtonDefaultRight = StandaloneMain.propConfig.getProperty(i + ".tuning.owRotateButtonDefaultRight", -1);
			gameManager.engine[i].owSkin = StandaloneMain.propConfig.getProperty(i + ".tuning.owSkin", -1);
			gameManager.engine[i].owMinDAS = StandaloneMain.propConfig.getProperty(i + ".tuning.owMinDAS", -1);
			gameManager.engine[i].owMaxDAS = StandaloneMain.propConfig.getProperty(i + ".tuning.owMaxDAS", -1);
			gameManager.engine[i].owDasDelay = StandaloneMain.propConfig.getProperty(i + ".tuning.owDasDelay", -1);
			gameManager.engine[i].owReverseUpDown = StandaloneMain.propConfig.getProperty(i + ".tuning.owReverseUpDown", false);
			gameManager.engine[i].owMoveDiagonal = StandaloneMain.propConfig.getProperty(i + ".tuning.owMoveDiagonal", -1);
			gameManager.engine[i].owBlockOutlineType = StandaloneMain.propConfig.getProperty(i + ".tuning.owBlockOutlineType", -1);
			gameManager.engine[i].owBlockShowOutlineOnly = StandaloneMain.propConfig.getProperty(i + ".tuning.owBlockShowOutlineOnly", -1);

			// Rule
			RuleOptions ruleopt = null;
			String rulename = strRulePath;
			if(rulename == null) {
				rulename = StandaloneMain.propConfig.getProperty(i + ".rule", "");
				if(gameManager.mode.getGameStyle() > 0) {
					rulename = StandaloneMain.propConfig.getProperty(i + ".rule." + gameManager.mode.getGameStyle(), "");
				}
			}
			if((rulename != null) && (rulename.length() > 0)) {
				log.debug("Load rule options from " + rulename);
				ruleopt = GeneralUtil.loadRule(rulename);
			} else {
				log.debug("Load rule options from setting file");
				ruleopt = new RuleOptions();
				ruleopt.readProperty(StandaloneMain.propConfig, i);
			}
			gameManager.engine[i].ruleopt = ruleopt;

			// NEXTOrder generation algorithm
			if((ruleopt.strRandomizer != null) && (ruleopt.strRandomizer.length() > 0)) {
				Randomizer randomizerObject = GeneralUtil.loadRandomizer(ruleopt.strRandomizer, gameManager.engine[i]);
				gameManager.engine[i].randomizer = randomizerObject;
			}

			// Wallkick
			if((ruleopt.strWallkick != null) && (ruleopt.strWallkick.length() > 0)) {
				Wallkick wallkickObject = GeneralUtil.loadWallkick(ruleopt.strWallkick);
				gameManager.engine[i].wallkick = wallkickObject;
			}

			// AI
			String aiName = StandaloneMain.propConfig.getProperty(i + ".ai", "");
			if(aiName.length() > 0) {
				AbstractAI aiObj = GeneralUtil.loadAIPlayer(aiName);
				gameManager.engine[i].ai = aiObj;
				gameManager.engine[i].aiMoveDelay = StandaloneMain.propConfig.getProperty(i + ".aiMoveDelay", 0);
				gameManager.engine[i].aiThinkDelay = StandaloneMain.propConfig.getProperty(i + ".aiThinkDelay", 0);
				gameManager.engine[i].aiUseThread = StandaloneMain.propConfig.getProperty(i + ".aiUseThread", true);
				gameManager.engine[i].aiShowHint = StandaloneMain.propConfig.getProperty(i+".aiShowHint", false);
				gameManager.engine[i].aiPrethink = StandaloneMain.propConfig.getProperty(i+".aiPrethink", false);
				gameManager.engine[i].aiShowState = StandaloneMain.propConfig.getProperty(i+".aiShowState", false);
			}
			gameManager.showInput = StandaloneMain.propConfig.getProperty("option.showInput", false);

			// Called at initialization
			gameManager.engine[i].init();
		}
	}
}

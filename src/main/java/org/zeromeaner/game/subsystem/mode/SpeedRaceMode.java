/*
    Copyright (c) 2010, NullNoname
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions are met:

        * Redistributions of source code must retain the above copyright
          notice, this list of conditions and the following disclaimer.
        * Redistributions in binary form must reproduce the above copyright
          notice, this list of conditions and the following disclaimer in the
          documentation and/or other materials provided with the distribution.
        * Neither the name of NullNoname nor the names of its
          contributors may be used to endorse or promote products derived from
          this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
    AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
    IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
    ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
    LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
    CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
    SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
    INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
    CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
    ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
    POSSIBILITY OF SUCH DAMAGE.
*/
package org.zeromeaner.game.subsystem.mode;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.zeromeaner.game.component.BGMStatus;
import org.zeromeaner.game.component.Controller;
import org.zeromeaner.game.component.PiecePlacement;
import org.zeromeaner.game.event.EventReceiver;
import org.zeromeaner.game.net.NetUtil;
import org.zeromeaner.game.play.GameEngine;
import org.zeromeaner.util.CustomProperties;
import org.zeromeaner.util.GeneralUtil;

/**
 * In speed race, you select a ppm you want to have, then try to keep above this ppm. If you drop underneath
 * your health goes down. If health goes down too much you die. The goal is to put the number of tetriminoes
 * in the field without topping out.
 * 
 * @author belzebub
 */
public class SpeedRaceMode extends NetDummyMode {
	
	/** Logger */
	static Logger log = Logger.getLogger(SpeedRaceMode.class);

	/** Number of entries in rankings */
	private static final int RANKING_MAX = 10;

	/** Target line count type */
	private static final int GOALTYPE_MAX = 4;

	/** Target line count constants */
	private static final int[] GOAL_TABLE = {20, 60, 80, 100};

	/** Drawing and event handling EventReceiver */
	private EventReceiver receiver;

	/** BGM number */
	private int bgmno;

	/** Big */
	private boolean big;

	/** Target line count type (0=20,1=40,2=100,3=10) */
	private int goaltype;

	/** Last preset number used */
	private int presetNumber;

	/** Current round's ranking rank */
	private int rankingRank;

	/** Rankings' times */
	private int[][] rankingTime;

	/** Rankings' piece counts */
	private int[][] rankingPiece;

	/** Rankings' PPS values */
	private float[][] rankingPPS;
	
	/** play the hurry up sound if dropping below the hard limit **/
	private boolean hurryUpSound;

	/** the unit for the finesse to display on the screen (0=fault,1=kpt) **/
	private int finesseUnit;
	
	/** Finesse stat display values */
	private static final String[] FINESSE_TABLE = {"FAULTS", "KPT"};
	
	private int softSpeedLimit;
	private int hardSpeedLimit;
	
	private double health = 100;
	private boolean started = false;
	
	/*
	 * Mode name
	 */
	@Override
	public String getName() {
		return "SPEED RACE";
	}

	/*
	 * Initialization for each player
	 */
	@Override
	public void playerInit(GameEngine engine, int playerID) {
		log.debug("playerInit");
		
		health=100;
		started=false;

		owner = engine.owner;
		receiver = engine.owner.receiver;

		bgmno = 0;
		big = false;
		goaltype = 0;
		presetNumber = 0;
		
		softSpeedLimit = 100;
		hardSpeedLimit = 90;
		hurryUpSound = false;
		finesseUnit = 0;
		
		rankingRank = -1;
		rankingTime = new int[GOALTYPE_MAX][RANKING_MAX];
		rankingPiece = new int[GOALTYPE_MAX][RANKING_MAX];
		rankingPPS = new float[GOALTYPE_MAX][RANKING_MAX];

		engine.framecolor = GameEngine.FRAME_COLOR_RED;

		netPlayerInit(engine, playerID);

		if(engine.owner.replayMode == false) {
			presetNumber = engine.owner.modeConfig.getProperty("speedrace.presetNumber", 0);
			loadPreset(engine, engine.owner.modeConfig, -1);
			loadRanking(owner.modeConfig, engine.ruleopt.strRuleName);
		} else {
			presetNumber = 0;
			loadPreset(engine, engine.owner.replayProp, -1);

			// NET: Load name
			netPlayerName = engine.owner.replayProp.getProperty(playerID + ".net.netPlayerName", "");
		}
	}

	/**
	 * Load options from a preset
	 * @param engine GameEngine
	 * @param prop Property file to read from
	 * @param preset Preset number
	 */
	private void loadPreset(GameEngine engine, CustomProperties prop, int preset) {
		engine.speed.gravity = prop.getProperty("speedrace.gravity." + preset, 4);
		engine.speed.denominator = prop.getProperty("speedrace.denominator." + preset, 256);
		engine.speed.are = prop.getProperty("speedrace.are." + preset, 0);
		engine.speed.areLine = prop.getProperty("speedrace.areLine." + preset, 0);
		engine.speed.lineDelay = prop.getProperty("speedrace.lineDelay." + preset, 0);
		engine.speed.lockDelay = prop.getProperty("speedrace.lockDelay." + preset, 30);
		engine.speed.das = prop.getProperty("speedrace.das." + preset, 14);
		bgmno = prop.getProperty("speedrace.bgmno." + preset, 0);
		big = prop.getProperty("speedrace.big." + preset, false);
		goaltype = prop.getProperty("speedrace.goaltype." + preset, 1);
		softSpeedLimit = prop.getProperty("speedrace.softlimit." + preset, 100);
		hardSpeedLimit = prop.getProperty("speedrace.hardlimit." + preset, 90);
		hurryUpSound = prop.getProperty("speedrace.hurryupsound." + preset, false);
		finesseUnit = prop.getProperty("speedrace.finesseUnit." + preset, 0);
	}

	/**
	 * Save options to a preset
	 * @param engine GameEngine
	 * @param prop Property file to save to
	 * @param preset Preset number
	 */
	private void savePreset(GameEngine engine, CustomProperties prop, int preset) {
		prop.setProperty("speedrace.gravity." + preset, engine.speed.gravity);
		prop.setProperty("speedrace.denominator." + preset, engine.speed.denominator);
		prop.setProperty("speedrace.are." + preset, engine.speed.are);
		prop.setProperty("speedrace.areLine." + preset, engine.speed.areLine);
		prop.setProperty("speedrace.lineDelay." + preset, engine.speed.lineDelay);
		prop.setProperty("speedrace.lockDelay." + preset, engine.speed.lockDelay);
		prop.setProperty("speedrace.das." + preset, engine.speed.das);
		prop.setProperty("speedrace.bgmno." + preset, bgmno);
		prop.setProperty("speedrace.big." + preset, big);
		prop.setProperty("speedrace.goaltype." + preset, goaltype);
		prop.setProperty("speedrace.softlimit." + preset, softSpeedLimit);
		prop.setProperty("speedrace.hardlimit." + preset, hardSpeedLimit);
		prop.setProperty("speedrace.hurryupsound." + preset, hurryUpSound);
		prop.setProperty("speedrace.finesseUnit." + preset, finesseUnit);
	}

	/*
	 * Called at settings screen
	 */
	@Override
	public boolean onSetting(GameEngine engine, int playerID) {
		// NET: Net Ranking
		if(netIsNetRankingDisplayMode) {
			netOnUpdateNetPlayRanking(engine, goaltype);
		}
		// Menu
		else if(engine.owner.replayMode == false) {
			// Configuration changes
			int change = updateCursor(engine, 13, playerID);

			if(change != 0) {
				engine.playSE("change");

				int m = 1;
				if(engine.ctrl.isPress(Controller.BUTTON_E)) m = 100;
				if(engine.ctrl.isPress(Controller.BUTTON_F)) m = 1000;

				switch(engine.statc[2]) {
				case 0:
					engine.speed.gravity += change * m;
					if(engine.speed.gravity < -1) engine.speed.gravity = 99999;
					if(engine.speed.gravity > 99999) engine.speed.gravity = -1;
					break;
				case 1:
					engine.speed.denominator += change * m;
					if(engine.speed.denominator < -1) engine.speed.denominator = 99999;
					if(engine.speed.denominator > 99999) engine.speed.denominator = -1;
					break;
				case 2:
					engine.speed.are += change;
					if(engine.speed.are < 0) engine.speed.are = 99;
					if(engine.speed.are > 99) engine.speed.are = 0;
					break;
				case 3:
					engine.speed.areLine += change;
					if(engine.speed.areLine < 0) engine.speed.areLine = 99;
					if(engine.speed.areLine > 99) engine.speed.areLine = 0;
					break;
				case 4:
					engine.speed.lineDelay += change;
					if(engine.speed.lineDelay < 0) engine.speed.lineDelay = 99;
					if(engine.speed.lineDelay > 99) engine.speed.lineDelay = 0;
					break;
				case 5:
					engine.speed.lockDelay += change;
					if(engine.speed.lockDelay < 0) engine.speed.lockDelay = 99;
					if(engine.speed.lockDelay > 99) engine.speed.lockDelay = 0;
					break;
				case 6:
					engine.speed.das += change;
					if(engine.speed.das < 0) engine.speed.das = 99;
					if(engine.speed.das > 99) engine.speed.das = 0;
					break;
				case 7:
					bgmno += change;
					if(bgmno < 0) bgmno = BGMStatus.BGM_COUNT - 1;
					if(bgmno > BGMStatus.BGM_COUNT - 1) bgmno = 0;
					break;
				case 8:
					big = !big;
					break;
				case 9:
					goaltype += change;
					if(goaltype < 0) goaltype = 2;
					if(goaltype > 3) goaltype = 0;
					break;
				case 10:
					softSpeedLimit += change;
					break;
				case 11:
					hardSpeedLimit += change;
					break;
				case 12:
					hurryUpSound = !hurryUpSound;
					break;
				case 13:
					finesseUnit += change;
					if(finesseUnit < 0) finesseUnit = 1;
					if(finesseUnit > 1) finesseUnit = 0;
					break;
				case 14:
				case 15:
					presetNumber += change;
					if(presetNumber < 0) presetNumber = 99;
					if(presetNumber > 99) presetNumber = 0;
					break;
				}

				// NET: Signal options change
				if(netIsNetPlay && (netNumSpectators > 0)) {
					netSendOptions(engine);
				}
			}

			// Confirm
			if(engine.ctrl.isPush(Controller.BUTTON_A) && (engine.statc[3] >= 5) && (!netIsWatch)) {
				engine.playSE("decide");

				if(engine.statc[2] == 14) {
					// Load preset
					loadPreset(engine, owner.modeConfig, presetNumber);

					// NET: Signal options change
					if(netIsNetPlay && (netNumSpectators > 0)) {
						netSendOptions(engine);
					}
				} else if(engine.statc[2] == 15) {
					// Save preset
					savePreset(engine, owner.modeConfig, presetNumber);
					receiver.saveModeConfig(owner.modeConfig);
				} else {
					// Save settings
					owner.modeConfig.setProperty("speedrace.presetNumber", presetNumber);
					savePreset(engine, owner.modeConfig, -1);
					receiver.saveModeConfig(owner.modeConfig);

					// NET: Signal start of the game
					if(netIsNetPlay) netLobby.netPlayerClient.send("start1p\n");

					return false;
				}
			}

			// Cancel
			if(engine.ctrl.isPush(Controller.BUTTON_B) && (!netIsNetPlay)) {
				engine.quitflag = true;
			}

			// NET: Netplay Ranking
			if(engine.ctrl.isPush(Controller.BUTTON_D) && netIsNetPlay && !big && engine.ai == null) {
				netEnterNetPlayRankingScreen(engine, playerID, goaltype);
			}

			engine.statc[3]++;
		}
		// Replay
		else {
			engine.statc[3]++;
			engine.statc[2] = -1;

			if(engine.statc[3] >= 60) {
				return false;
			}
		}

		return true;
	}

	/*
	 * Render settings screen
	 */
	@Override
	public void renderSetting(GameEngine engine, int playerID) {
		if(netIsNetRankingDisplayMode) {
			// NET: Netplay Ranking
			netOnRenderNetPlayRanking(engine, playerID, receiver);
		} else if(engine.statc[2] < 10) {
			drawMenu(engine, playerID, receiver, 0, EventReceiver.COLOR_BLUE, 0,
					"GRAVITY", String.valueOf(engine.speed.gravity),
					"G-MAX", String.valueOf(engine.speed.denominator),
					"ARE", String.valueOf(engine.speed.are),
					"ARE LINE", String.valueOf(engine.speed.areLine),
					"LINE DELAY", String.valueOf(engine.speed.lineDelay),
					"LOCK DELAY", String.valueOf(engine.speed.lockDelay),
					"DAS", String.valueOf(engine.speed.das),
					"BGM", String.valueOf(bgmno),
					"BIG",  GeneralUtil.getONorOFF(big),
					"GOAL", String.valueOf(GOAL_TABLE[goaltype]));
		} else {
			drawMenu(engine, playerID, receiver, 0, EventReceiver.COLOR_BLUE, 10,
					"WARNING LIMIT", String.valueOf(softSpeedLimit),
					"SPEED LIMIT", String.valueOf(hardSpeedLimit),
					"ALARM", GeneralUtil.getONorOFF(hurryUpSound),
					"FINESSE", String.valueOf(FINESSE_TABLE[finesseUnit]),
					"LOAD", String.valueOf(presetNumber),
					"SAVE", String.valueOf(presetNumber));
		}
	}

	/*
	 * This function will be called before the game actually begins (after Ready&Go screen disappears)
	 */
	@Override
	public void startGame(GameEngine engine, int playerID) {
		
		started = true;
		
		engine.big = big;

		if(netIsWatch) {
			owner.bgmStatus.bgm = BGMStatus.BGM_NOTHING;
		} else {
			owner.bgmStatus.bgm = bgmno;
		}

		engine.setMeterColor(GameEngine.METER_COLOR_GREEN);
		engine.setMeterValue(receiver.getMeterMax(engine));
	}

	/*
	 * Score display
	 */
	@Override
	public void renderLast(GameEngine engine, int playerID) {
		if(owner.menuOnly) return;
		
		updateHealth(engine);

		receiver.drawScoreFont(engine, playerID, 0, 0, "SPEEED RACE", EventReceiver.COLOR_RED);
		receiver.drawScoreFont(engine, playerID, 0, 1, "(" + GOAL_TABLE[goaltype] + " PIECES GAME)", EventReceiver.COLOR_RED);

		if( (engine.stat == GameEngine.STAT_SETTING) || ((engine.stat == GameEngine.STAT_RESULT) && (owner.replayMode == false)) ) {
			if(!owner.replayMode && !big && (engine.ai == null) && !netIsWatch) {
				float scale = (receiver.getNextDisplayType() == 2) ? 0.5f : 1.0f;
				int topY = (receiver.getNextDisplayType() == 2) ? 6 : 4;
				receiver.drawScoreFont(engine, playerID, 3, topY-1, "TIME     PIECE PPS", EventReceiver.COLOR_BLUE, scale);

				for(int i = 0; i < RANKING_MAX; i++) {
					receiver.drawScoreFont(engine, playerID,  0, topY+i, String.format("%2d", i + 1), EventReceiver.COLOR_YELLOW, scale);
					receiver.drawScoreFont(engine, playerID,  3, topY+i, GeneralUtil.getTime(rankingTime[goaltype][i]), (rankingRank == i), scale);
					receiver.drawScoreFont(engine, playerID, 12, topY+i, String.valueOf(rankingPiece[goaltype][i]), (rankingRank == i), scale);
					receiver.drawScoreFont(engine, playerID, 18, topY+i, String.format("%.5g", rankingPPS[goaltype][i]), (rankingRank == i), scale);
				}
			}
		} else {
			receiver.drawScoreFont(engine, playerID, 0, 3, "HEALTH", EventReceiver.COLOR_BLUE);
			
			String strHealth = String.valueOf((int)health);
			if(health < 0) strHealth = "0";
			int fontcolor = EventReceiver.COLOR_WHITE;
			
			if((health <= 75) && (health > 0)) fontcolor = EventReceiver.COLOR_YELLOW;
			if((health <= 50) && (health > 0)) fontcolor = EventReceiver.COLOR_ORANGE;
			if((health <= 25) && (health > 0)) fontcolor = EventReceiver.COLOR_RED;
			
			receiver.drawScoreFont(engine, playerID, 0, 4, strHealth, fontcolor);

			if(strHealth.length() == 1) {
				receiver.drawMenuFont(engine, playerID, 4, 21, strHealth, fontcolor, 2.0f);
			} else if(strHealth.length() == 2) {
				receiver.drawMenuFont(engine, playerID, 3, 21, strHealth, fontcolor, 2.0f);
			} else if(strHealth.length() == 3) {
				receiver.drawMenuFont(engine, playerID, 2, 21, strHealth, fontcolor, 2.0f);
			}

			receiver.drawScoreFont(engine, playerID, 0, 6, "PIECE", EventReceiver.COLOR_BLUE);
			receiver.drawScoreFont(engine, playerID, 0, 7, String.valueOf(engine.statistics.totalPieceLocked));

//			receiver.drawScoreFont(engine, playerID, 0, 9, "LINE/MIN", EventReceiver.COLOR_BLUE);
//			receiver.drawScoreFont(engine, playerID, 0, 10, String.valueOf(engine.statistics.lpm));

			receiver.drawScoreFont(engine, playerID, 0, 9, "CURRENT SPEED (PPM)", EventReceiver.COLOR_BLUE);
			receiver.drawScoreFont(engine, playerID, 0, 10, String.valueOf(engine.statistics.getCurrentPPM()));
			
			receiver.drawScoreFont(engine, playerID, 0, 12, "PIECE/SEC", EventReceiver.COLOR_BLUE);
			receiver.drawScoreFont(engine, playerID, 0, 13, String.valueOf(engine.statistics.getPps()));
			
			int paintColor = engine.statistics.ppm > softSpeedLimit ? EventReceiver.COLOR_GREEN : engine.statistics.ppm > hardSpeedLimit ? EventReceiver.COLOR_ORANGE : EventReceiver.COLOR_RED;
			engine.framecolor = engine.statistics.ppm > softSpeedLimit ? GameEngine.FRAME_COLOR_GREEN : engine.statistics.ppm > hardSpeedLimit ? GameEngine.FRAME_COLOR_YELLOW : GameEngine.FRAME_COLOR_RED;
			
			if ( hurryUpSound && engine.statistics.ppm < hardSpeedLimit && engine.statistics.getTime() > 250){
				engine.playSE("hurryup");
			}
			
			receiver.drawScoreFont(engine, playerID, 0, 15, "PIECE/MIN", EventReceiver.COLOR_BLUE);
			receiver.drawScoreFont(engine, playerID, 0, 16, String.valueOf(String.valueOf(engine.statistics.ppm)), paintColor);

			receiver.drawScoreFont(engine, playerID, 0, 18, "TIME", EventReceiver.COLOR_BLUE);
			receiver.drawScoreFont(engine, playerID, 0, 19, GeneralUtil.getTime(engine.statistics.getTime()));

			receiver.drawScoreFont(engine, playerID, 0, 21, "LIMIT", EventReceiver.COLOR_BLUE);
			receiver.drawScoreFont(engine, playerID, 0, 22, String.valueOf(hardSpeedLimit));
				
		}
		
		//System.out.println(engine.statistics.getCurrentPPM() + "," + engine.statistics.ppm);
		//System.out.println(engine.statistics.getTime() + "," + engine.statistics.getCurrentPPM() + "," + engine.statistics.ppm);
//		engine.statistics.speedEntries.add(engine.statistics.new SpeedEntry(engine.statistics.getTime(), engine.statistics.getCurrentPPM(), engine.statistics.ppm));
//		engine.setMeterValue((int)((engine.statistics.getCurrentPPM() * receiver.getMeterMax(engine)) / 360f));
//		engine.setMeterColor(GameEngine.METER_COLOR_RED);
//		
//		NormalFont.printFont(200, 480-16, " - " + engine.statistics.getTime(), NormalFont.COLOR_RED);

		// NET: Number of spectators
		netDrawSpectatorsCount(engine, 0, 18);
		// NET: All number of players
		if(playerID == getPlayers() - 1) {
			netDrawAllPlayersCount(engine);
			netDrawGameRate(engine);
		}
		// NET: Player name (It may also appear in offline replay)
		netDrawPlayerName(engine);
	}

	/*
	 * Calculate score
	 */
	@Override
	public void calcScore(GameEngine engine, int playerID, int lines) {
		
		int totalNumberOfPiecesPlayed = engine.statistics.totalPieceLocked;
		
		updateHealth(engine);

		// All clear
		if((lines >= 1) && (engine.field.isEmpty())) {
			engine.playSE("bravo");
		}

		if(totalNumberOfPiecesPlayed >= GOAL_TABLE[goaltype] || health < 1) {
			started = false;
			engine.ending = 1;
			engine.gameEnded();
			engine.stat =  (totalNumberOfPiecesPlayed >= GOAL_TABLE[goaltype]) ?GameEngine.STAT_EXCELLENT : GameEngine.STAT_GAMEOVER;
		} else if(totalNumberOfPiecesPlayed >= GOAL_TABLE[goaltype] - 10) {
			owner.bgmStatus.fadesw = true;
		}
	}

	private void updateHealth(GameEngine engine) {
		
		float currentSpeed = engine.statistics.getCurrentPPM();
		
		if (started && currentSpeed < hardSpeedLimit && engine.statistics.totalPieceLocked > 5){
			double delta = hardSpeedLimit - currentSpeed;
			
			double damage = Math.log10(delta + 1) * 10;
			
			health -= 1f/30f * damage;
		}
		
		engine.setMeterValue((int) (health * receiver.getMeterMax(engine)) / 100);
		
		if(health <= 75) engine.setMeterColor(GameEngine.METER_COLOR_YELLOW);
		if(health <= 50) engine.setMeterColor(GameEngine.METER_COLOR_ORANGE);
		if(health <= 25) engine.setMeterColor(GameEngine.METER_COLOR_RED);
	}

	private void printPieceTimings(GameEngine engine) {
//		System.out.print("--Piece timings:");
//		ArrayList<PiecePlacement> piecePlacements = engine.getPiecePlacements();
//		for (int i = 0; i<piecePlacements.size(); i++){
//			System.out.println(i + "," + piecePlacements.get(i).getTime());
//		}
//		System.out.print("--End piece timings");
	}

	/*
	 * Render results screen
	 */
	@Override
	public void renderResult(GameEngine engine, int playerID) {
		drawResultStats(engine, playerID, receiver, 1, EventReceiver.COLOR_BLUE, STAT_PIECE, STAT_PPM, STAT_PPS, STAT_TIME);
		
		receiver.drawMenuFont(engine, playerID, 0, 9, "LIMIT", EventReceiver.COLOR_BLUE);
		receiver.drawMenuFont(engine, playerID, 0, 10, String.format("%10d", (int) hardSpeedLimit));

		receiver.drawMenuFont(engine, playerID, 0, 11, "HEALTH", EventReceiver.COLOR_BLUE);
		receiver.drawMenuFont(engine, playerID, 0, 12, String.format("%10d", (int) health));
		
		drawResultRank(engine, playerID, receiver, 11, EventReceiver.COLOR_BLUE, rankingRank);
		drawResultNetRank(engine, playerID, receiver, 13, EventReceiver.COLOR_BLUE, netRankingRank[0]);
		drawResultNetRankDaily(engine, playerID, receiver, 15, EventReceiver.COLOR_BLUE, netRankingRank[1]);

		if(netIsPB) {
			receiver.drawMenuFont(engine, playerID, 2, 18, "NEW PB", EventReceiver.COLOR_ORANGE);
		}

		if(netIsNetPlay && (netReplaySendStatus == 1)) {
			receiver.drawMenuFont(engine, playerID, 0, 19, "SENDING...", EventReceiver.COLOR_PINK);
		} else if(netIsNetPlay && !netIsWatch && (netReplaySendStatus == 2)) {
			receiver.drawMenuFont(engine, playerID, 1, 19, "A: RETRY", EventReceiver.COLOR_RED);
		}
	}

	/*
	 * Save replay file
	 */
	@Override
	public void saveReplay(GameEngine engine, int playerID, CustomProperties prop) {
		savePreset(engine, engine.owner.replayProp, -1);

		prop.setProperty("speedrace.pieceSequence", getEncodedPiecePlacements(engine));
		
		// NET: Save name
		if((netPlayerName != null) && (netPlayerName.length() > 0)) {
			prop.setProperty(playerID + ".net.netPlayerName", netPlayerName);
		}

		// Update rankings
		if((!owner.replayMode) && (engine.statistics.lines >= GOAL_TABLE[goaltype]) && (!big) && (engine.ai == null) && (!netIsWatch))
		{
			updateRanking(engine.statistics.getTime(), engine.statistics.totalPieceLocked, engine.statistics.getPps());

			if(rankingRank != -1) {
				saveRanking(owner.modeConfig, engine.ruleopt.strRuleName);
				receiver.saveModeConfig(owner.modeConfig);
			}
		}
	}

	private String getEncodedPiecePlacements(GameEngine engine) {
		String encoded = ""; 
		ArrayList<PiecePlacement> piecePlacements = engine.getPiecePlacements();
		 for (PiecePlacement piecePlacement : piecePlacements){
			 encoded += piecePlacement.toString();
		 }
		 return encoded;
	}

	/**
	 * Read rankings from property file
	 * @param prop Property file
	 * @param ruleName Rule name
	 */
	@Override
	protected void loadRanking(CustomProperties prop, String ruleName) {
		for(int i = 0; i < GOALTYPE_MAX; i++) {
			for(int j = 0; j < RANKING_MAX; j++) {
				rankingTime[i][j] = prop.getProperty("speedrace.ranking." + ruleName + "." + i + ".time." + j, -1);
				rankingPiece[i][j] = prop.getProperty("speedrace.ranking." + ruleName + "." + i + ".piece." + j, 0);
				rankingPPS[i][j] = prop.getProperty("speedrace.ranking." + ruleName + "." + i + ".pps." + j, 0f);
			}
		}
	}

	/**
	 * Save rankings to property file
	 * @param prop Property file
	 * @param ruleName Rule name
	 */
	private void saveRanking(CustomProperties prop, String ruleName) {
		for(int i = 0; i < GOALTYPE_MAX; i++) {
			for(int j = 0; j < RANKING_MAX; j++) {
				prop.setProperty("speedrace.ranking." + ruleName + "." + i + ".time." + j, rankingTime[i][j]);
				prop.setProperty("speedrace.ranking." + ruleName + "." + i + ".piece." + j, rankingPiece[i][j]);
				prop.setProperty("speedrace.ranking." + ruleName + "." + i + ".pps." + j, rankingPPS[i][j]);
			}
		}
	}

	/**
	 * Update rankings
	 * @param time Time
	 * @param piece Piece count
	 */
	private void updateRanking(int time, int piece, float pps) {
		rankingRank = checkRanking(time, piece, pps);

		if(rankingRank != -1) {
			// Shift down ranking entries
			for(int i = RANKING_MAX - 1; i > rankingRank; i--) {
				rankingTime[goaltype][i] = rankingTime[goaltype][i - 1];
				rankingPiece[goaltype][i] = rankingPiece[goaltype][i - 1];
				rankingPPS[goaltype][i] = rankingPPS[goaltype][i - 1];
			}

			// Add new data
			rankingTime[goaltype][rankingRank] = time;
			rankingPiece[goaltype][rankingRank] = piece;
			rankingPPS[goaltype][rankingRank] = pps;
		}
	}

	/**
	 * Calculate ranking position
	 * @param time Time
	 * @param piece Piece count
	 * @return Position (-1 if unranked)
	 */
	private int checkRanking(int time, int piece, float pps) {
		for(int i = 0; i < RANKING_MAX; i++) {
			if((time < rankingTime[goaltype][i]) || (rankingTime[goaltype][i] < 0)) {
				return i;
			} else if((time == rankingTime[goaltype][i]) && ((piece < rankingPiece[goaltype][i]) || (rankingPiece[goaltype][i] == 0))) {
				return i;
			} else if((time == rankingTime[goaltype][i]) && (piece == rankingPiece[goaltype][i]) && (pps > rankingPPS[goaltype][i])) {
				return i;
			}
		}

		return -1;
	}

	/**
	 * NET: Send various in-game stats (as well as goaltype)
	 * @param engine GameEngine
	 */
	@Override
	protected void netSendStats(GameEngine engine) {
		String msg = "game\tstats\t";
		msg += engine.statistics.lines + "\t" + engine.statistics.totalPieceLocked + "\t";
		msg += engine.statistics.getTime() + "\t" + engine.statistics.lpm + "\t";
		msg += engine.statistics.getPps() + "\t" + goaltype + "\t";
		msg += engine.gameActive + "\t" + engine.timerActive;
		msg += "\n";
		netLobby.netPlayerClient.send(msg);
	}

	/**
	 * NET: Receive various in-game stats (as well as goaltype)
	 */
	@Override
	protected void netRecvStats(GameEngine engine, String[] message) {
		engine.statistics.lines = Integer.parseInt(message[4]);
		engine.statistics.totalPieceLocked = Integer.parseInt(message[5]);
		engine.statistics.setTime(Integer.parseInt(message[6]));
		engine.statistics.lpm = Float.parseFloat(message[7]);
		engine.statistics.setPps(Float.parseFloat(message[8]));
		goaltype = Integer.parseInt(message[9]);
		engine.gameActive = Boolean.parseBoolean(message[10]);
		engine.timerActive = Boolean.parseBoolean(message[11]);

		// Update meter
		int remainLines = GOAL_TABLE[goaltype] - engine.statistics.lines;
		engine.setMeterValue((remainLines * receiver.getMeterMax(engine)) / GOAL_TABLE[goaltype]);
		if(remainLines <= 30) engine.setMeterColor(GameEngine.METER_COLOR_YELLOW);
		if(remainLines <= 20) engine.setMeterColor(GameEngine.METER_COLOR_ORANGE);
		if(remainLines <= 10) engine.setMeterColor(GameEngine.METER_COLOR_RED);
	}

	/**
	 * NET: Send end-of-game stats
	 * @param engine GameEngine
	 */
	@Override
	protected void netSendEndGameStats(GameEngine engine) {
		String subMsg = "";
		subMsg += "LINE;" + engine.statistics.lines + "/" + GOAL_TABLE[goaltype] + "\t";
		subMsg += "PIECE;" + engine.statistics.totalPieceLocked + "\t";
		subMsg += "TIME;" + GeneralUtil.getTime(engine.statistics.getTime()) + "\t";
		subMsg += "LINE/MIN;" + engine.statistics.lpm + "\t";
		subMsg += "PIECE/SEC;" + engine.statistics.getPps() + "\t";

		String msg = "gstat1p\t" + NetUtil.urlEncode(subMsg) + "\n";
		netLobby.netPlayerClient.send(msg);
	}

	/**
	 * NET: Send game options to all spectators
	 * @param engine GameEngine
	 */
	@Override
	protected void netSendOptions(GameEngine engine) {
		String msg = "game\toption\t";
		msg += engine.speed.gravity + "\t" + engine.speed.denominator + "\t" + engine.speed.are + "\t";
		msg += engine.speed.areLine + "\t" + engine.speed.lineDelay + "\t" + engine.speed.lockDelay + "\t";
		msg += engine.speed.das + "\t" + bgmno + "\t" + big + "\t" + goaltype + "\t" + presetNumber;
		msg += "\n";
		netLobby.netPlayerClient.send(msg);
	}

	/**
	 * NET: Receive game options
	 */
	@Override
	protected void netRecvOptions(GameEngine engine, String[] message) {
		engine.speed.gravity = Integer.parseInt(message[4]);
		engine.speed.denominator = Integer.parseInt(message[5]);
		engine.speed.are = Integer.parseInt(message[6]);
		engine.speed.areLine = Integer.parseInt(message[7]);
		engine.speed.lineDelay = Integer.parseInt(message[8]);
		engine.speed.lockDelay = Integer.parseInt(message[9]);
		engine.speed.das = Integer.parseInt(message[10]);
		bgmno = Integer.parseInt(message[11]);
		big = Boolean.parseBoolean(message[12]);
		goaltype = Integer.parseInt(message[13]);
		presetNumber = Integer.parseInt(message[14]);
	}

	/**
	 * NET: Get goal type
	 */
	@Override
	protected int netGetGoalType() {
		return goaltype;
	}

	/**
	 * NET: It returns true when the current settings doesn't prevent leaderboard screen from showing.
	 */
	@Override
	protected boolean netIsNetRankingViewOK(GameEngine engine) {
		return (!big) && (engine.ai == null);
	}

	/**
	 * NET: It returns true when the current settings doesn't prevent replay data from sending.
	 */
	@Override
	protected boolean netIsNetRankingSendOK(GameEngine engine) {
		return netIsNetRankingViewOK(engine) && (engine.statistics.lines >= GOAL_TABLE[goaltype]);
	}
}

package org.zeromeaner.game.subsystem.mode;

import java.io.IOException;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.TimeZone;
import java.util.zip.Adler32;


import org.apache.log4j.Logger;
import org.zeromeaner.contrib.net.omegaboshi.nullpomino.game.subsystem.randomizer.Randomizer;
import org.zeromeaner.game.component.Block;
import org.zeromeaner.game.component.Controller;
import org.zeromeaner.game.component.Field;
import org.zeromeaner.game.component.Piece;
import org.zeromeaner.game.component.RuleOptions;
import org.zeromeaner.game.component.Statistics;
import org.zeromeaner.game.event.EventRenderer;
import org.zeromeaner.game.knet.KNetClient;
import org.zeromeaner.game.knet.KNetEvent;
import org.zeromeaner.game.knet.KNetEventSource;
import org.zeromeaner.game.knet.KNetListener;
import org.zeromeaner.game.knet.KNetPlayerInfo;
import org.zeromeaner.game.knet.obj.PieceHold;
import org.zeromeaner.game.knet.obj.PieceMovement;
import org.zeromeaner.game.knet.srv.KSChannelInfo;
import org.zeromeaner.game.play.GameEngine;
import org.zeromeaner.game.play.GameManager;
import org.zeromeaner.game.subsystem.wallkick.Wallkick;
import org.zeromeaner.gui.knet.KNetFrame;
import org.zeromeaner.util.CustomProperties;
import org.zeromeaner.util.GeneralUtil;

import static org.zeromeaner.game.knet.KNetEventArgs.*;

/**
 * Special base class for netplay
 */
public class AbstractNetMode extends AbstractMode implements KNetListener {
	/** Log (Declared in NetDummyMode) */
	static Logger log = Logger.getLogger(AbstractNetMode.class);

	/** NET: Lobby (Declared in NetDummyMode) */
	protected KNetClient knetClient;

	/** NET: GameManager (Declared in NetDummyMode; Don't override it!) */
	protected GameManager owner;

	/** NET: true if netplay (Declared in NetDummyMode) */
	protected boolean netIsNetPlay;

	/** NET: true if watch mode (Declared in NetDummyMode) */
	protected boolean netIsWatch;

	/** NET: Current room info. Sometimes null. (Declared in NetDummyMode) */
	protected KSChannelInfo channelInfo;

	/** NET: Number of spectators (Declared in NetDummyMode) */
	protected int netNumSpectators;

	/** NET: Send all movements even if there are no spectators */
	protected boolean netForceSendMovements;

	/** NET: Previous piece informations (Declared in NetDummyMode) */
	protected int netPrevPieceID, netPrevPieceX, netPrevPieceY, netPrevPieceDir;

	/** NET: The skin player using (Declared in NetDummyMode) */
	protected int netPlayerSkin;

	/** NET: If true, NetDummyMode will always send attributes when sending the field (Declared in NetDummyMode) */
	protected boolean netAlwaysSendFieldAttributes;

	/** NET: Player name (Declared in NetDummyMode) */
	protected String netPlayerName;

	/** NET: Replay send status (0:Before Send 1:Sending 2:Sent) (Declared in NetDummyMode) */
	protected int netReplaySendStatus;

	/** NET: Current round's online ranking rank (Declared in NetDummyMode) */
	protected int[] netRankingRank;

	/** NET: True if new personal record (Declared in NetDummyMode) */
	protected boolean netIsPB;

	/** NET: True if net ranking display mode (Declared in NetDummyMode) */
	protected boolean netIsNetRankingDisplayMode;

	/** NET: Net ranking cursor position (Declared in NetDummyMode) */
	protected int[] netRankingCursor;

	/** NET: Net ranking player's current rank (Declared in NetDummyMode) */
	protected int[] netRankingMyRank;

	/** NET: 0 if viewing all-time ranking, 1 if viewing daily ranking (Declared in NetDummyMode) */
	protected int netRankingView;

	/** NET: Net ranking type (Declared in NetDummyMode) */
	protected int netRankingType;

	/** NET: True if no data is present. [0] for all-time and [1] for daily. (Declared in NetDummyMode) */
	protected boolean[] netRankingNoDataFlag;

	/** NET: True if loading is complete. [0] for all-time and [1] for daily. (Declared in NetDummyMode) */
	protected boolean[] netRankingReady;

	/** NET: Net Rankings' rank (Declared in NetDummyMode) */
	protected LinkedList<Integer>[] netRankingPlace;

	/** NET: Net Rankings' names (Declared in NetDummyMode) */
	protected LinkedList<String>[] netRankingName;

	/** NET: Net Rankings' timestamps (Declared in NetDummyMode) */
	protected LinkedList<Calendar>[] netRankingDate;

	/** NET: Net Rankings' gamerates (Declared in NetDummyMode) */
	protected LinkedList<Float>[] netRankingGamerate;

	/** NET: Net Rankings' times (Declared in NetDummyMode) */
	protected LinkedList<Integer>[] netRankingTime;

	/** NET: Net Rankings' score (Declared in NetDummyMode) */
	protected LinkedList<Integer>[] netRankingScore;

	/** NET: Net Rankings' piece counts (Declared in NetDummyMode) */
	protected LinkedList<Integer>[] netRankingPiece;

	/** NET: Net Rankings' PPS values (Declared in NetDummyMode) */
	protected LinkedList<Float>[] netRankingPPS;

	/** NET: Net Rankings' line counts (Declared in NetDummyMode) */
	protected LinkedList<Integer>[] netRankingLines;

	/** NET: Net Rankings' score/line (Declared in NetDummyMode) */
	protected LinkedList<Double>[] netRankingSPL;

	/** NET: Net Rankings' roll completed flag (Declared in NetDummyMode) */
	protected LinkedList<Integer>[] netRankingRollclear;

	/** NET-VS: Local player's seat ID (-1:Spectator) */
	protected int netvsMySeatID;

	protected boolean synchronousPlay;
	
	public boolean isSynchronousPlay() {
		return synchronousPlay;
	}

	/*
	 * NET: Mode name
	 */
	@Override
	public String getName() {
		return "NET-DUMMY";
	}

	/**
	 * NET: Netplay Initialization. NetDummyMode will set the lobby's current mode to this.
	 */
	@Override
	public void netplayInit(Object obj) {
	}

	/**
	 * NET: Netplay Unload. NetDummyMode will set the lobby's current mode to null.
	 */
	@Override
	public void netplayUnload(Object obj) {
	}

	/**
	 * NET: Mode Initialization. NetDummyMode will set the "owner" variable.
	 */
	@Override
	public void modeInit(GameManager manager) {
		log.debug("modeInit() on NetDummyMode");
		owner = manager;
		netIsNetPlay = false;
		netIsWatch = false;
		netNumSpectators = 0;
		netForceSendMovements = false;
		netPlayerName = "";
		netRankingCursor = new int[2];
		netRankingMyRank = new int[2];
		netRankingView = 0;
		netRankingNoDataFlag = new boolean[2];
		netRankingReady = new boolean[2];

		netRankingPlace = new LinkedList[2];
		netRankingName = new LinkedList[2];
		netRankingDate = new LinkedList[2];
		netRankingGamerate = new LinkedList[2];
		netRankingTime = new LinkedList[2];
		netRankingScore = new LinkedList[2];
		netRankingPiece = new LinkedList[2];
		netRankingPPS = new LinkedList[2];
		netRankingLines = new LinkedList[2];
		netRankingSPL = new LinkedList[2];
		netRankingRollclear = new LinkedList[2];
	}

	/**
	 * NET: Initialization for each player. NetDummyMode will stop and hide all players.
	 * Call netPlayerInit if you want to init NetPlay variables.
	 */
	@Override
	public void playerInit(GameEngine engine, int playerID) {
		engine.stat = GameEngine.STAT_NOTHING;
		engine.isVisible = false;
	}

	/**
	 * NET: Initialize various NetPlay variables. Usually called from playerInit.
	 * @param engine GameEngine
	 * @param playerID Player ID
	 */
	protected void netPlayerInit(GameEngine engine, int playerID) {
		netPrevPieceID = Piece.PIECE_NONE;
		netPrevPieceX = 0;
		netPrevPieceY = 0;
		netPrevPieceDir = 0;
		netPlayerSkin = 0;
		netReplaySendStatus = 0;
		netRankingRank = new int[2];
		netRankingRank[0] = -1;
		netRankingRank[1] = -1;
		netIsPB = false;
		netIsNetRankingDisplayMode = false;
		netAlwaysSendFieldAttributes = false;

		if(netIsWatch) {
			engine.isNextVisible = false;
			engine.isHoldVisible = false;
		}
	}

	/**
	 * NET: When the pieces can move. NetDummyMode will send field/next/stats/piece movements.
	 */
	@Override
	public boolean onMove(GameEngine engine, int playerID) {
		// NET: Send field, next, and stats
		if((engine.ending == 0) && (engine.statc[0] == 0) && (engine.holdDisable == false) &&
		   (netIsNetPlay) && (!netIsWatch) && ((netNumSpectators > 0) || (netForceSendMovements)))
		{
			netSendField(engine);
			netSendStats(engine);
		}
		// NET: Send piece movement
		if((engine.ending == 0) && (netIsNetPlay) && (!netIsWatch) && (engine.nowPieceObject != null) &&
		   ((netNumSpectators > 0) || (netForceSendMovements)))
		{
			if(netSendPieceMovement(engine, false)) {
				netSendNextAndHold(engine);
			}
		}
		// NET: Stop game in watch mode
		if(netIsWatch) {
			return true;
		}

		return false;
	}

	@Override
	public boolean onReady(GameEngine engine, int playerID) {
		return super.onReady(engine, playerID);
	}
	
	/**
	 * NET: When the piece locked. NetDummyMode will send field and stats.
	 */
	@Override
	public void pieceLocked(GameEngine engine, int playerID, int lines) {
		// NET: Send field and stats
		if((engine.ending == 0) && (netIsNetPlay) && (!netIsWatch) && ((netNumSpectators > 0) || (netForceSendMovements))) {
			netSendField(engine);
			netSendStats(engine);
			knetClient.fireTCP(GAME, true, GAME_PIECE_LOCKED, true);
		}
	}

	/**
	 * NET: Line clear. NetDummyMode will send field and stats.
	 */
	@Override
	public boolean onLineClear(GameEngine engine, int playerID) {
		// NET: Send field and stats
		if((engine.statc[0] == 1) && (engine.ending == 0) && (netIsNetPlay) && (!netIsWatch) && ((netNumSpectators > 0) || (netForceSendMovements))) {
			netSendField(engine);
			netSendStats(engine);
		}
		return false;
	}

	/**
	 * NET: ARE. NetDummyMode will send field, next and stats.
	 */
	@Override
	public boolean onARE(GameEngine engine, int playerID) {
		// NET: Send field, next, and stats
		if((engine.statc[0] == 0) && (engine.ending == 0) && (netIsNetPlay) && (!netIsWatch) && ((netNumSpectators > 0) || (netForceSendMovements))) {
			netSendField(engine);
			netSendNextAndHold(engine);
			netSendStats(engine);
		}
		return false;
	}

	/**
	 * NET: Ending start. NetDummyMode will send ending start messages.
	 */
	@Override
	public boolean onEndingStart(GameEngine engine, int playerID) {
		if(engine.statc[2] == 0) {
			// NET: Send game completed messages
			if(netIsNetPlay && !netIsWatch && ((netNumSpectators > 0) || (netForceSendMovements))) {
				netSendField(engine);
				netSendNextAndHold(engine);
				netSendStats(engine);
				knetClient.fire(GAME, true, GAME_ENDING, true);
			}
		}
		return false;
	}

	/**
	 * NET: "Excellent!" screen
	 */
	@Override
	public boolean onExcellent(GameEngine engine, int playerID) {
		if(engine.statc[0] == 0) {
			// NET: Send game completed messages
			if(netIsNetPlay && !netIsWatch && ((netNumSpectators > 0) || (netForceSendMovements))) {
				netSendField(engine);
				netSendNextAndHold(engine);
				netSendStats(engine);
//				netLobby.netPlayerClient.send("game\texcellent\n");
				knetClient.fire(GAME, true, GAME_EXCELLENT, true);
			}
		}
		return false;
	}

	/**
	 * NET: Game Over
	 */
	@Override
	public boolean onGameOver(GameEngine engine, int playerID) {
		// NET: Send messages / Wait for messages
		if(netIsNetPlay){
			if(!netIsWatch) {
				if(engine.statc[0] == 0) {
					// Send end-of-game messages
					if((netNumSpectators > 0) || (netForceSendMovements)) {
						netSendField(engine);
						netSendNextAndHold(engine);
						netSendStats(engine);
					}
					netSendEndGameStats(engine);
					knetClient.fire(DEAD, true);
				} else if(engine.statc[0] >= engine.field.getHeight() + 1 + 180) {
					// To results screen
					knetClient.fire(GAME, true, GAME_RESULTS_SCREEN, true);
				}
			} else {
				if(engine.statc[0] < engine.field.getHeight() + 1 + 180) {
					return false;
				} else {
					engine.field.reset();
					engine.stat = GameEngine.STAT_RESULT;
					engine.resetStatc();
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * NET: Results screen
	 */
	@Override
	public boolean onResult(GameEngine engine, int playerID) {
		// NET: Retry
		if(netIsNetPlay) {
			engine.allowTextRenderByReceiver = false;

			// Replay Send
			if(netIsWatch || owner.replayMode) {
				netReplaySendStatus = 2;
			} else if(netReplaySendStatus == 0) {
				netReplaySendStatus = 1;
				netSendReplay(engine);
			}

			// Retry
			if(engine.ctrl.isPush(Controller.BUTTON_A) && !netIsWatch && (netReplaySendStatus == 2)) {
				engine.playSE("decide");
				if((netNumSpectators > 0) || (netForceSendMovements)) {
					knetClient.fire(GAME, true, GAME_RETRY, true);
					netSendOptions(engine);
				}
				owner.reset();
			}

			return true;
		}

		return false;
	}

	/**
	 * NET: Render something such as HUD. NetDummyMode will render the number of players to bottom-right of the screen.
	 */
	@Override
	public void renderLast(GameEngine engine, int playerID) {
		if(playerID == getPlayers() - 1) netDrawAllPlayersCount(engine);
	}

	/**
	 * NET: Update menu cursor. NetDummyMode will signal cursor movement to all spectators.
	 */
	@Override
	protected int updateCursor(GameEngine engine, int maxCursor, int playerID) {
		// NET: Don't execute in watch mode
		if(netIsWatch) return 0;

		int change = super.updateCursor(engine, maxCursor, playerID);

		// NET: Signal cursor change
		if((engine.ctrl.isMenuRepeatKey(Controller.BUTTON_UP) || engine.ctrl.isMenuRepeatKey(Controller.BUTTON_DOWN)) &&
			netIsNetPlay && ((netNumSpectators > 0) || (netForceSendMovements)))
		{
			knetClient.fire(GAME, true, GAME_CURSOR, engine.statc[2]);
		}

		return change;
	}

	/**
	 * NET: Retry key
	 */
	@Override
	public void netplayOnRetryKey(GameEngine engine, int playerID) {
		if(netIsNetPlay && !netIsWatch) {
			owner.reset();
//			netLobby.netPlayerClient.send("reset1p\n");
			knetClient.fire(RESET_1P, true);
			netSendOptions(engine);
		}
	}

	/*
	 * NET: Message received
	 */
	@Override
	public void knetEvented(KNetClient client, KNetEvent e) {
		// Player status update
		if(e.is(PLAYER_UPDATE)) {
			netUpdatePlayerExist();
		}
		// When someone logout
		if(e.is(PLAYER_LOGOUT)) {
			KNetEventSource pInfo = (KNetEventSource) e.get(PLAYER_LOGOUT);

			if(channelInfo != null && channelInfo.getMembers().contains(pInfo)) {
				netUpdatePlayerExist();
			}
		}
		// Game started
		if(e.is(START)) {
			log.debug("NET: Game started");

			if(netIsWatch) {
				owner.reset();
				owner.engine[0].stat = GameEngine.STAT_READY;
				owner.engine[0].resetStatc();
			}
		}
		// Dead
		if(e.is(DEAD)) {
			log.debug("NET: Dead");

			if(netIsWatch) {
				owner.engine[0].gameEnded();

				if((owner.engine[0].stat != GameEngine.STAT_GAMEOVER) && (owner.engine[0].stat != GameEngine.STAT_RESULT)) {
					owner.engine[0].stat = GameEngine.STAT_GAMEOVER;
					owner.engine[0].resetStatc();
				}
			}
		}
		// Replay send fail
		if(message[0].equals("spsendng")) {
			netReplaySendStatus = 1;
			netSendReplay(owner.engine[0]);
		}
		// Replay send complete
		if(message[0].equals("spsendok")) {
			netReplaySendStatus = 2;
			netRankingRank[0] = Integer.parseInt(message[1]);
			netIsPB = Boolean.parseBoolean(message[2]);
			netRankingRank[1] = Integer.parseInt(message[3]);
		}
		// Netplay Ranking
		if(message[0].equals("spranking")) {
			netRecvNetPlayRanking(owner.engine[0], message);
		}
		// Reset
		if(e.is(RESET_1P)) {
			if(netIsWatch) {
				owner.reset();
			}
		}
		// Game messages
		if(e.is(GAME)) {
			if(netIsWatch) {
				GameEngine engine = owner.engine[0];
				if(engine.field == null) {
					engine.field = new Field();
				}

				// Move cursor
				if(e.is(GAME_CURSOR)) {
					if(engine.stat == GameEngine.STAT_SETTING) {
						engine.statc[2] = (Integer) e.get(GAME_CURSOR);
						engine.playSE("cursor");
					}
				}
				// Change game options
				if(e.is(GAME_OPTIONS)) {
					netRecvOptions(engine, e);
				}
				// Field
				if(e.is(GAME_FIELD)) {
					netRecvField(engine, e);
				}
				// Stats
				if(e.is(GAME_STATS)) {
					netRecvStats(engine, e);
				}
				// Current Piece
				if(e.is(GAME_PIECE_MOVEMENT)) {
					netRecvPieceMovement(engine, e);
				}
				// Next and Hold
				if(e.is(GAME_NEXT_PIECE)) {
					netRecvNextAndHold(engine, e);
				}
				// Ending
				if(e.is(GAME_ENDING)) {
					engine.ending = 1;
					if(!engine.staffrollEnable) engine.gameEnded();
					engine.stat = GameEngine.STAT_ENDINGSTART;
					engine.resetStatc();
				}
				// Excellent
				if(e.is(GAME_EXCELLENT)) {
					engine.stat = GameEngine.STAT_EXCELLENT;
					engine.resetStatc();
				}
				// Retry
				if(e.is(GAME_RETRY)) {
					engine.ending = 0;
					engine.gameEnded();
					engine.stat = GameEngine.STAT_SETTING;
					engine.resetStatc();
					engine.playSE("decide");
				}
				// Display results screen
				if(e.is(GAME_RESULTS_SCREEN)) {
					engine.field.reset();
					engine.stat = GameEngine.STAT_RESULT;
					engine.resetStatc();
				}
			}
		}
		
		if(isSynchronousPlay()) {
			GameEngine eng = owner.engine[0];
			eng.synchronousIncrement = channelInfo.getPlayers().size();
			if(e.is(GAME)) {
				if(e.is(GAME_SYNCHRONOUS)) {
					if(e.is(GAME_SYNCHRONOUS_LOCKED)) {
						eng.synchronousSync.decrementAndGet();
					}
				}
				if(e.is(GAME_RESULTS_SCREEN)) {
					eng.synchronousSync.set(0);
				}
			}
			if(e.is(PLAYER_LOGOUT)) {
				eng.synchronousSync.decrementAndGet();
			}
			if(e.is(DEAD)) {
				eng.synchronousSync.decrementAndGet();
			}
		}
	}

	/*
	 * NET: When the lobby window is closed
	 */
	public void netlobbyOnExit(KNetFrame lobby) {
		try {
			for(int i = 0; i < owner.engine.length; i++) {
				owner.engine[i].quitflag = true;
			}
		} catch (Exception e) {}
	}

	/**
	 * NET: When you join the room
	 * @param lobby NetLobbyFrame
	 * @param client NetPlayerClient
	 * @param roomInfo NetRoomInfo
	 */
	protected void netOnJoin(KNetClient client, KSChannelInfo roomInfo) {
		log.debug("onJoin on NetDummyMode");

		channelInfo = roomInfo;
		netIsNetPlay = true;
		netIsWatch = !channelInfo.getPlayers().contains(knetClient.getSource());
		netNumSpectators = 0;
		netUpdatePlayerExist();

		if(netIsWatch) {
			owner.engine[0].isNextVisible = false;
			owner.engine[0].isHoldVisible = false;
		}

		if(roomInfo != null) {
			// Set to locked rule
			if((roomInfo.ruleLock) && (netLobby != null) && (netLobby.ruleOptLock != null)) {
				log.info("Set locked rule");
				Randomizer randomizer = GeneralUtil.loadRandomizer(netLobby.ruleOptLock.strRandomizer);
				Wallkick wallkick = GeneralUtil.loadWallkick(netLobby.ruleOptLock.strWallkick);
				owner.engine[0].ruleopt.copy(netLobby.ruleOptLock);
				owner.engine[0].randomizer = randomizer;
				owner.engine[0].wallkick = wallkick;
				loadRanking(owner.modeConfig, owner.engine[0].ruleopt.strRuleName);
			}
		}
	}

	/**
	 * NET: Read rankings from property file. This is used from netOnJoin.
	 * @param prop Property file
	 * @param ruleName Rule name
	 */
	protected void loadRanking(CustomProperties prop, String ruleName) {
	}

	/**
	 * NET: Update player count
	 */
	protected void netUpdatePlayerExist() {
		netNumSpectators = 0;
		netPlayerName = "";

//		if((channelInfo != null) && (channelInfo.roomID != -1) && (netLobby != null)) {
//			for(NetPlayerInfo pInfo: netLobby.updateSameRoomPlayerInfoList()) {
//				if(pInfo.roomID == channelInfo.roomID) {
//					if(pInfo.seatID == 0) {
//						netPlayerName = pInfo.strName;
//					} else if(pInfo.seatID == -1) {
//						netNumSpectators++;
//					}
//				}
//			}
//		}
		
		netNumSpectators = channelInfo.getMembers().size() - channelInfo.getPlayers().size();
		netPlayerName = knetClient.getSource().getName();
	}

	/**
	 * NET: Draw number of players to bottom-right of screen.
	 * This subroutine uses "netLobby" and "owner" variables.
	 * @param engine GameEngine
	 */
	protected void netDrawAllPlayersCount(GameEngine engine) {
		if((netLobby != null) && (netLobby.netPlayerClient != null) && (netLobby.netPlayerClient.isConnected())) {
			int fontcolor = EventRenderer.COLOR_BLUE;
			if(netLobby.netPlayerClient.getObserverCount() > 0) fontcolor = EventRenderer.COLOR_GREEN;
			if(netLobby.netPlayerClient.getPlayerCount() > 1) fontcolor = EventRenderer.COLOR_RED;
			String strObserverInfo = String.format("%d/%d", netLobby.netPlayerClient.getObserverCount(), netLobby.netPlayerClient.getPlayerCount());
			String strObserverString = String.format("%40s", strObserverInfo);
			owner.receiver.drawDirectFont(engine, 0, 0, 480-16, strObserverString, fontcolor);
		}
	}

	/**
	 * NET: Draw game-rate to bottom-right of screen.
	 * @param engine GameEngine
	 */
	protected void netDrawGameRate(GameEngine engine) {
		if(netIsNetPlay && !netIsWatch && engine.gameStarted && (engine.startTime != 0)) {
			float gamerate = 0f;
			if(engine.endTime != 0) {
				gamerate = engine.statistics.gamerate;
			} else {
				long nowtime = System.nanoTime();
				gamerate = (float)(engine.replayTimer / (0.00000006*(nowtime-engine.startTime)));
			}

			String strTemp = String.format("%.0f%%", (float)(gamerate * 100f));
			String strTemp2 = String.format("%40s", strTemp);

			int fontcolor = EventRenderer.COLOR_BLUE;
			if(gamerate < 1f) fontcolor = EventRenderer.COLOR_YELLOW;
			if(gamerate < 0.9f) fontcolor = EventRenderer.COLOR_ORANGE;
			if(gamerate < 0.8f) fontcolor = EventRenderer.COLOR_RED;
			owner.receiver.drawDirectFont(engine, 0, 0, 480-32, strTemp2, fontcolor);
		}
	}

	/**
	 * NET: Draw spectator count in score area.
	 * @param engine GameEngine
	 * @param x X offset
	 * @param y Y offset
	 */
	protected void netDrawSpectatorsCount(GameEngine engine, int x, int y) {
		if(netIsNetPlay) {
			int fontcolor = netIsWatch ? EventRenderer.COLOR_GREEN : EventRenderer.COLOR_RED;
			owner.receiver.drawScoreFont(engine, engine.getPlayerID(), x, y+0, "SPECTATORS", fontcolor);
			owner.receiver.drawScoreFont(engine, engine.getPlayerID(), x, y+1, "" + netNumSpectators, EventRenderer.COLOR_WHITE);

			if(engine.stat == GameEngine.STAT_SETTING && !netIsWatch && netIsNetRankingViewOK(engine)) {
				int y2 = y + 2;
				if(y2 > 24) y2 = 24;
				String strBtnD = engine.getOwner().receiver.getKeyNameByButtonID(engine, Controller.BUTTON_D);
				owner.receiver.drawScoreFont(engine, engine.getPlayerID(), x, y2, "D(" + strBtnD + " KEY):\n NET RANKING", EventRenderer.COLOR_GREEN);
			}
		}
	}

	/**
	 * NET: Draw player's name. It may also appear in offline replay.
	 * @param engine GameEngine
	 */
	protected void netDrawPlayerName(GameEngine engine) {
		if((netPlayerName != null) && (netPlayerName.length() > 0)) {
			String name = netPlayerName;
			owner.receiver.drawTTFDirectFont(
					engine, engine.getPlayerID(),
					owner.receiver.getFieldDisplayPositionX(engine, engine.getPlayerID()),
					owner.receiver.getFieldDisplayPositionY(engine, engine.getPlayerID()) - 20,
					name);
		}
	}

	/**
	 * NET: Send the current piece's movement to all spectators.
	 * @param engine GameEngine
	 * @param forceSend <code>true</code> to force send a message
	 *        (if <code>false</code>, it won't send a message unless there is a movement)
	 * @return <code>true</code> if the message is sent
	 */
	protected boolean netSendPieceMovement(GameEngine engine, boolean forceSend) {
		if( ((engine.nowPieceObject == null) && (netPrevPieceID != Piece.PIECE_NONE)) || (engine.manualLock) )
		{
			
			PieceMovement pm = new PieceMovement();
			pm.setPieceId(netPrevPieceID);
			pm.setX(netPrevPieceX);
			pm.setY(netPrevPieceY);
			pm.setDirection(netPrevPieceDir);
			pm.setSkin(engine.getSkin());
			
			knetClient.fireUDP(knetClient.event(
					GAME, true,
					GAME_PIECE_MOVEMENT, pm));
			
			return true;
		}
		else if((engine.nowPieceObject.id != netPrevPieceID) || (engine.nowPieceX != netPrevPieceX) ||
				(engine.nowPieceY != netPrevPieceY) || (engine.nowPieceObject.direction != netPrevPieceDir) ||
				(forceSend))
		{
			netPrevPieceID = engine.nowPieceObject.id;
			netPrevPieceX = engine.nowPieceX;
			netPrevPieceY = engine.nowPieceY;
			netPrevPieceDir = engine.nowPieceObject.direction;

			int x = netPrevPieceX + engine.nowPieceObject.dataOffsetX[netPrevPieceDir];
			int y = netPrevPieceY + engine.nowPieceObject.dataOffsetY[netPrevPieceDir];
			
			PieceMovement pm = new PieceMovement();
			pm.setPieceId(netPrevPieceID);
			pm.setX(x);
			pm.setY(y);
			pm.setDirection(netPrevPieceDir);
			pm.setBottomY(engine.nowPieceBottomY);
			pm.setColor(engine.ruleopt.pieceColor[netPrevPieceID]);
			pm.setSkin(engine.getSkin());
			pm.setBig(engine.nowPieceObject.big);
			
			knetClient.fireUDP(knetClient.event(
					GAME, true,
					GAME_PIECE_MOVEMENT, pm));
			
			return true;
		}
		return false;
	}

	/**
	 * NET: Receive the current piece's movement. You can override it if you customize "piece" message.
	 * @param engine GameEngine
	 * @param message Message array
	 */
	protected void netRecvPieceMovement(GameEngine engine, KNetEvent e) {
		if(!e.is(GAME) || !e.is(GAME_PIECE_MOVEMENT))
			return;
		
		PieceMovement pm = (PieceMovement) e.get(GAME_PIECE_MOVEMENT);
		
		int id = pm.getPieceId();

		if(id >= 0) {
			int pieceX = pm.getX();;
			int pieceY = pm.getY();
			int pieceDir = pm.getDirection();
			int pieceBottomY = pm.getBottomY();
			int pieceColor = pm.getColor();
			int pieceSkin = pm.getSkin();
			boolean pieceBig = pm.isBig();

			engine.nowPieceObject = new Piece(id);
			engine.nowPieceObject.direction = pieceDir;
			engine.nowPieceObject.setAttribute(Block.BLOCK_ATTRIBUTE_VISIBLE, true);
			engine.nowPieceObject.setColor(pieceColor);
			engine.nowPieceObject.setSkin(pieceSkin);
			engine.nowPieceX = pieceX;
			engine.nowPieceY = pieceY;
			//engine.nowPieceBottomY = pieceBottomY;
			engine.nowPieceObject.big = pieceBig;
			engine.nowPieceObject.updateConnectData();
			engine.nowPieceBottomY =
				engine.nowPieceObject.getBottom(pieceX, pieceY, engine.field);

			if((engine.stat != GameEngine.STAT_EXCELLENT) && (engine.stat != GameEngine.STAT_GAMEOVER) &&
			   (engine.stat != GameEngine.STAT_RESULT))
			{
				engine.gameActive = true;
				engine.timerActive = true;
				engine.stat = GameEngine.STAT_MOVE;
				engine.statc[0] = 2;
			}

			netPlayerSkin = pieceSkin;
		} else {
			engine.nowPieceObject = null;
		}
	}

	/**
	 * NET: Send field to all spectators
	 * @param engine GameEngine
	 */
	protected void netSendField(GameEngine engine) {
		knetClient.fireUDP(GAME, true, GAME_FIELD, true, PAYLOAD, engine.field);
	}

	/**
	 * NET: Receive field message
	 * @param engine GameEngine
	 * @param message Message array
	 */
	protected void netRecvField(GameEngine engine, KNetEvent e) {
		engine.field = (Field) e.get(PAYLOAD);
	}

	/**
	 * NET: Send next and hold piece informations to all spectators
	 * @param engine GameEngine
	 */
	protected void netSendNextAndHold(GameEngine engine) {
		PieceHold hold = new PieceHold();
		hold.setPiece(engine.holdPieceObject);
		hold.setDisableHold(engine.holdDisable);
		
		Piece[] next = new Piece[engine.ruleopt.nextDisplay];
		for(int i = 0; i < next.length; i++) {
			next[i] = engine.getNextObject(engine.nextPieceCount + i);
		}

		knetClient.fireUDP(GAME, true, GAME_HOLD_PIECE, hold, GAME_NEXT_PIECE, next);
	}

	/**
	 * NET: Receive next and hold piece informations
	 * @param engine GameEngine
	 * @param message Message array
	 */
	protected void netRecvNextAndHold(GameEngine engine, KNetEvent e) {
		if(!e.is(GAME) || !e.is(GAME_HOLD_PIECE) || !e.is(GAME_NEXT_PIECE))
			return;
		
		PieceHold hold = (PieceHold) e.get(GAME_HOLD_PIECE);
		Piece[] next = (Piece[]) e.get(GAME_NEXT_PIECE);
		
		int maxNext = next.length;
		engine.ruleopt.nextDisplay = maxNext;
		engine.holdDisable = hold.isDisableHold();

		engine.holdPieceObject = hold.getPiece();
		
		if(engine.nextPieceArrayObject == null || engine.nextPieceArrayObject.length < maxNext)
			engine.nextPieceArrayObject = new Piece[maxNext];
		System.arraycopy(next, 0, engine.nextPieceArrayObject, 0, maxNext);

		engine.isNextVisible = true;
		engine.isHoldVisible = true;
	}

	/**
	 * Menu routine for 1P NetPlay online ranking screen. Usually called from onSetting(engine, playerID).
	 * @param engine GameEngine
	 * @param goaltype Goal Type
	 */
	protected void netOnUpdateNetPlayRanking(GameEngine engine, int goaltype) {
		if(netIsNetRankingDisplayMode) {
			int d = netRankingView;

			if(!netRankingNoDataFlag[d] && netRankingReady[d] && (netRankingPlace != null) && (netRankingPlace[d] != null)) {
				// Up
				if(engine.ctrl.isMenuRepeatKey(Controller.BUTTON_UP)) {
					netRankingCursor[d]--;
					if(netRankingCursor[d] < 0) netRankingCursor[d] = netRankingPlace[d].size() - 1;
					engine.playSE("cursor");
				}
				// Down
				if(engine.ctrl.isMenuRepeatKey(Controller.BUTTON_DOWN)) {
					netRankingCursor[d]++;
					if(netRankingCursor[d] > netRankingPlace[d].size() - 1) netRankingCursor[d] = 0;
					engine.playSE("cursor");
				}
				// Download
				if(engine.ctrl.isPush(Controller.BUTTON_A)) {
					engine.playSE("decide");
					String strMsg = "spdownload\t" + NetUtil.urlEncode(channelInfo.ruleName) + "\t" +
									NetUtil.urlEncode(getName()) + "\t" + goaltype + "\t" +
									(netRankingView != 0) + "\t" + NetUtil.urlEncode(netRankingName[d].get(netRankingCursor[d])) + "\n";
					netLobby.netPlayerClient.send(strMsg);
					netIsNetRankingDisplayMode = false;
					owner.menuOnly = false;
				}
			}

			// Left/Right
			if(engine.ctrl.isPush(Controller.BUTTON_LEFT) || engine.ctrl.isPush(Controller.BUTTON_RIGHT)) {
				if(netRankingView == 0) netRankingView = 1;
				else netRankingView = 0;
				engine.playSE("change");
			}

			// Exit
			if(engine.ctrl.isPush(Controller.BUTTON_B)) {
				netIsNetRankingDisplayMode = false;
				owner.menuOnly = false;
			}
		}
	}

	/**
	 * Render 1P NetPlay online ranking screen. Usually called from renderSetting(engine, playerID).
	 * @param engine GameEngine
	 * @param playerID Player ID
	 * @param receiver EventReceiver
	 */
	protected void netOnRenderNetPlayRanking(GameEngine engine, int playerID, EventRenderer receiver) {
		if(netIsNetRankingDisplayMode) {
			String strBtnA = receiver.getKeyNameByButtonID(engine, Controller.BUTTON_A);
			String strBtnB = receiver.getKeyNameByButtonID(engine, Controller.BUTTON_B);

			int d = netRankingView;

			if(!netRankingNoDataFlag[d] && netRankingReady[d] && (netRankingPlace != null) && (netRankingPlace[d] != null)) {
				receiver.drawMenuFont(engine, playerID, 0, 1, "<<", EventRenderer.COLOR_ORANGE);
				receiver.drawMenuFont(engine, playerID, 38, 1, ">>", EventRenderer.COLOR_ORANGE);
				receiver.drawMenuFont(engine, playerID, 3, 1,
						((d != 0) ? "DAILY" : "ALL-TIME") + " RANKING (" + (netRankingCursor[d]+1) + "/" + netRankingPlace[d].size() + ")",
						EventRenderer.COLOR_GREEN);

				int startIndex = (netRankingCursor[d] / 20) * 20;
				int endIndex = startIndex + 20;
				if(endIndex > netRankingPlace[d].size()) endIndex = netRankingPlace[d].size();
				int c = 0;

				if(netRankingType == NetSPRecord.RANKINGTYPE_GENERIC_SCORE) {
					receiver.drawMenuFont(engine, playerID, 1, 3, "    SCORE   LINE TIME     NAME", EventRenderer.COLOR_BLUE);
				} else if(netRankingType == NetSPRecord.RANKINGTYPE_GENERIC_TIME) {
					receiver.drawMenuFont(engine, playerID, 1, 3, "    TIME     PIECE PPS    NAME", EventRenderer.COLOR_BLUE);
				} else if(netRankingType == NetSPRecord.RANKINGTYPE_SCORERACE) {
					receiver.drawMenuFont(engine, playerID, 1, 3, "    TIME     LINE SPL    NAME", EventRenderer.COLOR_BLUE);
				} else if(netRankingType == NetSPRecord.RANKINGTYPE_DIGRACE) {
					receiver.drawMenuFont(engine, playerID, 1, 3, "    TIME     LINE PIECE  NAME", EventRenderer.COLOR_BLUE);
				} else if(netRankingType == NetSPRecord.RANKINGTYPE_ULTRA) {
					receiver.drawMenuFont(engine, playerID, 1, 3, "    SCORE   LINE PIECE    NAME", EventRenderer.COLOR_BLUE);
				} else if(netRankingType == NetSPRecord.RANKINGTYPE_COMBORACE) {
					receiver.drawMenuFont(engine, playerID, 1, 3, "    COMBO TIME     PPS    NAME", EventRenderer.COLOR_BLUE);
				} else if(netRankingType == NetSPRecord.RANKINGTYPE_DIGCHALLENGE) {
					receiver.drawMenuFont(engine, playerID, 1, 3, "    SCORE   LINE TIME     NAME", EventRenderer.COLOR_BLUE);
				} else if(netRankingType == NetSPRecord.RANKINGTYPE_TIMEATTACK) {
					receiver.drawMenuFont(engine, playerID, 1, 3, "    LINE  TIME     PPS    NAME", EventRenderer.COLOR_BLUE);
				}

				for(int i = startIndex; i < endIndex; i++) {
					if(i == netRankingCursor[d]) {
						receiver.drawMenuFont(engine, playerID, 0, 4 + c, "b", EventRenderer.COLOR_RED);
					}

					int rankColor = (i == netRankingMyRank[d]) ? EventRenderer.COLOR_PINK : EventRenderer.COLOR_YELLOW;
					if(netRankingPlace[d].get(i) == -1) {
						receiver.drawMenuFont(engine, playerID, 1, 4 + c, "N/A", rankColor);
					} else {
						receiver.drawMenuFont(engine, playerID, 1, 4 + c, String.format("%3d", netRankingPlace[d].get(i)+1), rankColor);
					}

					if(netRankingType == NetSPRecord.RANKINGTYPE_GENERIC_SCORE) {
						receiver.drawMenuFont(engine, playerID, 5, 4 + c, "" + netRankingScore[d].get(i), (i == netRankingCursor[d]));
						receiver.drawMenuFont(engine, playerID, 13, 4 + c, "" + netRankingLines[d].get(i), (i == netRankingCursor[d]));
						receiver.drawMenuFont(engine, playerID, 18, 4 + c, GeneralUtil.getTime(netRankingTime[d].get(i)), (i == netRankingCursor[d]));
						receiver.drawTTFMenuFont(engine, playerID, 27, 4 + c, netRankingName[d].get(i), (i == netRankingCursor[d]));
					} else if(netRankingType == NetSPRecord.RANKINGTYPE_GENERIC_TIME) {
						receiver.drawMenuFont(engine, playerID, 5, 4 + c, GeneralUtil.getTime(netRankingTime[d].get(i)), (i == netRankingCursor[d]));
						receiver.drawMenuFont(engine, playerID, 14, 4 + c, "" + netRankingPiece[d].get(i), (i == netRankingCursor[d]));
						receiver.drawMenuFont(engine, playerID, 20, 4 + c, String.format("%.5g", netRankingPPS[d].get(i)), (i == netRankingCursor[d]));
						receiver.drawTTFMenuFont(engine, playerID, 27, 4 + c, netRankingName[d].get(i), (i == netRankingCursor[d]));
					} else if(netRankingType == NetSPRecord.RANKINGTYPE_SCORERACE) {
						receiver.drawMenuFont(engine, playerID, 5, 4 + c, GeneralUtil.getTime(netRankingTime[d].get(i)), (i == netRankingCursor[d]));
						receiver.drawMenuFont(engine, playerID, 14, 4 + c, "" + netRankingLines[d].get(i), (i == netRankingCursor[d]));
						receiver.drawMenuFont(engine, playerID, 19, 4 + c, String.format("%.5g", netRankingSPL[d].get(i)), (i == netRankingCursor[d]));
						receiver.drawTTFMenuFont(engine, playerID, 26, 4 + c, netRankingName[d].get(i), (i == netRankingCursor[d]));
					} else if(netRankingType == NetSPRecord.RANKINGTYPE_DIGRACE) {
						receiver.drawMenuFont(engine, playerID, 5, 4 + c, GeneralUtil.getTime(netRankingTime[d].get(i)), (i == netRankingCursor[d]));
						receiver.drawMenuFont(engine, playerID, 14, 4 + c, "" + netRankingLines[d].get(i), (i == netRankingCursor[d]));
						receiver.drawMenuFont(engine, playerID, 19, 4 + c, "" + netRankingPiece[d].get(i), (i == netRankingCursor[d]));
						receiver.drawTTFMenuFont(engine, playerID, 26, 4 + c, netRankingName[d].get(i), (i == netRankingCursor[d]));
					} else if(netRankingType == NetSPRecord.RANKINGTYPE_ULTRA) {
						receiver.drawMenuFont(engine, playerID, 5, 4 + c, "" + netRankingScore[d].get(i), (i == netRankingCursor[d]));
						receiver.drawMenuFont(engine, playerID, 13, 4 + c, "" + netRankingLines[d].get(i), (i == netRankingCursor[d]));
						receiver.drawMenuFont(engine, playerID, 18, 4 + c, "" + netRankingPiece[d].get(i), (i == netRankingCursor[d]));
						receiver.drawTTFMenuFont(engine, playerID, 27, 4 + c, netRankingName[d].get(i), (i == netRankingCursor[d]));
					} else if(netRankingType == NetSPRecord.RANKINGTYPE_COMBORACE) {
						receiver.drawMenuFont(engine, playerID, 5, 4 + c, "" + (netRankingScore[d].get(i) - 1), (i == netRankingCursor[d]));
						receiver.drawMenuFont(engine, playerID, 11, 4 + c, GeneralUtil.getTime(netRankingTime[d].get(i)), (i == netRankingCursor[d]));
						receiver.drawMenuFont(engine, playerID, 20, 4 + c, String.format("%.4g", netRankingPPS[d].get(i)), (i == netRankingCursor[d]));
						receiver.drawTTFMenuFont(engine, playerID, 27, 4 + c, netRankingName[d].get(i), (i == netRankingCursor[d]));
					} else if(netRankingType == NetSPRecord.RANKINGTYPE_DIGCHALLENGE) {
						receiver.drawMenuFont(engine, playerID, 5, 4 + c, "" + netRankingScore[d].get(i), (i == netRankingCursor[d]));
						receiver.drawMenuFont(engine, playerID, 13, 4 + c, "" + netRankingLines[d].get(i), (i == netRankingCursor[d]));
						receiver.drawMenuFont(engine, playerID, 18, 4 + c, GeneralUtil.getTime(netRankingTime[d].get(i)), (i == netRankingCursor[d]));
						receiver.drawTTFMenuFont(engine, playerID, 27, 4 + c, netRankingName[d].get(i), (i == netRankingCursor[d]));
					} else if(netRankingType == NetSPRecord.RANKINGTYPE_TIMEATTACK) {
						int fontcolor = EventRenderer.COLOR_WHITE;
						if(netRankingRollclear[d].get(i) == 1) fontcolor = EventRenderer.COLOR_GREEN;
						if(netRankingRollclear[d].get(i) == 2) fontcolor = EventRenderer.COLOR_ORANGE;
						receiver.drawMenuFont(engine, playerID, 5, 4 + c, "" + netRankingLines[d].get(i), fontcolor);
						receiver.drawMenuFont(engine, playerID, 11, 4 + c, GeneralUtil.getTime(netRankingTime[d].get(i)), (i == netRankingCursor[d]));
						receiver.drawMenuFont(engine, playerID, 20, 4 + c, String.format("%.4g", netRankingPPS[d].get(i)), (i == netRankingCursor[d]));
						receiver.drawTTFMenuFont(engine, playerID, 27, 4 + c, netRankingName[d].get(i), (i == netRankingCursor[d]));
					}

					c++;
				}

				if((netRankingCursor[d] >= 0) && (netRankingCursor[d] < netRankingDate[d].size())) {
					String strDate = "----/--/-- --:--:--";
					Calendar calendar = netRankingDate[d].get(netRankingCursor[d]);
					if(calendar != null) {
						strDate = GeneralUtil.getCalendarString(calendar, TimeZone.getDefault());
					}
					receiver.drawMenuFont(engine, playerID, 1, 25, "DATE:" + strDate, EventRenderer.COLOR_CYAN);

					float gamerate = netRankingGamerate[d].get(netRankingCursor[d]);
					receiver.drawMenuFont(engine, playerID, 1, 26, "GAMERATE:" + ((gamerate == 0f) ? "UNKNOWN" : (100*gamerate)+"%"),
							EventRenderer.COLOR_CYAN);
				}

				receiver.drawMenuFont(engine, playerID, 1, 27,
						"A(" + strBtnA + " KEY):DOWNLOAD\nB(" + strBtnB + " KEY):BACK LEFT/RIGHT:" + ((d == 0) ? "DAILY" : "ALL-TIME"),
						EventRenderer.COLOR_ORANGE);
			} else if(netRankingNoDataFlag[d]) {
				receiver.drawMenuFont(engine, playerID, 0, 1, "<<", EventRenderer.COLOR_ORANGE);
				receiver.drawMenuFont(engine, playerID, 38, 1, ">>", EventRenderer.COLOR_ORANGE);
				receiver.drawMenuFont(engine, playerID, 3, 1,
						((d != 0) ? "DAILY" : "ALL-TIME") + " RANKING",
						EventRenderer.COLOR_GREEN);

				receiver.drawMenuFont(engine, playerID, 1, 3, "NO DATA", EventRenderer.COLOR_DARKBLUE);

				receiver.drawMenuFont(engine, playerID, 1, 28, "B(" + strBtnB + " KEY):BACK LEFT/RIGHT:" + ((d == 0) ? "DAILY" : "ALL-TIME"),
						EventRenderer.COLOR_ORANGE);
			} else if(!netRankingReady[d] && (netRankingPlace == null) || (netRankingPlace[d] == null)) {
				receiver.drawMenuFont(engine, playerID, 0, 1, "<<", EventRenderer.COLOR_ORANGE);
				receiver.drawMenuFont(engine, playerID, 38, 1, ">>", EventRenderer.COLOR_ORANGE);
				receiver.drawMenuFont(engine, playerID, 3, 1,
						((d != 0) ? "DAILY" : "ALL-TIME") + " RANKING",
						EventRenderer.COLOR_GREEN);

				receiver.drawMenuFont(engine, playerID, 1, 3, "LOADING...", EventRenderer.COLOR_CYAN);

				receiver.drawMenuFont(engine, playerID, 1, 28, "B(" + strBtnB + " KEY):BACK LEFT/RIGHT:" + ((d == 0) ? "DAILY" : "ALL-TIME"),
						EventRenderer.COLOR_ORANGE);
			}
		}
	}

	/**
	 * Enter the netplay ranking screen
	 * @param engine GameEngine
	 * @param playerID Player ID
	 * @param goaltype Game Type
	 */
	protected void netEnterNetPlayRankingScreen(GameEngine engine, int playerID, int goaltype) {
		if(netRankingPlace != null) {
			netRankingPlace[0] = null;
			netRankingPlace[1] = null;
		}
		netRankingCursor[0] = 0;
		netRankingCursor[1] = 0;
		netRankingMyRank[0] = -1;
		netRankingMyRank[1] = -1;
		netIsNetRankingDisplayMode = true;
		owner.menuOnly = true;
		String rule = (channelInfo.rated ? channelInfo.ruleName : "all");
		netLobby.netPlayerClient.send("spranking\t" + NetUtil.urlEncode(rule) + "\t" +
				NetUtil.urlEncode(getName()) + "\t" + goaltype + "\t" + false + "\n");
		netLobby.netPlayerClient.send("spranking\t" + NetUtil.urlEncode(rule) + "\t" +
				NetUtil.urlEncode(getName()) + "\t" + goaltype + "\t" + true + "\n");
	}

	/**
	 * Receive 1P NetPlay ranking.
	 * @param engine GameEngine
	 * @param message Message array
	 */
	protected void netRecvNetPlayRanking(GameEngine engine, String[] message) {
		String strDebugTemp = "";
		for(int i = 0; i < message.length; i++) {
			strDebugTemp += message[i] + " ";
		}
		log.debug(strDebugTemp);

		if(message.length > 7) {
			boolean isDaily = Boolean.parseBoolean(message[4]);
			int d = isDaily ? 1 : 0;

			netRankingType = Integer.parseInt(message[5]);
			int maxRecords = Integer.parseInt(message[6]);
			String[] arrayRow = message[7].split(";");
			maxRecords = Math.min(maxRecords, arrayRow.length);

			netRankingNoDataFlag[d] = false;
			netRankingReady[d] = false;
			netRankingPlace[d] = new LinkedList<Integer>();
			netRankingName[d] = new LinkedList<String>();
			netRankingDate[d] = new LinkedList<Calendar>();
			netRankingGamerate[d] = new LinkedList<Float>();
			netRankingTime[d] = new LinkedList<Integer>();
			netRankingScore[d] = new LinkedList<Integer>();
			netRankingPiece[d] = new LinkedList<Integer>();
			netRankingPPS[d] = new LinkedList<Float>();
			netRankingLines[d] = new LinkedList<Integer>();
			netRankingSPL[d] = new LinkedList<Double>();
			netRankingRollclear[d] = new LinkedList<Integer>();

			for(int i = 0; i < maxRecords; i++) {
				String[] arrayData = arrayRow[i].split(",");
				netRankingPlace[d].add(Integer.parseInt(arrayData[0]));
				String pName = NetUtil.urlDecode(arrayData[1]);
				netRankingName[d].add(pName);
				netRankingDate[d].add(GeneralUtil.importCalendarString(arrayData[2]));
				netRankingGamerate[d].add(Float.parseFloat(arrayData[3]));

				if(netRankingType == NetSPRecord.RANKINGTYPE_GENERIC_SCORE) {
					netRankingScore[d].add(Integer.parseInt(arrayData[4]));
					netRankingLines[d].add(Integer.parseInt(arrayData[5]));
					netRankingTime[d].add(Integer.parseInt(arrayData[6]));
				} else if(netRankingType == NetSPRecord.RANKINGTYPE_GENERIC_TIME) {
					netRankingTime[d].add(Integer.parseInt(arrayData[4]));
					netRankingPiece[d].add(Integer.parseInt(arrayData[5]));
					netRankingPPS[d].add(Float.parseFloat(arrayData[6]));
				} else if(netRankingType == NetSPRecord.RANKINGTYPE_SCORERACE) {
					netRankingTime[d].add(Integer.parseInt(arrayData[4]));
					netRankingLines[d].add(Integer.parseInt(arrayData[5]));
					netRankingSPL[d].add(Double.parseDouble(arrayData[6]));
				} else if(netRankingType == NetSPRecord.RANKINGTYPE_DIGRACE) {
					netRankingTime[d].add(Integer.parseInt(arrayData[4]));
					netRankingLines[d].add(Integer.parseInt(arrayData[5]));
					netRankingPiece[d].add(Integer.parseInt(arrayData[6]));
				} else if(netRankingType == NetSPRecord.RANKINGTYPE_ULTRA) {
					netRankingScore[d].add(Integer.parseInt(arrayData[4]));
					netRankingLines[d].add(Integer.parseInt(arrayData[5]));
					netRankingPiece[d].add(Integer.parseInt(arrayData[6]));
				} else if(netRankingType == NetSPRecord.RANKINGTYPE_COMBORACE) {
					netRankingScore[d].add(Integer.parseInt(arrayData[4]));
					netRankingTime[d].add(Integer.parseInt(arrayData[5]));
					netRankingPPS[d].add(Float.parseFloat(arrayData[6]));
				} else if(netRankingType == NetSPRecord.RANKINGTYPE_DIGCHALLENGE) {
					netRankingScore[d].add(Integer.parseInt(arrayData[4]));
					netRankingLines[d].add(Integer.parseInt(arrayData[5]));
					netRankingTime[d].add(Integer.parseInt(arrayData[6]));
				} else if(netRankingType == NetSPRecord.RANKINGTYPE_TIMEATTACK) {
					netRankingLines[d].add(Integer.parseInt(arrayData[4]));
					netRankingTime[d].add(Integer.parseInt(arrayData[5]));
					netRankingPPS[d].add(Float.parseFloat(arrayData[6]));
					netRankingRollclear[d].add(Integer.parseInt(arrayData[7]));
				} else {
					log.error("Unknown ranking type:" + netRankingType);
				}

				if(pName.equals(netPlayerName)) {
					netRankingCursor[d] = i;
					netRankingMyRank[d] = i;
				}
			}

			netRankingReady[d] = true;
		} else if(message.length > 4) {
			boolean isDaily = Boolean.parseBoolean(message[4]);
			int d = isDaily ? 1 : 0;
			netRankingNoDataFlag[d] = true;
			netRankingReady[d] = false;
		}
	}

	/**
	 * NET: Send various in-game stats (as well as goaltype)<br>
	 * Game modes should implement this.
	 * @param engine GameEngine
	 */
	protected void netSendStats(GameEngine engine) {
	}

	/**
	 * NET: Receive various in-game stats (as well as goaltype)<br>
	 * Game modes should implement this.
	 * @param engine GameEngine
	 * @param message Message array
	 */
	protected void netRecvStats(GameEngine engine, KNetEvent e) {
	}

	/**
	 * NET: Send end-of-game stats<br>
	 * Game modes should implement this.
	 * @param engine GameEngine
	 */
	protected void netSendEndGameStats(GameEngine engine) {
	}

	/**
	 * NET: Send game options to all spectators<br>
	 * Game modes should implement this.
	 * @param engine GameEngine
	 */
	protected void netSendOptions(GameEngine engine) {
	}

	/**
	 * NET: Receive game options.<br>
	 * Game modes should implement this.
	 * @param engine GameEngine
	 * @param message Message array
	 */
	protected void netRecvOptions(GameEngine engine, KNetEvent e) {
	}

	/**
	 * NET: Send replay data<br>
	 * Game modes should implement this. However, some basic codes are already implemented in NetDummyMode.
	 * @param engine GameEngine
	 */
	protected void netSendReplay(GameEngine engine) {
		if(netIsNetRankingSendOK(engine)) {
			NetSPRecord record = new NetSPRecord();
			record.setReplayProp(owner.replayProp);
			record.stats = new Statistics(engine.statistics);
			record.gameType = netGetGoalType();

			String strData = NetUtil.compressString(record.exportString());

			Adler32 checksumObj = new Adler32();
			checksumObj.update(NetUtil.stringToBytes(strData));
			long sChecksum = checksumObj.getValue();

			netLobby.netPlayerClient.send("spsend\t" + sChecksum + "\t" + strData + "\n");
		} else {
			netReplaySendStatus = 2;
		}
	}

	/**
	 * NET: Get goal type (used from the default implementation of netSendReplay)<br>
	 * Game modes should implement this, unless there is only 1 goal type.
	 * @return Goal type (default implementation will return 0)
	 */
	protected int netGetGoalType() {
		return 0;
	}

	/**
	 * NET: It returns <code>true</code> when the current settings doesn't prevent leaderboard screen from showing.
	 * Game modes should implement this. By default, this always returns false.
	 * @param engine GameEngine
	 * @return <code>true</code> when the current settings doesn't prevent leaderboard screen from showing.
	 */
	protected boolean netIsNetRankingViewOK(GameEngine engine) {
		return false;
	}

	/**
	 * NET: It returns <code>true</code> when the current settings doesn't prevent replay data from sending.
	 * By default, it just calls netIsNetRankingViewOK, but you should override it if you make "race" modes.
	 * @param engine GameEngine
	 * @return <code>true</code> when the current settings doesn't prevent replay data from sending.
	 */
	protected boolean netIsNetRankingSendOK(GameEngine engine) {
		return netIsNetRankingViewOK(engine);
	}
}

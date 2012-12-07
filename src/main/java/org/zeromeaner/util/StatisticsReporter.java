package org.zeromeaner.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.zeromeaner.game.component.Manipulation;
import org.zeromeaner.game.component.PiecePlacement;
import org.zeromeaner.game.component.Statistics;
import org.zeromeaner.game.component.Statistics.SpeedEntry;
import org.zeromeaner.game.play.GameEngine;
import org.zeromeaner.game.subsystem.mode.LineRaceMode;
import org.zeromeaner.gui.slick.NullpoMinoSlick;
import org.zeromeaner.util.fumen.FumenUtil;

public class StatisticsReporter {
	
	private static final String FUMEN_SERVER_FILE = "fumen.html"; //"http://fumen.zui.jp";

	static Logger log = Logger.getLogger(StatisticsReporter.class);
	
	private Statistics statistics;
	private GameEngine gameEngine;
	
	private double average;
	private double variance;
	private double stdev;
	
	private double idealFramesPerPiece;
	private double idealTime;
	
	private ArrayList<PiecePlacement> tooSlowPiecePlacements 	 = new ArrayList<PiecePlacement>();

	private String fumenUrlValue;
	private String fumenViewValue;
	private String fumenEncodedValue;
	
	private String username;
	private String password;
	
	public StatisticsReporter(Statistics statistics, GameEngine gameEngine) {
		super();
		this.statistics = statistics;
		this.gameEngine = gameEngine;
	}

	public void report(boolean netplay, boolean replay){
		calculateStatistics();
		
		String encodedFumenString = FumenUtil.getFumenUtil(gameEngine).encode();
		fumenUrlValue = FUMEN_SERVER_FILE + '?' + encodedFumenString;
		fumenViewValue = FUMEN_SERVER_FILE + "?m" + encodedFumenString.substring(1);
		fumenEncodedValue = encodedFumenString;
		
		StringBuilder report = createReport();
		writeToDisk(report);
		
		if (!netplay && !replay){
			postToServer();
		}
		
	}

	private void postToServer() {
		if (gameEngine.owner.mode instanceof LineRaceMode){
			LineRaceMode lineRaceMode = (LineRaceMode) gameEngine.owner.mode;
			int goal = LineRaceMode.GOAL_TABLE[lineRaceMode.goaltype];
		
		CustomProperties propGlobal = new CustomProperties();
		try {
			FileInputStream in = new FileInputStream("config/setting/global.cfg");
			propGlobal.load(in);
			in.close();
		} catch(IOException e) {}
		
		
		URL url;
		try {
			url = new URL("http://tetris.vanderhoven.be/upload/index.php");
			HttpURLConnection connection = (HttpURLConnection)url.openConnection();
			connection.setDoOutput(true);
			connection.setRequestProperty("Content-type","application/x-www-form-urlencoded");
			connection.setRequestMethod("POST");
			OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
			String data = URLEncoder.encode("player", "UTF-8") + "=" + URLEncoder.encode("0", "UTF-8");
			data += "&" + URLEncoder.encode("time", "UTF-8") + "=" + URLEncoder.encode(""+getStatistics().getTime(), "UTF-8");
			data += "&" + URLEncoder.encode("ppm", "UTF-8") + "=" + URLEncoder.encode(""+getStatistics().getPps()*60f, "UTF-8");
			data += "&" + URLEncoder.encode("pps", "UTF-8") + "=" + URLEncoder.encode(""+getStatistics().getPps(), "UTF-8");
			data += "&" + URLEncoder.encode("pieces", "UTF-8") + "=" + URLEncoder.encode(""+getStatistics().totalPieceLocked, "UTF-8");
			data += "&" + URLEncoder.encode("lines", "UTF-8") + "=" + URLEncoder.encode(""+getStatistics().lines, "UTF-8");
			data += "&" + URLEncoder.encode("hold", "UTF-8") + "=" + URLEncoder.encode(""+getStatistics().totalHoldUsed, "UTF-8");
			data += "&" + URLEncoder.encode("finesse", "UTF-8") + "=" + URLEncoder.encode(""+getStatistics().finesseDelta, "UTF-8");
			data += "&" + URLEncoder.encode("kpt", "UTF-8") + "=" + URLEncoder.encode(""+getStatistics().kpt, "UTF-8");
			data += "&" + URLEncoder.encode("ekpt", "UTF-8") + "=" + URLEncoder.encode(""+calculateEKPT(), "UTF-8");
			data += "&" + URLEncoder.encode("rotations", "UTF-8") + "=" + URLEncoder.encode(""+getStatistics().totalPieceRotate, "UTF-8");
			data += "&" + URLEncoder.encode("moves", "UTF-8") + "=" + URLEncoder.encode(""+getStatistics().totalPieceMoveWithDasAsOne, "UTF-8");
			data += "&" + URLEncoder.encode("idealtime", "UTF-8") + "=" + URLEncoder.encode(""+String.format("%.5g%n",getIdealTime()), "UTF-8");
			data += "&" + URLEncoder.encode("fumen", "UTF-8") + "=" + URLEncoder.encode(""+fumenEncodedValue, "UTF-8");
			data += "&" + URLEncoder.encode("frames", "UTF-8") + "=" + URLEncoder.encode(""+getTotalFramesUsed(), "UTF-8");
			data += "&" + URLEncoder.encode("frames_per_piece", "UTF-8") + "=" + URLEncoder.encode(String.format("%.2g%n",getIdealFramesPerPiece()), "UTF-8");
			data += "&" + URLEncoder.encode("ms_per_piece", "UTF-8") + "=" + URLEncoder.encode(""+String.format("%.4g%n",getAverage()/60d*1000d), "UTF-8");
			data += "&" + URLEncoder.encode("version", "UTF-8") + "=" + URLEncoder.encode("5.2", "UTF-8");
			
			data += "&" + URLEncoder.encode("username", "UTF-8") + "=" + URLEncoder.encode(propGlobal.getProperty("user.username"), "UTF-8");
			data += "&" + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(propGlobal.getProperty("user.password"), "UTF-8");

			data += "&" + URLEncoder.encode("goal", "UTF-8") + "=" + URLEncoder.encode(""+goal, "UTF-8");
			
			for (Manipulation manipulation : Manipulation.values()){
				data += "&" + URLEncoder.encode(manipulation.toString(), "UTF-8") + "=" + URLEncoder.encode(""+getStatistics().getManipulations().get(manipulation).size(), "UTF-8");
				
				ArrayList<Integer> manipulationTimes = getStatistics().getManipulations().get(manipulation);
				double averageTime = calculateAverageOf(manipulationTimes);
				data += "&" + URLEncoder.encode("TIME_"+manipulation.toString(), "UTF-8") + "=" + URLEncoder.encode(""+averageTime, "UTF-8");
			}			

			writer.write(data);
			writer.flush();
			
			String line;
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		    while ((line = reader.readLine()) != null) {
		      System.out.println(line);
		    }
	        writer.close(); 
	        reader.close(); 
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (ProtocolException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}    
		}
	}

	private void calculateStatistics() {
		try {
			calculateMean();
			calculateVariation();
			calculateStandardDeviation();
			calculateTrimmedAverage();
		} catch (Exception e){
			//just go on
		}
	}

	private void calculateTrimmedAverage() {
		double absStdev = Math.abs(getStdev());
		double upperbound = getAverage() + 2*absStdev;
		double underbound = getAverage() - 2*absStdev;
		
		ArrayList<PiecePlacement> piecePlacements = gameEngine.getPiecePlacements();

		double sum = 0;
		double piecesUsed = 0;
		
		for (int i=0; i<piecePlacements.size(); i++){
			
			double piecePlacementTime         = piecePlacements.get(i).getTime();
			double previousPiecePlacementTime = (i==0) ? 0 : piecePlacements.get(i-1).getTime();
			double framesDelta = piecePlacementTime - previousPiecePlacementTime;
			
			piecePlacements.get(i).setFramesDelta((int) framesDelta);
			
			if (framesDelta < upperbound && framesDelta > underbound){
				sum += framesDelta;
				piecesUsed++;
			} else if (framesDelta > upperbound){
				tooSlowPiecePlacements.add(piecePlacements.get(i));
			}
			
			double calculatedIdealFramesPerPiece = sum/piecesUsed;
			
			setIdealFramesPerPiece(calculatedIdealFramesPerPiece);
			setIdealTime((calculatedIdealFramesPerPiece*statistics.totalPieceLocked)/60f);
			
		}
	}

	private void calculateStandardDeviation() {
		setStdev(Math.pow(getVariance(), 1d/2d));
	}

	private void calculateVariation() {
		ArrayList<PiecePlacement> piecePlacements = gameEngine.getPiecePlacements();
		double n = piecePlacements.size();
		double sum = 0;
		for (int i = 0; i < n; i++) {
			double piecePlacementTime         = piecePlacements.get(i).getTime();
			double previousPiecePlacementTime = (i==0) ? 0 : piecePlacements.get(i-1).getTime();
			double framesDelta = piecePlacementTime - previousPiecePlacementTime;
			double distance = framesDelta - getAverage();
			sum += Math.pow(distance, 2);
		}
		setVariance(sum/(n-1));
	}

	private void calculateMean() {
		ArrayList<PiecePlacement> piecePlacements = gameEngine.getPiecePlacements();
		int totalFrames = piecePlacements.get(piecePlacements.size()-1).getTime();
		setAverage((double)totalFrames/(double)piecePlacements.size());
	}

	private StringBuilder createReport() {
		StringBuilder report = new StringBuilder();
		report.append("<html>\r\n");
		addHeader(report);
		addBody(report);
		report.append("</html>");
		return report;
	}

	private void addBody(StringBuilder report) {
		report.append("<body style='font-size:11px'>");
		report.append("<h1>Nullpomino Sprint Stats -" + getTitle() + "</h1>");
		
		report.append("<div id='tabs'>");
		
		report.append("<ul>");
		report.append("<li><a href='#general'>General</a></li>");
		report.append("<li><a href='#speed'>Speed</a></li>");
		report.append("<li><a href='#finesse'>Finesse</a></li>");
		report.append("<li><a href='#lines'>lines</a></li>");
		report.append("<li><a href='#manipulations'>manipulations</a></li>");
		report.append("<li><a href='#stats'>statistics</a></li>");
		report.append("<li><a href='#fumentab'>fumen</a></li>");
		report.append("<li><a href='#replay'>replay</a></li>");
		report.append("<li><a href='#other'>other</a></li>");
		report.append("</ul>");
		
		report.append("<div id='general'><table style='font-size:11px'>");
		report.append("<tr><td>time</td><td>"+getStatistics().getTime()/60f+"s</td></tr>");
		report.append("<tr><td>ppm</td><td>"+getStatistics().getPps()*60f+" pieces/minute</td></tr>");
		report.append("<tr><td>pps</td><td>"+getStatistics().getPps()+" pieces/second</td></tr>");
		report.append("<tr><td>level</td><td>"+getStatistics().level+"</td></tr>");
		report.append("<!-- totals -->");
		report.append("<tr><td>pieces</td><td>"+getStatistics().totalPieceLocked+"</td></tr>");
		report.append("<tr><td>lines</td><td>"+getStatistics().lines+"</td></tr>");
		report.append("<tr><td>holds</td><td>"+getStatistics().totalHoldUsed+"</td></tr>");
		report.append("<tr><td>&nbsp;</td></tr>");
		report.append("<div id='fumen' title='Fumen of "+ getTitle() +"'>");
		report.append("<iframe src='"+ fumenViewValue +"' width='290' height='260' frameborder='0' scrolling='no'></iframe>");
		report.append("</div>");
		report.append("</table></div>");
		
		report.append("<div id='speed'><table style='font-size:11px'>");
		report.append("<tr><td>time</td><td>"+getStatistics().getTime()/60f+"</td></tr>");
		report.append("<tr><td>ppm</td><td>"+getStatistics().getPps()*60f+"</td></tr>");
		report.append("<tr><td>pps</td><td>"+getStatistics().getPps()+"</td></tr>");
		report.append("<tr><td colspan='2'><div id='speed_div'></div></td></tr>");
		report.append("<tr><td colspan='2'><div id='piece_speed_frames_div'></div></td></tr>");
		report.append("<tr><td colspan='2'><div id='piece_speed_ms_div'></div></td></tr>");
		report.append("</table></div>");
		
		report.append("<div id='finesse'><table style='font-size:11px'>");
		report.append("<tr><td>total finesse faults</td><td>"+getStatistics().finesseDelta+"</td></tr>");
		report.append("<tr><td>kpt</td><td>"+getStatistics().kpt+"</td></tr>");
		report.append("<tr><td>ekpt</td><td>"+calculateEKPT()+"</td></tr>");
		report.append("</table></div>");
		
		report.append("<div id='lines'><table style='font-size:11px'>");
		report.append("<tr><td>lps</td><td>"+getStatistics().lps+"</td></tr>");
		report.append("<tr><td>lpm</td><td>"+getStatistics().lpm+"</td></tr>");
		report.append("<tr><td>max combo</td><td>"+getStatistics().maxCombo+"</td></tr>");
		report.append("<tr><td>&nbsp;</td></tr>");
		report.append("<tr><td>total singles</td><td>"+getStatistics().totalSingle+"</td></tr>");
		report.append("<tr><td>total doubles</td><td>"+getStatistics().totalDouble+"</td></tr>");
		report.append("<tr><td>total triples</td><td>"+getStatistics().totalTriple+"</td></tr>");
		report.append("<tr><td>total tetrises</td><td>"+getStatistics().totalFour+"</td></tr>");
		report.append("<tr><td>total B2B tetrises</td><td>"+getStatistics().totalB2BFour+"</td></tr>");
		report.append("<tr><td>total B2B T-spin</td><td>"+getStatistics().totalB2BTSpin+"</td></tr>");
		report.append("<tr><td>total TSZ</td><td>"+getStatistics().totalTSpinZero+"</td></tr>");
		report.append("<tr><td>total TSZ Mini</td><td>"+getStatistics().totalTSpinZeroMini+"</td></tr>");
		report.append("<tr><td>total TSS</td><td>"+getStatistics().totalTSpinSingle+"</td></tr>");
		report.append("<tr><td>total TSS Mini</td><td>"+getStatistics().totalTSpinSingleMini+"</td></tr>");
		report.append("<tr><td>total TSD</td><td>"+getStatistics().totalTSpinDouble+"</td></tr>");
		report.append("<tr><td>total TSD Mini</td><td>"+getStatistics().totalTSpinDoubleMini+"</td></tr>");
		report.append("<tr><td>total TST</td><td>"+getStatistics().totalTSpinTriple+"</td></tr>");
		report.append("</table></div>");
		
		report.append("<div id='manipulations'><table style='font-size:11px'>");
		report.append("<tr><td>total rotations</td><td>"+getStatistics().totalPieceRotate+"</td></tr>");
		report.append("<tr><td>total moves</td><td>"    +getStatistics().totalPieceMove+"</td></tr>");
		report.append("<tr><td>total moves (das=1)</td><td>"    +getStatistics().totalPieceMoveWithDasAsOne+"</td></tr>");
		report.append("<tr><td>&nbsp;</td><td></td></tr>");

		for (Manipulation manipulation : Manipulation.values()){
			report.append("<tr><td>" + manipulation+ "</td><td>"+getStatistics().getManipulations().get(manipulation).size() + " times - used frames "+ getStatistics().getManipulations().get(manipulation) + "</td></tr>");
		}
		
		report.append("<tr><td colspan='2'><div id='frames_per_key_div'></div></td></tr>");
		report.append("<tr><td colspan='2'><div id='manipulations_div'></div></td></tr>");
		report.append("</table></div>");

		report.append("<div id='stats'><table style='font-size:11px'>");
		report.append("<tr><td style='width:300px;'>ideal time</td><td>"+String.format("%.5g%n",getIdealTime())+"s</td></tr>");
		report.append("<tr><td>ideal frames per piece</td><td>"+String.format("%.2g%n",getIdealFramesPerPiece())+" frames/piece</td></tr>");
		report.append("<tr><td>ideal slowest number of frames of a piece</td><td>"+String.format("%.2g%n",getAverage() + 2*Math.abs(getStdev())) +" frames</td></tr>");
		report.append("<tr><td></td><td></td></tr>");
		report.append("<tr><td>Too slow pieces</td><td>" + getFormattedTooSlowPiecePlacements() + "</td></tr>");
		report.append("<tr><td></td><td></td></tr>");
		report.append("<tr><td>total frames used</td><td>"+ getTotalFramesUsed()+"</td></tr>");
		report.append("<tr><td>average amount of frames used per piece</td><td>"+String.format("%.2g%n",getAverage())+" frames</td></tr>");
		report.append("<tr><td>average amount of ms used per piece</td><td>"+String.format("%.4g%n",getAverage()/60d*1000d)+" ms</td></tr>");
		report.append("<tr><td>variance of frames used per piece</td><td>"+String.format("%.2g%n",getVariance())+"</td></tr>");
		report.append("<tr><td>standard deviation of frames used per piece</td><td>"+String.format("%.2g%n",getStdev())+"</td></tr>");
		report.append("</table></div>");
		
		report.append("<div id='fumentab'><table style='font-size:11px'>");
		appendFumens(report);
		report.append("</table></div>");

		report.append("<div id='replay'><table style='font-size:11px'>");
		appendReplayFumens(report);
		report.append("</table></div>");

		report.append("<div id='other'><table style='font-size:11px'>");
		report.append("<tr><td>gamerate</td><td>"+getStatistics().gamerate+"</td></tr>");
		report.append("<tr><td>sps</td><td>"+getStatistics().sps+"</td></tr>");
		report.append("<tr><td>spm</td><td>"+getStatistics().spm+"</td></tr>");
		report.append("<tr><td>spl</td><td>"+getStatistics().spl+"</td></tr>");
		report.append("</table></div>");
		
		report.append("</div>");
		
		report.append("");
		report.append("</body>");
	}

	private int getTotalFramesUsed() {
		if (gameEngine.getPiecePlacements().size()-1 > 0){
			return gameEngine.getPiecePlacements().get(gameEngine.getPiecePlacements().size()-1).getTime();
		} else return -1;
	}

	private void appendFumens(StringBuilder report) {
		
		boolean fumenrecording = NullpoMinoSlick.propConfig.getProperty("option.fumenrecording", false);
		if (fumenrecording){
		report.append("<tr><td colspan='2'><a href='"+fumenUrlValue+"'>open fumen to edit</a></td></tr>");
		report.append("<tr><td colspan='2'><a href='"+fumenViewValue+"'>open fumen to view</a></td></tr>");
		report.append("<tr><td>fumen data to load</td><td><textarea rows='10' cols='100'>"+fumenEncodedValue+"</textarea></td></tr>");
	
		report.append("<tr><td colspan='2'>");
		report.append("<h2>All fumens</h2>");
		report.append("<table><tr>");
		
		for (GameEngine engine : gameEngine.owner.engine){
			report.append("<td>");
			
			String engineEncodedFumenString = FumenUtil.getFumenUtil(engine).encode();
			String engineFumenViewValue = FUMEN_SERVER_FILE + "?m" + engineEncodedFumenString.substring(1);
			
			report.append("<h3>"+ engine.playerID +" - " + engine.playerName + "</h3>");
			report.append("<iframe class='fumeniframe' src='"+ engineFumenViewValue +"' width='290' height='260' frameborder='0' scrolling='no'></iframe>");
			report.append("</td>");
		}
		
		report.append("</tr>");
		report.append("<tr><td>");
		report.append("<input type='button' name='totalbackward' value='|<' onClick='javascript:totalRewindFumens();'>");
		report.append("<input type='button' name='backward' value='<' onClick='javascript:rewindFumens();'>");
		report.append("<input type='button' name='forward' value='>' onClick='javascript:forwardFumens();'>");
		report.append("<input type='button' name='totalforward' value='>|' onClick='javascript:totalFastForwardFumens();'>");
		report.append("</td></tr>");
		report.append("</table>");
		report.append("</td></tr>");
		}
		else report.append("<tr><td>fumen recording was disabled when creating this report, enable via options -> general options -> fumen recording</td></tr>");
	}
	
	private void appendReplayFumens(StringBuilder report) {
		
		boolean fullframefumenrecording = NullpoMinoSlick.propConfig.getProperty("option.fullframefumenrecording", false);
		
		if (fullframefumenrecording){
		
		report.append("<tr><td colspan='2'>");
		report.append("<table><tr>");
		
		for (GameEngine engine : gameEngine.owner.engine){
			report.append("<td>");
			
			String engineEncodedFumenString = FumenUtil.getFullFrameFumenUtil(engine).encode();
			String engineFumenViewValue = FUMEN_SERVER_FILE + "?m" + engineEncodedFumenString.substring(1);
			
			String playerName = engine.playerName != null ? " - " + engine.playerName : "";
			
			report.append("<h3>" + engine.playerID + playerName + "</h3>");
			report.append("<iframe class='replayfumeniframe' src='"+ engineFumenViewValue +"' width='290' height='260' frameborder='0' scrolling='no'></iframe>");
			report.append("</td>");
		}
		
		report.append("</tr>");
		report.append("<tr><td>");
		
		report.append("<div id='controlbar' style='display:block; font-size:12px;'>");
		report.append("	<span id='toolbar' class='ui-widget-header ui-corner-all'>");
		report.append("		<button id='beginning' onclick='replayTotalRewindFumens(); return false;'>go to beginning</button>");
		report.append("		<button id='rewind'    onclick='rewind(); return false;'>rewind</button>");
		report.append("		<button id='play'      onclick='play(); return false;'>play</button>");
		report.append("		<button id='stop'      onclick='pause(); return false;'>stop</button>");
		report.append("		<button id='end'       onclick='replayTotalFastForwardFumens(); return false;'>go to end</button>");
		report.append("		&nbsp;&nbsp;&nbsp;&nbsp;<span id='fumenspeed'>1x</span>&nbsp;&nbsp;");
		report.append("		<span id='speedtuning'>");
		report.append("			<button id='slower' onclick='slower(); return false;'>slower</button>");
		report.append("			<button id='faster' onclick='faster(); return false;'>faster</button>");
		report.append("		</span>");
		report.append(" </span>");
		report.append("</div>");
		
		report.append("</td></tr>");
		report.append("</table>");
		report.append("</td></tr>");
		} else {
			report.append("<tr>full frame fumen recording was disabled when creating this report, enable via options -> general options -> full frame fumen recording (takes memory)</tr>");
		}
	}

	private float calculateEKPT() {
		return (getStatistics().kpt*getStatistics().totalPieceLocked-(float)getStatistics().finesseDelta)/(float)getStatistics().totalPieceLocked;
	}

	private String getFormattedTooSlowPiecePlacements() {
		String output = "<table style='font-size:11px'>";
		for (PiecePlacement piecePlacement : tooSlowPiecePlacements){
			output += "<tr>";
			output += "<td>"+piecePlacement.getPieceAsString()+"</td>";
			output += "<td> at </td>";
			output += "<td>"+String.format("%.2g%n",piecePlacement.getTime()*1/60d)+"s</td>";
			output += "<td> took "+String.format("%.2g%n",(double)piecePlacement.getFramesDelta()*1d/60d) + "s (=" + String.format("%.2g%n",(double)piecePlacement.getFramesDelta()) +"frames) - </td>";
			output += "<td>(x="+piecePlacement.getX()+",y="+piecePlacement.getY()+",direction="+piecePlacement.getDirection()+")</td>";
			output += "</tr>";
		}
		output += "</table>";
		return output;
	}

	private void addHeader(StringBuilder report) {
		report.append("<html><head>");
		report.append("<title>" + getTitle() + "</title>");
		report.append("<link type='text/css' href='css/redmond/jquery-ui-1.8.18.custom.css' rel='stylesheet' />	");
		report.append("<script type='text/javascript' src='js/jquery-1.7.1.min.js'></script>");
		report.append("<script type='text/javascript' src='js/jquery-ui-1.8.18.custom.min.js'></script>");
		report.append("<script type='text/javascript' src='https://www.google.com/jsapi'></script>");
		report.append("<script>$(function() {$('#tabs').tabs();});</script>");
		report.append("<script>$(function() {$('#fumen').dialog();});</script>");
		report.append("<script type='text/javascript'>");
		report.append("	google.load('visualization', '1.0', {'packages':['corechart']});");
		report.append(" 	google.setOnLoadCallback(drawGraphs);");
		report.append("");
		report.append("function drawGraphs(){");
		report.append("  drawSpeedGraph();");
		report.append("  drawPieceSpeedFramesGraph();");
		report.append("  drawPieceSpeedMSGraph();");
		report.append("  drawFramesPerKeyGraph();");
		report.append("  drawManipulationGraph();");
		report.append("}");
		addFumenControlFunctions(report);
		addReplayControlFunctions(report);
		addDrawSpeedGraph(report);
		addDrawPieceSpeedFramesGraph(report);
		addDrawPieceSpeedMSGraph(report);
		addFramesPerKeyGraph(report);
		addDrawManipulationsGraph(report);
		
		report.append("</script>");
		report.append("<style>#toolbar {padding: 12px 4px;}#cm {border:0px; border-top:4px solid #aaa; height:30px;	}</style>");
		report.append("</head>");
	}

	private void addReplayControlFunctions(StringBuilder report) {
		report.append("var defaultfrequency = 16;");
		report.append("var frequency = 16;");
		report.append("var playInterval = 0;");
		report.append("var rewindInterval = 0;");
		report.append("function play() {");
		report.append("	pause(); ");
		report.append("	playInterval = setInterval( 'next()', frequency );");
		report.append("}");
		report.append("function rewind() {");
		report.append("	pause();   ");
		report.append("	rewindInterval = setInterval( 'prev()', frequency );");
		report.append("}");
		report.append("function pause(){");
		report.append("	if(playInterval > 0) clearInterval(playInterval);  ");
		report.append("	if(rewindInterval > 0) clearInterval(rewindInterval);  ");
		report.append("}");
		report.append("function slower(){");
		report.append("	frequency*=2;");
		report.append("	if (rewindInterval > 0){");
		report.append("		rewind();");
		report.append("	} else if (playInterval > 0){");
		report.append("		play();");
		report.append("	}");
		report.append("	refreshSpeedLabel();");
		report.append("}");
		report.append("function faster(){");
		report.append("	frequency/=2;");
		report.append("	if (rewindInterval > 0){");
		report.append("		rewind();");
		report.append("	} else if (playInterval > 0){");
		report.append("		play();");
		report.append("	}");
		report.append("	refreshSpeedLabel();");
		report.append("}");
		report.append("function resetspeed(){");
		report.append("	frequency=defaultfrequency;");
		report.append("	if (rewindInterval > 0){");
		report.append("		rewind();");
		report.append("	} else if (playInterval > 0){");
		report.append("		play();");
		report.append("	}");
		report.append("	refreshSpeedLabel();");
		report.append("}");
		report.append("function next(){");
		report.append("  $(function() {");
		report.append("	   $('.replayfumeniframe').contents().find('#nx').click();");
		report.append("  });");
		report.append("}");
		report.append("function prev(){");
		report.append("  $(function() {");
		report.append("	   $('.replayfumeniframe').contents().find('#prev').click();");
		report.append("  });");
		report.append("}");
		report.append("function replayTotalRewindFumens(){");
		report.append("  $(function() {");
		report.append("	   $('.replayfumeniframe').contents().find('#first').click();");
		report.append("  });");
		report.append("}");
		report.append("function replayTotalFastForwardFumens(){");
		report.append("  $(function() {");
		report.append("	   $('.replayfumeniframe').contents().find('#last').click();");
		report.append("  });");
		report.append("}");
		report.append("	function refreshSpeedLabel(){" +
				"		$('#fumenspeed').html(defaultfrequency/frequency + 'x');}" +
				"$(function() {" +
				"	$( '#beginning' ).button({text: false,icons: {primary: 'ui-icon-seek-start'}});" +
				"	$( '#rewind' ).button({text: false,icons: {primary: 'ui-icon-seek-prev'}});" +
				"	$( '#play' ).button({text: false,icons: {primary: 'ui-icon-play'}});" +
				"	$( '#stop' ).button({text: false,icons: {primary: 'ui-icon-pause'}});" +
				"	$( '#end' ).button({text: false,icons: {primary: 'ui-icon-seek-end'}});" +
				"	$( '#slower' ).button({text: false,icons: {primary: 'ui-icon-minus'}});" +
				"	$( '#faster' ).button({text: false,icons: {primary: 'ui-icon-plus'}});" +
				"	$( '#speedtuning' ).buttonset();" +
				"});");
	}

	private void addFumenControlFunctions(StringBuilder report) {
		report.append("function forwardFumens(){");
		report.append("  $(function() {");
		report.append("	   $('.fumeniframe').contents().find('#nx').click();");
		report.append("  });");
		report.append("}");
		report.append("function rewindFumens(){");
		report.append("  $(function() {");
		report.append("	   $('.fumeniframe').contents().find('#prev').click();");
		report.append("  });");
		report.append("}");
		report.append("function totalRewindFumens(){");
		report.append("  $(function() {");
		report.append("	   $('.fumeniframe').contents().find('#first').click();");
		report.append("  });");
		report.append("}");
		report.append("function totalFastForwardFumens(){");
		report.append("  $(function() {");
		report.append("	   $('.fumeniframe').contents().find('#last').click();");
		report.append("  });");
		report.append("}");
	}

	private String getTitle() {
		Calendar c = Calendar.getInstance();
		DateFormat dfm = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
		return dfm.format(c.getTime());
	}

	private void addFramesPerKeyGraph(StringBuilder report) {

		ArrayList<PiecePlacement> piecePlacements = gameEngine.getPiecePlacements();
		
		report.append(" function drawFramesPerKeyGraph() {");
		report.append("var data = new google.visualization.DataTable();");
		report.append("data.addColumn('number', 'Piece');");
		report.append("data.addColumn('number', 'Frames/Key');");
		report.append("data.addRows([");
		
		for (int i=0; i<piecePlacements.size(); i++){
			
			int piecePlacementTime         = piecePlacements.get(i).getTime();
			int previousPiecePlacementTime = (i==0) ? 0 : piecePlacements.get(i-1).getTime();
			
			int framesDelta = piecePlacementTime - previousPiecePlacementTime;
			int keys = piecePlacements.get(i).getMoves() + piecePlacements.get(i).getRotations();
			
			report.append("[");
			report.append(i+1);
			report.append(",");
			report.append((double)framesDelta/(double)keys);
			report.append("]");
			
			if (i != piecePlacements.size()-1){
				report.append(",");
			}
		}
		report.append("]);");
		report.append("var options = {'title':'Frames per key per piece',");
		report.append("'width':1000,");
		report.append("'height':500};");
		report.append("var chart = new google.visualization.LineChart(document.getElementById('frames_per_key_div'));");
		report.append("chart.draw(data, options);");
		report.append("}");
	}
	
	private void addDrawPieceSpeedFramesGraph(StringBuilder report) {

		ArrayList<PiecePlacement> piecePlacements = gameEngine.getPiecePlacements();
		
		report.append(" function drawPieceSpeedFramesGraph() {");
		report.append("var data = new google.visualization.DataTable();");
		report.append("data.addColumn('number', 'Piece');");
		report.append("data.addColumn('number', 'Frames/Piece');");
		report.append("data.addRows([");
		
		for (int i=0; i<piecePlacements.size(); i++){
			
			int piecePlacementTime         = piecePlacements.get(i).getTime();
			int previousPiecePlacementTime = (i==0) ? 0 : piecePlacements.get(i-1).getTime();
			
			int framesDelta = piecePlacementTime - previousPiecePlacementTime;
			
			report.append("[");
			report.append(i+1);
			report.append(",");
			report.append(framesDelta);
			report.append("]");
			
			if (i != piecePlacements.size()-1){
				report.append(",");
			}
		}
		report.append("]);");
		report.append("var options = {'title':'Piece Speed in frames',");
		report.append("'width':1000,");
		report.append("'height':500};");
		report.append("var chart = new google.visualization.LineChart(document.getElementById('piece_speed_frames_div'));");
		report.append("chart.draw(data, options);");
		report.append("}");
	}

	private void addDrawPieceSpeedMSGraph(StringBuilder report) {

		ArrayList<PiecePlacement> piecePlacements = gameEngine.getPiecePlacements();
		
		report.append(" function drawPieceSpeedMSGraph() {");
		report.append("var data = new google.visualization.DataTable();");
		report.append("data.addColumn('number', 'Piece');");
		report.append("data.addColumn('number', 'milliseconds/Piece');");
		report.append("data.addRows([");
		
		for (int i=0; i<piecePlacements.size(); i++){
			
			int piecePlacementTime         = piecePlacements.get(i).getTime();
			int previousPiecePlacementTime = (i==0) ? 0 : piecePlacements.get(i-1).getTime();
			
			int framesDelta = piecePlacementTime - previousPiecePlacementTime;
			
			report.append("[");
			report.append(i+1);
			report.append(",");
			report.append((double)framesDelta*1f/60f*1000f);
			report.append("]");
			
			if (i != piecePlacements.size()-1){
				report.append(",");
			}
		}
		report.append("]);");
		report.append("var options = {'title':'Piece Speed in milliseconds',");
		report.append("'width':1000,");
		report.append("'height':500};");
		report.append("var chart = new google.visualization.LineChart(document.getElementById('piece_speed_ms_div'));");
		report.append("chart.draw(data, options);");
		report.append("}");
	}
	
	private void addDrawSpeedGraph(StringBuilder report) {
		
		report.append("function drawSpeedGraph() {");
		report.append("var data = new google.visualization.DataTable();");
		report.append("data.addColumn('number', 'Frame');");
		report.append("data.addColumn('number', 'Local Speed');");
		report.append("data.addColumn('number', 'Global Speed');");
		report.append("data.addRows([");
		
		for (int i=0; i<gameEngine.statistics.speedEntries.size(); i++){
			SpeedEntry speedEntry = gameEngine.statistics.speedEntries.get(i);
			report.append("[");
			report.append(speedEntry.getTime());
			report.append(",");
			report.append(speedEntry.getLocalspeed());
			report.append(",");
			report.append(speedEntry.getGlobalspeed());
			report.append("]");
			if (i != gameEngine.statistics.speedEntries.size()-1){
				report.append(",");
			}
		}
		
		report.append("]);");
		report.append("var options = {'title':'Speed','width':1000,'height':500};");
		report.append("var chart = new google.visualization.LineChart(document.getElementById('speed_div'));");
		report.append("chart.draw(data, options);");
		report.append("}");
		
	}

	private void addDrawManipulationsGraph(StringBuilder report) {
		
		report.append("function drawManipulationGraph() {");
		
		report.append("var data = new google.visualization.DataTable();");
		report.append("data.addColumn('string', 'Manipulation');");
		report.append("data.addColumn('number', 'Average Manipulation Speed');");
		report.append("data.addRows([");
		
		final HashMap<Manipulation, ArrayList<Integer>> manipulations = gameEngine.statistics.getManipulations();
		
		List<Manipulation> sortedManipulations = sortManipulationsByAverage(manipulations);
		
		boolean firstItem = true;
		
		for (Manipulation manipulation : sortedManipulations){
			
			if (!firstItem){			
				report.append(",");
			}

			firstItem = false;
			
			ArrayList<Integer> manipulationTimes = manipulations.get(manipulation);
			
			double averageTime = calculateAverageOf(manipulationTimes);
		
			report.append("[");
			report.append("'" + manipulation.toString() + "'");
			report.append(",");
			report.append(averageTime);
			report.append("]");
		}
		
		report.append("]);");
		report.append("var options = {'title':'Manipulation Speed','animation.duration':500, 'width':1500, 'height':500};");
		report.append("var chart = new google.visualization.BarChart(document.getElementById('manipulations_div'));");
		report.append("chart.draw(data, options);");
		report.append("}");
		
	}

	private List<Manipulation> sortManipulationsByAverage(
			final HashMap<Manipulation, ArrayList<Integer>> manipulations) {
		List<Manipulation> sortedManipulations = new ArrayList<Manipulation>(manipulations.keySet());
		
		Collections.sort(sortedManipulations, new Comparator<Manipulation>(){
			@Override
			public int compare(Manipulation arg0, Manipulation arg1) {
				double delta = calculateAverageOf(manipulations.get(arg0)) - calculateAverageOf(manipulations.get(arg1));
				if(delta > 0) return -1;
			    if(delta < 0) return 1;
			    return 0;
			}
		});
		return sortedManipulations;
	}

	private double calculateAverageOf(ArrayList<Integer> manipulationTimes) {
		if (manipulationTimes.size() == 0) return 0;
		int sum = 0;
		for (Integer manipulationTime : manipulationTimes){
			sum += manipulationTime;
		}
		return (double) sum / (double) manipulationTimes.size();
	}

	private void writeToDisk(StringBuilder stringBuilder) {
		String foldername = "report";
		String filename = foldername + "/" + getReportFilename();
		try {
			File reportfolder = new File(foldername);
			if (!reportfolder.exists()) {
				if (reportfolder.mkdir()) {
					log.info("Created replay folder: " + foldername);
				} else {
					log.info("Couldn't create replay folder at "+ foldername);
				}
			}

			FileOutputStream out = new FileOutputStream(filename);
			out.write(stringBuilder.toString().getBytes());
			out.close();
			gameEngine.latestReportLocation = filename;
			log.info("Saved report file: " + filename);
		} catch(IOException e) {
			log.error("Couldn't save report file to " + filename, e);
		}
	}
	
	public static String getReportFilename() {
		Calendar c = Calendar.getInstance();
		DateFormat dfm = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
		String filename = dfm.format(c.getTime())+ ".html";
		return filename;
	}
	
	public Statistics getStatistics() {
		return statistics;
	}

	public void setStatistics(Statistics statistics) {
		this.statistics = statistics;
	}

	public double getAverage() {
		return average;
	}

	public void setAverage(double average) {
		this.average = average;
	}

	public double getVariance() {
		return variance;
	}

	public void setVariance(double variance) {
		this.variance = variance;
	}

	public double getStdev() {
		return stdev;
	}

	public void setStdev(double stdev) {
		this.stdev = stdev;
	}

	public void setIdealTime(double idealTime) {
		this.idealTime = idealTime;
	}

	public double getIdealTime() {
		return idealTime;
	}

	public void setIdealFramesPerPiece(double idealFramesPerPiece) {
		this.idealFramesPerPiece = idealFramesPerPiece;
	}

	public double getIdealFramesPerPiece() {
		return idealFramesPerPiece;
	}

}

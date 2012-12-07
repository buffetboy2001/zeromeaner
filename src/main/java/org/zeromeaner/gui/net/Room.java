package org.zeromeaner.gui.net;

import java.util.Properties;

import org.zeromeaner.game.net.NetRoomInfo;
import org.zeromeaner.util.CustomProperties;


/**
 * Room class used to store information about the NullpoMino league.
 * 
 * The NetRoomInfo class contains the "normal" data from the original client.
 * This class contains the data for the league.
 * 
 * @author nickvanderhoven
 */
public class Room {
	
	private int id;
	private String division;
	private String name;
	private boolean ratedInOriginalVersion;
	private RoomType type;
	private String ruleName;
	private String status;
	private int players;
	private int maxNumberOfPlayers;
	private int spectators;
	
	private static Properties propLang;
	private static Properties propLangDefault;
	
	public static Room fromNetRoomInfo(NetRoomInfo r, CustomProperties defaultLanguageProperties, CustomProperties uiLanguageProperties){
	
		propLang = uiLanguageProperties;
		propLangDefault = defaultLanguageProperties;
		
		Room newRoom = new Room();
		
		newRoom.setId(r.roomID);
		newRoom.setDivision(getDivisionFromRoomName(r.strName));
		newRoom.setName(r.strName);
		newRoom.setRated(r.rated);
		newRoom.setType(getTypeFromRoomName(r));
		newRoom.setRuleName(getRuleNameFromNetRoomInfo(r));
		newRoom.setStatus(getStatusFromNetRoomInfo(r));
		newRoom.setPlayers(r.playerSeatedCount);
		newRoom.setMaxNumberOfPlayers(r.maxPlayers);
		newRoom.setSpectators(r.spectatorCount);
		
		return newRoom;
	}

	private static String getStatusFromNetRoomInfo(NetRoomInfo r) {
		return r.playing ? getUIText("RoomTable_Status_Playing") : getUIText("RoomTable_Status_Waiting");
	}

	private static String getRuleNameFromNetRoomInfo(NetRoomInfo r) {
		return r.strName.contains("1VS1") ? "NPL" : (r.ruleLock ? r.ruleName.toUpperCase() : getUIText("RoomTable_RuleName_Any"));
	}

	private static RoomType getTypeFromRoomName(NetRoomInfo r) {
		return 
			( r.strName.contains("1VS1") && r.strName.contains("League") ) ? RoomType.League : 
				(r.strName.contains("1VS1") && r.strName.contains("Tournament") ) ? RoomType.Tournament : RoomType.Practice;
	}
	
	private static String getDivisionFromRoomName(String strName) {
		if (strName.contains("1VS1") && strName.contains("BR"))
			return "Bronze";
		if (strName.contains("1VS1") && strName.contains("GO"))
			return "Gold";
		if (strName.contains("1VS1") && strName.contains("SI"))
			return "Silver";
		return "None";
	}

	public static String getUIText(String str) {
		String result = propLang.getProperty(str);
		if(result == null) {
			result = propLangDefault.getProperty(str, str);
		}
		return result;
	}
	
	public void updateRoom(Room fromRoom){
		setId(fromRoom.getId());
		setDivision(getDivisionFromRoomName(fromRoom.getName()));
		setName(fromRoom.getName());
		setRated(fromRoom.isRated());
		setType(fromRoom.getType());
		setRuleName(fromRoom.getRuleName());
		setStatus(fromRoom.getStatus());
		setPlayers(fromRoom.getPlayers());
		setMaxNumberOfPlayers(fromRoom.getMaxNumberOfPlayers());
		setSpectators(fromRoom.getSpectators());
	}
	
	public boolean is1VS1(){
		return getName().contains("1VS1");
	}
	public boolean isWaitingForMorePlayers(){
		return getStatus().equals(getUIText("RoomTable_Status_Waiting"));
	}
	public boolean isLeageMatch(){
		return getType().equals(RoomType.League);
	}
	public String getDisplayName(){
		String displayName = getName();
		if (displayName.contains("1VS1")){
			displayName = displayName.substring(0, displayName.lastIndexOf("-"));
			displayName = displayName.replaceAll("BR", "Bronze");
			displayName = displayName.replaceAll("SI", "Silver");
			displayName = displayName.replaceAll("GO", "Gold");
		}
		return displayName;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getDivision() {
		return division;
	}
	public void setDivision(String division) {
		this.division = division;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isRated() {
		return ratedInOriginalVersion;
	}
	public void setRated(boolean rated) {
		this.ratedInOriginalVersion = rated;
	}
	public String getRuleName() {
		return ruleName;
	}
	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public int getPlayers() {
		return players;
	}
	public void setPlayers(int players) {
		this.players = players;
	}
	public int getMaxNumberOfPlayers() {
		return maxNumberOfPlayers;
	}
	public void setMaxNumberOfPlayers(int maxNumberOfPlayers) {
		this.maxNumberOfPlayers = maxNumberOfPlayers;
	}
	public int getSpectators() {
		return spectators;
	}
	public void setSpectators(int spectators) {
		this.spectators = spectators;
	}
	public RoomType getType() {
		return type;
	}
	public void setType(RoomType type) {
		this.type = type;
	}

}

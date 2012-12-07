package org.zeromeaner.gui.net;

public enum RoomType {
	
	League("League"),
	Practice("Practice"),
	Tournament("Tournament");
	
	String roomTypeName;
	
	RoomType(String typeName){
		roomTypeName = typeName;
	}

	public String getRoomTypeName() {
		return roomTypeName;
	}

	public void setRoomTypeName(String roomTypeName) {
		this.roomTypeName = roomTypeName;
	}
	
}
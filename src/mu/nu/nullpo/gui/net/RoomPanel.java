package mu.nu.nullpo.gui.net;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import mu.nu.nullpo.game.net.NetPlayerInfo;

class RoomPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private Room room;
	private NetLobbyFrame netLobbyFrame;

	public RoomPanel(Room room, NetLobbyFrame netLobbyFrame) {
		super();
		setRoom(room);
		setNetLobbyFrame(netLobbyFrame);
		initialize();
	}
	
	public void refresh(){
		removeAll();
		initialize();
		revalidate();
	}

	private void initialize() {
		ImagePanel imagePanel = new ImagePanel(new ImageIcon("res/graphics/roomback.jpg").getImage());

		boolean currentUserInThisRoom = isCurrentUserInThisRoom();

		JLabel roomTitle 		= createTitleLabel(currentUserInThisRoom);
		JLabel playersLabel 	= createPlayersLabel();
		JLabel watcherssLabel 	= createSpectatorsLabel();
		JLabel typeLabel 		= createTypeLabel();

		JButton joinRoomButton  = createJoinButton();
		JButton watchRoomButton = createWatchButton();
		JButton leaveRoomButton = createLeaveButton();
		
		
		if (playerCanJoin()){
			imagePanel.add(joinRoomButton);
		}
		
		if (!currentUserInThisRoom){
			imagePanel.add(watchRoomButton);
		}
		
		if (currentUserInThisRoom){
			imagePanel.add(leaveRoomButton);
		}
		
		imagePanel.add(roomTitle);
		imagePanel.add(playersLabel);
		imagePanel.add(watcherssLabel);
		imagePanel.add(typeLabel);
		imagePanel.setVisible(true);
		
		setBackground(Color.white);
		
		addTooltip(imagePanel);

		add(imagePanel);
	}

	private void addTooltip(ImagePanel imagePanel) {
		String tooltip = "<html><b>Players in this room:</b><div style='padding:10px;'>";
		List<NetPlayerInfo> playersInThisRoom = getPlayersInThisRoom();
		for (NetPlayerInfo netPlayerInfo : playersInThisRoom) {
			String name = netPlayerInfo.strName;
			String status = "watching";
			if (netPlayerInfo.seatID > -1){
				status = "playing";
			}
			if (netPlayerInfo.uid == getCurrentUserId()){
				name = "<b>"+name+"</b>";
			}
			tooltip +=  name + " <i>("+ status +")</i><br>";
		}
		tooltip += "</div></html>";
		
		imagePanel.setToolTipText(tooltip);
	}

	private JLabel createTypeLabel() {
		JLabel typeLabel = new JLabel("<html><b>"+getRoom().getType()+"</b></small></html>");
		typeLabel.setForeground(Color.black);
		typeLabel.setBounds(20, 55, 200, 20);
		return typeLabel;
	}

	private JLabel createSpectatorsLabel() {
		JLabel watcherssLabel = new JLabel("<html><small><b>"+ getRoom().getSpectators() +" watching</b></small></html>");
		watcherssLabel.setForeground(Color.black);
		watcherssLabel.setBounds(100, 35, 200, 20);
		return watcherssLabel;
	}

	private JLabel createPlayersLabel() {
		JLabel playersLabel = new JLabel("<html><small><b>"+getRoom().getPlayers() + "/" + getRoom().getMaxNumberOfPlayers() +" players</b></small></html>");
		playersLabel.setForeground(Color.black);
		playersLabel.setBounds(20, 35, 200, 20);
		return playersLabel;
	}

	private JLabel createTitleLabel(boolean currentUserInThisRoom) {
		JLabel roomTitle = new JLabel("<html><h3>" + getRoom().getDisplayName() + "</h3></html>");
		if (currentUserInThisRoom){
			roomTitle.setForeground(Color.red);
		} else {
			roomTitle.setForeground(Color.white);
		}
		roomTitle.setBounds(10, 10, 400, 20);
		return roomTitle;
	}

	private JButton createJoinButton() {
		JButton joinRoomButton = new JButton("Join", new ImageIcon("res/icons/tournament.png"));
		joinRoomButton.setBounds(170, 35, 90, 45);
		joinRoomButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				getNetLobbyFrame().joinRoom(getRoom().getId(), false);
			}
		});
		return joinRoomButton;
	}

	private JButton createWatchButton() {
		JButton watchRoomButton = new JButton("Watch", new ImageIcon("res/icons/watch.png"));
		watchRoomButton.setBounds(260, 35, 90, 45);
		watchRoomButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				getNetLobbyFrame().joinRoom(getRoom().getId(), true);
			}
		});
		return watchRoomButton;
	}
	
	private JButton createLeaveButton() {
		JButton leaveRoomButton = new JButton("Leave", new ImageIcon("res/icons/exit.png"));
		leaveRoomButton.setBounds(260, 35, 90, 45);
		leaveRoomButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				getNetLobbyFrame().leaveRoom();
			}
		});
		return leaveRoomButton;
	}

	private boolean playerCanJoin() {
		return getRoom().getPlayers() < getRoom().getMaxNumberOfPlayers() && !isCurrentUserInThisRoom();
	}
	
	private boolean isCurrentUserInThisRoom(){
		int currentUserId = getCurrentUserId();
		List<NetPlayerInfo> playersInThisRoom = getPlayersInThisRoom();
		for (NetPlayerInfo netPlayerInfo : playersInThisRoom) {
			if (currentUserId == netPlayerInfo.uid){
				return true;
			}
		}
		return false;
	}
	
	private List<NetPlayerInfo> getPlayersInThisRoom(){
		LinkedList<NetPlayerInfo> playerInfoList = getNetLobbyFrame().netPlayerClient.getPlayerInfoList();
		List<NetPlayerInfo> playersInThisRoom = new ArrayList<NetPlayerInfo>();
		for (NetPlayerInfo netPlayerInfo : playerInfoList){
			if (netPlayerInfo.roomID == getRoom().getId()){
				playersInThisRoom.add(netPlayerInfo);
			}
		}
		return playersInThisRoom;
	}
	
	public int getCurrentUserId(){
		return getNetLobbyFrame().netPlayerClient.getPlayerUID();
	}

	private Room getRoom() {
		return room;
	}

	private void setRoom(Room room) {
		this.room = room;
	}

	public NetLobbyFrame getNetLobbyFrame() {
		return netLobbyFrame;
	}

	public void setNetLobbyFrame(NetLobbyFrame netLobbyFrame) {
		this.netLobbyFrame = netLobbyFrame;
	}

}
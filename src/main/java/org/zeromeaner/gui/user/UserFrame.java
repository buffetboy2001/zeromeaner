package org.zeromeaner.gui.user;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.UIManager;

import org.apache.log4j.Logger;
import org.zeromeaner.gui.slick.StateEnterUserName;
import org.zeromeaner.util.CustomProperties;

public class UserFrame extends JFrame {

	private static final long serialVersionUID = 1L;

	static final Logger log = Logger.getLogger(UserFrame.class);

	protected CustomProperties propGlobal;
	protected CustomProperties propSwingConfig;
	protected CustomProperties propLangDefault;
	protected CustomProperties propLang;
	
	private StateEnterUserName stateEnterUserName;

	public UserFrame(StateEnterUserName stateEnterUserName) {
		super();
		this.stateEnterUserName = stateEnterUserName;
		init();
	}

	public void init() {
		
		propGlobal = new CustomProperties();
		try {
			FileInputStream in = new FileInputStream("config/setting/global.cfg");
			propGlobal.load(in);
			in.close();
		} catch(IOException e) {}


		propLangDefault = new CustomProperties();
		try {
			FileInputStream in = new FileInputStream("config/lang/netlobby_default.properties");
			propLangDefault.load(in);
			in.close();
		} catch (Exception e) {
			log.error("Couldn't load default UI language file", e);
		}

		propLang = new CustomProperties();
		try {
			FileInputStream in = new FileInputStream("config/lang/netlobby_" + Locale.getDefault().getCountry() + ".properties");
			propLang.load(in);
			in.close();
		} catch(IOException e) {}
		
		propSwingConfig = new CustomProperties();
		try {
			FileInputStream in = new FileInputStream("config/setting/swing.cfg");
			propSwingConfig.load(in);
			in.close();
		} catch(IOException e) {}

		if(propSwingConfig.getProperty("option.usenativelookandfeel", true) == true) {
			try {
				UIManager.getInstalledLookAndFeels();
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch(Exception e) {
				log.warn("Failed to set native look&feel", e);
			}
		}

		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setTitle("Use Information");

		initUi();
		
	}

	private void initUi() {
		setLayout(new GridLayout(3, 2));
		setTitle("Enter user information");
		setSize(300, 130);
		setLocation(400, 400);
		
		final JTextField username = new JTextField();
		final JTextField password = new JTextField();
		final JButton saveButton = new JButton("ok");

		username.setSize(100, 8);
		password.setSize(100, 8);
		
		add(new JLabel("Username:"));
		add(new JLabel("Password:"));

		add(username);
		add(password);
		add(new JLabel(""));
		add(saveButton);
		
		username.setText(propGlobal.getProperty("user.username"));
		password.setText(propGlobal.getProperty("user.password"));
		
		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				propGlobal.put("user.username", username.getText());
				propGlobal.put("user.password", password.getText());
				saveGlobalConfig();
				stateEnterUserName.exitState();
				dispose();
			}
		});
		
		setVisible(true);
	}

	public String getUIText(String str) {
		String result = propLang.getProperty(str);
		if(result == null) {
			result = propLangDefault.getProperty(str, str);
		}
		return result;
	}
	
	public void saveGlobalConfig() {
		try {
			FileOutputStream out = new FileOutputStream("config/setting/global.cfg");
			propGlobal.store(out, "NullpoMino Global Config");
			out.close();
		} catch (IOException e) {
			log.warn("Failed to save global config file", e);
		}
	}

}

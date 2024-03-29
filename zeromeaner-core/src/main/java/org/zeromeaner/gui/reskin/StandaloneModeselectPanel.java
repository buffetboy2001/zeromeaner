package org.zeromeaner.gui.reskin;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import org.zeromeaner.game.component.RuleOptions;
import org.zeromeaner.game.subsystem.mode.GameMode;
import org.zeromeaner.gui.tool.RuleEditorPanel;
import org.zeromeaner.util.CustomProperties;
import org.zeromeaner.util.LstResourceMap;
import org.zeromeaner.util.ModeList;
import org.zeromeaner.util.Options;
import org.zeromeaner.util.RuleList;

public class StandaloneModeselectPanel extends JPanel {
	private static String formatButtonText(String modeOrRuleName) {
		List<String> lines = new ArrayList<String>(Arrays.asList(modeOrRuleName.split("-+")));
		for(int i = lines.size() - 1; i > 0; i--) {
			if(lines.get(i).length() + lines.get(i-1).length() < 20)
				lines.set(i-1, lines.get(i-1) + " " + lines.remove(i));
		}
		StringBuilder sb = new StringBuilder();
		String sep = "<html><center>";
		for(String line : lines) {
			sb.append(sep);
			sb.append(line);
			sep = "<br>";
		}
		return sb.toString();
	}
	
	private static final String SELECT_CARD = "select";
	private static final String EDIT_CARD = "edit";
	
	private LstResourceMap recommended = new LstResourceMap("config/list/recommended_rules.lst");
	
	private List<ModeButton> modeButtons = new ArrayList<ModeButton>();
	private List<RuleButton> ruleButtons = new ArrayList<RuleButton>();
	
	private ModeButton currentMode;
	private RuleButton currentRule;
	
	private CardLayout cards = new CardLayout();
	
	private RuleOptions custom;
	
	private RuleEditorPanel ruleEditor = new RuleEditorPanel();
	
	private void saveCustom() {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		try {
			CustomProperties p = new CustomProperties();
			custom.writeProperty(p, 0);
			p.store(bout, "Custom Rule");
		} catch(IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}
	
	public StandaloneModeselectPanel() {
		setLayout(cards);
		
		JPanel edit = new JPanel(new BorderLayout());
		add(edit, EDIT_CARD);
		edit.add(ruleEditor, BorderLayout.CENTER);
		edit.add(new JButton(new AbstractAction("Done Editing Custom Rule") {
			@Override
			public void actionPerformed(ActionEvent e) {
				ruleEditor.writeRuleFromUI(custom);
				saveCustom();
				cards.show(StandaloneModeselectPanel.this, SELECT_CARD);
			}
		}), BorderLayout.SOUTH);
		custom = new RuleOptions(RuleList.getRules().getNamed("STANDARD"));
		custom.strRuleName = "CUSTOM RULE";
		custom.resourceName = "config/rule/Custom.rul";
		saveCustom();
		
		JPanel select = new JPanel(new BorderLayout());
		add(select, SELECT_CARD);
		cards.show(this, SELECT_CARD);

		ButtonGroup g = new ButtonGroup();
		JPanel modeButtons = new JPanel(new GridLayout(0, 8, 10, 10));
		modeButtons.setBorder(BorderFactory.createTitledBorder("Available Modes"));
		JPanel p = new JPanel(new BorderLayout());
		for(GameMode mode : ModeList.getModes()) {
			ModeButton b = new ModeButton(mode);
			modeButtons.add(b);
			g.add(b);
			this.modeButtons.add(b);
		}
		p.add(modeButtons, BorderLayout.CENTER);
		select.add(p, BorderLayout.NORTH);
		
		g = new ButtonGroup();
		JPanel ruleButtons = new JPanel(new GridLayout(0, 8, 10, 10));
		ruleButtons.setBorder(BorderFactory.createTitledBorder("Available Rules"));
		p = new JPanel(new BorderLayout());
		RuleList rules = RuleList.getRules();
		rules.add(0, custom);
		for(RuleOptions rule : rules) {
			RuleButton b = new RuleButton(rule);
			
			if(rule == custom) {
				b.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						ruleEditor.readRuleToUI(custom);
						cards.show(StandaloneModeselectPanel.this, EDIT_CARD);
					}
				});
			}
			
			ruleButtons.add(b);
			this.ruleButtons.add(b);
			g.add(b);
			for(ModeButton mb : this.modeButtons) {
				mb.addActionListener(b);
			}
//			if(rule.resourceName.equals(StandaloneMain.propConfig.getProperty("0.rule")))
			if(rule.resourceName.equals(Options.general().RULE_NAME.value()))
				b.setSelected(true);
		}
		p.add(ruleButtons, BorderLayout.CENTER);
		select.add(p, BorderLayout.SOUTH);
		
		for(ModeButton mb : this.modeButtons) {
//			if(mb.mode.getName().equals(StandaloneMain.propConfig.getProperty("name.mode")))
			if(mb.mode.getName().equals(Options.general().MODE_NAME.value()))
				mb.doClick();

		}
	}
	
	private class ModeButton extends JToggleButton {
		private GameMode mode;
		private RuleButton rule;
		
		public ModeButton(GameMode m) {
//			super("<html>" + m.getName().replaceAll("-+", "<br>"));
			super(formatButtonText(m.getName()));
			this.mode = m;
//			setFont(getFont().deriveFont(8f));
			setMargin(new Insets(0, 3, 0, 3));
			addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					currentMode = ModeButton.this;
//					StandaloneMain.propConfig.setProperty("name.mode", mode.getName());
					Options.general().MODE_NAME.set(mode.getName());
//					String ruleResource = StandaloneMain.propConfig.getProperty("mode." + mode.getName() + ".rule");
					String ruleResource = Options.mode(mode.getName()).RULE_RSOURCE.value();
					if(ruleResource != null) {
						for(RuleButton rb : ruleButtons) {
							if(ruleResource.equals(rb.rule.resourceName)) {
								rb.doClick();
								cards.show(StandaloneModeselectPanel.this, SELECT_CARD);
							}
						}
					}
				}
			});
		}
	}
	
	private class RuleButton extends JToggleButton implements ActionListener {
		private RuleOptions rule;
		public RuleButton(RuleOptions r) {
//			super("<html>" + r.strRuleName.replaceAll("-+", "<br>"));
			super(formatButtonText(r.strRuleName));
			this.rule = r;
//			setFont(getFont().deriveFont(8f));
			setMargin(new Insets(0, 3, 0, 3));
			addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if(currentMode != null)
						currentMode.rule = RuleButton.this;
					currentRule = RuleButton.this;
//					StandaloneMain.propConfig.setProperty("0.rule", rule.resourceName);
					Options.general().RULE_NAME.set(rule.resourceName);
//					StandaloneMain.propConfig.setProperty("mode." + currentMode.mode.getName() + ".rule", rule.resourceName);
					Options.mode(currentMode.mode.getName()).RULE_RSOURCE.set(rule.resourceName);
				}
			});
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			if(!(e.getSource() instanceof ModeButton))
				return;
			ModeButton mb = (ModeButton) e.getSource();
			if(recommended.get(mb.mode.getName()).contains(rule.resourceName))
				setBorderPainted(true);
			else
				setBorderPainted(false);
		}
	}
}

package org.tetrevil.nullpo;

import mu.nu.nullpo.game.component.RuleOptions;
import mu.nu.nullpo.util.GeneralUtil;

public class TNRuleOptions extends RuleOptions {
	public TNRuleOptions(RuleOptions opt) {
		copy(GeneralUtil.loadRule("config/rule/Tetrevil.rul"));
		strRandomizer = opt.strRandomizer;
		strWallkick = "mu.nu.nullpo.game.subsystem.wallkick.StandardWallkick";
		strRuleName = "TETREVIL";
		nextDisplay = 0;
		holdEnable = false;
		holdInitial = false;
		minARE = 0;
		maxARE = 0;
		minARELine = 0;
		maxARELine = 0;
		rotateWallkick = true;
	}
}

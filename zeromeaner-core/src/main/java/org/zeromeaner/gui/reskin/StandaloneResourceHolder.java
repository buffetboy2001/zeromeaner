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
package org.zeromeaner.gui.reskin;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.LinkedList;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.zeromeaner.gui.WaveEngine;
import org.zeromeaner.util.MusicList;
import org.zeromeaner.util.Options;

/**
 * Class to the management of image and sound
 */
public class StandaloneResourceHolder {
	/** BackgroundOfcount */
	public static final int BACKGROUND_MAX = 20;

	/** Number of images for block spatter animation during line clears */
	public static final int BLOCK_BREAK_MAX = 8;

	/** Number of image splits for block spatter animation during line clears */
	public static final int BLOCK_BREAK_SEGMENTS = 2;

	/** Number of gem block clear effects */
	public static final int PERASE_MAX = 7;

	/** Log */
	static Logger log = Logger.getLogger(StandaloneResourceHolder.class);

	/** Block images */
	public static LinkedList<Image> imgNormalBlockList, imgSmallBlockList, imgBigBlockList;

	/** Block sticky flag */
	public static LinkedList<Boolean> blockStickyFlagList;

	/** Regular font */
	public static Image imgFont, imgFontSmall;

	/** Field frame */
	public static Image imgFrame;

	/** Field background */
	public static Image imgFieldbg, imgFieldbg2, imgFieldbg2Small, imgFieldbg2Big;

	/** Block spatter animation during line clears */
	public static Image[][] imgBreak;

	/** Effects for clearing gem blocks */
	public static Image[] imgPErase;

	/** In playBackground */
	public static Image[] imgPlayBG;

	/** Audio file management */
	public static WaveEngine soundManager;

	/**
	 * Loading images and sound files
	 */
	public static void load() {
		String skindir = "res";

		// Blocks
		int numBlocks = 0;
		File file = null;
		while(true) {
//			file = new File(skindir + "/graphics/blockskin/normal/n" + numBlocks + ".png");
//			if(file.canRead()) {
//				numBlocks++;
//			} else {
//				break;
//			}
			if(getURL(skindir + "/graphics/blockskin/normal/n" + numBlocks + ".png") != null)
				numBlocks++;
			else
				break;
		}
		log.debug(numBlocks + " block skins found");

		imgNormalBlockList = new LinkedList<Image>();
		imgSmallBlockList = new LinkedList<Image>();
		imgBigBlockList = new LinkedList<Image>();
		blockStickyFlagList = new LinkedList<Boolean>();

		for(int i = 0; i < numBlocks; i++) {
			Image imgNormal = loadImage(getURL(skindir + "/graphics/blockskin/normal/n" + i + ".png"));
			imgNormalBlockList.add(imgNormal);
			imgSmallBlockList.add(loadImage(getURL(skindir + "/graphics/blockskin/small/s" + i + ".png")));
			imgBigBlockList.add(loadImage(getURL(skindir + "/graphics/blockskin/big/b" + i + ".png")));

			if((imgNormal.getWidth(null) >= 400) && (imgNormal.getHeight(null) >= 304)) {
				blockStickyFlagList.add(Boolean.TRUE);
			} else {
				blockStickyFlagList.add(Boolean.FALSE);
			}
		}

		// Other images
		imgFont = loadImage(getURL(skindir + "/graphics/font.png"));
		imgFontSmall = loadImage(getURL(skindir + "/graphics/font_small.png"));
		imgFrame = loadImage(getURL(skindir + "/graphics/frame.png"));
		imgFieldbg = loadImage(getURL(skindir + "/graphics/fieldbg.png"));
		imgFieldbg2 = loadImage(getURL(skindir + "/graphics/fieldbg2.png"));
		imgFieldbg2Small = loadImage(getURL(skindir + "/graphics/fieldbg2_small.png"));
		imgFieldbg2Big = loadImage(getURL(skindir + "/graphics/fieldbg2_big.png"));

		if(Options.standalone().SHOW_LINE_EFFECT.value()) {
			loadLineClearEffectImages();
		}
		if(Options.standalone().SHOW_BG.value()) {
			loadBackgroundImages();
		}

		// music
		MusicList.getInstance();
		
		// Sound effects
		soundManager = new WaveEngine();
		if(Options.standalone().SE_ENABLED.value()) {
			soundManager.load("cursor", skindir + "/se/cursor.wav");
			soundManager.load("decide", skindir + "/se/decide.wav");
			soundManager.load("erase1", skindir + "/se/erase1.wav");
			soundManager.load("erase2", skindir + "/se/erase2.wav");
			soundManager.load("erase3", skindir + "/se/erase3.wav");
			soundManager.load("erase4", skindir + "/se/erase4.wav");
			soundManager.load("died", skindir + "/se/died.wav");
			soundManager.load("gameover", skindir + "/se/gameover.wav");
			soundManager.load("hold", skindir + "/se/hold.wav");
			soundManager.load("holdfail", skindir + "/se/holdfail.wav");
			soundManager.load("initialhold", skindir + "/se/initialhold.wav");
			soundManager.load("initialrotate", skindir + "/se/initialrotate.wav");
			soundManager.load("levelup", skindir + "/se/levelup.wav");
			soundManager.load("linefall", skindir + "/se/linefall.wav");
			soundManager.load("lock", skindir + "/se/lock.wav");
			soundManager.load("move", skindir + "/se/move.wav");
			soundManager.load("pause", skindir + "/se/pause.wav");
			soundManager.load("rotate", skindir + "/se/rotate.wav");
			soundManager.load("step", skindir + "/se/step.wav");
			soundManager.load("piece0", skindir + "/se/piece0.wav");
			soundManager.load("piece1", skindir + "/se/piece1.wav");
			soundManager.load("piece2", skindir + "/se/piece2.wav");
			soundManager.load("piece3", skindir + "/se/piece3.wav");
			soundManager.load("piece4", skindir + "/se/piece4.wav");
			soundManager.load("piece5", skindir + "/se/piece5.wav");
			soundManager.load("piece6", skindir + "/se/piece6.wav");
			soundManager.load("piece7", skindir + "/se/piece7.wav");
			soundManager.load("piece8", skindir + "/se/piece8.wav");
			soundManager.load("piece9", skindir + "/se/piece9.wav");
			soundManager.load("piece10", skindir + "/se/piece10.wav");
			soundManager.load("harddrop", skindir + "/se/harddrop.wav");
			soundManager.load("softdrop", skindir + "/se/softdrop.wav");
			soundManager.load("levelstop", skindir + "/se/levelstop.wav");
			soundManager.load("endingstart", skindir + "/se/endingstart.wav");
			soundManager.load("excellent", skindir + "/se/excellent.wav");
			soundManager.load("b2b_start", skindir + "/se/b2b_start.wav");
			soundManager.load("b2b_continue", skindir + "/se/b2b_continue.wav");
			soundManager.load("b2b_end", skindir + "/se/b2b_end.wav");
			soundManager.load("gradeup", skindir + "/se/gradeup.wav");
			soundManager.load("countdown", skindir + "/se/countdown.wav");
			soundManager.load("tspin0", skindir + "/se/tspin0.wav");
			soundManager.load("tspin1", skindir + "/se/tspin1.wav");
			soundManager.load("tspin2", skindir + "/se/tspin2.wav");
			soundManager.load("tspin3", skindir + "/se/tspin3.wav");
			soundManager.load("ready", skindir + "/se/ready.wav");
			soundManager.load("go", skindir + "/se/go.wav");
			soundManager.load("movefail", skindir + "/se/movefail.wav");
			soundManager.load("rotfail", skindir + "/se/rotfail.wav");
			soundManager.load("medal", skindir + "/se/medal.wav");
			soundManager.load("change", skindir + "/se/change.wav");
			soundManager.load("bravo", skindir + "/se/bravo.wav");
			soundManager.load("cool", skindir + "/se/cool.wav");
			soundManager.load("regret", skindir + "/se/regret.wav");
			soundManager.load("garbage", skindir + "/se/garbage.wav");
			soundManager.load("stageclear", skindir + "/se/stageclear.wav");
			soundManager.load("stagefail", skindir + "/se/stagefail.wav");
			soundManager.load("gem", skindir + "/se/gem.wav");
			soundManager.load("danger", skindir + "/se/danger.wav");
			soundManager.load("matchend", skindir + "/se/matchend.wav");
			soundManager.load("hurryup", skindir + "/se/hurryup.wav");
			soundManager.load("square_s", skindir + "/se/square_s.wav");
			soundManager.load("square_g", skindir + "/se/square_g.wav");

			for(int i = 0; i < 20; i++) {
				soundManager.load("combo" + (i + 1), skindir + "/se/combo" + (i + 1) + ".wav");
			}

			soundManager.setVolume(Options.standalone().SE_VOLUME.value());
		}
	}

	/**
	 * Load background images.
	 */
	public static void loadBackgroundImages() {
		if(imgPlayBG == null) {
			imgPlayBG = new Image[BACKGROUND_MAX];

			String skindir = "res";
			for(int i = 0; i < BACKGROUND_MAX; i++) {
				imgPlayBG[i] = loadImage(getURL(skindir + "/graphics/back" + i + ".png"));
				Graphics g = (Graphics2D) imgPlayBG[i].getGraphics();
//				g.setColor(new Color(255, 255, 255, 192));
//				for(int j = 0; j < imgPlayBG[i].getHeight(null); j += 2) {
//					g.fillRect(0, j, imgPlayBG[i].getWidth(null), 1);
//				}
				Image img = new BufferedImage(imgPlayBG[i].getWidth(null), imgPlayBG[i].getHeight(null), BufferedImage.TYPE_INT_ARGB);
				g = img.getGraphics();
				g.drawImage(imgPlayBG[i], 0, 0, null);
				imgPlayBG[i] = img;
			}
		}
	}

	/**
	 * Load line clear effect images.
	 */
	public static void loadLineClearEffectImages() {
		String skindir = "res";

		if(imgBreak == null) {
			imgBreak = new Image[BLOCK_BREAK_MAX][BLOCK_BREAK_SEGMENTS];

			for(int i = 0; i < BLOCK_BREAK_MAX; i++) {
				for(int j = 0; j < BLOCK_BREAK_SEGMENTS; j++) {
					imgBreak[i][j] = loadImage(getURL(skindir + "/graphics/break" + i + "_" + j + ".png"));
				}
			}
		}
		if(imgPErase == null) {
			imgPErase = new Image[PERASE_MAX];

			for(int i = 0; i < imgPErase.length; i++) {
				imgPErase[i] = loadImage(getURL(skindir + "/graphics/perase" + i + ".png"));
			}
		}
	}

	/**
	 * Load an image
	 * @param url Image filesURL
	 * @return Image file (Failurenull)
	 */
	public static BufferedImage loadImage(URL url) {
		BufferedImage img = null;
		try {
			img = ImageIO.read(url);
		} catch (Throwable e) {
			try {
				img = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
			} catch (Throwable e2) {}
		}

		return img;
	}

	/**
	 * Resource FilesURLReturns
	 * @param str Filename
	 * @return Resource FilesURL
	 */
	public static URL getURL(String str) {
		
		URL url = StandaloneResourceHolder.class.getClassLoader().getResource("org/zeromeaner/" + str);
		return url;
		
//		URL url = null;
//
//		try {
//			char sep = File.separator.charAt(0);
//			String file = str.replace(sep, '/');
//
//			// Reference(Annihilation):http://www.asahi-net.or.jp/~DP8T-ASM/java/tips/HowToMakeURL.html
//			if(file.charAt(0) != '/') {
//				String dir = System.getProperty("user.dir");
//				dir = dir.replace(sep, '/') + '/';
//				if(dir.charAt(0) != '/') {
//					dir = "/" + dir;
//				}
//				file = dir + file;
//			}
//			url = new URL("file", "", file);
//		} catch(MalformedURLException e) {
//			log.warn("Invalid URL:" + str, e);
//			return null;
//		}
//
//		return url;
	}
}

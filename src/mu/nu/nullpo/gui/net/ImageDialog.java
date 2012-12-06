package mu.nu.nullpo.gui.net;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JDialog;

import mu.nu.nullpo.gui.slick.NullpoMinoSlick;

public class ImageDialog extends JDialog {

  private Image img;

  public ImageDialog(String img) {
    this(new ImageIcon(img).getImage());
  }

  public ImageDialog(Image img) {
    this.img = img;
    Dimension size = new Dimension(img.getWidth(null), img.getHeight(null));
    setPreferredSize(size);
    setMinimumSize(size);
    setMaximumSize(size);
    setSize(size);
    setLayout(null);
    setModal(true);
    setLocationRelativeTo(NullpoMinoSlick.mainFrame);
    setVisible(true);
  }

  public void paintComponent(Graphics g) {
    g.drawImage(img, 0, 0, null);
  }

}
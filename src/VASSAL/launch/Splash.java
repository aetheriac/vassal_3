/*
 * $Id$
 *
 * Copyright (c) 2008 by Joel Uckelman
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License (LGPL) as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, copies are available
 * at http://www.opensource.org.
 */

package VASSAL.launch;

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class Splash extends JPanel {
  public static final long serialVersionUID = 1L;

//  private final BufferedImage img;
  private final Timer timer;
  private int alpha = 0;

  private static final BufferedImage cimg;
//  private static final BufferedImage gimg;

  static {
    final ImageIcon icon =
      new ImageIcon(Splash.class.getResource("/images/Splash.png"));
    final Image i = icon.getImage();

    cimg = new BufferedImage(i.getWidth(null), i.getHeight(null),
                             BufferedImage.TYPE_INT_ARGB);
    final Graphics2D g = cimg.createGraphics();
    g.drawImage(i, 0, 0, null);
    g.dispose();
  }

/*
  static {
    final ImageIcon icon =
      new ImageIcon(Splash.class.getResource("/images/Splash-grey.png"));
    final Image i = icon.getImage();

    gimg = new BufferedImage(i.getWidth(null), i.getHeight(null),
                             BufferedImage.TYPE_INT_ARGB);
    final Graphics2D g = gimg.createGraphics();
    g.drawImage(i, 0, 0, null);
    g.dispose();
  }
*/

  public Splash() {
/*
    final ImageIcon icon =
      new ImageIcon(getClass().getResource("/images/Splash.png"));
    final Image i = icon.getImage();

    img = new BufferedImage(i.getWidth(null), i.getHeight(null),
                            BufferedImage.TYPE_INT_ARGB);
    final Graphics2D g = img.createGraphics();
    g.drawImage(i, 0, 0, null);
    g.dispose();    
*/
    timer = new Timer(true);
    timer.schedule(new TimerTask() {
      private int delta = 5;
      private int wait = -1;

      public void run() {
        if (wait <= 0) repaint();

        if (alpha == 100) {
          if (wait++ > 20) {
            wait = -1;
            delta = -2;
          }
          else delta = 0;
        }
        else if (alpha == 0 && delta < 0) {
          delta = 0;
          timer.cancel();
          setVisible(false);
        }

        alpha += delta;
      }
    }, 0l, 50l);
  }

  public void paintComponent(Graphics g) {
    super.paintComponent(g);
/*
    final int x = (getWidth() - cimg.getWidth())/2;
    final int y = (getHeight() - cimg.getHeight())/2;

    final Graphics2D g2d = (Graphics2D) g;

    g2d.drawImage(gimg, x, y, this);
    g2d.setComposite(
      AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha/100.0f));
    g2d.drawImage(cimg, x, y, this);
    g2d.setComposite(AlphaComposite.Src);
*/

    final Graphics2D g2d = (Graphics2D) g;

    g2d.setComposite(
      AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha/100.0f));

    final int x = (getWidth() - cimg.getWidth())/2;
    final int y = (getHeight() - cimg.getHeight())/2;

    g2d.drawImage(cimg, x, y, this);
    g2d.setComposite(AlphaComposite.Src);
  }
}

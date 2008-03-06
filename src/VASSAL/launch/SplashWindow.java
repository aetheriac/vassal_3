package VASSAL.launch;

import java.awt.AlphaComposite;
import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JWindow;

public class SplashWindow extends JWindow {
  private Image bg;
  private BufferedImage fg;
  private final Dimension screen;

  
  public SplashWindow() {
    final ImageIcon icon = 
      new ImageIcon(Splash.class.getResource("/images/Splash.png"));
    final Image i = icon.getImage();

    fg = new BufferedImage(i.getWidth(null), i.getHeight(null),
                           BufferedImage.TYPE_INT_ARGB);
    final Graphics2D g = fg.createGraphics();
    g.drawImage(i, 0, 0, null);
    g.dispose();

    screen = Toolkit.getDefaultToolkit().getScreenSize();
    
    setBounds(screen.width / 2 - fg.getWidth() / 2,
              screen.height / 2 - fg.getHeight() / 2,
              fg.getWidth(), fg.getHeight());
    
    addMouseListener(new MouseAdapter() {
      public void mouseReleased(MouseEvent evt) {
        setVisible(false);
        dispose();
      }
    });    

    try {
      bg = new Robot().createScreenCapture(getBounds());
    }
    catch (AWTException e) {
    }

    add(new Splash());
//    pack();
  }

  public void paint(Graphics g) {
//    g.drawImage(bg, 0, 0, this);
    paintComponents(g);
  }

  protected class Splash extends JPanel {
    private final Timer timer;
    private int alpha = 0;

    public Splash() {
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
            SplashWindow.this.setVisible(false);
          }
  
          alpha += delta;
        }
      }, 0l, 50l);
    } 
  
    public void paintComponent(Graphics g) {
//      super.paintComponent(g);

      final Graphics2D g2d = (Graphics2D) g;

 //     g2d.drawImage(bg, 0, 0, this);

      g2d.setComposite(
        AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.05f));

      g2d.drawImage(fg, 0, 0, this);
      g2d.setComposite(AlphaComposite.Src);
    }
  }

  public static void main(String[] args) {
    new SplashWindow().setVisible(true);
  }
}

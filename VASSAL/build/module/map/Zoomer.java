/*
 * $Id$
 *
 * Copyright (c) 2000-2007 by Rodney Kinney, Joel Uckelman
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License (LGPL) as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, copies are available
 * at http://www.opensource.org.
 */
package VASSAL.build.module.map;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Enumeration;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.JWindow;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.basic.BasicArrowButton;
import VASSAL.build.AbstractConfigurable;
import VASSAL.build.AutoConfigurable;
import VASSAL.build.Buildable;
import VASSAL.build.GameModule;
import VASSAL.build.module.GameComponent;
import VASSAL.build.module.Map;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.build.module.map.boardPicker.Board;
import VASSAL.configure.Configurer;
import VASSAL.configure.ConfigurerFactory;
import VASSAL.configure.IconConfigurer;
import VASSAL.configure.SingleChildInstance;
import VASSAL.tools.BackgroundTask;
import VASSAL.tools.LaunchButton;

/**
 * Controls the zooming in/out of a Map Window
 */
public class Zoomer extends AbstractConfigurable implements GameComponent {
  protected Map map;
  protected double zoom = 1.0;
  protected double zoomStep = 1.5;
  protected int zoomLevel = 0;
  protected int zoomStart = 1;
  protected int zoomFull = 1;
  protected double[] zoomFactor;
  protected int maxZoom = 3;
  protected LaunchButton zoomInButton;
  protected LaunchButton zoomOutButton;

  protected ZoomField zoomField;
  protected JButton zoomButton;
  protected ZoomMenu zoomMenu; 

  public Zoomer() {
    ActionListener zoomIn = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        zoomIn();
      }
    };

    ActionListener zoomOut = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        zoomOut();
      }
    };

/*
    ActionListener zoomOut = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (zoomLevel < zoomFactor.length - 1) {
          final JWindow w = new JWindow(SwingUtilities.getWindowAncestor(map.getView()));
          w.getContentPane().setBackground(Color.white);
          JLabel l = new JLabel("Scaling Map ...");
          l.setFont(new Font("Dialog",Font.PLAIN,48));
          l.setBackground(Color.white);
          l.setForeground(Color.black);
          l.setBorder(new BevelBorder(BevelBorder.RAISED,Color.lightGray,Color.darkGray));
          w.getContentPane().add(l);
          w.pack();
          Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
          w.setLocation(d.width/2-w.getSize().width/2,d.height/2-w.getSize().height/2);
          final Vector finished = new Vector();
          Runnable runnable = new Runnable() {
            public void run() {
              try {
                Thread.sleep(100);
                if (!finished.contains(w)) {
                  w.setVisible(true);
                }
              }
              catch (InterruptedException e1) {
              }
            }
          };
          new Thread(runnable).start();
          BackgroundTask task = new BackgroundTask() {
            public void doFirst() {
              scaleBoards(zoomFactor[zoomLevel + 1]);
            }

            public void doLater() {
              zoomOut();
              finished.add(w);
              w.dispose();
            }
          };
          task.start();
        }
      }
    };
*/

///////////
    zoomField = new ZoomField();
    zoomMenu = new ZoomMenu();
//    zoomButton = new JButton("v");
    zoomButton = new BasicArrowButton(BasicArrowButton.SOUTH);

    zoomButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        zoomMenu.show(zoomButton, 0, zoomButton.getHeight());
      }
    });
///////////

    //zoomInButton = new LaunchButton("Z", null, ZOOM_IN, zoomIn);
    zoomInButton = new LaunchButton(null, IN_TOOLTIP, IN_BUTTON_TEXT, ZOOM_IN, IN_ICON_NAME, zoomIn);
    zoomInButton.setAttribute(IN_TOOLTIP, "Zoom in");
    zoomInButton.setAttribute(IN_ICON_NAME, IN_DEFAULT_ICON);
    //zoomInButton.setEnabled(false);
    //zoomOutButton = new LaunchButton("z", null, ZOOM_OUT, zoomOut);
    zoomOutButton = new LaunchButton(null, OUT_TOOLTIP, OUT_BUTTON_TEXT, ZOOM_OUT, OUT_ICON_NAME, zoomOut);
    zoomOutButton.setAttribute(OUT_TOOLTIP, "Zoom out");
    zoomOutButton.setAttribute(OUT_ICON_NAME, OUT_DEFAULT_ICON);

    setConfigureName(null);
  }

  public static String getConfigureTypeName() {
    return "Zoom capability";
  }

  public String[] getAttributeNames() {
    return new String[]{FACTOR, MAX, ZOOM_START, FULL_SIZE, 
        IN_TOOLTIP, IN_BUTTON_TEXT, IN_ICON_NAME, ZOOM_IN, 
        OUT_TOOLTIP, OUT_BUTTON_TEXT, OUT_ICON_NAME, ZOOM_OUT};
  }

  public String[] getAttributeDescriptions() {
    return new String[]{"Magnification factor",
                        "Number of zoom levels",
                        "Starting zoom level",
                        "Full-size zoom level",
                        "Zoom in tooltip text",
                        "Zoom in button text",
                        "Zoom in Icon",
                        "Zoom in hotkey",
                        "Zoom out tooltip text",
                        "Zoom out button text",
                        "Zoom out Icon",
                        "Zoom out hotkey"};
  }

  public Class[] getAttributeTypes() {
    return new Class[]{Double.class,
                       Integer.class,
                       Integer.class,
                       Integer.class,
                       String.class,
                       String.class,
                       InIconConfig.class,
                       KeyStroke.class,
                       String.class,
                       String.class,
                       OutIconConfig.class,
                       KeyStroke.class};
  }

  public static class InIconConfig implements ConfigurerFactory {
    public Configurer getConfigurer(AutoConfigurable c, String key, String name) {
      return new IconConfigurer(key, name, IN_DEFAULT_ICON);
    }
  }
  
  public static class OutIconConfig implements ConfigurerFactory {
    public Configurer getConfigurer(AutoConfigurable c, String key, String name) {
      return new IconConfigurer(key, name, OUT_DEFAULT_ICON);
    }
  }
  
  protected static final String FACTOR = "factor";
  protected static final String MAX = "max";
  protected static final String ZOOM_START = "zoomStart";
  protected static final String FULL_SIZE = "zoomFull";
  
  protected static final String ZOOM_IN = "zoomInKey";
  protected static final String IN_TOOLTIP = "inTooltip";
  protected static final String IN_BUTTON_TEXT = "inButtonText";
  protected static final String IN_ICON_NAME = "inIconName";
  protected static final String IN_DEFAULT_ICON = "/images/zoomIn.gif";
  
  protected static final String ZOOM_OUT = "zoomOutKey";
  protected static final String OUT_TOOLTIP = "outTooltip";
  protected static final String OUT_BUTTON_TEXT = "outButtonText";
  protected static final String OUT_ICON_NAME = "outIconName";
  protected static final String OUT_DEFAULT_ICON = "/images/zoomOut.gif";
  
  public void addTo(Buildable b) {
    GameModule.getGameModule().getGameState().addGameComponent(this);

    map = (Map) b;

    validator = new SingleChildInstance(map,getClass());

    map.setZoomer(this);
    map.getToolBar().add(zoomInButton);
//    java.net.URL image = getClass().getResource("/images/zoomIn.gif");
//    if (image != null) {
//      zoomInButton.setIcon(new ImageIcon(image));
//      zoomInButton.setText("");
//    }
    map.getToolBar().add(zoomField);
    map.getToolBar().add(zoomButton);

    map.getToolBar().add(zoomOutButton);
//    image = getClass().getResource("/images/zoomOut.gif");
//    if (image != null) {
//      zoomOutButton.setIcon(new ImageIcon(image));
//      zoomOutButton.setText("");
//    }

  }

  public String getAttributeValueString(String key) {
    if (MAX.equals(key)) {
      return "" + maxZoom;
    } 
    else if (ZOOM_START.equals(key)) {
	  return "" + zoomStart;
    }
    else if (FULL_SIZE.equals(key)) {
	  return "" + zoomFull;
    }
    else if (FACTOR.equals(key)) {
      return "" + zoomStep;
    }
    else if (zoomInButton.getAttributeValueString(key) != null) {
      return zoomInButton.getAttributeValueString(key);
    }
    else {
      return zoomOutButton.getAttributeValueString(key);
    }
  }

  public void setAttribute(String key, Object val) {
    if (MAX.equals(key)) {
      if (val instanceof String) {
        val = new Integer((String) val);
      }
      if (val != null) {
        maxZoom = ((Integer) val).intValue();
      }
      initZoomFactors();
    }
	 else if (ZOOM_START.equals(key)) {
	  if (val instanceof String) {
		val = new Integer((String) val);
	  }
	  if (val != null) {
		zoomStart = ((Integer) val).intValue();
	  }
	  if (zoomStart < 1) {
	  	 zoomStart = 1;
	  }
	  if (zoomStart > maxZoom) {
	  	 zoomStart = maxZoom;
	  }
	  initZoomFactors();
	}
   else if (FULL_SIZE.equals(key)) {
	  if (val instanceof String) {
		val = new Integer((String) val);
	  }
	  if (val != null) {
		zoomFull = ((Integer) val).intValue();
	  }
	  if (zoomFull < 1) {
	  	 zoomFull = 1;
	  }
	  if (zoomFull > maxZoom) {
	  	 zoomFull = maxZoom;
	  }
	  initZoomFactors();
	}
   else if (FACTOR.equals(key)) {
      if (val instanceof String) {
        val = new Double((String) val);
      }
      if (val != null) {
        zoomStep = ((Double) val).doubleValue();
      }
      initZoomFactors();
    }
    else {
      zoomInButton.setAttribute(key, val);
      zoomOutButton.setAttribute(key, val);
    }
  }

  private void initZoomFactors() {
    zoomFactor = new double[maxZoom];
    for (int i = 0; i < zoomFactor.length; ++i) {
      zoomFactor[i] = Math.pow(zoomStep, zoomFull - i - 1);
    }

    if (zoomStart < 1) {
    	zoomLevel = 0;
    }
    else if (zoomStart > maxZoom) {
    	zoomLevel = maxZoom-1;
    }
    else {
    	zoomLevel = zoomStart-1;
    }
	 
    zoomInButton.setEnabled(zoomLevel > 0);
	  zoomOutButton.setEnabled(zoomLevel < maxZoom - 1);

    zoomField.setText(formatZoomPct(zoomFactor[zoomLevel]*100));
    zoomMenu.initZoomItems(); 
  }

  public Class[] getAllowableConfigureComponents() {
    return new Class[0];
  }

  public void removeFrom(Buildable b) {
    map = (Map) b;
    map.setZoomer(null);
    map.getToolBar().remove(zoomInButton);
    map.getToolBar().remove(zoomField);
    map.getToolBar().remove(zoomButton);
    map.getToolBar().remove(zoomOutButton);
  }

  public double getZoomFactor() {
    return zoom;
  }

  private void scaleBoards(double z) {
    for (Enumeration e = map.getAllBoards(); e.hasMoreElements();) {
      Board b = (Board) e.nextElement();
      b.getScaledImage(z, map.getView());
    }
  }

  public void setZoomFactor(final double z) {
    final JWindow w =
      new JWindow(SwingUtilities.getWindowAncestor(map.getView()));
    w.getContentPane().setBackground(Color.white);
    JLabel l = new JLabel("Scaling Map ...");
    l.setFont(new Font("Dialog",Font.PLAIN,48));
    l.setBackground(Color.white);
    l.setForeground(Color.black);
    l.setBorder(new BevelBorder(BevelBorder.RAISED,Color.lightGray,
                                Color.darkGray));
    w.getContentPane().add(l);
    w.pack();
    Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
    w.setLocation(d.width/2-w.getSize().width/2,
                  d.height/2-w.getSize().height/2);
    
    final Vector finished = new Vector();

    Runnable runnable = new Runnable() {
      public void run() {
        try {
          Thread.sleep(100);
          if (!finished.contains(w)) {
            w.setVisible(true);
          }
        }
        catch (InterruptedException e1) {
        }
      }
    };
 
    new Thread(runnable).start();
    
    BackgroundTask task = new BackgroundTask() {
      public void doFirst() {
        scaleBoards(z);
      }

      public void doLater() {
        Rectangle r = map.getView().getVisibleRect();
        Point center = new Point(r.x + r.width / 2, r.y + r.height / 2);
        center = map.mapCoordinates(center);

        zoom = z;

        int i = 0;
        for ( ; zoom < zoomFactor[i] && i < zoomFactor.length; ++i);
        
        zoomInButton.setEnabled(i > 0);
        zoomOutButton.setEnabled(i < maxZoom - 1);
    
        zoomMenu.updateZoom();
        zoomField.updateZoom();
     
        map.centerAt(center);
        map.repaint(true);
        map.getView().revalidate();

        finished.add(w);
        w.dispose();
      }
    };
    
    task.start();

/*    
    Rectangle r = map.getView().getVisibleRect();
    Point center = new Point(r.x + r.width / 2, r.y + r.height / 2);
    center = map.mapCoordinates(center);

    zoom = z;

    zoomMenu.updateZoom();
    zoomField.updateZoom();
     
    map.centerAt(center);
    map.repaint(true);
    map.getView().revalidate();
*/
  }

  public void zoomIn() {
    if (zoomInButton.isEnabled()) {
      // find next level
      for (zoomLevel = zoomFactor.length - 1;
           zoomFactor[zoomLevel] <= zoom && zoomLevel >= 0;
           --zoomLevel);
    
//      zoomInButton.setEnabled(zoomLevel > 0);
//      zoomOutButton.setEnabled(zoomLevel < maxZoom - 1);

      if (zoomLevel >= 0)
        setZoomFactor(zoomFactor[zoomLevel]);
      else zoomLevel = -1;
    }
  }

  public void zoomOut() {
    if (zoomOutButton.isEnabled()) {
      // find next level
      for (zoomLevel = 0;
           zoomFactor[zoomLevel] >= zoom && zoomLevel < zoomFactor.length;
           ++zoomLevel);

//      zoomInButton.setEnabled(zoomLevel > 0);
//      zoomOutButton.setEnabled(zoomLevel < maxZoom - 1);

      if (zoomLevel < zoomFactor.length)
        setZoomFactor(zoomFactor[zoomLevel]);
      else zoomLevel = -1;
    }
  }

  public VASSAL.build.module.documentation.HelpFile getHelpFile() {
    return HelpFile.getReferenceManualPage("Map.htm", "Zoom");
  }

  public void setup(boolean gameStarting) {
    if (!gameStarting) {
      zoomLevel = zoomStart-1;
	    zoomInButton.setEnabled(zoomLevel > 0);
  	  zoomOutButton.setEnabled(zoomLevel < maxZoom - 1);
    }

    zoomField.setEnabled(gameStarting);
    zoomButton.setEnabled(gameStarting);
  }

  public VASSAL.command.Command getRestoreCommand() {
    return null;
  }

  protected class ZoomMenu extends JPopupMenu
                           implements ActionListener {
    public JRadioButtonMenuItem dummy;
    protected JMenuItem fw;
    protected JMenuItem fh;
    protected JMenuItem fv;
    protected JPopupMenu.Separator sep;
    protected ButtonGroup bg;

    public ZoomMenu() {
      super();

      sep = new JPopupMenu.Separator();
      add(sep);

      bg = new ButtonGroup();

      fw = new JMenuItem("Fit Width");
      fw.setActionCommand("Fit Width");
      fw.addActionListener(this);
      add(fw);

      fh = new JMenuItem("Fit Height");
      fh.setActionCommand("Fit Height");
      fh.addActionListener(this);
      add(fh);

      fv = new JMenuItem("Fit Visible");
      fv.setActionCommand("Fit Visible");
      fv.addActionListener(this);
      add(fv);

      dummy = new JRadioButtonMenuItem();
      bg.add(dummy);
    }

    public void initZoomItems() {
      while (getComponent(0) != sep) remove(0);

      for (int i = 0; i < zoomFactor.length; ++i) {
        String zs = formatZoomPct(zoomFactor[i]*100);
        JMenuItem item = new JRadioButtonMenuItem(zs);
        item.setActionCommand(Integer.toString(i));
        item.addActionListener(this);
        bg.add(item);
        zoomMenu.insert(item, i);
      }

      ((JRadioButtonMenuItem)getComponent(zoomLevel)).setSelected(true);
    }

    public void actionPerformed(ActionEvent a) {
      try {
        int i = Integer.parseInt(a.getActionCommand());
        zoomLevel = i;
        setZoomFactor(zoomFactor[zoomLevel]);
        return;
      }
      catch (NumberFormatException e) {
      }
      
      // FIXME: should be map.getSize() for consistency?
      if (a.getActionCommand().equals("Fit Width")) {
        Dimension vd = map.getView().getVisibleRect().getSize();
        Dimension md = map.mapSize();
        zoomLevel = -1;
        setZoomFactor(vd.getWidth()/md.getWidth());
      }
      else if (a.getActionCommand().equals("Fit Height")) {
        Dimension vd = map.getView().getVisibleRect().getSize();
        Dimension md = map.mapSize();
        zoomLevel = -1;
        setZoomFactor(vd.getHeight()/md.getHeight());
      }
      else if (a.getActionCommand().equals("Fit Visible")) {
        Dimension vd = map.getView().getVisibleRect().getSize();
        Dimension md = map.mapSize();
        zoomLevel = -1;
        setZoomFactor(Math.min(vd.getWidth()/md.getWidth(),
                               vd.getHeight()/md.getHeight()));
      }
    }

    public void updateZoom() {
      if (zoomLevel < 0)
        dummy.setSelected(true); 
      else
        ((JRadioButtonMenuItem)getComponent(zoomLevel)).setSelected(true);
    }
  }

  protected class ZoomField extends JTextField {
    public ZoomField() {
      super(4);

      addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          validateInput();
        }
      });

      addFocusListener(new FocusAdapter() {
        public void focusLost(FocusEvent e) {
          validateInput();
        }
      });
    }

    protected void validateInput() {
      Pattern p = Pattern.compile("\\s*(\\.\\d+|\\d+(?:\\.\\d+)?)\\s*%?\\s*");
      Matcher m = p.matcher(getText());
      if (m.matches()) {
        try {
          double z = Double.parseDouble(m.group(1));
          // zoom must be within [0.01,6400] (approximately 1:64 - 64:1)
          if (Math.rint(z*100)/100 >= 0.01 &&
              Math.rint(z*100)/100 <= 6400) {
            // input is ok
            String zs = formatZoomPct(z);
            setText(zs);
        
            for (int i = 0; i < zoomFactor.length; ++i) {
              if (Math.abs(zoomFactor[i]*100 - z) < 0.005) {
                ((JRadioButtonMenuItem)zoomMenu.getComponent(i))
                                               .setSelected(true);
                if (zoomLevel == i) return;
                zoomLevel = i;
                setZoomFactor(zoomFactor[zoomLevel]);
                return;
              }
            }

            if (Math.abs(zoom - z/100) < 0.005) return;
            zoomLevel = -1;
            setZoomFactor(z/100);
            return;
          }
        }
        catch (NumberFormatException e) {
        }
      }

      // input is bad
      setText(formatZoomPct(zoom*100));
    }

    public void updateZoom() {
      setText(formatZoomPct(zoom*100));
    }
  }

  protected static String formatZoomPct(double z) {
    String ret;

    if (Math.abs(Math.rint(z) - z) < 0.005) {
      ret = Integer.toString((int)z);
    }
    else if (Math.abs(Math.rint(z*10) - z*10) < 0.005) {
      ret = Double.toString(Math.rint(z*10)/10);
    }
    else {
      ret = Double.toString(Math.rint(z*100)/100);
    } 
    
    return ret + "%";
  }
}

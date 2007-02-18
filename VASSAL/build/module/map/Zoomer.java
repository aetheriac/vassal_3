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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JWindow;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
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
import VASSAL.configure.ListConfigurer;
import VASSAL.configure.SingleChildInstance;
import VASSAL.configure.StringArrayConfigurer;
import VASSAL.tools.BackgroundTask;
import VASSAL.tools.LaunchButton;

/**
 * Controls the zooming in/out of a Map Window
 */
public class Zoomer extends AbstractConfigurable implements GameComponent {
  protected Map map;
  protected double zoom = 1.0;
//  protected double zoomStep = 1.6;
  protected int zoomLevel = 0;
  protected int zoomStart = 2;
//  protected int zoomFull = 1;
  protected double[] zoomFactor;
  protected int maxZoom = 4;
  protected LaunchButton zoomInButton;
  protected LaunchButton zoomPickButton;
  protected LaunchButton zoomOutButton;

  protected TreeSet zoomLevels = new TreeSet();

  protected ZoomMenu zoomMenu; 

  public Zoomer() {
    zoomLevels.add(new Double(1/1.6/1.6));
    zoomLevels.add(new Double(1/1.6));
    zoomLevels.add(new Double(1.0));
    zoomLevels.add(new Double(1.6));

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

    zoomMenu = new ZoomMenu();

    ActionListener zoomPick = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        zoomMenu.show(zoomPickButton, 0, zoomPickButton.getHeight());
      } 
    };

    zoomPickButton = new LaunchButton(null, PICK_TOOLTIP, PICK_BUTTON_TEXT,
      ZOOM_PICK, PICK_ICON_NAME, zoomPick); 
    zoomPickButton.setAttribute(PICK_TOOLTIP, "Select Zoom");
    zoomPickButton.setAttribute(PICK_ICON_NAME, PICK_DEFAULT_ICON);

    zoomInButton = new LaunchButton(null, IN_TOOLTIP, IN_BUTTON_TEXT,
      ZOOM_IN, IN_ICON_NAME, zoomIn);
    zoomInButton.setAttribute(IN_TOOLTIP, "Zoom in");
    zoomInButton.setAttribute(IN_ICON_NAME, IN_DEFAULT_ICON);

    zoomOutButton = new LaunchButton(null, OUT_TOOLTIP, OUT_BUTTON_TEXT,
      ZOOM_OUT, OUT_ICON_NAME, zoomOut);
    zoomOutButton.setAttribute(OUT_TOOLTIP, "Zoom out");
    zoomOutButton.setAttribute(OUT_ICON_NAME, OUT_DEFAULT_ICON);

    setConfigureName(null);
  }

  public static String getConfigureTypeName() {
    return "Zoom capability";
  }

  public String[] getAttributeNames() {
    return new String[]{
//                         FACTOR,
//                         MAX,
                         ZOOM_START,
//                         FULL_SIZE,
                         ZOOM_LEVELS,
                         IN_TOOLTIP,
                         IN_BUTTON_TEXT,
                         IN_ICON_NAME,
                         ZOOM_IN,
                         PICK_TOOLTIP,
                         PICK_BUTTON_TEXT,
                         PICK_ICON_NAME,
                         ZOOM_PICK,
                         OUT_TOOLTIP,
                         OUT_BUTTON_TEXT,
                         OUT_ICON_NAME,
                         ZOOM_OUT
                        };
  }

  public String[] getAttributeDescriptions() {
    return new String[]{ 
//                         "Magnification factor",
//                         "Number of zoom levels",
                         "Starting zoom level",
//                         "Full-size zoom level",
                         "Preset zoom levels",
                         "Zoom in tooltip text",
                         "Zoom in button text",
                         "Zoom in Icon",
                         "Zoom in hotkey",
                         "Zoom select tooltip text",
                         "Zoom select button text",
                         "Zoom select Icon",
                         "Zoom select hotkey",
                         "Zoom out tooltip text",
                         "Zoom out button text",
                         "Zoom out Icon",
                         "Zoom out hotkey"};
  }

  public Class[] getAttributeTypes() {
    return new Class[]{ 
//                        Double.class,
//                        Integer.class,
                        Integer.class,
//                        Integer.class,
                        String[].class,
                        String.class,
                        String.class,
                        InIconConfig.class,
                        KeyStroke.class,
                        String.class,
                        String.class,
                        PickIconConfig.class,
                        KeyStroke.class,
                        String.class,
                        String.class,
                        OutIconConfig.class,
                        KeyStroke.class};
  }

  public static class InIconConfig implements ConfigurerFactory {
    public Configurer getConfigurer(AutoConfigurable c,
                                    String key, String name) {
      return new IconConfigurer(key, name, IN_DEFAULT_ICON);
    }
  }
  
  public static class PickIconConfig implements ConfigurerFactory {
    public Configurer getConfigurer(AutoConfigurable c,
                                    String key, String name) {
      return new IconConfigurer(key, name, PICK_DEFAULT_ICON);
    }
  }

  public static class OutIconConfig implements ConfigurerFactory {
    public Configurer getConfigurer(AutoConfigurable c,
                                    String key, String name) {
      return new IconConfigurer(key, name, OUT_DEFAULT_ICON);
    }
  }
  
//  protected static final String FACTOR = "factor";
//  protected static final String MAX = "max";
  protected static final String ZOOM_START = "zoomStart";
//  protected static final String FULL_SIZE = "zoomFull";
  protected static final String ZOOM_LEVELS = "zoomLevels";

  protected static final String ZOOM_IN = "zoomInKey";
  protected static final String IN_TOOLTIP = "inTooltip";
  protected static final String IN_BUTTON_TEXT = "inButtonText";
  protected static final String IN_ICON_NAME = "inIconName";
  protected static final String IN_DEFAULT_ICON = "/images/zoomIn.png";
  
  protected static final String ZOOM_PICK = "zoomPickKey";
  protected static final String PICK_TOOLTIP = "pickTooltip";
  protected static final String PICK_BUTTON_TEXT = "pickButtonText";
  protected static final String PICK_ICON_NAME = "pickIconName";
  protected static final String PICK_DEFAULT_ICON = "/images/zoom.png";

  protected static final String ZOOM_OUT = "zoomOutKey";
  protected static final String OUT_TOOLTIP = "outTooltip";
  protected static final String OUT_BUTTON_TEXT = "outButtonText";
  protected static final String OUT_ICON_NAME = "outIconName";
  protected static final String OUT_DEFAULT_ICON = "/images/zoomOut.png";
  
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
    map.getToolBar().add(zoomPickButton);
    map.getToolBar().add(zoomOutButton);
//    image = getClass().getResource("/images/zoomOut.gif");
//    if (image != null) {
//      zoomOutButton.setIcon(new ImageIcon(image));
//      zoomOutButton.setText("");
//    }

  }

  public String getAttributeValueString(String key) {
/*
    if (MAX.equals(key)) {
      return "" + maxZoom;
    } 
*/
    if (ZOOM_START.equals(key)) {
	    return "" + zoomStart;
    }
/*
    else if (FULL_SIZE.equals(key)) {
	    return "" + zoomFull;
    }
    else if (FACTOR.equals(key)) {
      return "" + zoomStep;
    }
*/
    else if (ZOOM_LEVELS.equals(key)) {
      String[] s = new String[zoomLevels.size()];
      int j = s.length-1;
      for (Iterator i = zoomLevels.iterator(); i.hasNext(); --j) {
        s[j] = ((Double)(i.next())).toString();
      }
 
      return StringArrayConfigurer.arrayToString(s);
    }
    else if (zoomInButton.getAttributeValueString(key) != null) {
      return zoomInButton.getAttributeValueString(key);
    }
    else if (zoomPickButton.getAttributeValueString(key) != null) {
      return zoomPickButton.getAttributeValueString(key);
    }
    else {
      return zoomOutButton.getAttributeValueString(key);
    }
  }

  public void setAttribute(String key, Object val) {
/*
    if (MAX.equals(key)) {
      if (val instanceof String) {
        val = new Integer((String) val);
      }
      if (val != null) {
        maxZoom = ((Integer) val).intValue();
      }
//      initZoomFactors();
    }
*/
	  if (ZOOM_START.equals(key)) {
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
/*
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
//      initZoomFactors();
    }
    else if (FACTOR.equals(key)) {
      if (val instanceof String) {
        val = new Double((String) val);
      }
      if (val != null) {
        zoomStep = ((Double) val).doubleValue();
      }
//      initZoomFactors();
    }
*/
    else if (ZOOM_LEVELS.equals(key)) {
      if (val instanceof String) {
        val = StringArrayConfigurer.stringToArray((String) val);
      }
      if (val != null) {
        zoomLevels.clear();
        String[] a = (String[]) val;
        for (int i = 0; i < a.length; ++i) {
          zoomLevels.add(Double.valueOf(a[i]));
        }
      }
      initZoomFactors();
    }
    else {
      zoomInButton.setAttribute(key, val);
      zoomPickButton.setAttribute(key, val);
      zoomOutButton.setAttribute(key, val);
    }
  }

  private void initZoomFactors() {
/*
    zoomFactor = new double[maxZoom];
    for (int i = 0; i < zoomFactor.length; ++i) {
      zoomFactor[i] = Math.pow(zoomStep, zoomFull - i - 1);
    }
*/

    maxZoom = zoomLevels.size();
    zoomFactor = new double[maxZoom];
    int j = zoomFactor.length-1;
    for (Iterator i = zoomLevels.iterator(); i.hasNext(); --j) {
      zoomFactor[j] = ((Double)i.next()).doubleValue();
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
    zoomPickButton.setEnabled(true);
	  zoomOutButton.setEnabled(zoomLevel < maxZoom - 1);

    zoomMenu.initZoomItems(); 
  }

  public Class[] getAllowableConfigureComponents() {
    return new Class[0];
  }

  public void removeFrom(Buildable b) {
    map = (Map) b;
    map.setZoomer(null);
    map.getToolBar().remove(zoomInButton);
    map.getToolBar().remove(zoomPickButton);
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
        for ( ; i < zoomFactor.length && zoom < zoomFactor[i]; ++i);
        
        zoomInButton.setEnabled(i > 0);
        zoomOutButton.setEnabled(i < maxZoom - 1);
    
        zoomMenu.updateZoom();
     
        map.centerAt(center);
        map.repaint(true);
        map.getView().revalidate();

        finished.add(w);
        w.dispose();
      }
    };
    
    task.start();
  }

  public void zoomIn() {
    if (zoomInButton.isEnabled()) {
      // find next level
      for (zoomLevel = zoomFactor.length - 1;
           zoomLevel >= 0 && zoomFactor[zoomLevel] <= zoom;
           --zoomLevel);

      if (zoomLevel >= 0)
        setZoomFactor(zoomFactor[zoomLevel]);
      else zoomLevel = -1;
    }
  }

  public void zoomOut() {
    if (zoomOutButton.isEnabled()) {
      // find next level
      for (zoomLevel = 0;
           zoomLevel < zoomFactor.length && zoomFactor[zoomLevel] >= zoom;
           ++zoomLevel);

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

    zoomPickButton.setEnabled(gameStarting);
  }

  public VASSAL.command.Command getRestoreCommand() {
    return null;
  }

  protected class ZoomMenu extends JPopupMenu
                           implements ActionListener {
    protected JRadioButtonMenuItem other;
    protected JPopupMenu.Separator sep;
    protected ButtonGroup bg;

    public ZoomMenu() {
      super();

      sep = new JPopupMenu.Separator();
      add(sep);

      bg = new ButtonGroup();

      other = new JRadioButtonMenuItem("Other...");
      other.setActionCommand("Other...");
      other.addActionListener(this);
      bg.add(other);
      add(other);

      addSeparator();

      JMenuItem fw = new JMenuItem("Fit Width");
      fw.setActionCommand("Fit Width");
      fw.addActionListener(this);
      add(fw);

      JMenuItem fh = new JMenuItem("Fit Height");
      fh.setActionCommand("Fit Height");
      fh.addActionListener(this);
      add(fh);

      JMenuItem fv = new JMenuItem("Fit Visible");
      fv.setActionCommand("Fit Visible");
      fv.addActionListener(this);
      add(fv);
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
      
      if (a.getActionCommand().equals("Other...")) {      
        ZoomDialog dialog = new ZoomDialog((Frame)
          SwingUtilities.getAncestorOfClass(Frame.class, map.getView()),
          "Select Zoom Ratio", true);
        dialog.setVisible(true);
        double z = dialog.getResult()/100.0;
        if (z > 0 && z != zoom) {
          zoomLevel = -1;
          setZoomFactor(z);
        }
      }
      // FIXME: should be map.getSize() for consistency?
      else if (a.getActionCommand().equals("Fit Width")) {
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
      for (int i = 0; i < zoomFactor.length; ++i) {
        if (Math.abs(zoom - zoomFactor[i]) < 0.005) {
          zoomLevel = i;
          ((JRadioButtonMenuItem)getComponent(zoomLevel)).setSelected(true);
          return;
        }
      }

      zoomLevel = -1;
      other.setSelected(true); 
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

  protected class ZoomDialog extends JDialog
                             implements ActionListener,
                                        ChangeListener {
    protected double result;
    protected JSpinner ratioNumeratorSpinner;
    protected JSpinner ratioDenominatorSpinner;
    protected JSpinner percentSpinner;
    protected SpinnerNumberModel ratioNumeratorModel;
    protected SpinnerNumberModel ratioDenominatorModel;
    protected SpinnerNumberModel percentModel;
    protected JButton okButton;   
 
    public ZoomDialog(Frame owner, String title, boolean modal) {
      super(owner, title, modal);

      final int hsep = 5;

/*
      Box titleBox = new Box(BoxLayout.X_AXIS);
      JLabel titleLabel = new JLabel("Select Zoom Ratio");
      titleBox.add(titleLabel);
   
      titleBox.add(Box.createHorizontalGlue());      
*/

      JPanel controlsPane = new JPanel(new GridBagLayout());
      GridBagConstraints c = new GridBagConstraints();
      c.fill = GridBagConstraints.HORIZONTAL;

      Insets linset = new Insets(0, 0, 11, 11);
      Insets dinset = new Insets(0, 0, 0, 0);

      JLabel ratioLabel = new JLabel("Zoom Ratio:");
      c.gridx = 0;
      c.gridy = 0;
      c.weightx = 0;
      c.weighty = 0.5;
      c.insets = linset;
      c.anchor = GridBagConstraints.LINE_START;
      controlsPane.add(ratioLabel, c);

      Box ratioBox = new Box(BoxLayout.X_AXIS);
      ratioLabel.setLabelFor(ratioBox);
      c.gridx = 1;
      c.gridy = 0;
      c.weightx = 1;
      c.weighty = 0.5;
      c.insets = dinset;
      c.anchor = GridBagConstraints.LINE_START;
      controlsPane.add(ratioBox, c);

      ratioNumeratorModel = new SpinnerNumberModel(1, 1, 256, 1);
      ratioNumeratorSpinner = new JSpinner(ratioNumeratorModel);
      ratioNumeratorSpinner.addChangeListener(this);
      ratioBox.add(ratioNumeratorSpinner);
      

      ratioBox.add(Box.createHorizontalStrut(hsep));

      JLabel ratioColon = new JLabel(":");
      ratioBox.add(ratioColon);

      ratioBox.add(Box.createHorizontalStrut(hsep));

      ratioDenominatorModel = new SpinnerNumberModel(1, 1, 256, 1);
      ratioDenominatorSpinner = new JSpinner(ratioDenominatorModel);
      ratioDenominatorSpinner.addChangeListener(this);
      ratioBox.add(ratioDenominatorSpinner);

      JLabel percentLabel = new JLabel("Zoom:");
      c.gridx = 0;
      c.gridy = 1;
      c.weightx = 0;
      c.weighty = 0.5;
      c.insets = linset;
      c.anchor = GridBagConstraints.LINE_START;
      controlsPane.add(percentLabel, c);

      Box percentBox = new Box(BoxLayout.X_AXIS);
      c.gridx = 1;
      c.gridy = 1;
      c.weightx = 1;
      c.weighty = 0.5;
      c.insets = dinset;
      c.anchor = GridBagConstraints.LINE_START;
      controlsPane.add(percentBox, c);
     
      percentModel = new SpinnerNumberModel(zoom*100.0, 0.39, 25600.0, 10.0);
      percentSpinner = new JSpinner(percentModel);
      percentLabel.setLabelFor(percentSpinner);
      percentSpinner.addChangeListener(this);
      percentBox.add(percentSpinner);
 
      percentBox.add(Box.createHorizontalStrut(hsep));
 
      JLabel percentSign = new JLabel("%");
      percentBox.add(percentSign);

      // buttons
      Box buttonBox = new Box(BoxLayout.X_AXIS);
      buttonBox.add(Box.createHorizontalGlue());

      okButton = new JButton("Ok");
      okButton.addActionListener(this);
      getRootPane().setDefaultButton(okButton);
      buttonBox.add(okButton);

      buttonBox.add(Box.createHorizontalStrut(hsep));
 
      JButton cancelButton = new JButton("Cancel");
      cancelButton.addActionListener(this);
      buttonBox.add(cancelButton);

      Dimension okDim = okButton.getPreferredSize();
      Dimension cancelDim = cancelButton.getPreferredSize();
      Dimension buttonDimension = new Dimension(
         Math.max(okDim.width,  cancelDim.width),
         Math.max(okDim.height, cancelDim.height));
      okButton.setPreferredSize(buttonDimension);
      cancelButton.setPreferredSize(buttonDimension);

      JComponent contentPane = (JComponent)getContentPane();
      contentPane.setBorder(new EmptyBorder(12, 12, 11, 11));
      contentPane.setLayout(new BorderLayout(0, 11));
//      contentPane.add(titleBox, BorderLayout.PAGE_START);
      contentPane.add(controlsPane, BorderLayout.CENTER);
      contentPane.add(buttonBox, BorderLayout.PAGE_END);

      // FIXME: no way to set minimum size in 1.4.2?

      pack();
    }

    public double getResult() {
      return result;
    }

    public void actionPerformed(ActionEvent e) {
      result = e.getSource() == okButton ?
               percentModel.getNumber().doubleValue() : 0;

      setVisible(false);
    }

    public void stateChanged(ChangeEvent e) {
      if (e.getSource() == ratioNumeratorSpinner ||
          e.getSource() == ratioDenominatorSpinner) {
        percentSpinner.removeChangeListener(this);

        percentModel.setValue(new Double(
          ratioNumeratorModel.getNumber().doubleValue() /
          ratioDenominatorModel.getNumber().doubleValue() * 100.0));

// FIXME: is this the best way to prevent event circularity?
        percentSpinner.addChangeListener(this);
      }
      else if (e.getSource() == percentSpinner) {

        // see http://svn.gnome.org/viewcvs/gimp/trunk/libgimpwidgets/gimpzoommodel.c?view=markup
        // also http://www.virtualdub.org/blog/pivot/entry.php?id=81
        double z = percentModel.getNumber().doubleValue() / 100.0;
        
        boolean swapped = false;
        if (z < 1.0) {
          z = 1.0/z;
          swapped = true;
        }

        int p0 = 1;
        int q0 = 0;
        int p1 = (int)Math.floor(z);
        int q1 = 1;
        int p2;
        int q2;

        double r = z - p1;
        double next_cf;
        
        while (Math.abs(r) >= 0.0001 &&
               Math.abs((double)p1/q1 - z) > 0.0001) {
          r = 1.0/r;
          next_cf = Math.floor(r);

          p2 = (int)(next_cf * p1 + p0);
          q2 = (int)(next_cf * q1 + q0);

          if (p2 > 256 || q2 > 256 || (p2 > 1 && q2 > 1 && p2 * q2 > 200))
            break;

          p0 = p1;
          p1 = p2;
          q0 = q1;
          q1 = q2;

          r -= next_cf;
        }

        z = (double)p1/q1;

        if (z > 256.0) {
          p1 = 256;
          q1 = 1;
        }
        else if (z < 1.0/256.0) {
          p1 = 1;
          q1 = 256;
        }
       
        ratioNumeratorSpinner.removeChangeListener(this);
        ratioDenominatorSpinner.removeChangeListener(this);
 
        if (swapped) {
          ratioNumeratorModel.setValue(new Integer(q1));
          ratioDenominatorModel.setValue(new Integer(p1));
        }
        else {
          ratioNumeratorModel.setValue(new Integer(p1));
          ratioDenominatorModel.setValue(new Integer(q1));
        }

        ratioNumeratorSpinner.addChangeListener(this);
        ratioDenominatorSpinner.addChangeListener(this);
      }
    }
  }
}

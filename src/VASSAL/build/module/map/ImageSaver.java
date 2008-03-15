/*
 * $Id$
 *
 * Copyright (c) 2000-2008 by Rodney Kinney, Joel Uckelman
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
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.event.IIOWriteProgressListener;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import VASSAL.build.AbstractConfigurable;
import VASSAL.build.AutoConfigurable;
import VASSAL.build.Buildable;
import VASSAL.build.GameModule;
import VASSAL.build.module.Map;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.configure.Configurer;
import VASSAL.configure.ConfigurerFactory;
import VASSAL.configure.IconConfigurer;
import VASSAL.i18n.Resources;
import VASSAL.tools.ErrorLog;
import VASSAL.tools.FileChooser;
import VASSAL.tools.LaunchButton;

// FIXME: switch back to javax.swing.SwingWorker on move to Java 1.6
//import javax.swing.SwingWorker;
import org.jdesktop.swingworker.SwingWorker;


/**
 * This allows the user to capture a snapshot of the entire map into a PNG file
 */
public class ImageSaver extends AbstractConfigurable {
  protected LaunchButton launch;
  protected Map map;
  protected boolean promptToSplit = false;
  protected static final String DEFAULT_ICON = "/images/camera.gif";

  public ImageSaver() {
    final ActionListener al = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        writeMapAsImage();
      }
    };
  
    launch =
      new LaunchButton(null, TOOLTIP, BUTTON_TEXT, HOTKEY, ICON_NAME, al);

    // Set defaults for backward compatibility
    launch.setAttribute(TOOLTIP, "Save Map as PNG file");
    launch.setAttribute(BUTTON_TEXT, "");
    launch.setAttribute(ICON_NAME, DEFAULT_ICON);
  }

  public ImageSaver(Map m) {
    super();
    map = m;
  }

  /**
   * Expects to be added to a {@link Map}. Adds a button to the map window
   * toolbar that initiates the capture
   */
  public void addTo(Buildable b) {
    map = (Map) b;
    map.getToolBar().add(launch);
  }

  public void removeFrom(Buildable b) {
    map = (Map) b;
    map.getToolBar().remove(launch);
    map.getToolBar().revalidate();
  }

  protected static final String HOTKEY = "hotkey";
  protected static final String BUTTON_TEXT = "buttonText";
  protected static final String TOOLTIP = "tooltip";
  protected static final String ICON_NAME = "icon";

  public String[] getAttributeNames() {
    return new String[] {
      BUTTON_TEXT,
      TOOLTIP,
      ICON_NAME,
      HOTKEY
    };
  }

  public String[] getAttributeDescriptions() {
    return new String[] {
      "Button Text:  ",
      "Tooltip Text:  ",
      "Button icon:  ",
      "Hotkey:  "
    };
  }

  public Class<?>[] getAttributeTypes() {
    return new Class<?>[] {
      String.class,
      String.class,
      IconConfig.class,
      KeyStroke.class
    };
  }

  public static class IconConfig implements ConfigurerFactory {
    public Configurer getConfigurer(AutoConfigurable c, String key, String name) {
      return new IconConfigurer(key, name, DEFAULT_ICON);
    }
  }

  public void setAttribute(String key, Object value) {
    launch.setAttribute(key, value);
  }

  public String getAttributeValueString(String key) {
    return launch.getAttributeValueString(key);
  }

  private static class ProgressDialog extends JDialog {
    private final JLabel label;
    private final JProgressBar progbar;
    private final JButton cancel;

    public ProgressDialog(Frame parent, String title) {
      super(parent, title, true);

      final Box box = Box.createVerticalBox();
      box.setBorder(new EmptyBorder(12, 12, 11, 11));
      add(box);

      final Box lb = Box.createHorizontalBox();
      label = new JLabel("Saving map image...");
      lb.add(label);
      lb.add(Box.createHorizontalGlue());
      box.add(lb);

      box.add(Box.createVerticalStrut(11));

      progbar = new JProgressBar(0, 100);
      progbar.setStringPainted(true);
      progbar.setValue(0);
      box.add(progbar);
    
      box.add(Box.createVerticalStrut(17));

      final Box bb = Box.createHorizontalBox();
      bb.add(Box.createHorizontalGlue());
      cancel = new JButton(Resources.getString("General.cancel"));
      cancel.setSelected(true);
      bb.add(cancel);
      bb.add(Box.createHorizontalGlue());
      box.add(bb);
    }

    public void setLabel(String text) {
      label.setText(text);
    }

    public void setProgress(int percent) {
      progbar.setValue(percent);
    }

    public void addActionListener(ActionListener l) {
      cancel.addActionListener(l);
    }
  }

  private class SnapshotTask extends SwingWorker<Void,Void> {
    private ImageWriter iw;
    private ImageOutputStream os;

    private final String filename;
    private final int x;
    private final int y;
    private final int w;
    private final int h;

    public SnapshotTask(String filename, int x, int y, int w, int h) {
      this.filename = filename;
      this.x = x;
      this.y = y;
      this.w = w;
      this.h = h;
    }

    @Override
    public Void doInBackground() throws Exception {
      setProgress(0);

      final BufferedImage img =
        new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

      final Graphics2D g = img.createGraphics();
      map.paintSynchronously(g, x, y);
      g.dispose();

      iw = ImageIO.getImageWritersByFormatName("png").next();
      iw.addIIOWriteProgressListener(new IIOWriteProgressListener() {
        public void imageComplete(ImageWriter source) { }
      
        public void imageProgress(ImageWriter source, float percentageDone) {
          setProgress(Math.round(percentageDone));
        }

        public void imageStarted(ImageWriter source, int imageIndex) { }

        public void thumbnailComplete(ImageWriter source) { }

        public void thumbnailProgress(ImageWriter source,
                                      float percentageDone) { }

        public void thumbnailStarted(ImageWriter source,
                                     int imageIndex, int thumbnailIndex) { }
  
        public void writeAborted(ImageWriter source) { }
      });

      os = new MemoryCacheImageOutputStream(new FileOutputStream(filename));
      iw.setOutput(os);
      iw.write(img);
      return null;
    }

    @Override
    protected void done() {
      try {
        get();
      }
      catch (OutOfMemoryError e) {
        iw.abort();
        ErrorLog.warn(e);
// FIXME: add code here to prompt for splitting         
      }
      catch (Exception e) {
        iw.abort();
        ErrorLog.warn(e);
      }
      finally {
        if (iw != null) iw.dispose();

        if (os != null) {
          try {
            os.close();
          }
          catch (IOException e) {
            ErrorLog.warn(e);
          }
        }
      }
    } 
  }

  public void writeMapAsImage() {
    final Dimension s = map.mapSize();
    s.width *= map.getZoom();
    s.height *= map.getZoom();
    writeMapRectAsImage(0, 0, s.width, s.height);
  }

  protected void writeMapRectAsImage(int x, int y, int w, int h) {
    // prompt user for image filename
    final FileChooser fc = GameModule.getGameModule().getFileChooser();
    fc.setSelectedFile(new File(fc.getCurrentDirectory(),
      GameModule.getGameModule().getGameName() + "Map.png"));

    if (fc.showSaveDialog(map.getView()) != FileChooser.APPROVE_OPTION) return; 
   
    final String filename = fc.getSelectedFile().getPath();

    final ProgressDialog pd = new ProgressDialog(
      (Frame) SwingUtilities.getAncestorOfClass(Frame.class, map.getView()),
      "Progress");
    
    final SnapshotTask task = new SnapshotTask(filename, 0, 0, w, h);

    task.addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent e) {
        if ("progress".equals(e.getPropertyName())) {
          final int pct = (Integer) e.getNewValue();
          pd.setProgress(pct);
        }
        else if ("state".equals(e.getPropertyName())) {
          if ((SwingWorker.StateValue) e.getNewValue() ==
              SwingWorker.StateValue.DONE) {
            pd.setVisible(false);
            pd.dispose();
          }
        }
      }
    });

    pd.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        task.cancel(true);
      }
    });

    task.execute();

    pd.pack();
    pd.setVisible(true);
  }

  /**
   * Outputs a snapshot of the Map to a PNG file. Displays a file dialog to
   * prompt the user for the file
   */
/*
  public void writeMapAsImage() {
    int sections = 1;
    if (promptToSplit) {
      final String s = JOptionPane.showInputDialog("Divide map into how many sections?\n(Using more sections requires less memory)");
      if (s == null) {
        return;
      }
      try {
        sections = Integer.parseInt(s);
      }
      catch (NumberFormatException ex) {
// FIXME: should do proper validation here
      }
    }

    final FileChooser fc = GameModule.getGameModule().getFileChooser();
    fc.setSelectedFile(
      new File(fc.getCurrentDirectory(),
               GameModule.getGameModule().getGameName() + "Map.png"));
    
    if (fc.showSaveDialog(map.getView()) != FileChooser.APPROVE_OPTION) return; 

    final int sectionCount = sections;
    final String fileName = fc.getSelectedFile().getPath();

    final ProgressDialog pd = new ProgressDialog(
      (Frame) SwingUtilities.getAncestorOfClass(Frame.class, map.getView()),
      "Progress");

    final SwingWorker<Void,Void> task = new SwingWorker<Void,Void>() {
      private ImageWriter iw;

      @Override
      public Void doInBackground() throws Exception {

        final Dimension buffer = map.getEdgeBuffer();
        final int totalWidth =
          (int) ((map.mapSize().width - 2 * buffer.width) * map.getZoom());
        final int totalHeight =
          (int) ((map.mapSize().height - 2 * buffer.height) * map.getZoom());

        for (int i = 0; i < sectionCount; ++i) {
          setProgress(0);

          String sectionName = fileName;
          if (sectionCount > 1) {
            if (fileName.lastIndexOf(".") >= 0) {
              sectionName =
                fileName.substring(0, fileName.lastIndexOf(".")) +
                (i + 1) + fileName.substring(fileName.lastIndexOf("."));
            }
            else {
              sectionName = fileName + (i + 1);
            }
          }

          final ImageOutputStream os = new MemoryCacheImageOutputStream(
            new FileOutputStream(sectionName));

          final int height = totalHeight / sectionCount;

          final BufferedImage output =
            new BufferedImage(totalWidth, height, BufferedImage.TYPE_INT_ARGB);

          final Graphics2D gg = output.createGraphics();

          map.paintSynchronously(gg,
            -(int) (map.getZoom() * buffer.width),
            -(int) (map.getZoom() * buffer.height) + height * i);
          gg.dispose();

          iw = ImageIO.getImageWritersByFormatName("png").next();
          iw.addIIOWriteProgressListener(new IIOWriteProgressListener() {
            public void imageComplete(ImageWriter source) {
              pd.setVisible(false);
              pd.dispose(); 
            }
      
            public void imageProgress(ImageWriter source,
                                      float percentageDone) {
              setProgress(Math.round(percentageDone));
            }

            public void imageStarted(ImageWriter source, int imageIndex) {
            }

            public void thumbnailComplete(ImageWriter source) {
            }

            public void thumbnailProgress(ImageWriter source,
                                          float percentageDone) {
            }

            public void thumbnailStarted(ImageWriter source,
                                         int imageIndex, int thumbnailIndex) {
            }
  
            public void writeAborted(ImageWriter source) {
              pd.setVisible(false);
              pd.dispose();
            }
          });

          iw.setOutput(os);

          try {
            iw.write(output);
          }     
          finally {
            try {
              iw.dispose();
              os.close();
            }
            catch (IOException e) {
              e.printStackTrace();
            }
          }
        }
          
        return null;
      }

      @Override
      protected void done() {
        try { get(); }
        catch (OutOfMemoryError e) {
          e.printStackTrace();

          final String msg = "Insufficient memory\n" + "Zooming out will reduce memory requirements\n" + "Otherwise, try again and you will be prompted to split the map\n" + "into a number of sections";

          JOptionPane.showMessageDialog(
            map.getView().getTopLevelAncestor(),
            msg, "Error saving map image", JOptionPane.ERROR_MESSAGE);

          promptToSplit = true;
        }
        catch (Exception e) {
          iw.abort();

          e.printStackTrace();
          String msg = e.getMessage();
          if (msg == null || msg.length() == 0) {
            msg = e.getClass().getName();
            msg = msg.substring(msg.lastIndexOf(".") + 1);
          }

          JOptionPane.showMessageDialog(map.getView().getTopLevelAncestor(),
            msg, "Error saving map image", JOptionPane.ERROR_MESSAGE);
        }
      }
    };

    task.addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent e) {
        if ("progress".equals(e.getPropertyName())) {
          final int pct = (Integer) e.getNewValue();
          pd.setProgress(pct);
        }
      }
    });

    pd.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        task.cancel(true);
      }
    });

    task.execute();

    pd.pack();
    pd.setVisible(true);
  }
*/

  /**
   * Write a PNG-encoded snapshot of the map to the given OutputStreams,
   * dividing the map into vertical sections, one per stream
   *
   * @deprecated
   */
  @Deprecated
  public void writeImage(OutputStream[] out) throws IOException {
    Dimension buffer = map.getEdgeBuffer();
    int totalWidth =
      (int) ((map.mapSize().width - 2 * buffer.width) * map.getZoom());
    int totalHeight =
      (int) ((map.mapSize().height - 2 * buffer.height) * map.getZoom());
    for (int i = 0; i < out.length; ++i) {
      int height = totalHeight / out.length;
      if (i == out.length - 1) {
        height = totalHeight - height * (out.length - 1);
      }

      Image output = map.getView().createImage(totalWidth, height);
      Graphics2D gg = (Graphics2D) output.getGraphics();
      map.paintSynchronously(gg,
        -(int) (map.getZoom() * buffer.width),
        -(int) (map.getZoom() * buffer.height) + height * i);
      gg.dispose();
      try {
        MediaTracker t = new MediaTracker(map.getView());
        t.addImage(output, 0);
        t.waitForID(0);
      }
      catch (Exception e) {
        e.printStackTrace();
      }

      try {
        if (output instanceof RenderedImage) {
          ImageIO.write((RenderedImage) output, "png", out[i]);
        }
        else {
          throw new IOException("Bad image type");
        }
      }
      finally {
        try {
          out[i].close();
        }
        catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public HelpFile getHelpFile() {
    return HelpFile.getReferenceManualPage("Map.htm", "ImageCapture");
  }

  public static String getConfigureTypeName() {
    return "Image Capture Tool";
  }

  public Class[] getAllowableConfigureComponents() {
    return new Class[0];
  }
}

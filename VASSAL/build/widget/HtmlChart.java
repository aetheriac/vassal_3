/*
 * $Id$
 *
 * Copyright (c) 2000-2003 by Rodney Kinney
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
package VASSAL.build.widget;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.ComponentView;
import javax.swing.text.Element;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import VASSAL.build.Buildable;
import VASSAL.build.GameModule;
import VASSAL.build.Widget;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.tools.ScrollPane;

/**
 * An HtmlChart is used for displaying html information for the module. The
 * charts are loaded as html files stored in the DataArchive. As a subclass of
 * Widget, a Chart may be added to any Widget, but it may not contain children
 * of its own
 */
public class HtmlChart extends Widget implements MouseListener {
  public static final String NAME = "chartName";
  public static final String FILE = "fileName";

  private String fileName;
  private JScrollPane scroller;
  private JEditorPane htmlWin;

  public HtmlChart() {
  }

  private boolean isURL() {
    return htmlWin.getDocument().getProperty("stream") != null;
  }

  private void setText(String text) {
    htmlWin.setText(text);
    // ensure hyperlink engine knows we no longer at the last URL
    htmlWin.getDocument().putProperty("stream", null);
    htmlWin.revalidate();
  }

  private void setFile(String fname) {
    setText(getFile(fname));
  }

  private String getFile(String fname) {
    if (fname == null) {
      return null;
    }
    try {
      InputStream stream = GameModule.getGameModule().getDataArchive().getFileStream(fname);
      byte[] bytes = new byte[stream.available()];
      int totalBytes = stream.available();
      int numRead = 0;
      int iBytes = 0;
      while (iBytes < totalBytes && ((numRead = stream.read(bytes, iBytes, totalBytes - iBytes)) > 0)) {
        iBytes += numRead;
      }
      stream.close();
      return new String(bytes, "UTF-8");
    }
    catch (IOException ex) {
      return "Page " + fname + " not found";
    }
  }

  // Warning, creating a JEditorPane with a "jar" url or using setPage() with a jar
  // url will leave a resource open in the MOD file, making it impossible to
  // save or rename it. This might be acceptable for people playing a MOD,
  // but is unacceptable for editors; they can only save their work to a new file.
  // Therefore, we read the entire file instead of simply using:
  //    GameModule.getGameModule().getDataArchive().getURL( fileName );
  public Component getComponent() {
    if (htmlWin == null) {
      htmlWin = new JEditorPane();
      htmlWin.setEditable(false);
      htmlWin.setContentType("text/html");
      XTMLEditorKit myHTMLEditorKit = new XTMLEditorKit();
      htmlWin.setEditorKit(myHTMLEditorKit);

      htmlWin.addHyperlinkListener(new HtmlChartHyperlinkListener());
      htmlWin.addMouseListener(this);

      setFile(fileName);

      scroller = new ScrollPane(htmlWin);
      scroller.getViewport().setPreferredSize(htmlWin.getPreferredSize());
      scroller.getViewport().setAlignmentY(0.0F);
    }
    return scroller;
  }

  public String getFileName() {
    return fileName;
  }

  public void addTo(Buildable parent) {
  }

  public static String getConfigureTypeName() {
    return "HTML Chart";
  }

  public void removeFrom(Buildable parent) {
  }

  public HelpFile getHelpFile() {
    return HelpFile.getReferenceManualPage("ChartWindow.htm", "HtmlChart");
  }

  public void setAttribute(String key, Object val) {
    if (NAME.equals(key)) {
      setConfigureName((String) val);
    }
    else if (FILE.equals(key)) {
      if (val instanceof File) {
        val = ((File) val).getName();
      }
      fileName = (String) val;
      if (htmlWin != null) {
        setFile(fileName);
      }
    }
  }

  public Class[] getAllowableConfigureComponents() {
    return new Class[0];
  }

  /**
   * The Attributes of a Chart are:
   *
   * <pre>
   * <code>
   * NAME
   * </code>
   *
   *
   *
   *
   *
   *
   *
   *
   *
   *           for the name of the chart
   *
   *
   *
   *
   *
   *
   *
   *
   *
   *
   * <code>
   * FILE
   * </code>
   *
   *
   *
   *
   *
   *
   *
   *
   *
   *           for the name of the HTML file in the {@link VASSAL.tools.DataArchive}
   *
   *
   *
   *
   *
   *
   *
   *
   *
   *
   * </pre>
   */
  public String[] getAttributeNames() {
    return new String[]{NAME, FILE};
  }

  public String[] getAttributeDescriptions() {
    return new String[]{"Name", "HTML File"};
  }

  public Class[] getAttributeTypes() {
    return new Class[]{String.class, File.class};
  }

  public String getAttributeValueString(String name) {
    if (NAME.equals(name)) {
      return getConfigureName();
    }
    else if (FILE.equals(name)) {
      return fileName;
    }
    return null;
  }

  public void mousePressed(MouseEvent event) {
    if (event.isMetaDown()) {
      JPopupMenu popup = new JPopupMenu();
      JMenuItem item = new JMenuItem("Return to default page");

      item.addActionListener(new ActionListener() {
        // Return to default page
        public void actionPerformed(ActionEvent e) {
          setFile(fileName);
        }
      });

      popup.add(item);
      popup.show(event.getComponent(), event.getX(), event.getY());
    }
  }

  public void mouseClicked(MouseEvent e) {
  }

  public void mouseEntered(MouseEvent e) {
  }

  public void mouseExited(MouseEvent e) {
  }

  public void mouseReleased(MouseEvent e) {
  }

  public class HtmlChartHyperlinkListener implements HyperlinkListener {

    public void hyperlinkUpdate(HyperlinkEvent event) {
      if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
        if (!isURL() && event.getDescription().indexOf("/") < 0 || event.getURL() == null) {
          setFile(event.getDescription());
        }
        else {
          try {
            htmlWin.setPage(event.getURL());
          }
          catch (IOException exception) {
            setText("Can't open " + event.getURL() + " : " + exception.getMessage());
          }
          htmlWin.revalidate();
        }
      }
    }
  }


  /**
   * Extended HTML Editor kit to extend the <src> tag to display images
   * from the module DataArchive where no pathname included in the image name.
   * The image is placed on a label and returned as a ComponentView. An
   * ImageView cannot be used as the standard Java HTML Renderer can only
   * display Images from an external URL.
   */


  public class XTMLEditorKit extends HTMLEditorKit {

    public ViewFactory getViewFactory() {
      return new XTMLFactory();
    }

    public class XTMLFactory extends HTMLFactory implements ViewFactory {

      public XTMLFactory() {
        super();

      }

      public View create(javax.swing.text.Element element) {
        HTML.Tag kind = (HTML.Tag) (element.getAttributes().getAttribute(javax.swing.text.StyleConstants.NameAttribute));

        if (kind instanceof HTML.Tag && element.getName().equals("img")) {
          String imageName = (String) element.getAttributes().getAttribute(HTML.Attribute.SRC);
          if (imageName.indexOf("/") < 0) {
            return new ImageComponentView(element);
          }
        }
        return super.create(element);
      }

      public class ImageComponentView extends ComponentView {

        protected String imageName;


        /**
         * Very basic Attribute handling only. Expand as needed
         */
        public ImageComponentView(Element e) {
          super(e);
          imageName = (String) e.getAttributes().getAttribute(HTML.Attribute.SRC);
        }

        protected Component createComponent() {
          try {
            JLabel label = new JLabel();
            ImageIcon icon = new ImageIcon(GameModule.getGameModule().getDataArchive().getCachedImage(imageName));
            label.setIcon(icon);
            return label;
          }
          catch (Exception e) {
            return null;
          }
        }
      }
    }
  }

}

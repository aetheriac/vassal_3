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
package VASSAL.counters;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import VASSAL.build.GameModule;
import VASSAL.tools.ScrollPane;
import VASSAL.tools.filechooser.FileChooser;
import VASSAL.tools.filechooser.ImageFileFilter;
import VASSAL.tools.imageop.Op;
import VASSAL.tools.imageop.OpIcon;

public class ImagePicker extends JPanel
                         implements MouseListener, ItemListener {
  private static final long serialVersionUID = 1L;
  private String imageName = null;
  protected static Font FONT = new Font("Dialog", 0, 11);
  private final JTextArea noImage;
  private final JComboBox select;
  private final OpIcon icon;
  private final JLabel imageView;
  private final JPanel imageViewer;
  private final JScrollPane imageScroller;

  public ImagePicker() {
    noImage = new JTextArea(1,10);
    noImage.setFont(FONT);
    noImage.setText("Double-click here to add new image");
    noImage.addMouseListener(this);
    noImage.setEditable(false);
    noImage.setLineWrap(true);
    noImage.setWrapStyleWord(true);
    icon = new OpIcon();
    imageView = new JLabel(icon);
    imageView.addMouseListener(this);
    
    imageViewer = new JPanel(new BorderLayout());
    imageScroller = new ScrollPane(
      imageView,
      JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
      JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    imageViewer.add(imageScroller, BorderLayout.CENTER);
  
    select = new JComboBox(
      GameModule.getGameModule().getDataArchive().getImageNames());
    select.addItemListener(this);
    setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
    add(noImage);
    add(select);
  }

  public String getImageName() {
    return imageName;
  }

  protected void setViewSize() {
  }

  public void setImageName(String name) {
    imageName = name;
    remove(0);
    if (name == null || name.length() == 0) {
      add(noImage,0);
    }
    else {
      icon.setOp(Op.load(imageName));
      Dimension d = new Dimension(icon.getIconWidth(), icon.getIconHeight());
      if (d.width > 400) d.width = 400;
      if (d.height > 400) d.height = 400;
      imageScroller.setPreferredSize(d);
        
      add(imageViewer,0);
/*
      try {
        icon.setImage(getImage());
        
        Dimension d = new Dimension (icon.getIconWidth(), icon.getIconHeight());
        if (d.width > 400) d.width = 400;
        if (d.height > 400) d.height = 400;
        imageScroller.setPreferredSize(d);
        
        name = imageName;
        add(imageViewer,0);
      }
      catch (java.io.IOException e) {
        name = null;
        add(noImage,0);
      }
*/
    }

    select.removeItemListener(this);
    select.setSelectedItem(name);
    if (name != null && !name.equals(select.getSelectedItem())) {
      select.setSelectedItem(name+".gif");
    }
    select.addItemListener(this);
    revalidate();
    final Window w = SwingUtilities.getWindowAncestor(this);
    if (w != null) {
      w.pack();
    }
    repaint();
  }

  public void mouseEntered(MouseEvent e) {
  }

  public void mouseExited(MouseEvent e) {
  }

  public void mouseClicked(MouseEvent e) {
  }

  public void mousePressed(MouseEvent e) {
  }

  public void mouseReleased(MouseEvent e) {
    if (e.getClickCount() > 1) {
      pickImage();
    }
  }

  public void itemStateChanged(ItemEvent e) {
    setImageName((String) select.getSelectedItem());
  }

  public void pickImage() {
    final FileChooser fc = GameModule.getGameModule().getFileChooser();
    fc.setFileFilter(new ImageFileFilter());
    
    if (fc.showOpenDialog(this) == FileChooser.APPROVE_OPTION
         && fc.getSelectedFile().exists()) {
      String name = fc.getSelectedFile().getName();
      GameModule.getGameModule()
                .getArchiveWriter()
                .addImage(fc.getSelectedFile().getPath(), name);
      select.setModel(new DefaultComboBoxModel(
         GameModule.getGameModule().getDataArchive().getImageNames()));
      setImageName(name);
    }
    else {
      setImageName(" ");
    }
    repaint();
  }
}

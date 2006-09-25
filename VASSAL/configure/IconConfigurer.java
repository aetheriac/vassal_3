package VASSAL.configure;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

import VASSAL.build.GameModule;

/*
 * $Id$
 *
 * Copyright (c) 2003 by Rodney Kinney
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

public class IconConfigurer extends Configurer {
  private JPanel controls;
  private String imageName;
  private String defaultImage;
  private Icon icon;

  public IconConfigurer(String key, String name, String defaultImage) {
    super(key, name);
    this.defaultImage = defaultImage;
  }

  public String getValueString() {
    return imageName;
  }

  public void setValue(String s) {
    icon = null;
    imageName = s == null ? "" : s;
    if (imageName.startsWith("/")) {
      URL imageURL = getClass().getResource(imageName);
      if (imageURL != null) {
        icon = new ImageIcon(imageURL);
      }
    }
    else if (imageName.length() > 0) {
      try {
        icon = new ImageIcon(GameModule.getGameModule().getDataArchive().getCachedImage(imageName));
      }
      catch (IOException e) {
      }
    }
    setValue((Object)imageName);
  }

  public Icon getIconValue() {
    return icon;
  }

  public Component getControls() {
    if (controls == null) {
      controls = new JPanel();
      controls.setLayout(new BoxLayout(controls,BoxLayout.X_AXIS));
      controls.add(new JLabel(getName()));
      final JPanel p = new JPanel() {
        public void paint(Graphics g) {
          g.clearRect(0,0,getSize().width,getSize().height);
          Icon i = getIconValue();
          if (i != null) {
            i.paintIcon(this,g,getSize().width/2-i.getIconWidth()/2,getSize().height/2-i.getIconHeight()/2);
          }
        }
      };
      p.setPreferredSize(new Dimension(32,32));
      controls.add(p);
      JButton reset = new JButton("Select");
      reset.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          selectImage();
          p.repaint();
        }
      });
      controls.add(reset);
      if (defaultImage != null) {
        JButton useDefault = new JButton("Default");
        useDefault.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            setValue(defaultImage);
            p.repaint();
          }
        });
        controls.add(useDefault);
      }
    }
    return controls;
  }

  private void selectImage() {
    JFileChooser fc = GameModule.getGameModule().getFileChooser();
    if (fc.showOpenDialog(controls) == JFileChooser.CANCEL_OPTION) {
      setValue(null);
    }
    else {
      File f = fc.getSelectedFile();
      if (f != null
        && f.exists()) {
        GameModule.getGameModule().getArchiveWriter().addImage(f.getPath(),f.getName());
        setValue(f.getName());
      }
    }
  }
}

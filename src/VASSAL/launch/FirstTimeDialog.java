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
package VASSAL.launch;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.util.Locale;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import VASSAL.build.GameModule;
import VASSAL.build.module.Documentation;
import VASSAL.configure.ShowHelpAction;
import VASSAL.i18n.Resources;
import VASSAL.tools.DataArchive;

/**
 * A dialog for first-time users.
 *
 * @since 3.1.0
 */
public class FirstTimeDialog extends JDialog {
  private static final long serialVersionUID = 1L;

  public FirstTimeDialog() {
    super((Frame) null, true);
    setLocationRelativeTo(ModuleManagerWindow.getInstance());

    setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });

    final JPanel b = new JPanel();
    b.setLayout(new BoxLayout(b, BoxLayout.Y_AXIS));

    final JLabel about = new JLabel(
      new ImageIcon(getClass().getResource("/images/Splash.png")));
    about.setAlignmentX(0.5F);
    b.add(about);

    b.add(getControls());

    add(b);
    pack();

    setLocationRelativeTo(null);
  }

  private JPanel getControls() {
    final File tourModule = new File(
      Documentation.getDocumentationBaseDir(), "tour.mod");  //$NON-NLS-1$
    final File tourLogFile = new File(
      Documentation.getDocumentationBaseDir(), "tour.log");  //$NON-NLS-1$

    final JPanel panel = new JPanel();
    panel.setBorder(new EmptyBorder(5,5,5,5));
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

    final JLabel l = new JLabel();
    l.setFont(new Font("SansSerif", 1, 40));  //$NON-NLS-1$
    l.setText(Resources.getString("Main.welcome"));  //$NON-NLS-1$
    l.setForeground(Color.black);
    l.setAlignmentX(0.5F);
    panel.add(l);

    Box b = Box.createHorizontalBox();
    final JButton tour =
      new JButton(Resources.getString("Main.tour"));  //$NON-NLS-1$
    final JButton jump =
      new JButton(Resources.getString("Main.jump_right_in"));  //$NON-NLS-1$
    final JButton help = new JButton(Resources.getString(Resources.HELP));
    b.add(tour);
    b.add(jump);
    b.add(help);

    final JPanel p = new JPanel();
    p.add(b);
    panel.add(p);

// FIXME: make sure the tour spawns a new Player
    tour.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        try {
          GameModule.init(
            new BasicModule(new DataArchive(tourModule.getPath())));
          GameModule.getGameModule().getFrame().setVisible(true);
          GameModule.getGameModule()
                    .getGameState().loadGameInBackground(tourLogFile);
          FirstTimeDialog.this.dispose();
        }
        catch (Exception e) {
          e.printStackTrace();
          JOptionPane.showMessageDialog
              (null,
               e.getMessage(),
               Resources.getString("Main.open_error"),  //$NON-NLS-1$
               JOptionPane.ERROR_MESSAGE);
        }
      }
    });

    jump.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        FirstTimeDialog.this.dispose();
        ModuleManagerWindow.getInstance().setVisible(true);
      }
    });

    try {
      final File readme =
        new File (Documentation.getDocumentationBaseDir(),"README.html");
      help.addActionListener(new ShowHelpAction(readme.toURI().toURL(), null));
    }
    catch (MalformedURLException e) {
      e.printStackTrace();
    }

    b = Box.createHorizontalBox();
    b.add(new JLabel(Resources.getString("Prefs.language")+":  "));
    final JComboBox box =
      new JComboBox(Resources.getSupportedLocales().toArray());
    box.setRenderer(new DefaultListCellRenderer() {
      private static final long serialVersionUID = 1L;

      public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        setText(((Locale) value).getDisplayName());
        return this;
      }
    });

    box.setSelectedItem(Resources.getLocale());
    box.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Resources.setLocale((Locale) box.getSelectedItem());
        final Container parent = panel.getParent();
        parent.remove(panel);
        parent.add(getControls());
        FirstTimeDialog.this.pack();
      }
    });
    b.add(box);
    panel.add(b);

    return panel;
  }
}

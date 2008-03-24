/*
 * $Id: GameModule.java 3077 2008-02-15 13:37:08Z uckelman $
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

package VASSAL.tools;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import VASSAL.build.GameModule;
import VASSAL.configure.PasswordConfigurer;
import VASSAL.configure.StringConfigurer;
import VASSAL.i18n.Resources;

// FXIME: Would be better if this didn't set the username and password
// directly, but instead had a static method for returning them.
// FIXME: Could be made prettier if it didn't use Configurers, or if
// we made Configurers prettier.

/**
 * A dialog for setting a username and password.
 *
 * @author Joel Uckelman
 */
public class UsernameAndPasswordDialog extends JDialog {
  private static final long serialVersionUID = 1L;
    
  public UsernameAndPasswordDialog(Frame parent) {
    super(parent, "Set Your Username and Password", true);
    setLocationRelativeTo(parent);
    setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

    final JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setBorder(new EmptyBorder(12, 12, 11, 11));
    add(panel);

    final StringConfigurer nameConfig = new StringConfigurer(null,
      Resources.getString("WizardSupport.RealName")); //$NON-NLS-1$
    final StringConfigurer pwd = new PasswordConfigurer(null,
      Resources.getString("WizardSupport.Password")); //$NON-NLS-1$
    final StringConfigurer pwd2 = new PasswordConfigurer(null,
      Resources.getString("WizardSupport.ConfirmPassword")); //$NON-NLS-1$

    panel.add(nameConfig.getControls());
    panel.add(pwd.getControls());
    panel.add(pwd2.getControls());

    final JLabel note =
      new JLabel(Resources.getString("WizardSupport.NameAndPasswordDetails"));
    note.setAlignmentX(0.5f);
    note.setBorder(new EmptyBorder(12,0,0,0));
    panel.add(note);

    final JLabel error = new JLabel(Resources.getString(
      "WizardSupport.EnterNameAndPassword")); //$NON-NLS-1$
    error.setAlignmentX(0.5f);
    error.setBorder(new EmptyBorder(12,0,17,0));
    panel.add(error);
   
    panel.add(Box.createVerticalGlue());

    final Box bb = Box.createHorizontalBox();
    bb.add(Box.createHorizontalGlue());    

    final JButton ok = new JButton(Resources.getString("General.ok"));
    ok.setEnabled(false);
    ok.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        GameModule.getGameModule()
                  .getPrefs()
                  .getOption(GameModule.REAL_NAME)
                  .setValue(nameConfig.getValueString());
        GameModule.getGameModule()
                  .getPrefs()
                  .getOption(GameModule.SECRET_NAME)
                  .setValue(pwd.getValueString());

        try {
          GameModule.getGameModule().getPrefs().write();
          UsernameAndPasswordDialog.this.dispose(); 
        }
        catch (IOException ex) {
          final String msg = ex.getMessage();
          error.setText(
            msg == null ? Resources.getString("Prefs.unable_to_save") : msg);
          error.setForeground(Color.red);
        }
      }
    });
    bb.add(ok);

    bb.add(Box.createHorizontalStrut(5));

    final JButton cancel = new JButton(Resources.getString("General.cancel"));
    cancel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        UsernameAndPasswordDialog.this.dispose(); 
      }
    });
    bb.add(cancel);
 
    // make buttons the same width
    JButton[] buttons = new JButton[] { ok, cancel };
    int maxwidth = 0;
    for (JButton b : buttons) {
      final Dimension d = b.getPreferredSize();
      if (d.width > maxwidth) maxwidth = d.width;
    }
    final Dimension d = ok.getPreferredSize();
    d.width = maxwidth;
    for (JButton b : buttons) {
      b.setPreferredSize(d);
    } 

    panel.add(bb);
    
    pack();
    setMinimumSize(getSize());

    // This listener handles validating the input, updating the error
    // message, and enabling the Ok button.
    final PropertyChangeListener pl = new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        if (nameConfig.getValue() == null ||
            "".equals(nameConfig.getValue())) { //$NON-NLS-1$
          if (pwd.getValue() == null || "".equals(pwd.getValue())) {
            error.setText(Resources.getString(
              "WizardSupport.EnterNameAndPassword")); //$NON-NLS-1$
          }
          else {
            error.setText(Resources.getString(
              "WizardSupport.EnterYourName")); //$NON-NLS-1$
          }
          error.setForeground(Color.black);
          ok.setEnabled(false);
        }
        else if (pwd.getValue() == null ||
                 "".equals(pwd.getValue())) { //$NON-NLS-1$
          error.setText(Resources.getString(
            "WizardSupport.EnterYourPassword")); //$NON-NLS-1$
          error.setForeground(Color.black);
          ok.setEnabled(false);
        }
        else if (pwd2.getValue() == null ||
                 "".equals(pwd2.getValue())) { //$NON-NLS-1$
          error.setText("Please confirm your password");
          error.setForeground(Color.black);
          ok.setEnabled(false);
        }
        else if (!pwd.getValue().equals(pwd2.getValue())) {
          error.setText(Resources.getString(
            "WizardSupport.PasswordsDontMatch")); //$NON-NLS-1$
          error.setForeground(Color.red);
          ok.setEnabled(false);
        }
        else {
          // everything is ok
          error.setText("");  //$NON-NLS-1$
          error.setForeground(Color.black);
          ok.setEnabled(true); 
        }
      }
    };

    nameConfig.addPropertyChangeListener(pl);
    pwd.addPropertyChangeListener(pl);
    pwd2.addPropertyChangeListener(pl);
  }
}

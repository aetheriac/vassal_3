/*
 * $Id: Info.java 3388 2008-03-30 21:51:32Z uckelman $
 *
 * Copyright (c) 2008 by Joel Uckelman 
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

package VASSAL.tools.menu;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;

import com.apple.eawt.Application;
import com.apple.eawt.ApplicationAdapter;
import com.apple.eawt.ApplicationEvent;

public class MacOSXMenuManager extends MenuManager {
  private final MasterJMenuBar menuBar = new MasterJMenuBar();

  public MacOSXMenuManager() {
    super();
  }

  @Override
  public JMenuBar getMenuBarFor(JFrame fc) {
    return menuBar.createSlave();
  }

  @Override
  public JMenu createMenu(String text) {
    return new MasterJMenu(text);
  }

  @Override
  public JMenuItem createMenuItem(Action action) {
    return new MasterJMenuItem(action);
  }

  @Override
  public JCheckBoxMenuItem createCheckBoxMenuItem(Action action) {
    return new MasterJCheckBoxMenuItem(action);
  }

  @Override
  public JRadioButtonMenuItem createRadioButtonMenuItem(Action action) {
    return new MasterJRadioButtonMenuItem(action);
  }

  @Override
  public JSeparator createSeparator() {
    return new MasterJSeparator();
  }

  @Override
  public JMenuItem addKey(String key) {
    // don't reserve slots for Quit, Preferences, or About on Macs
    if ("General.quit".equals(key) ||
        "Prefs.edit_preferences".equals(key) ||
        "AboutScreen.about_vassal".equals(key))
      return null;

    return super.addKey(key);
  }

  @Override
  public void addAction(String key, final Action action) {
    // Quit, Preferences, and About go on the special application menu

    if ("General.quit".equals(key)) {
      final Application app = Application.getApplication();
      app.addApplicationListener(new ApplicationAdapter() {
        @Override
        public void handleQuit(ApplicationEvent e) {
          e.setHandled(false);
          action.actionPerformed(null);
        }
      });

      // no need to track enabled state, quit is always active
    }
    else if ("Prefs.edit_preferences".equals(key)) {
      final Application app = Application.getApplication();
      app.addApplicationListener(new ApplicationAdapter() {
        @Override
        public void handlePreferences(ApplicationEvent e) {
          e.setHandled(true);
          action.actionPerformed(null);
        }
      });

      app.addPreferencesMenuItem();
      app.setEnabledPreferencesMenu(action.isEnabled());

      // track the enabled state of the prefs action
      action.addPropertyChangeListener(new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent e) {
          if ("enabled".equals(e.getPropertyName())) {
            app.setEnabledPreferencesMenu((Boolean) e.getNewValue());
          }
        }
      });
    }
    else if ("AboutScreen.about_vassal".equals(key)) {
      final Application app = Application.getApplication();
      app.addApplicationListener(new ApplicationAdapter() {
        @Override
        public void handleAbout(ApplicationEvent e) {
          e.setHandled(true);
          action.actionPerformed(null);
        }
      });

      app.addAboutMenuItem();
      app.setEnabledAboutMenu(action.isEnabled());

      // track the enabled state of the prefs action
      action.addPropertyChangeListener(new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent e) {
          if ("enabled".equals(e.getPropertyName())) {
            app.setEnabledAboutMenu((Boolean) e.getNewValue());
          }
        }
      });
    }
    else {
      // this is not one of the special actions
      super.addAction(key, action);
    }
  }
}

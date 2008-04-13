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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;

import VASSAL.Info;


public abstract class MenuManager {
  protected static MenuManager instance;

  public static MenuManager getInstance() {
    if (instance == null) throw new IllegalStateException();
    return instance;
  }

  public MenuManager() {
    if (instance != null) throw new IllegalStateException();
    instance = this;
  }

  public abstract JMenuBar getMenuBarFor(JFrame fc);

  public abstract JMenu createMenu(String text);

  public abstract JMenuItem createMenuItem(Action action);

  public abstract JCheckBoxMenuItem createCheckBoxMenuItem(Action action);

  public abstract JRadioButtonMenuItem createRadioButtonMenuItem(Action action);

  public abstract JSeparator createSeparator();

  private Map<String,List<JMenuItem>> actionLocations =
    new HashMap<String,List<JMenuItem>>();

  public JMenuItem addKey(String key) {
    List<JMenuItem> items = actionLocations.get(key);
    if (items == null) {
      items = new ArrayList<JMenuItem>();
      actionLocations.put(key, items);
    }

    final JMenuItem item = createMenuItem(null);
    items.add(item);
    return item;
  }

  public void addAction(String key, Action a) {
    final List<JMenuItem> items = actionLocations.get(key);
    if (items != null) {
      for (JMenuItem i : items) {
        i.setAction(a);
      }
    }
  }  
}

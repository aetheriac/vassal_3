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

import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;

public abstract class GeneralMenuManager extends MenuManager {
  public GeneralMenuManager() {
    super();
  }

  @Override
  public JMenu createMenu(String text) {
    return new JMenu(text);
  }

  @Override
  public JMenuItem createMenuItem(Action action) {
    return new JMenuItem(action);
  }

  @Override
  public JCheckBoxMenuItem createCheckBoxMenuItem(Action action) {
    return new JCheckBoxMenuItem(action);
  }

  @Override
  public JRadioButtonMenuItem createRadioButtonMenuItem(Action action) {
    return new JRadioButtonMenuItem(action);
  }

  @Override
  public JSeparator createSeparator() {
    return new JSeparator();
  }
}

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

import java.awt.event.ActionListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JMenuItem;

public class MasterJMenuItem extends JMenuItem { 
  private List<WeakReference<JMenuItem>> slaves =
    new ArrayList<WeakReference<JMenuItem>>();
  
  public MasterJMenuItem() {
    super();
  }

  public MasterJMenuItem(Action a) {
    super(a);
  }

  public MasterJMenuItem(Icon icon) {
    super(icon);
  }

  public MasterJMenuItem(String text) {
    super(text);
  }

  public MasterJMenuItem(String text, Icon icon) {
    super(text, icon);
  }

  public MasterJMenuItem(String text, int mnemonic) {
    super(text, mnemonic);
  }

  public JMenuItem createSlave() {
    final JMenuItem item;
    final Action a = getAction();
    if (a != null) {
      item = new JMenuItem(a);
    }
    else {
      item = new JMenuItem(getText(), getIcon());
      for (ActionListener l : getActionListeners()) {
        item.addActionListener(l);
      }
    }
    
    slaves.add(new WeakReference<JMenuItem>(item));
    return item;
  }
}

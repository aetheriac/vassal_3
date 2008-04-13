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
import javax.swing.JRadioButtonMenuItem;

public class MasterJRadioButtonMenuItem extends JRadioButtonMenuItem {
  private static final long serialVersionUID = 1L;
 
  private List<WeakReference<JRadioButtonMenuItem>> slaves =
    new ArrayList<WeakReference<JRadioButtonMenuItem>>();
  
  public MasterJRadioButtonMenuItem() {
    super();
  }

  public MasterJRadioButtonMenuItem(Action a) {
    super(a);
  }

  public MasterJRadioButtonMenuItem(Icon icon) {
    super(icon);
  }

  public MasterJRadioButtonMenuItem(Icon icon, boolean state) {
    super(icon, state);
  }

  public MasterJRadioButtonMenuItem(String text) {
    super(text);
  }

  public MasterJRadioButtonMenuItem(String text, boolean state) {
    super(text, state);
  }

  public MasterJRadioButtonMenuItem(String text, Icon icon) {
    super(text, icon);
  }

  public MasterJRadioButtonMenuItem(String text, Icon icon, boolean state) {
    super(text, icon, state);
  }

  public JRadioButtonMenuItem createSlave() {
    final JRadioButtonMenuItem item;
    final Action a = getAction();
    if (a != null) {
      item = new JRadioButtonMenuItem(a);
    }
    else {
      item = new JRadioButtonMenuItem(getText(), getIcon());
      for (ActionListener l : getActionListeners()) {
        item.addActionListener(l);
      }
    }

//    item.setSelected(getSelected());   
    
// FIXME: do something to keep state synchronized?

    slaves.add(new WeakReference<JRadioButtonMenuItem>(item));
    return item;
  }

/*
  JRadioButtonMenuItem[] getSlaves() {
    final ArrayList<JRadioButtonMenuItem> sl =
      new ArrayList<JRadioButtonMenuItem>(slaves.size());

    for (WeakReference<JRadioButtonMenuItem> ref : slaves) {
      final ButtonGroup bg = ref.get();
      if (bg != null) {
        sl.add(bg);
      }
    }

    return sl.toArray(new JRadioButtonMenuItem[sl.size()]);
  }
*/
}

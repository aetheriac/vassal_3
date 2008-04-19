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

import java.awt.Container;
import java.awt.event.ActionListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;

public class MasterJCheckBoxMenuItem extends JCheckBoxMenuItem {
  private static final long serialVersionUID = 1L;
 
  private List<WeakReference<JCheckBoxMenuItem>> slaves =
    new ArrayList<WeakReference<JCheckBoxMenuItem>>();
  
  public MasterJCheckBoxMenuItem() {
    super();
  }

  public MasterJCheckBoxMenuItem(Action a) {
    super(a);
  }

  public MasterJCheckBoxMenuItem(Icon icon) {
    super(icon);
  }

  public MasterJCheckBoxMenuItem(String text) {
    super(text);
  }

  public MasterJCheckBoxMenuItem(String text, boolean state) {
    super(text, state);
  }

  public MasterJCheckBoxMenuItem(String text, Icon icon) {
    super(text, icon);
  }

  public MasterJCheckBoxMenuItem(String text, Icon icon, boolean state) {
    super(text, icon, state);
  }

  void unparent() {
    final Container parent = getParent();
    if (parent != null) parent.remove(this);

    for (WeakReference<JCheckBoxMenuItem> ref : slaves) {
      final JCheckBoxMenuItem item = ref.get();
      if (item != null) {
        final Container p = item.getParent();
        if (p != null) {
          p.remove(item);
        }
      }
    }
  }

  JCheckBoxMenuItem createSlave() {
    final JCheckBoxMenuItem item;
    final Action a = getAction();
    if (a != null) {
      item = new JCheckBoxMenuItem(a);
    }
    else {
      item = new JCheckBoxMenuItem(getText(), getIcon());
      for (ActionListener l : getActionListeners()) {
        item.addActionListener(l);
      }
    }

    item.setSelected(isSelected());   
    
// FIXME: do something to keep state synchronized?

    slaves.add(new WeakReference<JCheckBoxMenuItem>(item));
    return item;
  }
}

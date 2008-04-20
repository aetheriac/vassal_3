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

public class MenuItemProxy extends AbstractProxy<JMenuItem> {
  protected Action action;

  public MenuItemProxy() {
    this(null);
  }

  public MenuItemProxy(Action action) {
    this.action = action;
  }

  public Action getAction() {
    return action;
  }

  public void setAction(final Action action) {
    this.action = action;

    forEachPeer(new Functor<JMenuItem>() {
      public void apply(JMenuItem item) {
        item.setAction(action);
      }
    });
  }

  @Override
  JMenuItem createPeer() {
    final JMenuItem item = new JMenuItem(action);
  
    peers.add(new WeakReference<JMenuItem>(item, queue));
    return item;
  }
}

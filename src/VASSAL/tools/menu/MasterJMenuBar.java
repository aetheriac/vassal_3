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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

public class MasterJMenuBar extends JMenuBar {
  private static final long serialVersionUID = 1L;

  private List<WeakReference<JMenuBar>> slaves =
    new ArrayList<WeakReference<JMenuBar>>();
 
  @Override 
  public JMenu add(JMenu menu) {
    if (!(menu instanceof MasterJMenu)) throw new IllegalStateException();

    super.add(menu);
    for (WeakReference<JMenuBar> ref : slaves) {
      final JMenuBar mb = ref.get();
      if (mb != null) {
        mb.add(((MasterJMenu) menu).createSlave());
      }
    }
    return menu;
  } 

  public JMenuBar createSlave() {
    final JMenuBar mb = new JMenuBar();

    final int count = getMenuCount();
    for (int i = 0; i < count; i++) {
      mb.add(((MasterJMenu) getMenu(i)).createSlave());
    }
    
    slaves.add(new WeakReference<JMenuBar>(mb));
    return mb;
  }
}

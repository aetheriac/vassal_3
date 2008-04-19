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
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JSeparator;

public class MasterJSeparator extends JSeparator {
  private static final long serialVersionUID = 1L;

  private List<WeakReference<JSeparator>> slaves =
    new ArrayList<WeakReference<JSeparator>>();

  void unparent() {
    final Container parent = getParent();
    if (parent != null) parent.remove(this);

    for (WeakReference<JSeparator> ref : slaves) {
      final JSeparator sep = ref.get();
      if (sep != null) {
        final Container p = sep.getParent();
        if (p != null) {
          p.remove(sep);
        }
      }
    }
  }

  JSeparator createSlave() {
    final JSeparator sep = new JSeparator();
    slaves.add(new WeakReference<JSeparator>(sep));
    return sep;
  }

/*
  List<JSeparator> getSlaves() {
    final ArrayList<JSeparator> l = new ArrayList<JSeparator>(slaves.size());
    for (WeakReference<JSeparator> ref : slaves) {
      final JSeparator sep = ref.get();
      if (sep != null) l.add(sep);
    }
    return l;
  }
*/ 
}

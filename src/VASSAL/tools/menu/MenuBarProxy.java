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
import javax.swing.JMenuBar;

public class MenuBarProxy extends AbstractProxy<JMenuBar> {
  private final List<AbstractProxy<?>> children =
    new ArrayList<AbstractProxy<?>>();

/*
  private final List<ButtonGroupProxy> groups =
    new ArrayList<ButtonGroupProxy>();
*/

  @Override
  public void add(final AbstractProxy<?> item) {
    children.add(item);
    item.parent = this;

    if (item instanceof Marker) return;
    if (!(item instanceof MenuProxy)) throw new UnsupportedOperationException();

    forEachPeer(new Functor<JMenuBar>() {
      public void apply(JMenuBar mb) {
        mb.add(item.createPeer());
      }
    });
  }
  
  protected int proxyIndexToRealIndex(int pos) {
    // find the true position, neglecting markers
    int j = -1;
    for (int i = 0; i <= pos; i++) {
      if (!(children.get(i) instanceof Marker)) j++;
    }
    return j;
  }

  @Override
  public void insert(final AbstractProxy<?> item, int pos) {
    children.add(pos, item);
    item.parent = this;
    
    if (item instanceof Marker) return;
    if (!(item instanceof MenuProxy)) throw new UnsupportedOperationException();

    final int rpos = proxyIndexToRealIndex(pos);

    forEachPeer(new Functor<JMenuBar>() {
      public void apply(JMenuBar mb) {
        mb.add(item.createPeer(), rpos);
      }
    });
  }

  @Override
  public void remove(AbstractProxy<?> item) {
    if (children.remove(item)) {
      item.parent = null;
      item.unparent();
    }
  }

  @Override
  public void remove(int pos) {
    final AbstractProxy<?> item = children.remove(pos);
    item.parent = null;
    item.unparent();
  }

  @Override
  public int getChildCount() {
    return children.size();
  }  
  
  @Override
  public AbstractProxy<?>[] getChildren() {
    return children.toArray(new AbstractProxy<?>[children.size()]);
  }

  @Override
  public AbstractProxy<?> getChild(int pos) {
    return children.get(pos);
  }

  public int getIndex(MenuProxy menu) {
    return children.indexOf(menu);
  }

/*
  public ButtonGroupProxy addButtonGroup(final ButtonGroupProxy group) {
    groups.add(group);

    forEachPeer(new Functor<JMenuBar>() {
      public void apply(JMenuBar mb) {
        group.createPeer().setOwner(mb);
      }
    });

    return group;
  }

  public void removeButtonGroup(final ButtonGroupProxy group) {
    groups.remove(group);
  }
*/

  @Override
  public JMenuBar createPeer() {
    final JMenuBar mb = new JMenuBar();
 
    for (AbstractProxy<?> item : children) {
      if (item instanceof Marker) continue;
      mb.add(item.createPeer());
    }

/*
    for (ButtonGroupProxy group : groups) {
      group.createPeer().setOwner(mb);
    }
*/

    peers.add(new WeakReference<JMenuBar>(mb, queue));
    return mb;
  }
}

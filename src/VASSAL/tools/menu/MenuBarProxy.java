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
  private final List<MenuProxy> children = new ArrayList<MenuProxy>();

/*
  private final List<ButtonGroupProxy> groups =
    new ArrayList<ButtonGroupProxy>();
*/

  public void add(final MenuProxy menu) {
    children.add(menu);
    menu.parent = this;

    forEachPeer(new Functor<JMenuBar>() {
      public void apply(JMenuBar mb) {
        mb.add(menu.createPeer());
      }
    });
  } 

  public void insert(final MenuProxy menu, final int pos) {
    children.add(pos, menu);
    menu.parent = this;
    
    forEachPeer(new Functor<JMenuBar>() {
      public void apply(JMenuBar mb) {
        mb.add(menu.createPeer(), pos);
      }
    });
  }

  public void remove(MenuProxy menu) {
    if (children.remove(menu)) {
      menu.parent = null;
      menu.unparent();
    }
  }

  @Override
  public void remove(int pos) {
    final MenuProxy menu = children.remove(pos);
    menu.parent = null;
    menu.unparent();
  }

  @Override
  public AbstractProxy<?>[] getChildren() {
    return children.toArray(new AbstractProxy<?>[children.size()]);
  }

  @Override
  public MenuProxy getChild(int pos) {
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
 
    for (MenuProxy menu : children) {
      mb.add(menu.createPeer());
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

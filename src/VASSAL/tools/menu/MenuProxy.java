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
import javax.swing.JMenu;

public class MenuProxy extends AbstractProxy<JMenu> {
  private final List<AbstractProxy<?>> children =
    new ArrayList<AbstractProxy<?>>();

  private String text;

  public MenuProxy() {
  }

  public MenuProxy(String text) {
    this.text = text;
  }

  @Override
  public void add(final AbstractProxy<?> item) {
    children.add(item);
    item.parent = this;
 
    forEachPeer(new Functor<JMenu>() {
      public void apply(JMenu menu) {
        menu.add(item.createPeer());
      }
    });
  } 

  @Override
  public void insert(final AbstractProxy<?> item, final int pos) {
    children.add(pos, item);
    item.parent = this;
    
    forEachPeer(new Functor<JMenu>() {
      public void apply(JMenu menu) {
        menu.add(item.createPeer(), pos);
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
  public AbstractProxy<?>[] getChildren() {
    return children.toArray(new AbstractProxy<?>[children.size()]);
  }

  @Override
  public AbstractProxy<?> getChild(int pos) {
    return children.get(pos);
  }

  public void addSeparator() {
    add(new SeparatorProxy());
  }

  public void insertSeparator(int pos) {
    insert(new SeparatorProxy(), pos);
  }

  public void setText(final String text) {
    this.text = text;

    forEachPeer(new Functor<JMenu>() {
      public void apply(JMenu menu) {
        menu.setText(text);
      }
    });
  }

  @Override
  public int getIndex(AbstractProxy<?> child) {
    return children.indexOf(child);
  }

  @Override
  JMenu createPeer() {
    final JMenu menu = new JMenu(text);

    for (AbstractProxy<?> item : children) {
      menu.add(item.createPeer());
    }
    
    peers.add(new WeakReference<JMenu>(menu, queue));
    return menu;
  }
}

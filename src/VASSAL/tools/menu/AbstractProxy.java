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
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;

public abstract class AbstractProxy<T extends JComponent> {
  protected final List<WeakReference<T>> peers =
    new ArrayList<WeakReference<T>>();

  protected final ReferenceQueue<T> queue = new ReferenceQueue<T>();

  protected void processQueue() {
    Reference<? extends T> ref;
    while ((ref = queue.poll()) != null) {
      peers.remove(ref); 
    }
  }

  public int getChildCount() {
    return 0;
  }

  public AbstractProxy<?>[] getChildren() {
    return new AbstractProxy<?>[0];
  } 

  public AbstractProxy<?> getChild(int pos) {
    throw new UnsupportedOperationException();
  }

  public int getIndex(AbstractProxy<?> child) {
    throw new UnsupportedOperationException();
  }

  protected AbstractProxy<?> parent;

  public AbstractProxy<?> getParent() {
    return parent;
  } 

  public void add(AbstractProxy<?> item) {
    throw new UnsupportedOperationException();
  } 
 
  public void insert(AbstractProxy<?> item, int pos) {
    throw new UnsupportedOperationException();
  } 

  public void remove(AbstractProxy<?> item) {
    throw new UnsupportedOperationException();
  } 

  public void remove(int pos) {
    throw new UnsupportedOperationException();
  } 

  protected boolean enabled = true;

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(final boolean enabled) {
    this.enabled = enabled;

    forEachPeer(new Functor<T>() {
      public void apply(T peer) {
        peer.setEnabled(enabled);
      }
    });
  }

  abstract T createPeer();

  protected void forEachPeer(Functor<T> functor) {
    processQueue();

    for (WeakReference<T> ref : peers) {
      final T peer = ref.get();
      if (peer != null) {
        functor.apply(peer);
      }
    }
  }

  void unparent() {
    forEachPeer(new Functor<T>() {
      public void apply(T peer) {
        final Container parent = peer.getParent();
        if (parent != null) {
          parent.remove(peer);
        }
      }
    });
  }

  interface Functor<T> {
    public void apply(T peer);
  }
}

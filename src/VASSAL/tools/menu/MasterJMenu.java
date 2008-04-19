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

import java.awt.Component;
import java.awt.Container;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

public class MasterJMenu extends JMenu {
  private static final long serialVersionUID = 1L;

  private List<WeakReference<JMenu>> slaves =
    new ArrayList<WeakReference<JMenu>>();

  public MasterJMenu() {
    super();
  }
  
  public MasterJMenu(String text) {
    super(text);
  }

  @Override
  public JMenuItem add(JMenuItem item) {
    if (item instanceof MasterJMenuItem) {
      super.add(item);
      for (WeakReference<JMenu> ref : slaves) {
        final JMenu menu = ref.get();
        if (menu != null) {
          menu.add(((MasterJMenuItem) item).createSlave());
        }
      }
    }
    else if (item instanceof MasterJCheckBoxMenuItem) {
      super.add(item);
      for (WeakReference<JMenu> ref : slaves) {
        final JMenu menu = ref.get();
        if (menu != null) {
          menu.add(((MasterJCheckBoxMenuItem) item).createSlave());
        }
      }
    }
    else if (item instanceof MasterJRadioButtonMenuItem) {
      super.add(item);
      for (WeakReference<JMenu> ref : slaves) {
        final JMenu menu = ref.get();
        if (menu != null) {
          menu.add(((MasterJRadioButtonMenuItem) item).createSlave());
        }
      }
    }
    else throw new IllegalStateException();

    return item;
  } 

  @Override
  public Component add(Component comp) {
    if (comp instanceof MasterJSeparator) {
      super.add(comp);
      for (WeakReference<JMenu> ref : slaves) {
        final JMenu menu = ref.get();
        if (menu != null) {
          menu.add(((MasterJSeparator) comp).createSlave());
        }
      }
    }
    else throw new IllegalStateException();

    return comp;
  }

  @Override
  public void remove(Component comp) {
    if (comp instanceof MasterJMenu) {
      ((MasterJMenu) comp).unparent(); 
    }
    else if (comp instanceof MasterJMenuItem) {
      ((MasterJMenuItem) comp).unparent(); 
    }
    else if (comp instanceof MasterJCheckBoxMenuItem) {
      ((MasterJCheckBoxMenuItem) comp).unparent(); 
    }
    else if (comp instanceof MasterJRadioButtonMenuItem) {
      ((MasterJRadioButtonMenuItem) comp).unparent(); 
    }
    else if (comp instanceof MasterJSeparator) {
      ((MasterJSeparator) comp).unparent(); 
    }
    else {
      throw new IllegalStateException();
    }
  }

  void unparent() {
    final Container parent = getParent();
    if (parent != null) parent.remove(this);

    for (WeakReference<JMenu> ref : slaves) {
      final JMenu menu = ref.get();
      if (menu != null) {
        final Container p = menu.getParent();
        if (p != null) {
          p.remove(menu);
        }
      }
    }
  }

  @Override
  public void addSeparator() {
    add(new MasterJSeparator());
  }

  JMenu createSlave() {
    final JMenu menu = new JMenu(getText());

    for (Component c : getMenuComponents()) {
      if (c instanceof MasterJMenu) {
        menu.add(((MasterJMenu) c).createSlave());
      }
      else if (c instanceof MasterJMenuItem) {
        menu.add(((MasterJMenuItem) c).createSlave());
      }
      else if (c instanceof MasterJCheckBoxMenuItem) {
        menu.add(((MasterJCheckBoxMenuItem) c).createSlave());
      }
      else if (c instanceof MasterJRadioButtonMenuItem) {
        menu.add(((MasterJRadioButtonMenuItem) c).createSlave());
      }
      else if (c instanceof MasterJSeparator) {
        menu.add(((MasterJSeparator) c).createSlave());
      }
      else {
        throw new IllegalStateException();
      }
    }
    
    slaves.add(new WeakReference<JMenu>(menu));
    return menu;
  }

/*
  List<JMenu> getSlaves() {
    final ArrayList<JMenu> l = new ArrayList<JMenu>(slaves.size());
    for (WeakReference<JMenu> ref : slaves) {
      final JMenu menu = ref.get();
      if (menu != null) l.add(menu);
    }
    return l;
  }
*/
}

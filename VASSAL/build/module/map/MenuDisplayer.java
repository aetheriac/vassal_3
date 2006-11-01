/*
 * $Id$
 *
 * Copyright (c) 2000-2003 by Rodney Kinney
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
package VASSAL.build.module.map;

import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import VASSAL.build.Buildable;
import VASSAL.build.module.Map;
import VASSAL.counters.Deck;
import VASSAL.counters.EventFilter;
import VASSAL.counters.GamePiece;
import VASSAL.counters.KeyCommand;
import VASSAL.counters.KeyCommandSubMenu;
import VASSAL.counters.PieceFinder;
import VASSAL.counters.Properties;

public class MenuDisplayer extends MouseAdapter implements Buildable {
  public static Font POPUP_MENU_FONT = new Font("Dialog", 0, 10);

  protected Map map;
  protected PieceFinder targetSelector;

  public void addTo(Buildable b) {
    targetSelector = createTargetSelector();
    map = (Map) b;
    map.addLocalMouseListener(this);
  }

  /**
   * Return a {@link PieceFinder} instance that will select a
   * {@link GamePiece} whose menu will be displayed when the
   * user clicks on the map
   * @return
   */
  protected PieceFinder createTargetSelector() {
    return new PieceFinder.PieceInStack() {
      public Object visitDeck(Deck d) {
        Point pos = d.getPosition();
        Point p = new Point(pt.x - pos.x, pt.y - pos.y);
        if (d.getShape().contains(p)) {
          return d;
        }
        else {
          return null;
        }
      }
    };
  }

  public void add(Buildable b) {
  }

  public Element getBuildElement(Document doc) {
    return doc.createElement(getClass().getName());
  }

  public void build(Element e) {
  }

  public static JPopupMenu createPopup(GamePiece target) {
    return createPopup(target, false);
  }

  /**
   *
   * @param target
   * @param global If true, then apply the KeyCommands globally, i.e. to all selected pieces
   * @return
   */
  public static JPopupMenu createPopup(GamePiece target, boolean global) {
    JPopupMenu popup = new JPopupMenu();
    KeyCommand c[] = (KeyCommand[]) target.getProperty(Properties.KEY_COMMANDS);
    if (c != null) {
      List commands = new ArrayList();
      List strokes = new ArrayList();
      HashMap subMenus = new HashMap(); // Maps instances of KeyCommandSubMenu to corresponding JMenu
      HashMap commandNames = new HashMap(); // Maps name to a list of commands with that name
      for (int i = 0; i < c.length; ++i) {
        c[i].setGlobal(global);
        KeyStroke stroke = c[i].getKeyStroke();
        JMenuItem item = null;
        if (c[i] instanceof KeyCommandSubMenu) {
          JMenu subMenu = new JMenu(c[i].getName());
          subMenu.setFont(POPUP_MENU_FONT);
          subMenus.put(c[i], subMenu);
          item = subMenu;
          commands.add(item);
          strokes.add(KeyStroke.getKeyStroke('\0'));
        }
        else {
          if (strokes.contains(stroke)) {
            JMenuItem command = (JMenuItem) commands.get(strokes.indexOf(stroke));
            String commandName = (String) command.getAction().getValue(Action.NAME);
            if (commandName == null
                || commandName.length() < c[i].getName().length()) {
              item = new JMenuItem(c[i]);
              item.setFont(POPUP_MENU_FONT);
              item.setEnabled(c[i].isEnabled());
              commands.set(strokes.indexOf(stroke), item);
            }
          }
          else {
            strokes.add(stroke != null ? stroke : KeyStroke.getKeyStroke('\0'));
            item = new JMenuItem(c[i]);
            item.setFont(POPUP_MENU_FONT);
            item.setEnabled(c[i].isEnabled());
            commands.add(item);
          }
        }
        if (c[i].getName() != null
            && c[i].getName().length() > 0
            && item != null) {
          List l = (List) commandNames.get(c[i].getName());
          if (l == null) {
            l = new ArrayList();
            commandNames.put(c[i].getName(), l);
          }
          l.add(item);
        }
      }
      // Move commands from main menu into submenus
      for (Iterator it = subMenus.keySet().iterator(); it.hasNext();) {
        KeyCommandSubMenu menuCommand = (KeyCommandSubMenu) it.next();
        JMenu subMenu = (JMenu) subMenus.get(menuCommand);
        for (Iterator it2 = menuCommand.getCommands(); it2.hasNext();) {
          List matchingCommands = (List) commandNames.get(it2.next());
          if (matchingCommands != null) {
            for (Iterator it3 = matchingCommands.iterator(); it3.hasNext();) {
              JMenuItem item = (JMenuItem) it3.next();
              subMenu.add(item);
              commands.remove(item);
            }
          }
        }
      }
      for (Iterator it = commands.iterator();
           it.hasNext();) {
        popup.add((JMenuItem) it.next());
      }
    }
    return popup;
  }

  public void mouseReleased(MouseEvent e) {
    if (e.isMetaDown()) {
      final GamePiece p = map.findPiece(e.getPoint(), targetSelector);
      if (p != null) {
        EventFilter filter = (EventFilter) p.getProperty(Properties.SELECT_EVENT_FILTER);
        if (filter == null
            || !filter.rejectEvent(e)) {
          JPopupMenu popup = createPopup(p, true);
          Point pt = map.componentCoordinates(e.getPoint());
          popup.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuCanceled
                (javax.swing.event.PopupMenuEvent evt) {
              map.repaint();
            }

            public void popupMenuWillBecomeInvisible
                (javax.swing.event.PopupMenuEvent evt) {
              map.repaint();
            }

            public void popupMenuWillBecomeVisible
                (javax.swing.event.PopupMenuEvent evt) {
            }
          });
//          KeyBuffer.getBuffer().clear();
          popup.show(map.getView(), pt.x, pt.y);
          e.consume();
        }
      }
    }
  }
}

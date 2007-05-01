/*
 * $Id: TranslateAction.java 1814 2007-03-06 13:28:24 +0000 (Tue, 06 Mar 2007) rodneykinney $
 *
 * Copyright (c) 2000-2007 by Rodney Kinney, Brent Easton
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
package VASSAL.i18n;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Hashtable;
import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;

import VASSAL.build.Configurable;
import VASSAL.build.module.documentation.HelpWindow;
import VASSAL.configure.ConfigureTree;

/**
 * Action to open the Translation Window for a component
 */
public class TranslateAction extends AbstractAction {
  private static final long serialVersionUID = 1L;

  protected Configurable target;
  protected HelpWindow helpWindow;
  protected static Hashtable openWindows = new Hashtable();
  protected Frame dialogOwner;
  protected ConfigureTree tree;

  public TranslateAction(Configurable target, HelpWindow helpWindow, ConfigureTree tree) {
    super("Translate");
    this.helpWindow = helpWindow;
    this.target = target;
    this.dialogOwner = (Frame) SwingUtilities.getAncestorOfClass(Frame.class, tree);
    this.tree = tree;
  }

  public void actionPerformed(ActionEvent evt) {
    TranslateWindow w = (TranslateWindow) openWindows.get(target);
      if (w == null) {
        w = new TranslateWindow(dialogOwner, false, target, helpWindow, tree);
        w.addWindowListener(new WindowAdapter() {
          public void windowClosed(WindowEvent e) {
            openWindows.remove(target);
          }
        });
        openWindows.put(target,w);
        w.setVisible(true);
      }
      w.toFront();
  }
}

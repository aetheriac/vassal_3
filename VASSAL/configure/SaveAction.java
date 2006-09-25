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
package VASSAL.configure;

import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

/**
 * General-purpose "Save" action
 */
public abstract class SaveAction extends AbstractAction {
  public SaveAction() {
    URL iconURL = getClass().getResource("/images/Save16.gif");
    if (iconURL != null) {
      putValue(Action.SMALL_ICON, new ImageIcon(iconURL));
    }
    else {
      putValue(Action.NAME, "Save");
    }
    putValue(Action.SHORT_DESCRIPTION, "Save");
  }

}

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
/*
 * Created by IntelliJ IDEA.
 * User: rkinney
 * Date: Jun 30, 2002
 * Time: 6:12:50 AM
 * To change template for new class use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package VASSAL.build.module;

import java.awt.Dimension;
import java.awt.Rectangle;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.build.module.map.CounterDetailViewer;
import VASSAL.build.module.map.HandMetrics;
import VASSAL.build.module.map.StackExpander;
import VASSAL.build.module.map.StackMetrics;
import VASSAL.counters.GamePiece;

public class PlayerHand extends PrivateMap {
  public void build(org.w3c.dom.Element el) {
    super.build(el);
    if (el == null) {
      for (StackExpander se : getComponentsOf(StackExpander.class)) {
        remove(se);
        removeLocalMouseListener(se);
      }
      for (CounterDetailViewer cdv :
              getComponentsOf(CounterDetailViewer.class)) {
        remove(cdv);
        cdv.removeFrom(this);
      }
    }
  }

  public static String getConfigureTypeName() {
    return "Player Hand";
  }

  public HelpFile getHelpFile() {
    return HelpFile.getReferenceManualPage("PlayerHand.htm"); //$NON-NLS-1$
  }

  public StackMetrics getStackMetrics() {
    if (metrics == null) {
      metrics = new HandMetrics();
      metrics.build(null);
      add(metrics);
      metrics.addTo(this);
    }
    return metrics;
  }

  public Dimension mapSize() {
    final Rectangle r = new Rectangle(0,0,200,200);
    for (GamePiece p : pieces.getPieces()) r.add(boundingBoxOf(p));
    r.add(new Rectangle(super.mapSize()));
    return r.getSize();
  }
}

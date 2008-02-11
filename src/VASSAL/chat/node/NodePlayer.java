/*
 *
 * Copyright (c) 2000-2007 by Rodney Kinney
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
 * Copyright (c) 2003 by Rodney Kinney.  All rights reserved.
 * Date: May 11, 2003
 */
package VASSAL.chat.node;

import java.util.Properties;
import VASSAL.chat.SimplePlayer;
import VASSAL.chat.SimpleStatus;

/**
 * A {@link SimplePlayer} subclass used in clients of the hierarchical server
 */
public class NodePlayer extends SimplePlayer {
  public static final String ID = "id"; //$NON-NLS-1$

  public NodePlayer(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof NodePlayer)) return false;

    final NodePlayer hPlayer = (NodePlayer) o;

    if (id != null ? !id.equals(hPlayer.id) : hPlayer.id != null) return false;

    return true;
  }

  public int hashCode() {
    return (id != null ? id.hashCode() : 0);
  }

  public static final String NAME = "name"; //$NON-NLS-1$
  public static final String LOOKING = "looking"; //$NON-NLS-1$
  public static final String AWAY = "away"; //$NON-NLS-1$
  public static final String PROFILE = "profile"; //$NON-NLS-1$

  public void setInfo(Properties p) {
    name = p.getProperty(NAME,"???"); //$NON-NLS-1$
    id = p.getProperty(ID,id);
    setStatus(new SimpleStatus("true".equals(p.getProperty(LOOKING)),"true".equals(p.getProperty(AWAY)),p.getProperty(PROFILE,""))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }



  public Properties toProperties() {
    Properties p1 = new Properties();
    if (name != null) {
      p1.put(NAME,name);
    }
    SimpleStatus status = (SimpleStatus)getStatus();
    p1.put(LOOKING,""+status.isLooking()); //$NON-NLS-1$
    p1.put(AWAY,""+status.isAway()); //$NON-NLS-1$
    String profile = status.getProfile();
    if (profile != null) {
      p1.put(PROFILE,profile);
    }
    Properties p = p1;
    p.put(ID,id);
    return p;
  }

}
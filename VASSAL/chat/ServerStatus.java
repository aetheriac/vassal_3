/*
 * $Id: ServerStatus.java,v 1.5 2006-12-09 22:49:24 rkinney Exp $
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
package VASSAL.chat;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the history and current state of connections to the chat room server 
 * @author rkinney
 *
 */
public interface ServerStatus {
  /** Return the current connections to the server */
  public ModuleSummary[] getStatus();
  
  public String[] getSupportedTimeRanges();
  /**
   *  Return the connections to the server within <code>time</code> milliseconds of the current time
   */
  public ModuleSummary[] getHistory(String timeRange);

  public static class ModuleSummary {
    private String moduleName;
    private Map rooms = new HashMap();

    public ModuleSummary(String moduleName, Room[] rooms) {
      this.moduleName = moduleName;
      for (int i = 0; i < rooms.length; i++) {
        this.rooms.put(rooms[i].getName(),rooms[i]);
      }
    }

    public String getModuleName() {
      return moduleName;
    }

    public void addRoom(Room r) {
      rooms.put(r.getName(),r);
    }

    public SimpleRoom getRoom(String name) {
      return (SimpleRoom)rooms.get(name);
    }

    public Room[] getRooms() {
      return (Room[]) rooms.values().toArray(new Room[rooms.size()]);
    }

    public String toString() {
      return moduleName;
    }
  }
}

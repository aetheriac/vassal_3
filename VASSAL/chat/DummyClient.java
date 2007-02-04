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
package VASSAL.chat;

import java.beans.PropertyChangeListener;
import VASSAL.command.Command;

/** 
 * Empty server
 * @author rkinney
 *
 */
public class DummyClient implements ChatServerConnection {
  private Player playerInfo = new SimplePlayer("<nobody>");


  public DummyClient() {
  }

  public Room[] getAvailableRooms() {
    return new Room[0];
  }

  public Room getRoom() {
    return null;
  }

  public ServerStatus getStatusServer() {
    return null;
  }

  public void sendTo(Player recipient, Command c) {
  }

  public void setRoom(Room r) {
  }

  public void addPropertyChangeListener(String propertyName, PropertyChangeListener l) {
  }

  public boolean isConnected() {
    return false;
  }

  public void sendToOthers(Command c) {
  }

  public void setConnected(boolean connect) {
  }

  public Player getUserInfo() {
    return playerInfo;
  }

  public void setUserInfo(Player playerInfo) {
    this.playerInfo = playerInfo;
  }

}

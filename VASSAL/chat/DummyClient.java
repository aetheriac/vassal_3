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
import VASSAL.build.GameModule;
import VASSAL.chat.peer2peer.PeerPoolInfo;
import VASSAL.chat.ui.ChatControlsInitializer;
import VASSAL.chat.ui.ChatServerControls;
import VASSAL.chat.ui.MessageBoardControlsInitializer;
import VASSAL.chat.ui.ServerStatusControlsInitializer;
import VASSAL.command.Command;

/** 
 * Empty server
 * @author rkinney
 *
 */
public class DummyClient implements ChatServerConnection, ChatControlsInitializer {
  private Player playerInfo = new SimplePlayer("<nobody>");
  private HttpMessageServer httpMessageServer;
  private MessageBoardControlsInitializer msgControls;
  private ServerStatusControlsInitializer statusControls;


  public DummyClient() {
    PeerPoolInfo publicInfo = new PeerPoolInfo() {
      public String getModuleName() {
        return GameModule.getGameModule() == null ? "<unnamed module>" : GameModule.getGameModule().getGameName();
      }

      public String getUserName() {
        return GameModule.getGameModule() == null ? "<anonymous>" : (String) GameModule.getGameModule().getPrefs().getValue(GameModule.REAL_NAME);
      }
    };
    httpMessageServer = new HttpMessageServer("http://www.vassalengine.org/util/getMessages", "http://www.vassalengine.org/util/postMessage",
            "http://www.vassalengine.org/util/motd", publicInfo);
    msgControls = new MessageBoardControlsInitializer("Messages",httpMessageServer);
    statusControls = new ServerStatusControlsInitializer(new CgiServerStatus());
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

  public void initializeControls(ChatServerControls controls) {
    msgControls.initializeControls(controls);
    statusControls.initializeControls(controls);
  }

  public void uninitializeControls(ChatServerControls controls) {
    msgControls.uninitializeControls(controls);
    statusControls.uninitializeControls(controls);
  }

}

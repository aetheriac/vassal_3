/*
 *
 * Copyright (c) 2000-2006 by Rodney Kinney
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
package VASSAL.chat.node;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Properties;
import javax.swing.SwingUtilities;
import VASSAL.build.GameModule;
import VASSAL.chat.ChatServerConnection;
import VASSAL.chat.ChatServerFactory;
import VASSAL.chat.HttpMessageServer;
import VASSAL.chat.peer2peer.PeerPoolInfo;
import VASSAL.command.Command;
import VASSAL.configure.StringConfigurer;
import VASSAL.configure.TextConfigurer;

/**
 * @author rkinney
 */
public class NodeClientFactory extends ChatServerFactory {
  private static NodeClientFactory instance;
  private static final String UNNAMED_MODULE = "<unknown module>";
  private static final String UNKNOWN_USER = "<unknown user>";
  public static final String NODE_TYPE = "node";
  public static final String NODE_HOST = "nodeHost";
  public static final String NODE_PORT = "nodePort";

  private NodeClientFactory() {
  }

  public ChatServerConnection buildServer(Properties param) {
    final String host = param.getProperty(NODE_HOST,"63.144.41.13");
    final int port = Integer.parseInt(param.getProperty(NODE_PORT, "5050"));
    NodeServerInfo nodeServerInfo = new NodeServerInfo() {
      public String getHostName() {
        return host;
      }

      public int getPort() {
        return port;
      }
    };
    PeerPoolInfo info = new PeerPoolInfo() {
      public String getModuleName() {
        return GameModule.getGameModule() == null ? UNNAMED_MODULE : GameModule.getGameModule().getGameName();
      }

      public String getUserName() {
        return GameModule.getUserId();
      }
    };
    PeerPoolInfo publicInfo = new PeerPoolInfo() {
      public String getModuleName() {
        return GameModule.getGameModule() == null ? UNNAMED_MODULE : GameModule.getGameModule().getGameName();
      }

      public String getUserName() {
        return GameModule.getGameModule() == null ? UNKNOWN_USER : (String) GameModule.getGameModule().getPrefs().getValue(GameModule.REAL_NAME);
      }
    };
    HttpMessageServer httpMessageServer = new HttpMessageServer(publicInfo);
    SocketNodeClient server = new SocketNodeClient(GameModule.getGameModule().getGameName(), GameModule.getUserId()+"."+System.currentTimeMillis(), GameModule.getGameModule(), nodeServerInfo, httpMessageServer, httpMessageServer);
    GameModule.getGameModule().getPrefs().getOption(GameModule.REAL_NAME).fireUpdate();
    GameModule.getGameModule().getPrefs().getOption(GameModule.PERSONAL_INFO).fireUpdate();
    server.addPropertyChangeListener(ChatServerConnection.STATUS, new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        if (GameModule.getGameModule().getChatter() != null) {
          GameModule.getGameModule().getChatter().show((String) evt.getNewValue());
        }
      }
    });
    server.addPropertyChangeListener(ChatServerConnection.INCOMING_MSG, new PropertyChangeListener() {
      public void propertyChange(final PropertyChangeEvent evt) {
        final Command c = GameModule.getGameModule().decode((String) evt.getNewValue());
        if (c != null) {
          Runnable runnable = new Runnable() {
            public void run() {
              c.execute();
              GameModule.getGameModule().getLogger().log(c);
            }
          };
          SwingUtilities.invokeLater(runnable);
        }
      }
    });
    return server;
  }

  public static NodeClientFactory getInstance() {
    if (instance == null) {
      instance = new NodeClientFactory();
      StringConfigurer fullName = new StringConfigurer(GameModule.REAL_NAME, "Name: ", "newbie");
      TextConfigurer profile = new TextConfigurer(GameModule.PERSONAL_INFO, "Personal Info:", "");
      StringConfigurer user = new StringConfigurer("UserName", "Password", System.getProperty("user.name") + "'s password");
      user.addPropertyChangeListener(new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
          GameModule.setUserId((String) evt.getNewValue());
        }
      });
      GameModule.setUserId((String) user.getValue());
      GameModule.getGameModule().getPrefs().addOption("Personal", fullName, "Enter the name by which you would like to be known to other players");
      GameModule.getGameModule().getPrefs().addOption("Personal", user, "Enter a password for identification purposes.");
      GameModule.getGameModule().getPrefs().addOption("Personal", profile);
    }
    return instance;
  }
}

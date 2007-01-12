package VASSAL.launch;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.swing.JMenuItem;
import org.w3c.dom.Document;
import VASSAL.build.Buildable;
import VASSAL.build.Builder;
import VASSAL.build.GameModule;
import VASSAL.build.module.BasicCommandEncoder;
import VASSAL.build.module.BasicLogger;
import VASSAL.build.module.Chatter;
import VASSAL.build.module.Documentation;
import VASSAL.build.module.GameState;
import VASSAL.build.module.GlobalOptions;
import VASSAL.build.module.Map;
import VASSAL.build.module.PieceWindow;
import VASSAL.build.module.PlayerRoster;
import VASSAL.build.module.PrototypesContainer;
import VASSAL.build.module.ServerConnection;
import VASSAL.build.module.gamepieceimage.GamePieceImageDefinitions;
import VASSAL.build.module.properties.GlobalProperties;
import VASSAL.command.Command;
import VASSAL.preferences.PositionOption;
import VASSAL.preferences.Prefs;
import VASSAL.tools.DataArchive;
import VASSAL.tools.SequenceEncoder;

public class BasicModule extends GameModule {
  private static char COMMAND_SEPARATOR = (char) java.awt.event.KeyEvent.VK_ESCAPE;

  protected BasicModule(DataArchive archive, Prefs globalPrefs) {
    super(archive);
  }

  protected void build() throws IOException {
    String fileName = "buildFile";
    InputStream inStream = null;
    try {
      inStream = getDataArchive().getFileStream(fileName);
    }
    catch (IOException ex) {
      if (new File(getDataArchive().getName()).exists()) {
        throw new IOException("Not a VASSAL module");
      }
    }
    try {
      if (inStream == null) {
        build(null);
      }
      else {
        Document doc = Builder.createDocument(inStream);
        build(doc.getDocumentElement());
      }
    }
    catch (IOException ex) {
      throw new IllegalArgumentException(ex.getMessage());
    }
    getFileMenu().add(getPrefs().getEditor().getEditAction());
    JMenuItem q = new JMenuItem("Quit");
    q.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        quit();
      }
    });
    q.setMnemonic('Q');
    getFileMenu().add(q);
  }

  // TODO server initialization
  protected void initServer() {
  }

  protected void initLogger() {
    logger = new BasicLogger();
    ((BasicLogger) logger).build(null);
    ((BasicLogger) logger).addTo(this);
  }

  protected void initGameState() {
    theState = new GameState();
    theState.addTo(this);
    addCommandEncoder((GameState) theState);
  }

  public Command decode(String command) {
    if (command == null) {
      return null;
    }
    Command c = null;
    SequenceEncoder.Decoder st = new SequenceEncoder.Decoder(command, COMMAND_SEPARATOR);
    String first = st.nextToken();
    if (command.equals(first)) {
      c = decodeSubCommand(first);
    }
    else {
      Command next = null;
      c = decode(first);
      while (st.hasMoreTokens()) {
        next = decode(st.nextToken());
        c = c == null ? next : c.append(next);
      }
    }
    return c;
  }

  private Command decodeSubCommand(String subCommand) {
    Command c = null;
    for (int i = 0; i < commandEncoders.length && c == null; ++i) {
      c = commandEncoders[i].decode(subCommand);
    }
    return c;
  }

  public String encode(Command c) {
    if (c == null) {
      return null;
    }
    String s = encodeSubCommand(c);
    String s2;
    Command sub[] = c.getSubCommands();
    if (sub.length > 0) {
      SequenceEncoder se = new SequenceEncoder(s, COMMAND_SEPARATOR);
      for (int i = 0; i < sub.length; ++i) {
        s2 = encode(sub[i]);
        if (s2 != null) {
          se.append(s2);
        }
      }
      s = se.getValue();
    }
    return s;
  }

  private String encodeSubCommand(Command c) {
    String s = null;
    for (int i = 0; i < commandEncoders.length && s == null; ++i) {
      s = commandEncoders[i].encode(c);
    }
    return s;
  }

  public ServerConnection getServer() {
    // TODO Auto-generated method stub
    return null;
  }

  protected void buildDefaultComponents() {
    addComponent(BasicCommandEncoder.class);
    addComponent(Documentation.class);
    addComponent(PlayerRoster.class);
    addComponent(GlobalOptions.class);
    addComponent(Map.class);
    addComponent(GamePieceImageDefinitions.class);
    addComponent(GlobalProperties.class);
    addComponent(PrototypesContainer.class);
    addComponent(PieceWindow.class);
    addComponent(Chatter.class);
  }

  protected void initFrame() {
    Rectangle screen = VASSAL.Info.getScreenBounds(frame);
    String key = "BoundsOfGameModule";
    if (GlobalOptions.getInstance().isUseSingleWindow()) {
      frame.setLocation(screen.getLocation());
      frame.setSize(screen.width, screen.height / 3);
    }
    else {
      Rectangle r = new Rectangle(0, 0, screen.width, screen.height / 4);
      getPrefs().addOption(new PositionOption(key, frame, r));
    }
    frame.setVisible(true);
    warn(gameName + " version " + moduleVersion);
    System.err.println("-- " + gameName + " version " + moduleVersion);
    frame.setTitle(gameName);
  }

  protected void ensureComponent(Class componentClass) {
    if (!getComponents(componentClass).hasMoreElements()) {
      addComponent(componentClass);
    }
  }

  protected void addComponent(Class componentClass) {
    try {
      Buildable child = (Buildable) componentClass.newInstance();
      child.build(null);
      child.addTo(this);
      add(child);
    }
    catch (InstantiationException e) {
      e.printStackTrace();
    }
    catch (IllegalAccessException e) {
      e.printStackTrace();
    }
  }
}

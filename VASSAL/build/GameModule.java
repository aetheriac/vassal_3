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
package VASSAL.build;

import java.awt.BorderLayout;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import VASSAL.Info;
import VASSAL.build.module.BasicCommandEncoder;
import VASSAL.build.module.ChartWindow;
import VASSAL.build.module.Chatter;
import VASSAL.build.module.DiceButton;
import VASSAL.build.module.Documentation;
import VASSAL.build.module.GameState;
import VASSAL.build.module.GlobalKeyCommand;
import VASSAL.build.module.GlobalOptions;
import VASSAL.build.module.Inventory;
import VASSAL.build.module.Map;
import VASSAL.build.module.ModuleExtension;
import VASSAL.build.module.MultiActionButton;
import VASSAL.build.module.NotesWindow;
import VASSAL.build.module.PieceWindow;
import VASSAL.build.module.PlayerHand;
import VASSAL.build.module.PlayerRoster;
import VASSAL.build.module.PredefinedSetup;
import VASSAL.build.module.PrivateMap;
import VASSAL.build.module.PrototypesContainer;
import VASSAL.build.module.RandomTextButton;
import VASSAL.build.module.ServerConnection;
import VASSAL.build.module.SpecialDiceButton;
import VASSAL.build.module.ToolbarMenu;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.build.module.properties.GlobalPropertiesContainer;
import VASSAL.build.module.properties.PropertySource;
import VASSAL.command.Command;
import VASSAL.command.CommandEncoder;
import VASSAL.command.Logger;
import VASSAL.configure.CompoundValidityChecker;
import VASSAL.configure.DirectoryConfigurer;
import VASSAL.configure.MandatoryComponent;
import VASSAL.counters.GamePiece;
import VASSAL.preferences.Prefs;
import VASSAL.tools.ArchiveWriter;
import VASSAL.tools.DataArchive;
import VASSAL.tools.KeyStrokeListener;
import VASSAL.tools.KeyStrokeSource;
import VASSAL.tools.ToolBarComponent;

/**
 * The GameModule class is the base class for a VASSAL module.  It is
 * the root of the {@link Buildable} containment hierarchy.
 * Components which are added directly to the GameModule are contained
 * in the <code>VASSAL.build.module</code> package
 *
 * It is a singleton, and contains access points for many other classes,
 * such as {@link DataArchive}, {@link ServerConnection}, {@link Logger},
 * and {@link Prefs} */
public abstract class GameModule extends AbstractConfigurable implements CommandEncoder, ToolBarComponent, PropertySource, GlobalPropertiesContainer {
  protected static final String DEFAULT_NAME = "Unnamed module";
  public static final String MODULE_NAME = "name";
  public static final String MODULE_VERSION = "version";
  public static final String VASSAL_VERSION_CREATED = "VassalVersion";
  /** The System property of this name will return a version identifier for the version of VASSAL being run */
  public static final String VASSAL_VERSION_RUNNING = "runningVassalVersion";

  private static GameModule theModule;

  protected String moduleVersion = "0.0";
  protected String vassalVersionCreated = "0.0";
  protected String gameName = DEFAULT_NAME;
  protected String lastSavedConfiguration;
  protected JFileChooser fileChooser;
  protected FileDialog fileDialog;
  protected java.util.Map globalProperties = new HashMap();

  protected JPanel controlPanel = new JPanel();

  private JToolBar toolBar = new JToolBar();
  private JMenu fileMenu = new JMenu("File");

  protected GameState theState;
  protected DataArchive archive;
  protected Prefs preferences;
  protected Prefs globalPrefs;
  protected Logger logger;
  protected Chatter chat;
  protected Random RNG;

  protected JFrame frame = new JFrame();

  protected Vector keyStrokeSources = new Vector();
  protected Vector keyStrokeListeners = new Vector();
  protected CommandEncoder[] commandEncoders = new CommandEncoder[0];

  /**
   * @return the top-level frame of the controls window
   */
  public JFrame getFrame() {
    return frame;
  }

  protected GameModule(DataArchive archive) {
    frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

    this.archive = archive;
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        quit();
      }
    });

    frame.getContentPane().setLayout(new BorderLayout());
    frame.setJMenuBar(new JMenuBar());

    fileMenu.setMnemonic('F');
    frame.getJMenuBar().add(fileMenu);

    toolBar.setLayout(new VASSAL.tools.WrapLayout(FlowLayout.LEFT, 0, 0));
    toolBar.setAlignmentX(0.0F);
    toolBar.setFloatable(false);
    frame.getContentPane().add(toolBar, BorderLayout.NORTH);
    controlPanel.setLayout(new BorderLayout());
    addKeyStrokeSource
        (new KeyStrokeSource
            (frame.getRootPane(),
             JComponent.WHEN_IN_FOCUSED_WINDOW));
    frame.getContentPane().add(controlPanel, BorderLayout.CENTER);

    validator = new CompoundValidityChecker
        (new MandatoryComponent(this, Documentation.class),
         new MandatoryComponent(this, GlobalOptions.class));
  }

  /**
   * Initialize the module
   */
  protected abstract void build() throws IOException;

  public void setAttribute(String name, Object value) {
    if (MODULE_NAME.equals(name)) {
      gameName = (String) value;
      setConfigureName(gameName);
    }
    else if (MODULE_VERSION.equals(name)) {
      moduleVersion = (String) value;
    }
    else if (VASSAL_VERSION_CREATED.equals(name)) {
      vassalVersionCreated = (String) value;
      String runningVersion = Info.getVersion();
      if (Info.compareVersions(vassalVersionCreated, runningVersion) > 0) {
        javax.swing.JOptionPane.showMessageDialog
            (null,
             "This module was created using version " + value
             + " of the VASSAL engine\nYou are using version "
             + runningVersion + "\nIt's recommended you upgrade to the latest version of the VASSAL engine.",
             "Older version in use",
             javax.swing.JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  public String getAttributeValueString(String name) {
    if (MODULE_NAME.equals(name)) {
      return gameName;
    }
    else if (MODULE_VERSION.equals(name)) {
      return moduleVersion;
    }
    else if (VASSAL_VERSION_CREATED.equals(name)) {
      return vassalVersionCreated;
    }
    else if (VASSAL_VERSION_RUNNING.equals(name)) {
      return Info.getVersion();
    }
    return null;
  }

  /**
   *
   * A valid verson format is "w.x.y[bz]", where
   * 'w','x','y', and 'z' are integers.
   * @deprecated use {@link Info#compareVersions}
   * @return a negative number if <code>v2</code> is a later version
   * the <code>v1</code>, a positive number if an earlier version,
   * or zero if the versions are the same.
   *
   */
  public static int compareVersions(String v1, String v2) {
    return Info.compareVersions(v1, v2);
  }

  public void addTo(Buildable b) {
  }

  public static String getConfigureTypeName() {
    return "Module";
  }

  public void removeFrom(Buildable parent) {
  }

  public HelpFile getHelpFile() {
    return HelpFile.getReferenceManualPage("GameModule.htm");
  }

  public String[] getAttributeNames() {
    return new String[]{MODULE_NAME, MODULE_VERSION, VASSAL_VERSION_CREATED};
  }

  public String[] getAttributeDescriptions() {
    return new String[]{"Game Name", "Version No."};
  }

  public Class[] getAttributeTypes() {
    return new Class[]{String.class, String.class};
  }

  public Class[] getAllowableConfigureComponents() {
    Class[] c = {Map.class, PieceWindow.class, PrototypesContainer.class, ToolbarMenu.class, MultiActionButton.class, DiceButton.class, GlobalKeyCommand.class, Inventory.class, /*InternetDiceButton.class,*/
                 RandomTextButton.class, SpecialDiceButton.class, PredefinedSetup.class, ChartWindow.class, PrivateMap.class, PlayerHand.class, NotesWindow.class};
    return c;
  }

  /**
   * The GameModule acts as the mediator for hotkey events.
   *
   * Components that wish to fire hotkey events when they have the
   * focus should register themselves using this method.  These events will be
   * forwarded to all listeners that have registered themselves with {@link #addKeyStrokeListener}
   */
  public void addKeyStrokeSource(KeyStrokeSource src) {
    keyStrokeSources.addElement(src);
    for (int i = 0; i < keyStrokeListeners.size(); ++i) {
      ((KeyStrokeListener) keyStrokeListeners.elementAt(i))
          .addKeyStrokeSource(src);
    }
  }

  /**
   * The GameModule acts as the mediator for hotkey events.
   *
   * Objects that react to hotkey events should register themselves
   * using this method.  Any component that has been registered with {@link #addKeyStrokeSource}
   * will forward hotkey events to listeners registered with this method.
   */
  public void addKeyStrokeListener(KeyStrokeListener l) {
    keyStrokeListeners.addElement(l);
    for (int i = 0; i < keyStrokeSources.size(); ++i) {
      l.addKeyStrokeSource
          ((KeyStrokeSource) keyStrokeSources.elementAt(i));
    }
  }
  
  public void fireKeyStroke(KeyStroke stroke) {
  	if (stroke != null) {
  		for (Iterator it = keyStrokeListeners.iterator(); it.hasNext();) {
				KeyStrokeListener l = (KeyStrokeListener) it.next();
				l.keyPressed(stroke);
			}
  	}
  }

  /**
   * @return the name of the game for this module
   */
  public String getGameName() {
    return gameName;
  }

  public String getGameVersion() {
    return moduleVersion;
  }

  /** The {@link Prefs} key for the user's real name */
  public static final String REAL_NAME = "RealName";
  /** The {@link Prefs} key for the user's secret name */
  public static final String SECRET_NAME = "SecretName";
  /** The {@link Prefs} key for the user's personal info */
  public static final String PERSONAL_INFO = "Profile";

  /**
   * @return the preferences for this module
   */
  public Prefs getPrefs() {
    return preferences;
  }

  /**
   * A set of preferences that applies to all modules
   * @return
   */
  public Prefs getGlobalPrefs() {
    return globalPrefs;
  }

  /**
   * This method adds a {@link CommandEncoder} to the list of objects
   * that will attempt to decode/encode a command
   *
   * @see #decode
   * @see #encode
   */
  public void addCommandEncoder(CommandEncoder ce) {
    CommandEncoder[] oldValue = commandEncoders;
    commandEncoders = new CommandEncoder[oldValue.length + 1];
    System.arraycopy(oldValue, 0, commandEncoders, 0, oldValue.length);
    commandEncoders[oldValue.length] = ce;
  }

  /**
   * This method removes a {@link CommandEncoder} from the list of objects
   * that will attempt to decode/encode a command
   *
   *
   * @see #decode
   * @see #encode
   */
  public void removeCommandEncoder(CommandEncoder ce) {
    for (int i = 0; i < commandEncoders.length; ++i) {
      if (ce.equals(commandEncoders[i])) {
        CommandEncoder[] oldValue = commandEncoders;
        commandEncoders = new CommandEncoder[oldValue.length - 1];
        System.arraycopy(oldValue, 0, commandEncoders, 0, i);
        if (i < commandEncoders.length - 1) {
          System.arraycopy(oldValue, i + 1, commandEncoders, i, oldValue.length - i - 1);
        }
        break;
      }
    }
  }

  /**
   * Central location to create any type of GamePiece from within VASSAL
   * 
   * @param type
   * @return
   */
  public GamePiece createPiece(String type) {
    for (int i = 0; i < commandEncoders.length; ++i) {
      if (commandEncoders[i] instanceof BasicCommandEncoder) {
        GamePiece p = ((BasicCommandEncoder) commandEncoders[i]).createPiece(type);
        if (p != null) {
          return p;
        }
      }
    }
    return null;
  }
  
  public GamePiece createPiece(String type, GamePiece inner) {
    for (int i = 0; i < commandEncoders.length; ++i) {
      if (commandEncoders[i] instanceof BasicCommandEncoder) {
        GamePiece p = ((BasicCommandEncoder) commandEncoders[i]).createDecorator(type, inner);
        if (p != null) {
          return p;
        }
      }
    }
    return null;
  }
  
  /**
   * Display the given text in the control window's status line
   */
  public void warn(String s) {
    chat.show(" - " + s);
  }

  /**
   * @return a single Random number generator that all objects may share
   */
  public Random getRNG() {
    if (RNG == null) {
      RNG = new Random();
    }
    return RNG;
  }

  /**
   * @return the object responsible for logging commands to a logfile
   */
  public Logger getLogger() {
    return logger;
  }

  /**
   * set the object that displays chat text
   */
  public void setChatter(Chatter c) {
    chat = c;
  }

  public JComponent getControlPanel() {
    return controlPanel;
  }

  /**
   * @return the object that displays chat text
   */
  public Chatter getChatter() {
    return chat;
  }

  public void setPrefs(Prefs p) {
    preferences = p;
    preferences.getEditor().initDialog(getFrame());
  }

  public void setGlobalPrefs(Prefs p) {
    globalPrefs = p;
  }

  /**
   * Uses the registered  {@link CommandEncoder}s
   * to decode a String into a {@link Command}.
   */
  public Command decode(String command) {
    if (command == null) {
      return null;
    }
    else {
      Command c = null;
      for (int i = 0; i < commandEncoders.length && c == null; ++i) {
        c = commandEncoders[i].decode(command);
      }
      if (c == null) {
        System.err.println("Failed to decode " + command);
      }
      return c;
    }
  }

  /**
   * Uses the registered {@link CommandEncoder}s to encode a {@link Command} into a String object
   */
  public String encode(Command c) {
    if (c == null) {
      return null;
    }
    String s = null;
    for (int i = 0; i < commandEncoders.length && s == null; ++i) {
      s = commandEncoders[i].encode(c);
    }
    if (s == null) {
      System.err.println("Failed to encode " + c);
    }
    return s;
  }

  private static final String SAVE_DIR = "SaveDir";

  /**
   * @return a common FileChooser so that recent file locations
   * can be remembered
   */
  public JFileChooser getFileChooser() {
    if (fileChooser == null) {
      getPrefs().addOption(null, new DirectoryConfigurer(SAVE_DIR, null));
      fileChooser = new JFileChooser();
      File f = (File) getPrefs().getValue(SAVE_DIR);
      if (f != null) {
        fileChooser.setCurrentDirectory(f);
      }
    }
    else {
      fileChooser.resetChoosableFileFilters();
      fileChooser.rescanCurrentDirectory();
    }
    return fileChooser;
  }


  /**
   * @deprecated Use {@link #getFileChooser} instead.
   */
  public FileDialog getFileDialog() {
    if (fileDialog == null) {
      getPrefs().addOption(null, new DirectoryConfigurer(SAVE_DIR, null));
      fileDialog = new FileDialog(getFrame());
      File f = (File) getPrefs().getValue(SAVE_DIR);
      if (f != null) {
        fileDialog.setDirectory(f.getPath());
      }
      fileDialog.setModal(true);
    }
    else {
      fileDialog.setDirectory(fileDialog.getDirectory());
    }
    return fileDialog;
  }

  /**
   * @return the JToolBar of the command window
   */
  public JToolBar getToolBar() {
    return toolBar;
  }

  /**
   * @return the File menu of the command window
   */
  public JMenu getFileMenu() {
    return fileMenu;
  }

  /**
   * Append the string to the title of the controls window and all Map windows
   * @param s If null, set the title to the default.
   */
  public void appendToTitle(String s) {
    if (s == null) {
      frame.setTitle(gameName + " controls");
    }
    else {
      frame.setTitle(frame.getTitle() + s);
    }
    for (Enumeration e = getComponents(Map.class); e.hasMoreElements();) {
      ((Map) e.nextElement()).appendToTitle(s);
    }
  }

  /**
   * Exit the application, prompting user to save if necessary
   */
  public void quit() {
    boolean cancelled = false;
    try {
      getGameState().setup(false);
      cancelled = getGameState().isGameStarted();
      if (!cancelled) {
        if (fileDialog != null
            && fileDialog.getDirectory() != null) {
          getPrefs().getOption(SAVE_DIR).setValue(fileDialog.getDirectory());
        }
        else if (fileChooser != null) {
          getPrefs().getOption(SAVE_DIR).setValue(fileChooser.getCurrentDirectory());
        }
        getPrefs().write();
        if (getDataArchive() instanceof ArchiveWriter
            && !buildString().equals(lastSavedConfiguration)) {
          switch (JOptionPane.showConfirmDialog
              (frame, "Save Module?",
               "", JOptionPane.YES_NO_CANCEL_OPTION)) {
            case JOptionPane.YES_OPTION:
              save();
              break;
            case JOptionPane.CANCEL_OPTION:
              cancelled = true;
          }
        }
        else if (getArchiveWriter() != null) {
          for (Enumeration e = getComponents(ModuleExtension.class); e.hasMoreElements();) {
            ModuleExtension ext = (ModuleExtension) e.nextElement();
            cancelled = !ext.confirmExit();
          }
        }
      }
    }
    catch (IOException ex) {
      ex.printStackTrace();
    }
    finally {
      if (!cancelled) {
        System.exit(0);
      }
    }
  }

  /**
   * Encode the {@link Command}, send it to the server and write it
   * to a logfile (if any is open)
   *
   * @see #encode
   */
  public void sendAndLog(Command c) {
    if (c != null && !c.isNull()) {
      getServer().sendToOthers(c);
      getLogger().log(c);
    }
  }

  private static String userId = null;
	private PropertyChangeListener globalPropertyListener;

  /**
   * @return a String that uniquely identifies the user
   */
  public static String getUserId() {
    return userId;
  }

  /**
   * Set the identifier for the user
   */
  public static void setUserId(String newId) {
    userId = newId;
  }

  /**
   * @return the object reponsible for sending messages to the server
   */
  public abstract ServerConnection getServer();

  /**
   * Set the singleton GameModule and invoke {@link #build} on it
   */
  public static void init(GameModule module) throws IOException {
    if (theModule != null) {
      throw new IOException("Module " + theModule.getDataArchive().getName()
                            + " is already open");
    }
    else {
      theModule = module;
      try {
        theModule.build();
      }
      catch (IOException e) {
        theModule = null;
        throw e;
      }
    }

    if (theModule.getDataArchive() instanceof ArchiveWriter) {
      theModule.lastSavedConfiguration = theModule.buildString();
    }
  }

  /**
   * @return the object which stores data for the module
   */
  public DataArchive getDataArchive() {
    return archive;
  }

  /**
   * If the module is being edited, return the writeable archive for the module
   */
  public ArchiveWriter getArchiveWriter() {
    return archive.getWriter();
  }

  /**
   * @return the singleton instance of GameModule
   */
  public static GameModule getGameModule() {
    return theModule;
  }

  /**
   * Return the object responsible for tracking the state of a game.
   * Only one game in progress is allowed;
   */
  public GameState getGameState() {
    return theState;
  }

  public void saveAs() {
    save(true);
  }

  /**
   * If the module is being edited, write the module data
   */
  public void save() {
    save(false);
  }

  protected void save(boolean saveAs) {
    vassalVersionCreated = Info.getVersion();
    try {
      String save = buildString();
      getArchiveWriter().addFile
          ("buildFile",
           new java.io.ByteArrayInputStream(save.getBytes("UTF-8")));
      if (saveAs) {
        getArchiveWriter().saveAs();
      }
      else {
        getArchiveWriter().write();
      }
      lastSavedConfiguration = save;
    }
    catch (IOException err) {
      err.printStackTrace();
      JOptionPane.showMessageDialog
          (frame,
           "Couldn't save module.\n" + err.getMessage(),
           "Unable to save",
           JOptionPane.ERROR_MESSAGE);
    }
  }

  protected String buildString() {
    org.w3c.dom.Document doc = Builder.createNewDocument();
    doc.appendChild(getBuildElement(doc));
    return Builder.toString(doc);
  }

  /**
   * Return values of Global properties
   */
  public Object getProperty(Object key) {
    if (GlobalOptions.PLAYER_SIDE.equals(key)) {
      String mySide = PlayerRoster.getMySide();
      return mySide == null ? "" : mySide;
    }
    else if (GlobalOptions.PLAYER_NAME.equals(key)) {
      return getPrefs().getValue(GameModule.REAL_NAME);
    }
    else if (GlobalOptions.PLAYER_ID.equals(key)) {
      return GlobalOptions.getInstance().getPlayerId();
    }
    return globalProperties.get(key);
  }
  
  public PropertyChangeListener getPropertyListener() {
  	if (globalPropertyListener == null) {
  		globalPropertyListener = new PropertyChangeListener() {
  			public void propertyChange(PropertyChangeEvent evt) {
  				globalProperties.put(evt.getPropertyName(),evt.getNewValue());
          for (Iterator maps = Map.getAllMaps(); maps.hasNext(); ) {
            Map map = (Map) maps.next();
            map.repaint();            
          }
  			}
  		};
  	}
		return globalPropertyListener;
  }
  
}

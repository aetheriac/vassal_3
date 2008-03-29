
package VASSAL.launch;

import java.awt.Cursor;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

// FIXME: switch back to javax.swing.SwingWorker on move to Java 1.6
//import javax.swing.SwingWorker;
import org.jdesktop.swingworker.SwingWorker;

import VASSAL.Info;
import VASSAL.build.GameModule;
import VASSAL.build.module.ExtensionsLoader;
import VASSAL.build.module.GlobalOptions;
import VASSAL.build.module.ModuleExtension;
import VASSAL.i18n.Localization;
import VASSAL.i18n.Resources;
import VASSAL.launch.os.macos.MacOS;
import VASSAL.preferences.Prefs;
import VASSAL.tools.DataArchive;
import VASSAL.tools.ErrorLog;
import VASSAL.tools.JarArchive;
import VASSAL.tools.MacOSXMenuManager;
import VASSAL.tools.MenuManager;
import VASSAL.tools.OrderedMenu;

public class Player {
  protected boolean isFirstTime;
  protected boolean builtInModule;
  protected File moduleFile;
  protected File savedGame;
  protected List<String> extractTargets = new ArrayList<String>();
  protected List<String> autoExtensions = new ArrayList<String>();

  public Player(final String[] args) {
    StartUp.initSystemProperties();
    StartUp.setupErrorLog();

    Thread.setDefaultUncaughtExceptionHandler(new ErrorLog());
    
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        try {
          Player.this.configure(args);
          Player.this.extractResourcesAndLaunch(0);
        }
        catch (IOException e) {
          reportError(e);
        }
      }
    });
  }

  protected void extractResourcesAndLaunch(final int resourceIndex) throws IOException {
    if (resourceIndex >= extractTargets.size()) {
      launch();
    }
    else {
      final Properties props = new Properties();
      final InputStream in =
        Player.class.getResourceAsStream(extractTargets.get(resourceIndex));
      if (in != null) {
        try {
          props.load(in);
        }
        finally {
          try {
            in.close();
          }
          catch (IOException e) {
            e.printStackTrace();
          }
        }
      }

      new ResourceExtracter(Prefs.getGlobalPrefs(), props, new Observer() {
        public void update(Observable o, Object arg) {
          try {
            extractResourcesAndLaunch(resourceIndex + 1);
          }
          catch (IOException e) {
            reportError(e);
          }
        }
      }).install();
    }
  }

  protected void reportError(Exception e) {
    e.printStackTrace();
    String msg = e.getMessage();
    if (msg == null) {
      msg = e.getClass().getSimpleName();
    }
    JOptionPane.showMessageDialog(null, msg, Resources.getString("ResourceExtracter.install_failed"), JOptionPane.ERROR_MESSAGE);
  }

  protected void launch() throws IOException {
    if (Info.isMacOSX()) new MacOSXPlayerMenuManager();
    else new PlayerMenuManager();

    try {
      if (isFirstTime) {
        new FirstTimeDialog().setVisible(true);
      }
      else if (builtInModule) {
        GameModule.init(createModule(createDataArchive()));
        for (String ext : autoExtensions) {
          createExtension(ext).build();
        }
        createExtensionsLoader().addTo(GameModule.getGameModule());
        Localization.getInstance().translate();
        GameModule.getGameModule().getWizardSupport().showWelcomeWizard();
      }
      else if (moduleFile == null) {
        return;
      }
      else {
        GameModule.init(createModule(createDataArchive()));
        createExtensionsLoader().addTo(GameModule.getGameModule());
        Localization.getInstance().translate();
        final GameModule m = GameModule.getGameModule();
        if (savedGame != null) {
          m.getFrame().setVisible(true);
          m.getGameState().loadGameInBackground(savedGame);
        }
        else {
          m.getWizardSupport().showWelcomeWizard();
        }
      }
    }  
    finally {
      System.out.print("\n");
    }
  }

  protected ExtensionsLoader createExtensionsLoader() {
    return new ExtensionsLoader();
  }

  protected ModuleExtension createExtension(String name) {
    return new ModuleExtension(new JarArchive(name));
  }

  protected DataArchive createDataArchive() throws IOException {
    if (builtInModule) {
      return new JarArchive();
    }
    else {
      return new DataArchive(moduleFile.getPath());
    }
  }

  protected GameModule createModule(DataArchive archive) {
    return new BasicModule(archive);
  }

  protected void configure(final String[] args) {
    File prefsFile = new File(Info.getHomeDir(), "Preferences");
    isFirstTime = !prefsFile.exists();
    int n = -1;
    while (++n < args.length) {
      final String arg = args[n];
      if ("-auto".equals(arg)) {
        builtInModule = true;
      }
      else if ("-extract".equals(arg)) {
        extractTargets.add(args[++n]);
      }
      else if ("-autoextensions".equals(arg)) {
        for (String ext : args[++n].split(",")) {
          autoExtensions.add(ext.replace("_"," "));
        }
      }
      else if ("-load".equals(arg)) {
        savedGame = new File(args[++n]);
      }
      else if (!arg.startsWith("-")) {
        moduleFile = new File(arg);
      }
    }
  }

  public static class LaunchAction extends AbstractLaunchAction {
    private static final long serialVersionUID = 1L;

    public LaunchAction(ModuleManagerWindow mm, File module) {
      super(Resources.getString("Main.play_module"), mm,
            Player.class.getName(), new String[0], module);
      setEnabled(!editing.contains(module));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      // register that this module is being used
      if (editing.contains(module)) return;
      Integer count = using.get(module);
      using.put(module, count == null ? 1 : ++count);

      super.actionPerformed(e);
    }

    @Override
    protected LaunchTask getLaunchTask() {
      return new LaunchTask() {
        @Override
        protected void done() {
          super.done();

          // reduce the using count
          Integer count = using.get(mod);
          if (count == 1) using.remove(mod);
          else using.put(mod, --count);
        }

        @Override
        protected void process(List<Void> chunks) {
          super.process(chunks);
          ((ModuleManagerWindow) frame).addModule(mod);
        }
      };
    }
  }

  public static class PromptLaunchAction extends LaunchAction {
    private static final long serialVersionUID = 1L;

    public PromptLaunchAction(ModuleManagerWindow mm) {
      super(mm, null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      // prompt the user to pick a module
      if (promptForModule() == null) return;

      super.actionPerformed(e);
      module = null;
    }
  }

  private static class PlayerMenuManager extends MenuManager {
    private final JMenuBar menuBar = new JMenuBar();
    private final JMenu fileMenu;
    private final JMenu helpMenu;

    public PlayerMenuManager() {
      fileMenu = OrderedMenu.builder("General.file")
        .appendItem("GameState.new_game")
        .appendItem("GameState.load_game")
        .appendItem("GameState.save_game")
        .appendItem("GameState.close_game")
        .appendSeparator()
        .appendItem("BasicLogger.begin_logfile")
        .appendItem("BasicLogger.end_logfile")
        .appendSeparator()
        .appendItem("Prefs.edit_preferences")
        .appendSeparator()
        .appendItem("General.quit")
        .create();

      helpMenu = OrderedMenu.builder("General.help")
        .appendItem("General.help")
        .appendItem("about_module")
        .appendItem("AboutScreen.about_vassal")
        .create();

      menuBar.add(fileMenu);
      menuBar.add(helpMenu);

      parent.put("GameState.new_game", fileMenu);
      parent.put("GameState.load_game", fileMenu);
      parent.put("GameState.save_game", fileMenu);
      parent.put("GameState.close_game", fileMenu);
      parent.put("BasicLogger.begin_logfile", fileMenu);
      parent.put("BasicLogger.end_logfile", fileMenu);
      parent.put("Prefs.edit_preferences", fileMenu);
      parent.put("General.quit", fileMenu);
    
      parent.put("General.help", helpMenu);
      parent.put("about_module", helpMenu);
      parent.put("AboutScreen.about_vassal", helpMenu);
    }

    @Override
    public JMenuBar getMenuBar(int type) {
      return type == PLAYER ? menuBar : null;
    }
  }

  private static class MacOSXPlayerMenuManager extends MacOSXMenuManager {
    private final JMenu fileMenu;
    private final JMenu helpMenu;

    public MacOSXPlayerMenuManager() {
      fileMenu = OrderedMenu.builder("General.file")
        .appendItem("GameState.new_game")
        .appendItem("GameState.load_game")
        .appendItem("GameState.save_game")
        .appendItem("GameState.close_game")
        .appendSeparator()
        .appendItem("BasicLogger.begin_logfile")
        .appendItem("BasicLogger.end_logfile")
        .create();

      helpMenu = OrderedMenu.builder("General.help")
        .appendItem("General.help")
        .appendItem("about_module")
        .create();

      menuBar.add(fileMenu);
      menuBar.add(helpMenu);

      parent.put("GameState.new_game", fileMenu);
      parent.put("GameState.load_game", fileMenu);
      parent.put("GameState.save_game", fileMenu);
      parent.put("GameState.close_game", fileMenu);
      parent.put("BasicLogger.begin_logfile", fileMenu);
      parent.put("BasicLogger.end_logfile", fileMenu);
    
      parent.put("General.help", helpMenu);
      parent.put("about_module", helpMenu);
    }
  }

  public static void main(String[] args) {
    new Player(args);
  }
}

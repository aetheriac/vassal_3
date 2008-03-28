
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

    public LaunchAction(Frame frame, File module) {
      super(Resources.getString("Main.play_module"), frame,
            Player.class.getName(), new String[0], module);
      setEnabled(!editing.contains(module));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      if (module == null) { 
        // prompt the user to pick a module
        if (promptForModule() == null) return;
      }

      // register that this module is being played
      if (editing.contains(module)) return;
      Integer count = playing.get(module);
      playing.put(module, count == null ? 0 : ++count);

      super.actionPerformed(e);
    }

    @Override
    protected LaunchTask getLaunchTask() {
      return new LaunchTask() {
        @Override
        protected void done() {
          super.done();

          // reduce the playing count
          Integer count = playing.get(module);
          if (count == 1) {
            playing.remove(module);
// FIXME: setEnabled(true) for editing here also
          }
          else playing.put(module, --count);
        }
      };
    }
  }

  public static void main(String[] args) {
    new Player(args);
  }
}


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
import VASSAL.configure.DirectoryConfigurer;
import VASSAL.i18n.Localization;
import VASSAL.i18n.Resources;
import VASSAL.launch.os.macos.MacOS;
import VASSAL.preferences.Prefs;
import VASSAL.tools.DataArchive;
import VASSAL.tools.ErrorLog;
import VASSAL.tools.FileChooser;
import VASSAL.tools.JarArchive;
import VASSAL.tools.ProgressDialog;

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

    new Thread(new ErrorLog.Group(), "Main Thread") { //$NON-NLS-1$
      public void run() {
        Runnable runnable = new Runnable() {
          public void run() {
            try {
              Player.this.configure(args);
              Player.this.extractResourcesAndLaunch(0);
            }
            catch (IOException e) {
              reportError(e);
            }
          }
        };
        SwingUtilities.invokeLater(runnable);
      }
    }.start();
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
      if (savedGame != null) {
        GameModule.getGameModule().getFrame().setVisible(true);
        GameModule.getGameModule().getGameState().loadGameInBackground(savedGame);
      }
      else {
        GameModule.getGameModule().getWizardSupport().showWelcomeWizard();
      }
    }
    
    System.out.print("\n");
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
      String arg = args[n];
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

  public static final int DEFAULT_INITIAL_HEAP = 256;
  public static final int DEFAULT_MAXIMUM_HEAP = 512;

  public static class LaunchAction extends AbstractAction {
    private static final long serialVersionUID = 1L;

    private Frame frame;    
    private File module;

    public LaunchAction(Frame frame, File module) {
      super(Resources.getString("Main.play_module"));
      this.frame = frame;
      this.module = module;
    }

    public void actionPerformed(ActionEvent e) {
      if (module == null) {
        final FileChooser fc = FileChooser.createFileChooser(frame,
          (DirectoryConfigurer)
            Prefs.getGlobalPrefs().getOption(Prefs.MODULES_DIR_KEY));

        if (fc.showOpenDialog() == FileChooser.APPROVE_OPTION) {
          module = fc.getSelectedFile();
          if (module != null && !module.exists()) module = null;
        }

        if (module == null) return;
      }

      frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

      new SwingWorker<Void,Void>() {
        @Override
        public Void doInBackground() throws Exception {
          int initialHeap; 
          try {
            initialHeap = Integer.parseInt(Prefs.getGlobalPrefs()
              .getStoredValue(GlobalOptions.INITIAL_HEAP));
          }
          catch (NumberFormatException ex) {
            ErrorLog.warn(ex);
            initialHeap = DEFAULT_INITIAL_HEAP;
          }

          int maximumHeap;
          try {
            maximumHeap = Integer.parseInt(Prefs.getGlobalPrefs()
              .getStoredValue(GlobalOptions.MAXIMUM_HEAP));
          }
          catch (NumberFormatException ex) {
            ErrorLog.warn(ex);
            maximumHeap = DEFAULT_MAXIMUM_HEAP;
          }
  
          final ProcessBuilder pb = new ProcessBuilder(
            "java",
            "-Xms" + initialHeap + "M",
            "-Xmx" + maximumHeap + "M",
            "-cp", "lib/Vengine.jar",
            "VASSAL.launch.Player",
            module.getPath()
          );

          pb.directory(new File(System.getProperty("user.dir")));

          final Process p = pb.start();
          final InputStream in = p.getInputStream();
          in.read();
          return null;
        }

        @Override
        protected void done() {
          try {
            get();
          }
          catch (CancellationException e) {
          }
          catch (InterruptedException e) {
            ErrorLog.warn(e);
          }
          catch (ExecutionException e) {
            ErrorLog.warn(e);
          }

          frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
      }.execute();

/*
      int initialHeap; 
      try {
        initialHeap = Integer.parseInt(
          Prefs.getGlobalPrefs().getStoredValue(GlobalOptions.INITIAL_HEAP));
      }
      catch (NumberFormatException ex) {
        ErrorLog.warn(ex);
        initialHeap = DEFAULT_INITIAL_HEAP;
      }

      int maximumHeap;
      try {
        maximumHeap = Integer.parseInt(
          Prefs.getGlobalPrefs().getStoredValue(GlobalOptions.MAXIMUM_HEAP));
      }
      catch (NumberFormatException ex) {
        ErrorLog.warn(ex);
        maximumHeap = DEFAULT_MAXIMUM_HEAP;
      }

      try {
*/
/*
        final ProcessBuilder pb = new ProcessBuilder(
          "java",
          "-agentlib:yjpagent",
          "-Xms" + initialHeap + "M",
          "-Xmx" + maximumHeap + "M",
          "-cp", "classes:lib/*",
          "VASSAL.launch.Player",
          module.getPath()
        );
        
        final Map<String,String> env = pb.environment();
        env.put("LD_LIBRARY_PATH", "/home/uckelman/java/yjp/bin/linux-amd64");

        pb.start();
*/
        
/*
        final ProcessBuilder pb = new ProcessBuilder(
          "java",
          "-Xms" + initialHeap + "M",
          "-Xmx" + maximumHeap + "M",
          "-cp", "lib/Vengine.jar",
          "VASSAL.launch.Player",
          module.getPath()
        );
        
        final Process p = pb.start();
      }
      catch (IOException ex) {
        ErrorLog.warn(ex);
      }
*/
    } 
  }

  public static void main(String[] args) {
    new Player(args);
  }
}


package VASSAL.launch;

import java.awt.Cursor;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
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
import VASSAL.build.module.GlobalOptions;
import VASSAL.i18n.Resources;
import VASSAL.launch.os.macos.MacOS;
import VASSAL.preferences.Prefs;
import VASSAL.tools.ErrorLog;
import VASSAL.tools.FileChooser;
import VASSAL.tools.imports.ImportAction;


public class Editor {
  protected File moduleFile;
  protected List<String> extractTargets = new ArrayList<String>();

  private boolean newModule = false;
  private boolean importModule = false;

  public Editor(final String[] args) {
    StartUp.initSystemProperties();
    StartUp.setupErrorLog();

    Thread.setDefaultUncaughtExceptionHandler(new ErrorLog());
 
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        try {
          Editor.this.configure(args);
          Editor.this.extractResourcesAndLaunch(0);
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
        Editor.class.getResourceAsStream(extractTargets.get(resourceIndex));
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
      if (newModule) new CreateModuleAction(null).performAction(null);

      if (moduleFile == null) return;

      if (importModule) new ImportAction(null).loadModule(moduleFile); 
      else new EditModuleAction(moduleFile).loadModule(moduleFile);
    }
    finally {
      System.out.print("\n");
    }
  }

  protected void configure(final String[] args) {
    int n = -1;
    while (++n < args.length) {
      final String arg = args[n];
      if ("-extract".equals(arg)) {
        extractTargets.add(args[++n]);
      }
      else if ("-import".equals(arg)) {
        importModule = true; 
      }
      else if ("-new".equals(arg)) {
        newModule = true; 
      }
      else if (!arg.startsWith("-")) {
        moduleFile = new File(arg);
      }
    }
  }

  public static class NewModuleLaunchAction extends AbstractLaunchAction {
    private static final long serialVersionUID = 1L;

    public NewModuleLaunchAction(Frame frame) {
      super(Resources.getString("Main.new_module"), frame, 
            Editor.class.getName(), new String[]{ "-new" }, null);
    }

    @Override
    protected LaunchTask getLaunchTask() {
      return new LaunchTask();
    }
  }

  public static class ImportModuleLaunchAction extends AbstractLaunchAction {
    private static final long serialVersionUID = 1L;

    public ImportModuleLaunchAction(Frame frame) {
      super(Resources.getString("Editor.import_module"), frame, 
            Editor.class.getName(), new String[]{ "-import" }, null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      // prompt the user to pick a module
      if (promptForModule() == null) return;

      super.actionPerformed(e);
    }

    @Override
    protected File promptForModule() {
      // prompt the use to pick a module
      final FileChooser fc = ImportAction.getFileChooser(frame);

      if (fc.showOpenDialog() == FileChooser.APPROVE_OPTION) {
        module = fc.getSelectedFile();
        if (module != null && !module.exists()) module = null;
      } 
    
      return module;
    }

    @Override
    protected LaunchTask getLaunchTask() {
      return new LaunchTask();
    }
  }


  public static class LaunchAction extends AbstractLaunchAction {
    private static final long serialVersionUID = 1L;

    public LaunchAction(Frame frame, File module) {
      super(Resources.getString("Main.edit_module"), frame,
            Editor.class.getName(), new String[0], module);
      setEnabled(!editing.contains(module) && !playing.containsKey(module));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      if (module == null) { 
        // prompt the user to pick a module
        if (promptForModule() == null) return;
      }
      else { 
        // disable this action if it is for a specific module
        setEnabled(false);
      }

      // register that this module is being edited
      if (editing.contains(module) || playing.containsKey(module)) return;
      editing.add(module);

      super.actionPerformed(e);
    }

    @Override
    protected LaunchTask getLaunchTask() {
      return new LaunchTask() {
        @Override
        protected void done() {
          super.done();

          // register that this module is no longer being edited
          editing.remove(module);
          setEnabled(true);
        }
      };
    }
  }

  public static void main(String[] args) {
    new Editor(args);
  }
}

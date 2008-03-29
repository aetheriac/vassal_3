package VASSAL.launch;

import java.awt.Cursor;
import java.awt.Frame;
import java.io.File;
import java.io.InputStream;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import javax.swing.AbstractAction;

// FIXME: switch back to javax.swing.SwingWorker on move to Java 1.6
//import javax.swing.SwingWorker;
import org.jdesktop.swingworker.SwingWorker;

import VASSAL.Info;
import VASSAL.build.module.GlobalOptions;
import VASSAL.configure.DirectoryConfigurer;
import VASSAL.preferences.Prefs;
import VASSAL.tools.ErrorLog;
import VASSAL.tools.FileChooser;

public abstract class AbstractLaunchAction extends AbstractAction {
  private static final long serialVersionUID = 1L;
  
  public static final int DEFAULT_INITIAL_HEAP = 256;
  public static final int DEFAULT_MAXIMUM_HEAP = 512;

  protected final Frame frame; 
  protected File module;
  protected final String entryPoint;
  protected final String[] args;

  protected static final Set<File> editing = new HashSet<File>();
  protected static final Map<File,Integer> using =
    new HashMap<File,Integer>();

  public AbstractLaunchAction(String name, Frame frame, String entryPoint,
                              String[] args, File module) {
    super(name);
    this.frame = frame;
    this.entryPoint = entryPoint;
    this.args = args;
    this.module = module;
  }

  public void actionPerformed(ActionEvent e) {
    frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    getLaunchTask().execute();
  }

  protected abstract LaunchTask getLaunchTask(); 

  protected File promptForModule() {
    // prompt the use to pick a module
    final FileChooser fc = FileChooser.createFileChooser(frame,
      (DirectoryConfigurer)
        Prefs.getGlobalPrefs().getOption(Prefs.MODULES_DIR_KEY));

    if (fc.showOpenDialog() == FileChooser.APPROVE_OPTION) {
      module = fc.getSelectedFile();
      if (module != null && !module.exists()) module = null;
    }
    
    return module;
  }

  protected class LaunchTask extends SwingWorker<Void,Void> {
    // module might be reassigned before the task is over, keep a local copy
    protected final File mod = AbstractLaunchAction.this.module; 

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

      final String[] pa =
        new String[6 + args.length + (mod == null ? 0 : 1)];
      pa[0] = "java";
      pa[1] = "-Xms" + initialHeap + "M";
      pa[2] = "-Xmx" + maximumHeap + "M";
      pa[3] = "-cp"; 
      pa[4] = System.getProperty("java.class.path");
      pa[5] = entryPoint; 
      System.arraycopy(args, 0, pa, 6, args.length);
      if (mod != null) pa[pa.length-1] = mod.getPath();

      final ProcessBuilder pb = new ProcessBuilder(pa);
      pb.directory(Info.getBinDir());

      final Process p = pb.start();
      final InputStream in = p.getInputStream();

      // process writes to stdout to signal end of loading
      in.read();
      publish((Void) null);

      // block until the process ends
      p.waitFor();
      return null;
    }

    @Override
    protected void process(List<Void> chunks) {
      frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
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
    }
  }
}


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
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
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
import VASSAL.tools.DataArchive;
import VASSAL.tools.ErrorLog;
import VASSAL.tools.FileChooser;
import VASSAL.tools.MacOSXMenuManager;
import VASSAL.tools.MenuManager;
import VASSAL.tools.OrderedMenu;
import VASSAL.tools.imports.ImportAction;


public class Editor {
  protected File moduleFile;
  protected File extensionFile;
  protected List<String> extractTargets = new ArrayList<String>();

  private boolean newModule = false;
  private boolean newExtension = false;
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
    if (Info.isMacOSX()) new MacOSXEditorMenuManager();
    else new EditorMenuManager();

    try {
      if (newModule) new CreateModuleAction(null).performAction(null);

      if (moduleFile == null) return;

      if (newExtension) {
        GameModule.init(new BasicModule(new DataArchive(moduleFile.getPath())));
        final JFrame f = GameModule.getGameModule().getFrame();
        f.setVisible(true);
        new NewExtensionAction(f).performAction(null);
      }
      else if (extensionFile != null) {
        GameModule.init(new BasicModule(new DataArchive(moduleFile.getPath())));
        final JFrame f = GameModule.getGameModule().getFrame();
        f.setVisible(true);
        new EditExtensionAction(extensionFile).performAction(null);
      }
      else if (importModule) new ImportAction(null).loadModule(moduleFile); 
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
      else if ("-newext".equals(arg)) {
        newExtension = true;
      }
      else if ("-edext".equals(arg)) {
        extensionFile = new File(args[++n]); 
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

    public LaunchAction(ModuleManagerWindow mm, File module) {
      super(Resources.getString("Main.edit_module"), mm,
            Editor.class.getName(), new String[0], module);
      setEnabled(!editing.contains(module) && !using.containsKey(module));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      // register that this module is being edited
      if (editing.contains(module) || using.containsKey(module)) return;
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
          editing.remove(mod);
          setEnabled(true);
        }

        @Override
        protected void process(List<Void> chunks) {
          super.process(chunks);
          ((ModuleManagerWindow) frame).addModule(mod);
        }
      };
    }
  }

  public static class ListLaunchAction extends LaunchAction {
    private static final long serialVersionUID = 1L;

    public ListLaunchAction(ModuleManagerWindow mm, File module) {
      super(mm, module);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      super.actionPerformed(e);
      setEnabled(false);
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

  private static class EditorMenuManager extends MenuManager {
    private final JMenuBar playerBar = new JMenuBar();
    private final JMenu playerFileMenu;
    private final JMenu playerHelpMenu;

    private final JMenuBar editorBar = new JMenuBar();
    private final JMenu editorFileMenu;
    private final JMenu editorEditMenu;
    private final JMenu editorToolsMenu;
    private final JMenu editorHelpMenu;
 
    private JMenuItem playerQuitItem;
    private JMenuItem editorQuitItem;

    private JMenuItem playerAboutItem;
    private JMenuItem editorAboutItem;

    private JMenuItem playerHelpItem;
    private JMenuItem editorHelpItem;

    public EditorMenuManager() {
      playerFileMenu = OrderedMenu.builder("General.file")
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

      playerHelpMenu = OrderedMenu.builder("General.help")
        .appendItem("General.help")
        .appendItem("AboutScreen.about_vassal")
        .create();

      playerBar.add(playerFileMenu);
      playerBar.add(playerHelpMenu);

      editorFileMenu = OrderedMenu.builder("General.file")
        .appendItem("Editor.save")
        .appendItem("Editor.save_as")
        .appendSeparator()
        .appendItem("General.quit")
        .create();

      editorEditMenu = OrderedMenu.builder("General.edit")
        .appendItem("Editor.delete")
        .appendItem("Editor.cut")
        .appendItem("Editor.copy")
        .appendItem("Editor.paste")
        .appendItem("Editor.move")
        .appendSeparator()
        .appendItem("Editor.ModuleEditor.properties")
        .appendItem("Editor.ModuleEditor.translate")
        .create();

      editorToolsMenu = OrderedMenu.builder("General.tools")
        .appendItem("Create updater")  // fix
        .appendItem("Editor.ModuleEditor.updated_saved")
        .create();

      editorHelpMenu = OrderedMenu.builder("General.help")
        .appendItem("General.help")
        .appendItem("Editor.ModuleEditor.reference_manual")
        .appendSeparator()
        .appendItem("AboutScreen.about_vassal")
        .create();

      editorBar.add(editorFileMenu);
      editorBar.add(editorEditMenu);
      editorBar.add(editorToolsMenu);
      editorBar.add(editorHelpMenu);

      parent.put("GameState.new_game", playerFileMenu);
      parent.put("GameState.load_game", playerFileMenu);
      parent.put("GameState.save_game", playerFileMenu);
      parent.put("GameState.close_game", playerFileMenu);
      parent.put("BasicLogger.begin_logfile", playerFileMenu);
      parent.put("BasicLogger.end_logfile", playerFileMenu);
      parent.put("Prefs.edit_preferences", playerFileMenu);

      parent.put("about_module", playerHelpMenu);

      parent.put("Editor.save", editorFileMenu);
      parent.put("Editor.save_as", editorFileMenu);

      parent.put("Editor.delete", editorEditMenu);
      parent.put("Editor.cut", editorEditMenu);
      parent.put("Editor.copy", editorEditMenu);
      parent.put("Editor.paste", editorEditMenu);
      parent.put("Editor.move", editorEditMenu);
      parent.put("Editor.ModuleEditor.properties", editorEditMenu);
      parent.put("Editor.ModuleEditor.translate", editorEditMenu);

      parent.put("create_module_updater", editorToolsMenu);
      parent.put("Editor.ModuleEditor.update_saved", editorToolsMenu);

      parent.put("Editor.ModuleEditor.reference_manual", editorHelpMenu);
    }

    @Override
    public JMenuBar getMenuBar(int type) {
      switch (type) {
      case PLAYER: return playerBar;
      case EDITOR: return editorBar;
      default:     return null; 
      }
    }

    @Override
    public void addAction(String id, Action a) {
      if ("General.quit".equals(id)) {
        if (playerQuitItem != null) playerFileMenu.remove(playerQuitItem);
        if (editorQuitItem != null) editorFileMenu.remove(editorQuitItem);

        playerQuitItem = playerFileMenu.add(a);
        editorQuitItem = editorFileMenu.add(a);
        
        actions.put(id, a);
      }
      else if ("AboutScreen.about_vassal".equals(id)) {
        if (playerAboutItem != null) playerHelpMenu.remove(playerAboutItem);
        if (editorAboutItem != null) editorHelpMenu.remove(editorAboutItem);

        playerAboutItem = playerHelpMenu.add(a);
        editorAboutItem = editorHelpMenu.add(a);

        actions.put(id, a);
      }
      else if ("General.help".equals(id)) {
        if (playerHelpItem != null) playerHelpMenu.remove(playerHelpItem);
        if (editorHelpItem != null) editorHelpMenu.remove(editorHelpItem);

        playerHelpItem = playerHelpMenu.add(a);
        editorHelpItem = editorHelpMenu.add(a);

        actions.put(id, a);
      }
      else {
        super.addAction(id, a);
      }
    }
  }

  private static class MacOSXEditorMenuManager extends MacOSXMenuManager {
    private final JMenu fileMenu;
    private final JMenu editMenu;
    private final JMenu toolsMenu;
    private final JMenu helpMenu;

    public MacOSXEditorMenuManager() {
      fileMenu = OrderedMenu.builder("General.file")
        .appendItem("Editor.save")
        .appendItem("Editor.save_as")
        .appendSeparator()
        .appendItem("GameState.new_game")
        .appendItem("GameState.load_game")
        .appendItem("GameState.save_game")
        .appendItem("GameState.close_game")
        .appendSeparator()
        .appendItem("BasicLogger.begin_logfile")
        .appendItem("BasicLogger.end_logfile")
        .create();

      editMenu = OrderedMenu.builder("General.edit")
        .appendItem("Editor.delete")
        .appendItem("Editor.cut")
        .appendItem("Editor.copy")
        .appendItem("Editor.paste")
        .appendItem("Editor.move")
        .appendSeparator()
        .appendItem("Editor.ModuleEditor.properties")
        .appendItem("Editor.ModuleEditor.translate")
        .create();
  
      toolsMenu =  OrderedMenu.builder("General.tools")
        .appendItem("Create updater")  // fix
        .appendItem("Editor.ModuleEditor.updated_saved")
        .create();

      helpMenu = OrderedMenu.builder("General.help")
        .appendItem("General.help")
        .appendItem("Editor.ModuleEditor.reference_manual")
        .appendSeparator()
        .appendItem("about_module")
        .create();

      menuBar.add(fileMenu);
      menuBar.add(editMenu);
      menuBar.add(toolsMenu);
      menuBar.add(helpMenu);

      parent.put("Editor.save", fileMenu);
      parent.put("Editor.save_as", fileMenu);
      parent.put("GameState.new_game", fileMenu);
      parent.put("GameState.load_game", fileMenu);
      parent.put("GameState.save_game", fileMenu);
      parent.put("GameState.close_game", fileMenu);
      parent.put("BasicLogger.begin_logfile", fileMenu);
      parent.put("BasicLogger.end_logfile", fileMenu);
      parent.put("General.quit", fileMenu);
      
      parent.put("Editor.delete", editMenu);
      parent.put("Editor.cut", editMenu);
      parent.put("Editor.copy", editMenu);
      parent.put("Editor.paste", editMenu);
      parent.put("Editor.move", editMenu);
      parent.put("Editor.ModuleEditor.properties", editMenu);
      parent.put("Editor.ModuleEditor.translate", editMenu);

      parent.put("create_module_updater", toolsMenu);
      parent.put("Editor.ModuleEditor.update_saved", toolsMenu);

      parent.put("General.help", helpMenu);
      parent.put("Editor.ModuleEditor.reference_manual", helpMenu);
      parent.put("about_module", helpMenu);
    }
  }

  public static void main(String[] args) {
    new Editor(args);
  }
}

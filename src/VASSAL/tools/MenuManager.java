
package VASSAL.tools;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import com.apple.eawt.Application;
import com.apple.eawt.ApplicationAdapter;
import com.apple.eawt.ApplicationEvent;
import com.apple.eawt.ApplicationListener;

import VASSAL.Info;
import VASSAL.i18n.Resources;
import VASSAL.tools.OrderedMenu;

public abstract class MenuManager {
  private static final MenuManager instance =
    Info.isMacOSX() ? new MacOSXMenuManager() : new GeneralMenuManager();

  public static MenuManager getInstance() {
    return instance;
  }

  protected final Map<String,JMenuItem> items = new HashMap<String,JMenuItem>();
  protected final Map<String,JMenu> parent = new HashMap<String,JMenu>();
  protected final Map<String,Action> actions = new HashMap<String,Action>();

  public static final int MANAGER = 0;
  public static final int PLAYER = 1;
  public static final int EDITOR = 2;

  public abstract JMenuBar getMenuBar(int type);

  public JMenuItem getMenuItem(String id) {
    return items.get(id);
  }

  public Action getAction(String id) {
    return actions.get(id);
  } 

  public void addAction(Action a) {
    addAction((String) a.getValue(Action.ACTION_COMMAND_KEY), a);
  }

  public void addAction(String id, Action a) {
    final JMenu menu = parent.get(id);
    final JMenuItem old = items.put(id, menu.add(a));
    if (old != null) menu.remove(old);
    actions.put(id, a);      
  }
  
  public void addMenuItem(String id, JMenuItem item) {
    final JMenu menu = parent.get(id);
    final JMenuItem old = items.put(id, menu.add(item));
    if (old != null) menu.remove(old);
  }

  public void removeAction(String id) {
  }

  public void removeMenuItem(String id) {
  }

  private static class GeneralMenuManager extends MenuManager {
    private final JMenuBar managerBar;
    private final JMenu managerFileMenu;
    private final JMenu managerToolsMenu;
    private final JMenu managerHelpMenu;

    private final JMenuBar playerBar;
    private final JMenu playerFileMenu;
    private final JMenu playerHelpMenu;

    private final JMenuBar editorBar;
    private final JMenu editorFileMenu;
    private final JMenu editorEditMenu;
    private final JMenu editorToolsMenu;
    private final JMenu editorHelpMenu;
 
    private JMenuItem managerQuitItem;
    private JMenuItem playerQuitItem;
    private JMenuItem editorQuitItem;

    private JMenuItem managerAboutItem;
    private JMenuItem playerAboutItem;
    private JMenuItem editorAboutItem;

    private JMenuItem managerHelpItem;
    private JMenuItem playerHelpItem;
    private JMenuItem editorHelpItem;
 
    public GeneralMenuManager() {
      managerFileMenu = OrderedMenu.builder("General.file")
        .appendItem("Main.play_module")
        .appendItem("Main.edit_module")
        .appendItem("Main.new_module")
        .appendItem("Editor.import_module")
        .appendSeparator()
        .appendItem("General.quit")
        .create();

      managerToolsMenu = OrderedMenu.builder("General.tools")
        .appendItem("Chat.server_status")
        .appendItem("Editor.ModuleEditor.translate_vassal")
        .create();

      managerHelpMenu = OrderedMenu.builder("General.help")
        .appendItem("General.help")
        .appendItem("AboutScreen.about_vassal")
        .create();
      
      managerBar = new JMenuBar();
      managerBar.add(managerFileMenu);
      managerBar.add(managerToolsMenu);
      managerBar.add(managerHelpMenu);

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

      playerBar = new JMenuBar();
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

      editorBar = new JMenuBar();
      editorBar.add(editorFileMenu);
      editorBar.add(editorEditMenu);
      editorBar.add(editorToolsMenu);
      editorBar.add(editorHelpMenu);

      parent.put("Main.play_module", managerFileMenu);
      parent.put("Main.edit_module", managerFileMenu);
      parent.put("Main.new_module", managerFileMenu);
      parent.put("Editor.import_module", managerFileMenu);

      parent.put("Chat.server_status", managerToolsMenu);
      parent.put("Editor.ModuleEditor.translate_vassal", managerToolsMenu);

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
      case MANAGER: return managerBar;
      case PLAYER:  return playerBar;
      case EDITOR:  return editorBar; 
      default:      throw new IllegalArgumentException();
      }   
    }

    @Override
    public void addAction(String id, Action a) {
      if ("General.quit".equals(id)) {
        if (managerQuitItem != null) managerFileMenu.remove(managerQuitItem);
        if (playerQuitItem != null) playerFileMenu.remove(playerQuitItem);
        if (editorQuitItem != null) editorFileMenu.remove(editorQuitItem);

        managerQuitItem = managerFileMenu.add(a);
        playerQuitItem = playerFileMenu.add(a);
        editorQuitItem = editorFileMenu.add(a);
        
        actions.put(id, a);
      }
      else if ("AboutScreen.about_vassal".equals(id)) {
        if (managerAboutItem != null) managerHelpMenu.remove(managerAboutItem);
        if (playerAboutItem != null) playerHelpMenu.remove(playerAboutItem);
        if (editorAboutItem != null) editorHelpMenu.remove(editorAboutItem);

        managerAboutItem = managerHelpMenu.add(a);
        playerAboutItem = playerHelpMenu.add(a);
        editorAboutItem = editorHelpMenu.add(a);

        actions.put(id, a);
      }
      else if ("General.help".equals(id)) {
        if (managerHelpItem != null) managerHelpMenu.remove(managerHelpItem);
        if (playerHelpItem != null) playerHelpMenu.remove(playerHelpItem);
        if (editorHelpItem != null) editorHelpMenu.remove(editorHelpItem);

        managerHelpItem = managerHelpMenu.add(a);
        playerHelpItem = playerHelpMenu.add(a);
        editorHelpItem = editorHelpMenu.add(a);

        actions.put(id, a);
      }
      else {
        super.addAction(id, a);
      }
    }
  }

  private static class MacOSXMenuManager extends MenuManager {
    private final JMenuBar menubar = new JMenuBar();
    private final Application app = Application.getApplication();
    
    private ApplicationListener quitListener;
    private ApplicationListener prefsListener;
    private ApplicationListener aboutListener;

    private final JMenu fileMenu;
    private final JMenu editMenu;
    private final JMenu toolsMenu;
    private final JMenu helpMenu;

    public MacOSXMenuManager() {
      fileMenu = OrderedMenu.builder("General.file")
        .appendItem("Main.play_module")
        .appendSeparator()
        .appendItem("Main.edit_module")
        .appendItem("Main.new_module")
        .appendItem("Editor.save")
        .appendItem("Editor.save_as")
        .appendItem("Editor.import_module")
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
        .appendItem("Chat.server_status")
        .appendSeparator()
        .appendItem("Create updater")  // fix
        .appendItem("Editor.ModuleEditor.updated_saved")
        .create();

      helpMenu = OrderedMenu.builder("General.help")
        .appendItem("General.help")
        .appendItem("Editor.ModuleEditor.reference_manual")
        .appendSeparator()
        .appendItem("AboutScreen.about_vassal")
        .create();

      menubar.add(fileMenu);
      menubar.add(editMenu);
      menubar.add(toolsMenu);
      menubar.add(helpMenu);

      parent.put("Main.play_module", fileMenu);
      parent.put("Main.edit_module", fileMenu);
      parent.put("Main.new_module", fileMenu);
      parent.put("Editor.import_module", fileMenu);
      parent.put("GameState.new_game", fileMenu);
      parent.put("GameState.load_game", fileMenu);
      parent.put("GameState.save_game", fileMenu);
      parent.put("GameState.close_game", fileMenu);
      parent.put("BasicLogger.begin_logfile", fileMenu);
      parent.put("BasicLogger.end_logfile", fileMenu);
      
      parent.put("Editor.delete", editMenu);
      parent.put("Editor.cut", editMenu);
      parent.put("Editor.copy", editMenu);
      parent.put("Editor.paste", editMenu);
      parent.put("Editor.move", editMenu);
      parent.put("Editor.ModuleEditor.properties", editMenu);
      parent.put("Editor.ModuleEditor.translate", editMenu);

      parent.put("Chat.server_status", toolsMenu);
      parent.put("create_module_updater", toolsMenu);
      parent.put("Editor.ModuleEditor.update_saved", toolsMenu);
      parent.put("Editor.ModuleEditor.translate_vassal", toolsMenu);

      parent.put("General.help", helpMenu);
      parent.put("Editor.ModuleEditor.reference_manual", helpMenu);
      parent.put("about_module", helpMenu);
    }

    @Override
    public JMenuBar getMenuBar(int type) {
      return menubar;
    }

    @Override
    public void addAction(String id, final Action a) {
      if ("General.quit".equals(id)) {
        // install our listener to trigger the quit action
        app.removeApplicationListener(quitListener);
        quitListener = new ApplicationAdapter() {
          @Override
          public void handleQuit(ApplicationEvent e) {
            e.setHandled(false);
            a.actionPerformed(null);
          }
        };
        app.addApplicationListener(quitListener);

        // no need to track enabled state, quit is always active

        actions.put(id, a);
      }
      else if ("Prefs.edit_preferences".equals(id)) {
        // install our listener to trigger the prefs action
        app.removeApplicationListener(prefsListener);
        prefsListener = new ApplicationAdapter() {
          @Override
          public void handlePreferences(ApplicationEvent e) {
            e.setHandled(true);
            a.actionPerformed(null);
          }
        };
        app.addApplicationListener(prefsListener);

        app.addPreferencesMenuItem();
        app.setEnabledPreferencesMenu(a.isEnabled());

        // track the enabled state of the prefs action
        a.addPropertyChangeListener(new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent e) {
            if ("enabled".equals(e.getPropertyName())) {
              app.setEnabledPreferencesMenu((Boolean) e.getNewValue());
            }
          }
        });

        actions.put(id, a);
      }
      else if ("AboutScreen.about_vassal".equals(id)) {
        // install our listener to trigger the about action
        app.removeApplicationListener(aboutListener);
        aboutListener = new ApplicationAdapter() {
          @Override
          public void handleAbout(ApplicationEvent e) {
            e.setHandled(true);
            a.actionPerformed(null);
          }
        };
        app.addApplicationListener(aboutListener);

        app.addAboutMenuItem();
        app.setEnabledAboutMenu(a.isEnabled());

        // track the enabled state of the prefs action
        a.addPropertyChangeListener(new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent e) {
            if ("enabled".equals(e.getPropertyName())) {
              app.setEnabledAboutMenu((Boolean) e.getNewValue());
            }
          }
        });

        actions.put(id, a);
      }
      else {
        super.addAction(id, a);
      }
    }
  } 
}

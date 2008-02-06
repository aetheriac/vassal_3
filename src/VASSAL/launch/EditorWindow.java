package VASSAL.launch;


import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import VASSAL.build.module.documentation.HelpFile;
import VASSAL.build.module.documentation.HelpWindow;
import VASSAL.configure.ConfigureTree;
import VASSAL.configure.ModuleUpdaterDialog;
import VASSAL.configure.SaveAction;
import VASSAL.configure.SaveAsAction;
import VASSAL.configure.SavedGameUpdaterDialog;
import VASSAL.configure.ShowHelpAction;
import VASSAL.i18n.Resources;
import VASSAL.i18n.TranslateVassalWindow;

public class EditorWindow extends JFrame {
  protected static final EditorWindow instance = new EditorWindow();

  public static EditorWindow getInstance() {
    return instance;
  }

  protected final HelpWindow helpWindow = new HelpWindow(
    Resources.getString("Editor.ModuleEditor.reference_manual"), //$NON-NLS-1$
    null
  );

  protected final ConfigureTree tree = new ConfigureTree(null, helpWindow);

  private final Map<MenuKey,JMenuItem> menuItems =
    new HashMap<MenuKey,JMenuItem>();

  protected final JMenuBar menuBar = new JMenuBar();
  
  protected final JToolBar toolBar = new JToolBar();

  protected final JMenu fileMenu;
  protected final JMenu editMenu;
  protected final JMenu toolsMenu;
  protected final JMenu helpMenu;

  public JMenu getFileMenu() {
    return fileMenu;
  }

  public JMenu getEditMenu() {
    return editMenu;
  }

  public JMenu getToolsMenu() {
    return toolsMenu;
  }

  public JMenu getHelpMenu() {
    return helpMenu;
  }

   public enum MenuKey {
    SAVE,
    SAVE_AS,
    QUIT,
    CREATE_MODULE_UPDATER,
    UPDATE_SAVED,
    TRANSLATE_VASSAL,
    HELP
  };
 
  public JMenuItem getMenuItem(MenuKey key) {
    return menuItems.get(key);
  }

  private int findMenuItem(JMenu menu, JMenuItem item) {
    for (int i = 0; i < menu.getItemCount(); i++) {
      if (item == menu.getItem(i)) return i;
    }
    return -1;
  }

  public JMenuItem setMenuItem(MenuKey key, Action action) {
    final JMenuItem oldItem = getMenuItem(key);
    for (int i = 0; i < menuBar.getMenuCount(); i++) {
      final JMenu menu = menuBar.getMenu(i);
      final int pos = findMenuItem(menu, oldItem);
      if (pos != -1) {
        menu.remove(pos);
        return menuItems.put(key, menu.insert(action, pos));
      }
    }
    return null;
  }

  public JMenuItem setMenuItem(MenuKey key, JMenuItem item) {
    final JMenuItem oldItem = getMenuItem(key);
    for (int i = 0; i < menuBar.getMenuCount(); i++) {
      final JMenu menu = menuBar.getMenu(i);
      final int pos = findMenuItem(menu, oldItem);
      if (pos != -1) {
        menu.remove(pos);
        return menuItems.put(key, menu.insert(item, pos));
      }
    }
    return null;
  }

  protected final JScrollPane scrollPane =
    new JScrollPane(new JPanel(), // prevents a NullPointerException on packing
                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

  protected EditorWindow() {
    setTitle("VASSAL Editor");

    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    setLayout(new BorderLayout());
    setJMenuBar(menuBar);
    
    toolBar.setFloatable(false);
    add(toolBar, BorderLayout.NORTH);

    // build File menu
    fileMenu = new JMenu(Resources.getString("General.file"));
    fileMenu.setMnemonic(KeyEvent.VK_F);
    menuBar.add(fileMenu);

    final SaveAction saveAction = new SaveAction() {
      private static final long serialVersionUID = 1L;

      public void actionPerformed(ActionEvent e) {
//        ModuleEditWindow.this.save();
      }
    };

    menuItems.put(MenuKey.SAVE, fileMenu.add(saveAction));
    toolBar.add(saveAction);

    final SaveAsAction saveAsAction = new SaveAsAction() {
      private static final long serialVersionUID = 1L;

      public void actionPerformed(ActionEvent e) {
//        ModuleEditWindow.this.saveAs();
      }
    };

    menuItems.put(MenuKey.SAVE_AS, fileMenu.add(saveAsAction));
    toolBar.add(saveAsAction);

    fileMenu.addSeparator(); 

    final JMenuItem quitItem = new JMenuItem(Resources.QUIT);
    quitItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        System.exit(0);
      }
    });
   
    quitItem.setMnemonic('Q');
    menuItems.put(MenuKey.QUIT, fileMenu.add(quitItem));
 
    // build Edit menu
    editMenu = new JMenu(Resources.getString("General.edit"));
    menuBar.add(editMenu);

   // build Tools menu
    toolsMenu = new JMenu(Resources.getString("General.tools"));
    menuBar.add(toolsMenu);
    
    JMenuItem mi = menuItems.put(MenuKey.CREATE_MODULE_UPDATER,
      toolsMenu.add(Resources.getString(
        "Editor.ModuleEditor.create_module_updater"))); //$NON-NLS-1$
    mi.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        new ModuleUpdaterDialog(EditorWindow.this).setVisible(true);
      }
    });

    mi = menuItems.put(MenuKey.UPDATE_SAVED,
      toolsMenu.add(Resources.getString(
        "Editor.ModuleEditor.update_saved"))); //$NON-NLS-1$
    mi.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        new SavedGameUpdaterDialog(EditorWindow.this).setVisible(true);
      }
    });

    toolsMenu.addSeparator();

    mi = menuItems.put(MenuKey.TRANSLATE_VASSAL,
      toolsMenu.add(Resources.getString(
        "Editor.ModuleEditor.translate_vassal"))); //$NON-NLS-1$
    mi.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        new TranslateVassalWindow(EditorWindow.this).setVisible(true);
      }
    });

    // build Help menu
    helpMenu = new JMenu(Resources.getString("General.help"));
    menuBar.add(helpMenu);

    Action helpAction = null;

    try {
      File dir = VASSAL.build.module.Documentation.getDocumentationBaseDir();
      dir = new File(dir, "ReferenceManual"); //$NON-NLS-1$
      helpAction = new ShowHelpAction(HelpFile.toURL(new File(dir, "index.htm")), helpWindow.getClass().getResource("/images/Help16.gif")); //$NON-NLS-1$ //$NON-NLS-2$
      helpAction.putValue(Action.SHORT_DESCRIPTION, Resources.getString("Editor.ModuleEditor.reference_manual")); //$NON-NLS-1$

      toolBar.add(helpAction);
      menuItems.put(MenuKey.HELP, helpMenu.add(helpAction));
    }
    catch (MalformedURLException e) {
      e.printStackTrace();
    }

    add(scrollPane, BorderLayout.CENTER);

    pack();
  }

  public static void main(String[] args) {
    new EditorWindow().setVisible(true);
  }
}

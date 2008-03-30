/*
 * $Id$
 *
 * Copyright (c) 2000-2007 by Rodney Kinney
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
package VASSAL.launch;

import java.io.File;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.SwingUtilities;

import VASSAL.Info;
import VASSAL.tools.ErrorLog;
import VASSAL.tools.MacOSXMenuManager;
import VASSAL.tools.MenuManager;
import VASSAL.tools.OrderedMenu;

/**
 * Tracks recently-used modules and builds the main GUI window for 
 * interacting with modules.
 * 
 * @author rodneykinney
 * @since 3.1.0
 */
public class ModuleManager {
  public ModuleManager(final String[] args) {
    StartUp.initSystemProperties();
    StartUp.setupErrorLog();

    Thread.setDefaultUncaughtExceptionHandler(new ErrorLog());
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        launch();
      }
    });
  }

  protected void launch() {
    if (Info.isMacOSX()) new MacOSXModuleManagerMenuManager();
    else new ModuleManagerMenuManager();

    final File prefsFile = new File(Info.getHomeDir(), "Preferences");
    final boolean isFirstTime = !prefsFile.exists();

    if (isFirstTime) {
      new FirstTimeDialog().setVisible(true);
    }
    else {
      ModuleManagerWindow.getInstance().setVisible(true);
    }
  }
 
  private static class ModuleManagerMenuManager extends MenuManager {
    private final JMenuBar menuBar = new JMenuBar();
    private final JMenu fileMenu;
    private final JMenu toolsMenu;
    private final JMenu helpMenu;

    public ModuleManagerMenuManager() {
      fileMenu = OrderedMenu.builder("General.file")
        .appendItem("Main.play_module")
        .appendItem("Main.edit_module")
        .appendItem("Main.new_module")
        .appendItem("Editor.import_module")
        .appendSeparator()
        .appendItem("General.quit")
        .create();

      toolsMenu = OrderedMenu.builder("General.tools")
        .appendItem("Chat.server_status")
        .appendItem("Editor.ModuleEditor.translate_vassal")
        .create();

      helpMenu = OrderedMenu.builder("General.help")
        .appendItem("General.help")
        .appendItem("AboutScreen.about_vassal")
        .create();
      
      menuBar.add(fileMenu);
      menuBar.add(toolsMenu);
      menuBar.add(helpMenu);

      parent.put("Main.play_module", fileMenu);
      parent.put("Main.edit_module", fileMenu);
      parent.put("Main.new_module", fileMenu);
      parent.put("Editor.import_module", fileMenu);
      parent.put("General.quit", fileMenu);

      parent.put("Chat.server_status", toolsMenu);
      parent.put("Editor.ModuleEditor.translate_vassal", toolsMenu);

      parent.put("General.help", helpMenu);
      parent.put("Editor.ModuleEditor.reference_manual", helpMenu);
      parent.put("AboutScreen.about_vassal", helpMenu); 
    }
    
    @Override
    public JMenuBar getMenuBar(int type) {
      return type == MANAGER ? menuBar : null;
    }
  }

  private static class MacOSXModuleManagerMenuManager
                                                   extends MacOSXMenuManager {
    private final JMenu fileMenu;
    private final JMenu toolsMenu;
    private final JMenu helpMenu;

    public MacOSXModuleManagerMenuManager() {
      fileMenu = OrderedMenu.builder("General.file")
        .appendItem("Main.play_module")
        .appendItem("Main.edit_module")
        .appendItem("Main.new_module")
        .appendItem("Editor.import_module")
        .create();

      toolsMenu = OrderedMenu.builder("General.tools")
        .appendItem("Chat.server_status")
        .appendItem("Editor.ModuleEditor.translate_vassal")
        .create();

      helpMenu = OrderedMenu.builder("General.help")
        .appendItem("General.help")
        .appendItem("Editor.ModuleEditor.reference_manual")
        .create();
      
      menuBar.add(fileMenu);
      menuBar.add(toolsMenu);
      menuBar.add(helpMenu);

      parent.put("Main.play_module", fileMenu);
      parent.put("Main.edit_module", fileMenu);
      parent.put("Main.new_module", fileMenu);
      parent.put("Editor.import_module", fileMenu);

      parent.put("Chat.server_status", toolsMenu);
      parent.put("Editor.ModuleEditor.translate_vassal", toolsMenu);

      parent.put("General.help", helpMenu);
      parent.put("Editor.ModuleEditor.reference_manual", helpMenu);
    }

/*
    @Override
    public JMenuBar getMenuBar(int type) {
      return type == MANAGER ? menuBar : null;
    }
*/
  }

  public static void main(String[] args) {
    new ModuleManager(args);
  }
}

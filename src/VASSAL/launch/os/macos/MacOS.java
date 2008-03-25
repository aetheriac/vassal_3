
package VASSAL.launch.os.macos;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import com.apple.eawt.Application;
import com.apple.eawt.ApplicationAdapter;
import com.apple.eawt.ApplicationEvent;

import VASSAL.tools.ErrorLog;

public class MacOS {
  private MacOS() { }

  public static void setup() {
    // use the system menu bar
    System.setProperty("apple.laf.useScreenMenuBar", "true");

    // put "VASSAL" in the system menu bar
    System.setProperty(
      "com.apple.mrj.application.apple.menu.about.name", "VASSAL");

    // show the grow box in the lower right corner of windows
    System.setProperty("apple.awt.showGrowBox", "true");

    // grow box should not overlap other elements
    System.setProperty("com.apple.mrj.application.growbox.intrudes", "true");

    // live resize of app windows
    System.setProperty("com.apple.mrj.application.live-resize", "true");

    // use native LookAndFeel
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }
    catch (Exception e) {
      ErrorLog.warn(e);
    }

/*
    final Application app = Application.getApplication();
    app.addApplicationListener(new ApplicationAdapter() {
      public void handleAbout(ApplicationEvent e) {
        JOptionPane.showMessageDialog(null, "handleAbout");
        e.setHandled(false);
      }

      public void handleOpenApplication(ApplicationEvent e) {
        JOptionPane.showMessageDialog(null, "handleOpenApplication");
        e.setHandled(false);
      }
      
      public void handleOpenFile(ApplicationEvent e) {
        JOptionPane.showMessageDialog(null, "handleOpenFile");
        e.setHandled(false);
      }
      
      public void handlePreferences(ApplicationEvent e) {
        JOptionPane.showMessageDialog(null, "handlePreferences");
        e.setHandled(false);
      }

      public void handleQuit(ApplicationEvent e) {
        JOptionPane.showMessageDialog(null, "handleQuit");
        e.setHandled(false);
      }

      public void handleReOpenApplication(ApplicationEvent e) {
        JOptionPane.showMessageDialog(null, "handleReOpenApplication");
        e.setHandled(false);
      }
    });
*/
  }
}

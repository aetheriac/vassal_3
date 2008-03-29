package VASSAL.tools;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import javax.swing.JMenuBar;

import com.apple.eawt.Application;
import com.apple.eawt.ApplicationAdapter;
import com.apple.eawt.ApplicationEvent;
import com.apple.eawt.ApplicationListener;

public abstract class MacOSXMenuManager extends MenuManager {
  protected final JMenuBar menuBar = new JMenuBar();

  private final Application app = Application.getApplication();
  
  private ApplicationListener quitListener;
  private ApplicationListener prefsListener;
  private ApplicationListener aboutListener;

  @Override
  public JMenuBar getMenuBar(int type) {
    return menuBar;
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

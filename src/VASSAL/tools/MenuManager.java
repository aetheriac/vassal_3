
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
  protected final Map<String,JMenuItem> items = new HashMap<String,JMenuItem>();
  protected final Map<String,JMenu> parent = new HashMap<String,JMenu>();
  protected final Map<String,Action> actions = new HashMap<String,Action>();

  protected static MenuManager instance;

  public static MenuManager getInstance() {
    return instance;
  }

  public MenuManager() {
    if (instance != null) throw new IllegalStateException();
    instance = this;
  }
 
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

/*
  public void removeAction(String id) {
  }

  public void removeMenuItem(String id) {
  }
*/
}

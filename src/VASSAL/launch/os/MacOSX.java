
package VASSAL.launch.os;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import javax.swing.UIManager;

import VASSAL.tools.ErrorLog;

public class MacOSX {
  private MacOSX() { }
 
/* 
  private static class OSXAdapter implements InvocationHandler {
    public OSXAdapter() {
      try {
        // get Application.class and an instance
        final Class<?> appClass = Class.forName("com.apple.eawt.Application");
        final Object app = appClass.newInstance();

        // get ApplicationListener.class
        final Class<?> listenerClass =
          Class.forName("com.apple.eawt.ApplicationListener");
       
        // get Application.addApplicationListener()
        final Method addListenerMethod = appClass.getDeclaredMethod(
          "addApplicationListener",
          new Class<?>[] { listenerClass }
        );

        // create a proxy object for ApplicationListener
        final Object adapterProxy = Proxy.newProxyInstance(
          OSXAdapter.class.getClassLoader(),
          new Class<?>[] { listenerClass },
          this
        );

        // call app.addApplicationListener()
        addListenerMethod.invoke(app, new Object[] { adapterProxy });
      }
      catch (Exception e) {
        ErrorLog.warn(e);
      }
    }

    public void handleOpenApplication(Object event) {
      setApplicationEventHandled(event, false);
    }
        
    public void handleOpenFile(Object event) {
      setApplicationEventHandled(event, false);
    }
        
    public void handlePreferences(Object event) {
      setApplicationEventHandled(event, false);
    }

    public void handlePrintFile(Object event) {
      setApplicationEventHandled(event, false);
    }
  
    public void handleQuit(Object event) {
      setApplicationEventHandled(event, false);
    }

    public void handleReOpenApplication(Object event) {
      setApplicationEventHandled(event, false);
    }

    public Object invoke(Object proxy, Method method, Object[] args)
        throws Throwable {

      // sanity check
      if (args.length != 1) return null;

      // call the requested method
      final String name = method.getName();
      if ("handleOpenApplication".equals(name)) {
        handleOpenApplication(args[0]);  
      }
      else if ("handleOpenFile".equals(name)) {
        handleOpenFile(args[0]);
      }
      else if ("handlePreferences".equals(name)) {
        handlePreferences(args[0]);
      }
      else if ("handlePrintFile".equals(name)) {
        handlePrintFile(args[0]);
      }
      else if ("handleQuit".equals(name)) {
        handleQuit(args[0]);
      }
      else if ("handleReOpenApplication".equals(name)) {
        handleReOpenApplication(args[0]);
      }

      return null;
    }

    private void setApplicationEventHandled(Object event, boolean handled) {
      try {
        final Method setHandledMethod = event.getClass().getDeclaredMethod(
          "setHandled", new Class<?>[] { boolean.class });

        setHandledMethod.invoke(
          event, new Object[] { Boolean.valueOf(handled) });
      }
      catch (Exception e) {
        ErrorLog.warn(e);
      }
    } 
  }
*/
}

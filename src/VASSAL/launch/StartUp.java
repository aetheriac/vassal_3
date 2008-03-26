
package VASSAL.launch;

import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

import VASSAL.Info;
import VASSAL.launch.os.macos.MacOS;

public class StartUp {
  public static void initSystemProperties() {
    //
    // Error log setup
    //
    String stderr = "stderr";           //$NON-NLS-1$
    final String errorLog = "errorLog"; //$NON-NLS-1$

    // redirect stderr to errorLog by default
    if (System.getProperty(stderr) == null) {
      System.setProperty(stderr,
        new File(Info.getHomeDir(), errorLog).getPath());
    }

    // write no log if stderr is set to null
    stderr = System.getProperty(stderr); 
    if (!"null".equals(stderr)) { //$NON-NLS-1$
      try {
        System.setErr(new PrintStream(new FileOutputStream(stderr)));
      }
      catch (IOException ex) {
        System.err.println(
          "Unable to redirect stderr to " + stderr); //$NON-NLS-1$
      }
    }

    //
    // HTTP proxying setup
    //
    final String httpProxyHost = "http.proxyHost";  //$NON-NLS-1$
    final String proxyHost = "proxyHost";           //$NON-NLS-1$

    if (System.getProperty(httpProxyHost) == null && 
        System.getProperty(proxyHost) != null) {
      System.setProperty(httpProxyHost, System.getProperty(proxyHost));
    }

    final String httpProxyPort = "http.proxyPort"; //$NON-NLS-1$
    final String proxyPort = "proxyPort";          //$NON-NLS-1$

    if (System.getProperty(httpProxyPort) == null &&
        System.getProperty(proxyPort) != null) {
      System.setProperty(httpProxyPort, System.getProperty(proxyPort));
    }

    //
    // OS-specific setup
    //
    if (Info.isMacOSX()) MacOS.setup();
    
    //
    // Miscellaneous setup
    //
    System.setProperty("swing.aatext", "true"); //$NON-NLS-1$ //$NON-NLS-2$
    System.setProperty("swing.boldMetal", "false"); //$NON-NLS-1$ //$NON-NLS-2$
    System.setProperty("awt.useSystemAAFontSettings", "on"); //$NON-NLS-1$ //$NON-NLS-2$
  }
  
  public static void setupErrorLog() {
    // begin the error log
    System.err.println("-- OS " + System.getProperty("os.name")); //$NON-NLS-1$ //$NON-NLS-2$
    System.err.println("-- Java version " + System.getProperty("java.version")); //$NON-NLS-1$ //$NON-NLS-2$
    System.err.println("-- VASSAL version " + Info.getVersion()); //$NON-NLS-1$
  }  
}

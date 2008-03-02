/*
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
package VASSAL.tools;

import javax.swing.JOptionPane;

import VASSAL.build.GameModule;
import VASSAL.i18n.Resources;
import edu.stanford.ejalbert.BrowserLauncher;
import edu.stanford.ejalbert.exception.BrowserLaunchingInitializingException;
import edu.stanford.ejalbert.exception.UnsupportedOperatingSystemException;

// FIXME: Use java.awt.Desktop for this when we move to Java 1.6+.

/**
 * Utility class for displaying an external browser window
 * @author rkinney
 */
public class BrowserSupport {
  private static BrowserLauncher browserLauncher;
  private static Exception initializationError;
  
  public static void openURL(String url) {
    if (!initialized()) {
      initialize();
    }
    if (initializationError == null) {
      browserLauncher.openURLinBrowser(url);
    }
    else {
      String msg = Resources.getString("BrowserSupport.unable_to_launch")+"\n"; //$NON-NLS-1$
      if (initializationError.getMessage() != null) {
        msg += initializationError.getMessage()+"\n"; //$NON-NLS-1$
      }
      msg += Resources.getString("BrowserSupport.open_browser",url); //$NON-NLS-1$
      JOptionPane.showMessageDialog(GameModule.getGameModule().getFrame(), msg);
    }

  }

  private static void initialize() {
    try {
      browserLauncher = new BrowserLauncher();
    }
    catch (BrowserLaunchingInitializingException e) {
      initializationError = e;
    }
    catch (UnsupportedOperatingSystemException e) {
      initializationError = e;
    }
  }

  private static boolean initialized() {
    return browserLauncher != null || initializationError != null;
  }

}

/*
 * $Id$
 *
 * Copyright (c) 2003 by Rodney Kinney
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
package VASSAL;

import java.awt.Component;
import java.awt.GraphicsConfiguration;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.io.File;

import VASSAL.tools.VersionFormatException;
import VASSAL.tools.VersionTokenizer;

/**
 * Class for storing release-related information
 */
public final class Info {
  private static final String VERSION = "3.1.0-svn3368"; //$NON-NLS-1$
  private static File homeDir;

  private static final boolean isWindows;
  private static final boolean isMacOSX; 

  static {
    // set the OS flags
    final String os = System.getProperty("os.name").toLowerCase();
    isWindows =  os.startsWith("windows");
    isMacOSX = os.startsWith("mac os x");
  }

  /** This class should not be instantiated */
  private Info() { }

  /**
   * A valid version format is "w.x.[y|bz]", where 'w','x','y', and 'z' are
   * integers. In the version number, w.x are the major/minor release number,
   * y is the bug-fix release number, and the 'b' indicates a beta release,
   * e.g. 3.0b2.
   * 
   * @return the version of the VASSAL engine.
   */
  public static String getVersion() {
    return VERSION;
  }

  /**
   * The major/minor portion of the release version. If the version is a
   * beta-release number, a 'beta' is appended. For example, the minor
   * version of 3.0.2 is 3.0, and the minor version of 3.0b3 is 3.0beta.
   * 
   * @return
   */
  public static String getMinorVersion() {
// FIXME: check where this is used. maybe we can deprecate?
    final VersionTokenizer tok = new VersionTokenizer(VERSION);
    try {
      return Integer.toString(tok.next()) + "." +
             Integer.toString(tok.next());
    }
    catch (VersionFormatException e) {
      return null;
    }
  }

  /**
   * Get size of screen accounting for the screen insets (i.e. Windows taskbar)
   * 
   * @return
   */
  public static Rectangle getScreenBounds(Component c) {
    Rectangle bounds = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
    GraphicsConfiguration config = c.getGraphicsConfiguration();
    Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(config);
    bounds.translate(insets.left, insets.top);
    bounds.setSize(bounds.width - insets.left - insets.right,
                   bounds.height - insets.top - insets.bottom);
    return bounds;
  }

  public static boolean isMacOSX() {
    return isMacOSX;
  }

  /** Use {@link isMacOSX()} instead. */
  @Deprecated
  public static boolean isMacOsX() {
    return isMacOSX();
  }
  
  public static boolean isWindows() {
    return isWindows;
  }

  /**
   * Compares VASSAL version strings.
   *
   * @return negative if {@code v0 > v1}, positive if {@code v0 < v1}, and
   * zero if {@code v0 == v1} or if the ordering cannot be determined from
   * the parseable parts of the two <code>String</code>s.
   */
  public static int compareVersions(String v0, String v1) {
    VersionTokenizer tok0 = new VersionTokenizer(v0);
    VersionTokenizer tok1 = new VersionTokenizer(v1);

    try {
      // find the first token where v0 and v1 differ
      while (tok0.hasNext() && tok1.hasNext()) {
        final int n0 = tok0.next();
        final int n1 = tok1.next();
      
        if (n0 != n1) return n0 - n1;
      }    
    }
    catch (VersionFormatException e) {
      System.err.println("Invalid version format: " +  //$NON-NLS-1$
                         v0 + ", " + v1);              //$NON-NLS-2$
      return 0;
    }

    // otherwise, the shorter one is earlier; or they're the same
    return tok0.hasNext() ? -1 : (tok1.hasNext() ? 1 : 0);
  }

  /**
   * Returns the directory where VASSAL is installed.
   *
   * @return a {@link File} representing the directory
   */
  public static File getBaseDir() {
    return new File(System.getProperty("user.dir"));
  }

  /**
   * Returns the directory where the VASSAL documentation is installed.
   * 
   * @return a {@link File} representing the directory
   */
  public static File getDocsDir() {
    final String d = isMacOSX ? "Contents/Resources/doc" : "doc";
    return new File(getBaseDir(), d);
  }

// FIXME: we should have something like
// getAppDir(), getDocDir(), getConfDir(), getTmpDir()

  public static File getAppDir() {
    return new File(System.getProperty("user.dir"));
  }

  public static File getDocDir() {
    final String d = isMacOSX ? "Contents/Resources/doc" : "doc";
    return new File(getAppDir(), d);
  }

  public static File getConfDir() {
    return getHomeDir();
  }

  public static File getTempDir() {
    return new File(getHomeDir(), "tmp");
  }

// FIXME: this is a misleading name for this function
  public static File getHomeDir() {
    if (homeDir == null) {
      homeDir = new File(System.getProperty("user.home"), "VASSAL"); //$NON-NLS-1$ //$NON-NLS-2$
      if (!homeDir.exists()) {
        homeDir.mkdir();
      }
      else if (!homeDir.isDirectory()) {
// FIXME: Is this a good idea?!!
        homeDir.delete();
        homeDir.mkdir();
      }
    }
    return homeDir;
  }

  /**
   * @return true if this platform supports Swing Drag and Drop
   * @deprecated Check is no longer necessary since Java 1.4+ is required.
   */
  @Deprecated
  public static boolean isDndEnabled() {
    return true;
  }

  /**
   * @deprecated since Java 1.4 is now required
   * @return true if this platform supports Java2D
   */
  @Deprecated
  public static boolean is2dEnabled() {
    return true;
  }
}

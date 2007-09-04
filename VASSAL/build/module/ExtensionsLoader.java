/*
 * $Id$
 *
 * Copyright (c) 2000-2003 by Rodney Kinney
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
package VASSAL.build.module;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipException;

import VASSAL.build.GameModule;
import VASSAL.build.IllegalBuildException;
import VASSAL.command.Command;
import VASSAL.command.CommandEncoder;
import VASSAL.configure.DirectoryConfigurer;
import VASSAL.i18n.Resources;
import VASSAL.tools.DataArchive;
import VASSAL.tools.SequenceEncoder;

public class ExtensionsLoader implements CommandEncoder, FilenameFilter {
  // Preferences key for the list of extensions to load
  private static final String SPECIFY_DIR_IN_PREFS = "specifyExtensionDirInPrefs"; //$NON-NLS-1$
  private static final String EXTENSION_DIR = "extensionDIR"; //$NON-NLS-1$
  public static final String COMMAND_PREFIX = "EXT\t"; //$NON-NLS-1$

  private Set<String> loadedExtensions = new HashSet<String>();

  public void addTo(GameModule mod) {
    if ("true".equals(GlobalOptions.getInstance().getAttributeValueString(SPECIFY_DIR_IN_PREFS))) { //$NON-NLS-1$
      DirectoryConfigurer config = new DirectoryConfigurer(EXTENSION_DIR, Resources.getString("ExtensionsLoader.extensions_directory")); //$NON-NLS-1$
      config.setValue((Object) null);
      GameModule.getGameModule().getPrefs().addOption(Resources.getString("ExtensionsLoader.extensions_tab"), config); //$NON-NLS-1$
      config.addPropertyChangeListener(new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
          addExtensions();
        }
      });
    }
    mod.addCommandEncoder(this);
    addExtensions();
  }

  protected void addExtensions() {
    for (String extname : getExtensionNames()) {
      if (!loadedExtensions.contains(extname)) {
        try {
          ModuleExtension ext =
            new ModuleExtension(new DataArchive(extname));
          ext.build();
          String msg = Resources.getString("ExtensionsLoader.extension_loaded", ext.getName(), ext.getVersion()); //$NON-NLS-1$
          loadedExtensions.add(extname);
          GameModule.getGameModule().warn(msg);
          System.err.println(msg);
        }
        catch (ZipException e) {
          // Not a zip file.  Ignore
        }
        catch (IOException e) {
          reportBuildError(e, extname);
        }
        catch (IllegalBuildException e) {
          reportBuildError(e, extname);
        }
      }
    }
  }

  private void reportBuildError(Exception e, String name) {
    String msg = e.getMessage();
    if (msg == null || msg.length() == 0) {
      msg = e.getClass().getName();
      msg = msg.substring(msg.lastIndexOf('.'));
    }
    GameModule.getGameModule().warn(Resources.getString("ExtensionsLoader.unable_to_load", name , msg)); //$NON-NLS-1$
  }

  public Command decode(String command) {
    Command c = null;
    if (command.startsWith(COMMAND_PREFIX)) {
      SequenceEncoder.Decoder st = new SequenceEncoder.Decoder(command.substring(COMMAND_PREFIX.length()), '\t');
      c = new ModuleExtension.RegCmd(st.nextToken(), st.nextToken());
    }
    return c;
  }

  public String encode(Command c) {
    String s = null;
    if (c instanceof ModuleExtension.RegCmd) {
      ModuleExtension.RegCmd cmd = (ModuleExtension.RegCmd) c;
      SequenceEncoder se = new SequenceEncoder('\t');
      se.append(cmd.getName()).append(cmd.getVersion());
      s = COMMAND_PREFIX + se.getValue();
    }
    return s;
  }

  /**
   * Tests if the specified file should be accepted as an module extension file.
   * Currently we disallow any files that are hidden or "files" that are directories.
   *
   * @param   dir    the directory in which the file was found.
   * @param   name   the name of the file.
   * @return  <code>true</code> if and only if the name should be
   * included in the file list; <code>false</code> otherwise.
   */
  public boolean accept(File dir, String name) {
	  File fileCandidate = new File(dir, name);
	  
	  return !fileCandidate.isHidden() && !fileCandidate.isDirectory();
  }
  
  private String[] getExtensionNames() {
    String extensionDirectoryPath = getExtensionDirectory();
    File dir = new File(extensionDirectoryPath);
    
    File[] extensionFiles = dir.listFiles(this);
    if (extensionFiles == null) {
    	extensionFiles = new File[0];
    }
    
    String[] extensionFilenames = new String[extensionFiles.length];
    
    for (int i = 0; i < extensionFiles.length; i++) {
    	extensionFilenames[i] = extensionFiles[i].getPath();
    }
    return extensionFilenames;
  }

  public static String getExtensionDirectory() {
    String dirName;
    if ("true".equals(GlobalOptions.getInstance().getAttributeValueString(SPECIFY_DIR_IN_PREFS))) { //$NON-NLS-1$
      dirName = GameModule.getGameModule().getPrefs().getOption(EXTENSION_DIR).getValueString();
    }
    else {
      dirName = new File(GameModule.getGameModule().getDataArchive().getName()).getPath();
      int index = dirName.lastIndexOf('.');
      if (index > 0) {
        dirName = dirName.substring(0, index);
      }
      dirName = dirName + "_ext"; //$NON-NLS-1$
    }
    return dirName;
  }
}

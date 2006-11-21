
package VASSAL.tools;

import java.io.File;

import VASSAL.tools.FileFilter;

public class DirectoryFileFilter extends FileFilter {
   public boolean accept(File f) {
      return f.isDirectory();
   }

   public String getDescription() {
      return "Directories";
   }
}

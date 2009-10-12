package VASSAL.tools.nio.file.winfs;

import java.io.File;
import java.io.IOException;

import VASSAL.tools.nio.file.fs.RealPath;
import VASSAL.tools.nio.file.fs.RealFileSystem;

class WindowsPath extends RealPath {
  public WindowsPath(String path, RealFileSystem fs) {
    super(path, fs);
  }
  
  public WindowsPath(File file, WindowsFileSystem fs) {
    super(file, fs);
  }

  protected int findRootSep(String s) {
// FIXME: check that "C:" works 
    // C:\ is the root for a drive; \\ is the root for a network share.
    if (s.matches("^[a-zA-Z]:\\\\")) return 2; // In regexes, 4 '\' to match a single \
    else if (s.matches("^\\\\\\\\")) return 1; // as above: 8 '\' = \\
    else return -1;
  }
}

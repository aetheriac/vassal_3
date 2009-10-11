package VASSAL.tools.nio.file.fs;

import java.io.File;
import java.io.IOException;

class WindowsPath extends RealPath {
  public WindowsPath(String path, WindowsFileSystem fs) {
    super(path, fs);
  }
  
  public WindowsPath(File file, WindowsFileSystem fs) {
    super(file, fs);
  }

  protected int findRootSep(String s) {
// FIXME: check that "C:" works 
    // C:\ is the root for a drive; \\ is the root for a network share.
    if (s.matches("^[A-Za-z]:\\")) return 2;
    else if (s.matches("^\\\\")) return 1;
    else return -1;
  }
}

package VASSAL.tools.nio.file.fs;

import java.io.File;

public class RealWindowsPath extends RealPath {
  public RealWindowsPath(String path, RealFileSystem fs) {
    super(path, fs);
  }

  public RealWindowsPath(File file, RealFileSystem fs) {
    super(file, fs);
  }
 
  /** {@inheritDoc} */
  protected int findRootSep(String s) {
// FIXME: check that "C:" works
    // C:\ is the root for a drive; \\ is the root for a network share.
    if (s.matches("^[a-zA-Z]:\\\\")) return 2;
    else if (s.matches("^\\\\\\\\")) return 1;
    else return -1;
  }
}

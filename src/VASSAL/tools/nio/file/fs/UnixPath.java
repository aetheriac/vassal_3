package VASSAL.tools.nio.file.fs;

import java.io.File;
import java.io.IOException;

class UnixPath extends RealPath {
  public UnixPath(String path, UnixFileSystem fs) {
    super(path, fs);
  }
  
  public UnixPath(File file, UnixFileSystem fs) {
    super(file, fs);
  }

  protected int findRootSep(String s) {
    return s.startsWith("/") ? 0 : -1;
  }
}

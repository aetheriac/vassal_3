package VASSAL.tools.nio.file.fs;

import java.io.File;

public class RealUnixPath extends RealPath {
  public RealUnixPath(String path, RealFileSystem fs) {
    super(path, fs);
  }

  public RealUnixPath(File file, RealFileSystem fs) {
    super(file, fs);
  }
 
  /** {@inheritDoc} */
  protected int findRootSep(String s) {
    return s.startsWith("/") ? 0 : -1;
  }
}

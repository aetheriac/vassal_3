package VASSAL.tools.nio.file.realfs;

public class RealUnixPath extends RealPath {
  public RealUnixPath(String path, RealFileSystem fs) {
    super(path, fs);
  }

  /** {@inheritDoc} */
  protected int findRootSep(String s) {
    return s.startsWith("/") ? 0 : -1;
  }
}

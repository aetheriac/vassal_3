package VASSAL.tools.nio.file;

import java.io.IOException;

public abstract class PathMoveToExtIntTest extends PathMoveToTest {
  public PathMoveToExtIntTest(FileSystem fs, String src, String dst,
                              CopyOption[] opts, Object expected) {
    super(fs, src, dst, opts, expected);
  }

  protected Path getSrc() throws IOException {
    return Paths.get(src);
  }

  protected Path getDst() throws IOException {
    return fs.getPath(dst);
  }
}

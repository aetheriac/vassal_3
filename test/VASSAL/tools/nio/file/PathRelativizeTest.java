package VASSAL.tools.nio.file;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public abstract class PathRelativizeTest
                                       extends AbstractThrowingPathMethodTest {
  protected final String left;
  protected final String right;
  protected final String expected;

  public PathRelativizeTest(FileSystem fs, String left,
                            String right, String expected,
                            Class<? extends Throwable> tclass) {
    super(fs, tclass);

    this.left = left;
    this.right = right;
    this.expected = expected;
  }

  protected void doTest() {
    final Path path = fs.getPath(left).relativize(fs.getPath(right));
    final String result = path == null ? null : path.toString();
    assertEquals(expected, result);
  }
}

package VASSAL.tools.nio.file;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public abstract class PathStartsWithTest extends AbstractPathMethodTest {
  protected final String left;
  protected final String right;
  protected final boolean expected;

  public PathStartsWithTest(FileSystem fs, String left,
                            String right, boolean expected) {
    super(fs);

    this.left = left;
    this.right = right;
    this.expected = expected;
  }

  protected void doTest() {
    assertEquals(expected, fs.getPath(left).startsWith(fs.getPath(right)));
  }
}

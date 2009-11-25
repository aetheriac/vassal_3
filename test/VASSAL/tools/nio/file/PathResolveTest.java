package VASSAL.tools.nio.file;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public abstract class PathResolveTest extends AbstractPathMethodTest {
  protected final String left;
  protected final String right;
  protected final String expected;

  public PathResolveTest(FileSystem fs, String left,
                         String right, String expected) {
    super(fs);

    this.left = left;
    this.right = right;
    this.expected = expected;
  }

  protected void doTest() {
    assertEquals(
      expected,
      fs.getPath(left).resolve(fs.getPath(right)).toString()
    );
  }
}

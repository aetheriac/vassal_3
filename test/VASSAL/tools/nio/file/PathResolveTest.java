package VASSAL.tools.nio.file;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public abstract class PathResolveTest {
  protected final FileSystem fs;
  protected final String left;
  protected final String right;
  protected final String expected;

  public PathResolveTest(FileSystem fs, String left,
                         String right, String expected) {
    this.fs = fs;
    this.left = left;
    this.right = right;
    this.expected = expected;
  }

  @Test
  public void testResolve() {
    assertEquals(
      expected,
      fs.getPath(left).resolve(fs.getPath(right)).toString()
    );
  }
}

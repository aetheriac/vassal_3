package VASSAL.tools.nio.file;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public abstract class PathRelativizeTest {
  protected final FileSystem fs;
  protected final String left;
  protected final String right;
  protected final String expected;

  public PathRelativizeTest(FileSystem fs, String left,
                            String right, String expected) {
    this.fs = fs;
    this.left = left;
    this.right = right;
    this.expected = expected;
  }

  @Test
  public void testRelativize() {
    assertEquals(
      expected,
      fs.getPath(left).relativize(fs.getPath(right)).toString()
    );
  }
}

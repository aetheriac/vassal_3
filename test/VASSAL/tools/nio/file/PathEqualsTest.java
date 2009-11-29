package VASSAL.tools.nio.file;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public abstract class PathEqualsTest extends AbstractPathMethodTest {
  protected final String left;
  protected final Object right;

  public PathEqualsTest(FileSystem fs, String left,
                        Object right, Object expected) {
    super(fs, expected);

    this.left = left;
    this.right = right;
  }

  protected void doTest() {
    final Object obj =
      right instanceof String ? fs.getPath((String) right) : right;
    assertEquals(expected, fs.getPath(left).equals(obj));
  }
}

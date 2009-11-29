package VASSAL.tools.nio.file;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public abstract class PathGetParentTest extends AbstractPathMethodTest {
  protected final String input;
  protected final String expected;

  public PathGetParentTest(FileSystem fs, String input, String expected) {
    super(fs);

    this.input = input;
    this.expected = expected;
  }

  protected void doTest() {
    final Path path = fs.getPath(input).getParent();
    final String result = path == null ? null : path.toString();
    assertEquals(expected, result);
  }
}

package VASSAL.tools.nio.file;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public abstract class PathNormalizeTest extends AbstractPathMethodTest {
  protected final String input;
  protected final String expected;

  public PathNormalizeTest(FileSystem fs, String input, String expected) {
    super(fs);

    this.input = input;
    this.expected = expected;
  }

  protected void doTest() {
    final Path path = fs.getPath(input).normalize();
    final String result = path == null ? null : path.toString();
    assertEquals(expected, result);
  }
}

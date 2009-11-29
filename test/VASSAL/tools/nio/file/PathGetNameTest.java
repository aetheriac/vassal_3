package VASSAL.tools.nio.file;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public abstract class PathGetNameTest extends AbstractPathMethodTest {
  protected final String input;

  public PathGetNameTest(FileSystem fs, String input, Object expected) {
    super(fs, expected);

    this.input = input;
  }

  protected void doTest() {
    final Path path = fs.getPath(input).getName();
    final String result = path == null ? null : path.toString();
    assertEquals(expected, result);
  }
}

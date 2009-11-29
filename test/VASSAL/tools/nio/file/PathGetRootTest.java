package VASSAL.tools.nio.file;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public abstract class PathGetRootTest extends AbstractPathMethodTest {
  protected final String input;
  protected final String expected;

  public PathGetRootTest(FileSystem fs, String input, String expected) {
    super(fs);

    this.input = input;
    this.expected = expected;
  }

  protected void doTest() {
    final Path path = fs.getPath(input).getRoot();
    final String result = path == null ? null : path.toString();
    assertEquals(expected, result);
  }
}

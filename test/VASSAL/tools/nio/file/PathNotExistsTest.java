package VASSAL.tools.nio.file;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public abstract class PathNotExistsTest extends AbstractPathMethodTest {
  protected final String input;

  public PathNotExistsTest(FileSystem fs, String input, Object expected) {
    super(fs, expected);

    this.input = input;
  }

  protected void doTest() {
    assertEquals(expected, fs.getPath(input).notExists());
  }
}

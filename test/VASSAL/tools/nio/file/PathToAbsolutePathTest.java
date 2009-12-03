package VASSAL.tools.nio.file;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public abstract class PathToAbsolutePathTest extends AbstractPathMethodTest {
  protected final String input;

  public PathToAbsolutePathTest(FileSystem fs, String input, Object expected) {
    super(fs, expected);

    this.input = input;
  }

  protected void doTest() {
    assertEquals(expected, fs.getPath(input).toAbsolutePath().toString());
  }
}

package VASSAL.tools.nio.file;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public abstract class PathIsAbsoluteTest extends AbstractPathMethodTest {
  protected final String input;
  protected final boolean expected;

  public PathIsAbsoluteTest(FileSystem fs, String input, boolean expected) {
    super(fs);

    this.input = input;
    this.expected = expected;
  }

  protected void doTest() {
    assertEquals(expected, fs.getPath(input).isAbsolute());
  }
}

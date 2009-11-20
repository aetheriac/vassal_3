package VASSAL.tools.nio.file;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public abstract class PathIsAbsoluteTest {
  protected final FileSystem fs;
  protected final String input;
  protected final boolean expected;

  public PathIsAbsoluteTest(FileSystem fs, String input, boolean expected) {
    this.fs = fs;
    this.input = input;
    this.expected = expected;
  }

  @Test
  public void testIsAbsolute() {
    assertEquals(expected, fs.getPath(input).isAbsolute());
  }
}

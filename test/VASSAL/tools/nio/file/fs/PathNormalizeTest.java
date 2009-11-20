package VASSAL.tools.nio.file;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public abstract class PathNormalizeTest {
  protected final FileSystem fs;
  protected final String input;
  protected final String expected;

  public PathNormalizeTest(FileSystem fs, String input, String expected) {
    this.fs = fs;
    this.input = input;
    this.expected = expected;
  }

  @Test
  public void testNormalize() {
    assertEquals(expected, fs.getPath(input).normalize().toString());
  }
}

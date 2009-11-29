package VASSAL.tools.nio.file;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public abstract class PathSubpathTest extends AbstractPathMethodTest {
  protected final String input;
  protected final int begin;
  protected final int end;

  public PathSubpathTest(FileSystem fs, String input,
                         int begin, int end, Object expected) {
    super(fs, expected);

    this.input = input;
    this.begin = begin;
    this.end = end;
  }

  protected void doTest() {
    assertEquals(expected, fs.getPath(input).subpath(begin, end).toString());
  }
}

package VASSAL.tools.nio.file;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public abstract class PathSubpathTest extends AbstractThrowingPathMethodTest {
  protected final String input;
  protected final int begin;
  protected final int end;
  protected final String expected;

  public PathSubpathTest(FileSystem fs, String input,
                         int begin, int end, String expected,
                         Class<? extends Throwable> tclass) {
    super(fs, tclass);

    this.input = input;
    this.begin = begin;
    this.end = end;
    this.expected = expected;
  }

  protected void doTest() {
    assertEquals(expected, fs.getPath(input).subpath(begin, end).toString());
  }
}

package VASSAL.tools.nio.file;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public abstract class PathGetNameIntTest
                                       extends AbstractThrowingPathMethodTest {
  protected final String input;
  protected final int index;
  protected final String expected;

  public PathGetNameIntTest(
    FileSystem fs, String input, int index,
    String expected, Class<? extends Throwable> tclass)
  {
    super(fs, tclass);

    this.input = input;
    this.index = index;
    this.expected = expected;
  }

  protected void doTest() {
    assertEquals(expected, fs.getPath(input).getName(index).toString());
  }
}

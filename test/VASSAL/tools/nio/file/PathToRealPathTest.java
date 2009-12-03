package VASSAL.tools.nio.file;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public abstract class PathToRealPathTest extends AbstractPathMethodTest {
  protected final String input;
  protected final boolean resLinks;

  public PathToRealPathTest(FileSystem fs, String input,
                            boolean resLinks, Object expected) {
    super(fs, expected);

    this.input = input;
    this.resLinks = resLinks;
  }

  protected void doTest() throws IOException {
    assertEquals(expected, fs.getPath(input).toRealPath(resLinks).toString());
  }
}

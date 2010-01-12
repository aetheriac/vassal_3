package VASSAL.tools.nio.file;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public abstract class PathCreateFileTest extends AbstractPathMethodTest {
  protected final String input;

  public PathCreateFileTest(FileSystem fs, String input, Object expected) {
    super(fs, expected);

    this.input = input;
  }

  protected void doTest() throws IOException {
    final Path path = fs.getPath(input);
    path.createFile();
    assertEquals(true, path.getAttribute("isRegularFile"));
  }
}

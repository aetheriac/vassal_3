package VASSAL.tools.nio.file;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public abstract class PathDeleteTest extends AbstractPathMethodTest {
  protected final String input;

  public PathDeleteTest(FileSystem fs, String input, Object expected) {
    super(fs, expected);

    this.input = input;
  }

  protected void doTest() throws IOException {
    final Path path = fs.getPath(input);
    path.delete();
    assertEquals(false, path.exists());
  }
}

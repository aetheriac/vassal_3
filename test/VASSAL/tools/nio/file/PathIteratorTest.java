package VASSAL.tools.nio.file;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public abstract class PathIteratorTest extends AbstractPathMethodTest {
  protected final String input;

  public PathIteratorTest(FileSystem fs, String input, Object expected) {
    super(fs, expected);

    this.input = input;
  }

  protected void doTest() {
    final Path path = fs.getPath(input);
    final String[] parts = (String[]) expected;

    int i = 0;
    for (Path part : path) assertEquals(parts[i++], part.toString());
  }
}

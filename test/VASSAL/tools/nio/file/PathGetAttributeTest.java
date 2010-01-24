package VASSAL.tools.nio.file;

import java.io.IOException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public abstract class PathGetAttributeTest extends AbstractPathMethodTest {
  protected final String input;
  protected final String attrib;

  public PathGetAttributeTest(FSHandler fac, String input,
                              String attrib, Object expected) {
    super(fac, expected);

    this.input = input;
    this.attrib = attrib;
  }

  protected void doTest() throws IOException {
    final Object result = fs.getPath(input).getAttribute(attrib);
    
    if (expected instanceof byte[]) {
      assertArrayEquals((byte[]) expected, (byte[]) result);
    }
    else {
      assertEquals(expected, result);
    }
  }
}

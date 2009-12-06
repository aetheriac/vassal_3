package VASSAL.tools.nio.file;

import java.io.IOException;
import java.io.InputStream;

import VASSAL.tools.io.IOUtils;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

public abstract class PathNewInputStreamTest extends AbstractPathMethodTest {
  protected final String input;

  public PathNewInputStreamTest(FileSystem fs, String input, Object expected) {
    super(fs, expected);

    this.input = input;
  }

  protected void doTest() throws IOException {
    byte[] actualBytes = null;
    byte[] expectedBytes = null;

    InputStream in = null;
    try {
      in = fs.getPath(input).newInputStream();
      actualBytes = IOUtils.toByteArray(in); 
      in.close();
    }
    finally {
      IOUtils.closeQuietly(in);
    }

    in = null;
    try {
      in = Paths.get((String) expected).newInputStream();
      expectedBytes = IOUtils.toByteArray(in); 
      in.close();
    }
    finally {
      IOUtils.closeQuietly(in);
    }
 
    assertArrayEquals(expectedBytes, actualBytes);
  }
}

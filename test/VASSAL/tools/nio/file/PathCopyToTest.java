package VASSAL.tools.nio.file;

import java.io.IOException;

import VASSAL.tools.io.FileUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public abstract class PathCopyToTest extends AbstractPathMethodTest {
  protected final String src;
  protected final String dst;
  protected final CopyOption[] opts;

  public PathCopyToTest(FSHandler fac, String src, String dst,
                        CopyOption[] opts, Object expected) {
    super(fac, expected);

    this.src = src;
    this.dst = dst;
    this.opts = opts == null ? new CopyOption[0] : opts;
  }

  protected abstract Path getSrc() throws IOException;
  protected abstract Path getDst() throws IOException;

  protected void doTest() throws IOException {
    final Path sp = getSrc();
    final Path dp = getDst();    

    try {
      sp.copyTo(dp, opts);

      if (Boolean.TRUE.equals(sp.getAttribute("isDirectory"))) {
        assertTrue(dp.exists());
        assertEquals(Boolean.TRUE, dp.getAttribute("isDirectory")); 
      }
      else {
        assertTrue(FileUtils.contentEquals(sp, dp));
      }
    }
    finally {
      dp.deleteIfExists();
    }
  }
}

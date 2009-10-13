package VASSAL.tools.nio.file.fs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

import org.junit.Before;
import org.junit.Test;

import VASSAL.Info;

public class RealWindowsPathTest extends RealPathTest {

  @Before
  public void setUp() throws Exception {
    super.setUp();
    assumeTrue(Info.isWindows());
  }
  
  @Test
  public void testFindRootSep() {
  //  assumeTrue(Info.isWindows());
    assertEquals(1, pathTestingDirectory.findRootSep("\\\\TestServer\\testDir"));
    assertEquals(2, pathTestingDirectory.findRootSep("D:\\TestDir\\TestDir2"));
    assertEquals(-1, pathTestingDirectory.findRootSep("somethingelse"));
  }

  @Test
  public void testRealWindowsPathStringRealFileSystem() {
 //   assumeTrue(Info.isWindows());
    RealPath p1 = new RealWindowsPath(testFileCreated.getPath(), fs);
    assertEquals(p1.toString(), testFileCreated.toString());
  }

}

package VASSAL.tools.nio.file.fs;

import static org.junit.Assert.*;
import static org.junit.Assume.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import VASSAL.Info;
import VASSAL.tools.nio.file.AccessMode;
import VASSAL.tools.nio.file.LinkOption;
import VASSAL.tools.nio.file.NoSuchFileException;
import VASSAL.tools.nio.file.Path;
import VASSAL.tools.nio.file.Paths;
import VASSAL.tools.nio.file.attribute.BasicFileAttributeView;
import VASSAL.tools.nio.file.attribute.FileAttributeView;

public class RealUnixPathTest extends RealPathTest {


  @Before
  public void setUp() throws Exception {
    super.setUp();
    assumeTrue(!Info.isWindows());
  }


  @Test
  public void testFindRootSep() {
    assertEquals(0, pathTestingDirectory.findRootSep("/TestServer/testDir"));
    assertEquals(-1, pathTestingDirectory.findRootSep("somethingelse"));
  }

  @Test
  public void testUnixPathStringUnixFileSystem() {
    RealPath p1 = new RealUnixPath(testFileCreated.getPath(), fs);
    assertEquals(p1.toString(), testFileCreated.toString());
  }

}

package VASSAL.tools.nio.file.zipfs;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import static org.junit.Assert.*;

import VASSAL.tools.nio.file.FileSystems;
import VASSAL.tools.nio.file.Path;
import VASSAL.tools.nio.file.Paths;
import VASSAL.tools.nio.file.PathExistsTest;
import VASSAL.tools.nio.file.PathNotExistsTest;
import VASSAL.tools.nio.file.PathToAbsolutePathTest;
import VASSAL.tools.nio.file.PathToRealPathTest;
//import VASSAL.tools.nio.file.PathToUriTest;

import static VASSAL.tools.nio.file.AbstractPathMethodTest.t;

@RunWith(Suite.class)
@SuiteClasses({
  ZipFilePathReadTest.ExistsTest.class,
  ZipFilePathReadTest.NotExistsTest.class,
  ZipFilePathReadTest.ToAbsolutePathTest.class,
  ZipFilePathReadTest.ToRealPathTest.class
//  ZipFilePathReadTest.ToUriTest.class
})
public class ZipFilePathReadTest {

  protected static ZipFileSystem fs;

  protected static final String zfName = "test.zip";
  protected static final String zfPathName =
    "test/VASSAL/tools/nio/file/zipfs/".replace("/", File.separator) + zfName;

  protected static Path zfPath;
  protected static URI zfURI;

  @BeforeClass
  public static void setupFS() throws IOException {
    zfPath = Paths.get(zfPathName).toAbsolutePath();
    zfURI = URI.create("zip://" + zfPath.toString());

    fs = (ZipFileSystem) FileSystems.newFileSystem(zfURI, null);
  }

  @RunWith(Parameterized.class)
  public static class ExistsTest extends PathExistsTest{
    public ExistsTest(String input, Object expected) {
      super(ZipFilePathReadTest.fs, input, expected);
    }

    @Parameters
    public static List<Object[]> cases() {
      return Arrays.asList(new Object[][] {
        // Input           Expected
        { "/fileInZip",    true  },
        { "/fileNotInZip", false },
        { "fileInZip",     true  },
        { "fileNotInZip",  false }
      });
    }
  }

  @RunWith(Parameterized.class)
  public static class NotExistsTest extends PathNotExistsTest {
    public NotExistsTest(String input, Object expected) {
      super(ZipFilePathReadTest.fs, input, expected);
    }

    @Parameters
    public static List<Object[]> cases() {
      return Arrays.asList(new Object[][] {
        // Input           Expected
        { "/fileInZip",    false },
        { "/fileNotInZip", true  },
        { "fileInZip",     false },
        { "fileNotInZip",  true  }
      });
    }
  }

  @RunWith(Parameterized.class)
  public static class ToAbsolutePathTest extends PathToAbsolutePathTest{
    public ToAbsolutePathTest(String input, Object expected) {
      super(ZipFilePathReadTest.fs, input, expected);
    }

    @Parameters
    public static List<Object[]> cases() {
      return Arrays.asList(new Object[][] {
        // Input        Expected
        { "/fileInZip", "/fileInZip" },
        { "fileInZip",  "/fileInZip" }
      });
    }
  }

  @RunWith(Parameterized.class)
  public static class ToRealPathTest extends PathToRealPathTest{
    public ToRealPathTest(String input, boolean resLinks, Object expected) {
      super(ZipFilePathReadTest.fs, input, resLinks, expected);
    }

    @Parameters
    public static List<Object[]> cases() {
      return Arrays.asList(new Object[][] {
        // Input                    Resolve?  Expected
        { "/dirInZip/../fileInZip", true,     "/fileInZip"         },
        { "/dirInZip/../fileInZip", false,    "/fileInZip"         },
        { "fileInZip",              true,     "/fileInZip"         },
        { "fileInZip",              false,    "/fileInZip"         },
        { "fileNotInZip",           true,     t(IOException.class) },
        { "fileNotInZip",           false,    t(IOException.class) }
      });
    }
  }

/*
  @RunWith(Parameterized.class)
  public static class ToUriTest extends PathToUriTest{
    public ToUriTest(String input, Object expected) {
      super(ZipFilePathReadTest.fs, input, expected);
    }

    @Parameters
    public static List<Object[]> cases() {
      return Arrays.asList(new Object[][] {
        // Input        Expected
        { "/fileInZip", "zip://uri.resolve("#/fileInZip") },
        { "fileInZip",  uri.resolve("#/fileInZip") }
      });
    }
  }
*/

  @Test
  public void testGetFileSystem() {
    assertEquals(fs, fs.getPath("/fileInZip").getFileSystem());
  }

  @Test
  public void testIsHidden() {
    assertFalse(fs.getPath("/fileInZip").isHidden());
  }
}

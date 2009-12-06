package VASSAL.tools.nio.file.zipfs;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import static org.junit.Assert.*;

import VASSAL.tools.nio.file.FileSystems;
import VASSAL.tools.nio.file.NoSuchFileException;
import VASSAL.tools.nio.file.Path;
import VASSAL.tools.nio.file.Paths;
import VASSAL.tools.nio.file.PathExistsTest;
import VASSAL.tools.nio.file.PathGetAttributeTest;
import VASSAL.tools.nio.file.PathNotExistsTest;
import VASSAL.tools.nio.file.PathToAbsolutePathTest;
import VASSAL.tools.nio.file.PathToRealPathTest;
//import VASSAL.tools.nio.file.PathToUriTest;
import VASSAL.tools.nio.file.attribute.FileTime;

import static VASSAL.tools.nio.file.AbstractPathMethodTest.t;

@RunWith(Suite.class)
@SuiteClasses({
  ZipFilePathReadTest.ExistsTest.class,
  ZipFilePathReadTest.GetAttributeTest.class,
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
  public static class GetAttributeTest extends PathGetAttributeTest {
    public GetAttributeTest(String path, String attrib, Object expected) {
      super(ZipFilePathReadTest.fs, path, attrib, expected);
    }

    @Parameters
    public static List<Object[]> cases() {
      return Arrays.asList(new Object[][] {
        // Path            Attribute     Expected
        { "/fileNotInZip", "basic:size", t(NoSuchFileException.class) },
        { "/fileInZip", null,            t(NullPointerException.class) },
        { "/fileInZip", "whatever",      null                          },
        { "/fileInZip", "basic:lastModifiedTime", FileTime.from(1259794214L, TimeUnit.SECONDS) },
        { "/fileInZip", "basic:lastAccessTime",   FileTime.from(0, TimeUnit.SECONDS) },
        { "/fileInZip", "basic:creationTime",     FileTime.from(0, TimeUnit.SECONDS) },
        { "/fileInZip", "basic:size",           0L },
        { "/fileInZip", "basic:isRegularFile",  true  },
        { "/fileInZip", "basic:isDirectory",    false },
        { "/fileInZip", "basic:isSymbolicLink", false },
        { "/fileInZip", "basic:isOther",        false },
        { "/fileInZip", "basic:fileKey",        null  },
        { "/fileInZip", "zip:comment",          null  },
        { "/fileInZip", "zip:crc",              0L    },
        { "/fileInZip", "zip:extra",            null  },
        { "/fileInZip", "zip:method",           0     },
        { "/fileInZip", "zip:name",             "fileInZip".getBytes() },
        { "/fileInZip", "zip:isArchiveFile",    false },
        { "/fileInZip", "zip:versionMadeBy",    "UNIX" },
        { "/fileInZip", "zip:extAttrs",         0 },
        { "/dirInZip", null,                    t(NullPointerException.class) },
        { "/dirInZip", "whatever",              null },
        { "/dirInZip", "basic:lastModifiedTime", FileTime.from(1259917468L, TimeUnit.SECONDS) },
        { "/dirInZip", "basic:lastAccessTime", FileTime.from(-1L, TimeUnit.SECONDS) },
        { "/dirInZip", "basic:creationTime",   FileTime.from(-1L, TimeUnit.SECONDS) },
        { "/dirInZip", "basic:size",           0L },
        { "/dirInZip", "basic:isRegularFile",  false  },
        { "/dirInZip", "basic:isDirectory",    true },
        { "/dirInZip", "basic:isSymbolicLink", false },
        { "/dirInZip", "basic:isOther",        false },
        { "/dirInZip", "basic:fileKey",        null  },
        { "/dirInZip", "zip:comment",          null  },
        { "/dirInZip", "zip:crc",              0L    },
        { "/dirInZip", "zip:extra",            null  },
        { "/dirInZip", "zip:method",           0     },
        { "/dirInZip", "zip:name",             "dirInZip/".getBytes() },
        { "/dirInZip", "zip:isArchiveFile",    false },
        { "/dirInZip", "zip:versionMadeBy",    "UNIX" },
        { "/dirInZip", "zip:extAttrs",         0 }
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

  @Test
  public void testGetFileSystem() {
    assertEquals(fs, fs.getPath("/fileInZip").getFileSystem());
  }

  @Test
  public void testIsHidden() {
    assertFalse(fs.getPath("/fileInZip").isHidden());
  }

// FIXME: use zfURI somehow in parameterized case... need a pointer.
  @Test
  public void testToUri() {
    final URI expected = zfURI.resolve("#/fileInZip");
    assertEquals(expected, fs.getPath("/fileInZip").toUri());
  }
}

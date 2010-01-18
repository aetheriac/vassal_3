package VASSAL.tools.nio.file.zipfs;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import VASSAL.tools.nio.file.FileSystems;
import VASSAL.tools.nio.file.Path;
import VASSAL.tools.nio.file.Paths;
import VASSAL.tools.nio.file.PathCreateDirectoryTest;
import VASSAL.tools.nio.file.PathCreateFileTest;
import VASSAL.tools.nio.file.PathDeleteTest;
import VASSAL.tools.nio.file.PathDeleteIfExistsTest;
import VASSAL.tools.nio.file.ReadOnlyFileSystemException;
import VASSAL.tools.nio.file.StandardCopyOption;

import static VASSAL.tools.nio.file.AbstractPathMethodTest.t;

@RunWith(Suite.class)
@SuiteClasses({
  ZipFilePathWriteTest.CreateDirectoryTest.class,
  ZipFilePathWriteTest.CreateFileTest.class,
  ZipFilePathWriteTest.DeleteTest.class,
  ZipFilePathWriteTest.DeleteIfExistsTest.class
})
public class ZipFilePathWriteTest {

  protected static ZipFileSystem fs;

  protected static final String testDir =
    "test/VASSAL/tools/nio/file/zipfs/".replace("/", File.separator);

  protected static final String zfName = "write.zip";
  protected static final String zfPathName = testDir + zfName;

  protected static Path zfPath;

  @BeforeClass
  public static void setupFS() throws IOException {
    zfPath = Paths.get(testDir + "write.zip").toAbsolutePath();

    // work in a copy of test.zip
    final Path zfRead = Paths.get(testDir + "test.zip");
    zfRead.copyTo(Paths.get(testDir + "write.zip"),
                  StandardCopyOption.REPLACE_EXISTING);

    final URI zfURI = URI.create("zip://" + zfPath.toString());
    fs = (ZipFileSystem) FileSystems.newFileSystem(zfURI, null);
  }

  // FIXME: need to test with file attributes
  @RunWith(Parameterized.class)
  public static class CreateDirectoryTest extends PathCreateDirectoryTest {
    public CreateDirectoryTest(String input, Object expected) {
      super(ZipFilePathWriteTest.fs, input, expected);
    }

    @Parameters
    public static List<Object[]> cases() {
      return Arrays.asList(new Object[][] {
        // Input  Expected
        { "/foo", t(ReadOnlyFileSystemException.class) }
      });
    }
  }
  
  // FIXME: need to test with file attributes
  @RunWith(Parameterized.class)
  public static class CreateFileTest extends PathCreateFileTest {
    public CreateFileTest(String input, Object expected) {
      super(ZipFilePathWriteTest.fs, input, expected);
    }

    @Parameters
    public static List<Object[]> cases() {
      return Arrays.asList(new Object[][] {
        // Input  Expected
        { "/foo", t(ReadOnlyFileSystemException.class) }
      });
    }
  }

  @RunWith(Parameterized.class)
  public static class DeleteTest extends PathDeleteTest {
    public DeleteTest(String input, Object expected) {
      super(ZipFilePathWriteTest.fs, input, expected);
    }

    @Parameters
    public static List<Object[]> cases() {
      return Arrays.asList(new Object[][] {
        // Input  Expected
        { "/foo", t(ReadOnlyFileSystemException.class) }
      });
    }
  }

  @RunWith(Parameterized.class)
  public static class DeleteIfExistsTest extends PathDeleteIfExistsTest {
    public DeleteIfExistsTest(String input, Object expected) {
      super(ZipFilePathWriteTest.fs, input, expected);
    }

    @Parameters
    public static List<Object[]> cases() {
      return Arrays.asList(new Object[][] {
        // Input  Expected
        { "/foo", t(ReadOnlyFileSystemException.class) }
      });
    }
  }
}

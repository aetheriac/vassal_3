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

import VASSAL.tools.io.FileUtils;

import VASSAL.tools.nio.file.CopyOption;
import VASSAL.tools.nio.file.DirectoryNotEmptyException;
import VASSAL.tools.nio.file.FileAlreadyExistsException;
import VASSAL.tools.nio.file.FileSystems;
import VASSAL.tools.nio.file.NoSuchFileException;
import VASSAL.tools.nio.file.Path;
import VASSAL.tools.nio.file.Paths;
import VASSAL.tools.nio.file.PathCopyToExtIntTest;
import VASSAL.tools.nio.file.PathCopyToIntExtTest;
import VASSAL.tools.nio.file.PathCopyToIntIntTest;
import VASSAL.tools.nio.file.PathCreateDirectoryTest;
import VASSAL.tools.nio.file.PathCreateFileTest;
import VASSAL.tools.nio.file.PathDeleteTest;
import VASSAL.tools.nio.file.PathDeleteIfExistsTest;
import VASSAL.tools.nio.file.StandardCopyOption;

import static VASSAL.tools.nio.file.AbstractPathMethodTest.t;

import static VASSAL.tools.nio.file.StandardCopyOption.REPLACE_EXISTING;

@RunWith(Suite.class)
@SuiteClasses({
  RWZipFilePathWriteTest.CopyToExtIntTest.class,
  RWZipFilePathWriteTest.CopyToIntExtTest.class,
  RWZipFilePathWriteTest.CopyToIntIntTest.class,
  RWZipFilePathWriteTest.CreateDirectoryTest.class,
  RWZipFilePathWriteTest.CreateFileTest.class,
  RWZipFilePathWriteTest.DeleteTest.class,
  RWZipFilePathWriteTest.DeleteIfExistsTest.class
})
public class RWZipFilePathWriteTest {

  protected static ZipFileSystem fs;

  protected static final String thisDir =
    "test/VASSAL/tools/nio/file/zipfs/".replace("/", File.separator);

  protected static final String td =
    thisDir + ("test/".replace("/", File.separator));

  protected static final String zfName = "write.zip";
  protected static final String zfPathName = td + zfName;

  protected static Path zfPath;

  @BeforeClass
  public static void setupFS() throws IOException {
    zfPath = Paths.get(td + "write.zip").toAbsolutePath();

    // clear and create our test directory
    final Path tdPath = Paths.get(td);
    FileUtils.delete(tdPath);
    tdPath.createDirectory();
    tdPath.resolve("yea").createFile();

    // work in a copy of test.zip
    final Path zfRead = Paths.get(thisDir + "test.zip");
    zfRead.copyTo(Paths.get(td + "write.zip"), REPLACE_EXISTING);

    final URI zfURI = URI.create("zip://" + zfPath.toString());
    fs = (ZipFileSystem) FileSystems.newFileSystem(zfURI, null);
  }

  @RunWith(Parameterized.class)
  public static class CopyToIntIntTest extends PathCopyToIntIntTest {
    public CopyToIntIntTest(String src, String dst,
                            CopyOption[] opts, Object expected) {
      super(RWZipFilePathWriteTest.fs, src, dst, opts, expected);
    }

    @Parameters
    public static List<Object[]> cases() {
      return Arrays.asList(new Object[][] {
        // Source Destination Opts  Expected
/*
        { "foo",  null,       null, t(NullPointerException.class)             },
        { "bar",  td + "nay", null, t(NoSuchFileException.class)              },
        { "/bar", td + "nay", null, t(NoSuchFileException.class)              },
        { "foo",  td + "nay", null, null                                      },
        { "foo",  td + "yea", null, t(FileAlreadyExistsException.class)       },
        { "/foo", td + "nay", null, null                                      },
        { "/foo", td + "yea", null, t(FileAlreadyExistsException.class)       },
        { "foo",  td + "yea", new CopyOption[]{ REPLACE_EXISTING }, null      },
        { "/foo", td + "yea", new CopyOption[]{ REPLACE_EXISTING }, null      },
        { "dirInZip", td + "yea", null, t(FileAlreadyExistsException.class)   },
        { "dirInZip", td + "yea", new CopyOption[]{ REPLACE_EXISTING }, null  },
        { "/dirInZip", td + "yea", null, t(FileAlreadyExistsException.class)  },
        { "/dirInZip", td + "yea", new CopyOption[]{ REPLACE_EXISTING }, null }
*/
      });
    }
  }

  @RunWith(Parameterized.class)
  public static class CopyToIntExtTest extends PathCopyToIntExtTest {
    public CopyToIntExtTest(String src, String dst,
                            CopyOption[] opts, Object expected) {
      super(RWZipFilePathWriteTest.fs, src, dst, opts, expected);
    }

    @Parameters
    public static List<Object[]> cases() {
      return Arrays.asList(new Object[][] {
        // Source Destination Opts  Expected
        { "foo",  null,       null, t(NullPointerException.class)             },
        { "bar",  td + "nay", null, t(NoSuchFileException.class)              },
        { "/bar", td + "nay", null, t(NoSuchFileException.class)              },
        { "foo",  td + "nay", null, null                                      },
        { "foo",  td + "yea", null, t(FileAlreadyExistsException.class)       },
        { "/foo", td + "nay", null, null                                      },
        { "/foo", td + "yea", null, t(FileAlreadyExistsException.class)       },
        { "foo",  td + "yea", new CopyOption[]{ REPLACE_EXISTING }, null      },
        { "/foo", td + "yea", new CopyOption[]{ REPLACE_EXISTING }, null      },
        { "dirInZip", td + "yea", null, t(FileAlreadyExistsException.class)   },
        { "dirInZip", td + "yea", new CopyOption[]{ REPLACE_EXISTING }, null  },
        { "/dirInZip", td + "yea", null, t(FileAlreadyExistsException.class)  },
        { "/dirInZip", td + "yea", new CopyOption[]{ REPLACE_EXISTING }, null }
      });
    }
  }

  @RunWith(Parameterized.class)
  public static class CopyToExtIntTest extends PathCopyToExtIntTest {
    public CopyToExtIntTest(String src, String dst,
                            CopyOption[] opts, Object expected) {
      super(RWZipFilePathWriteTest.fs, src, dst, opts, expected);
    }

    @Parameters
    public static List<Object[]> cases() {
      return Arrays.asList(new Object[][] {
        // Source Destination Opts  Expected
/*
        { "foo",  null,       null, t(NullPointerException.class)             },
        { "bar",  td + "nay", null, t(NoSuchFileException.class)              },
        { "/bar", td + "nay", null, t(NoSuchFileException.class)              },
        { "foo",  td + "nay", null, null                                      },
        { "foo",  td + "yea", null, t(FileAlreadyExistsException.class)       },
        { "/foo", td + "nay", null, null                                      },
        { "/foo", td + "yea", null, t(FileAlreadyExistsException.class)       },
        { "foo",  td + "yea", new CopyOption[]{ REPLACE_EXISTING }, null      },
        { "/foo", td + "yea", new CopyOption[]{ REPLACE_EXISTING }, null      },
        { "dirInZip", td + "yea", null, t(FileAlreadyExistsException.class)   },
        { "dirInZip", td + "yea", new CopyOption[]{ REPLACE_EXISTING }, null  },
        { "/dirInZip", td + "yea", null, t(FileAlreadyExistsException.class)  },
        { "/dirInZip", td + "yea", new CopyOption[]{ REPLACE_EXISTING }, null }
*/
      });
    }
  }

  // FIXME: need to test with file attributes
  @RunWith(Parameterized.class)
  public static class CreateDirectoryTest extends PathCreateDirectoryTest {
    public CreateDirectoryTest(String input, Object expected) {
      super(RWZipFilePathWriteTest.fs, input, expected);
    }

    @Parameters
    public static List<Object[]> cases() {
      return Arrays.asList(new Object[][] {
        // Input           Expected
        { "/dirInZip",     t(FileAlreadyExistsException.class) },
        { "/foodir",       null                                },
        { "dirInZip",      t(FileAlreadyExistsException.class) },
        { "bardir",        null                                }
      });
    }
  }
  
  // FIXME: need to test with file attributes
  @RunWith(Parameterized.class)
  public static class CreateFileTest extends PathCreateFileTest {
    public CreateFileTest(String input, Object expected) {
      super(RWZipFilePathWriteTest.fs, input, expected);
    }

    @Parameters
    public static List<Object[]> cases() {
      return Arrays.asList(new Object[][] {
        // Input           Expected
        { "/fileInZip",    t(FileAlreadyExistsException.class) },
        { "/foo",          null                                },
        { "fileInZip",     t(FileAlreadyExistsException.class) },
        { "bar",           null                                }
      });
    }
  }

  @RunWith(Parameterized.class)
  public static class DeleteTest extends PathDeleteTest {
    public DeleteTest(String input, Object expected) {
      super(RWZipFilePathWriteTest.fs, input, expected);
    }

    @Parameters
    public static List<Object[]> cases() {
      return Arrays.asList(new Object[][] {
        // Input           Expected
        { "/notAFile",     t(NoSuchFileException.class) },
        { "/foo",          null                                },
        { "/dirInZip",     t(DirectoryNotEmptyException.class) },
        { "bar",           null                                }
      });
    }
  }

  @RunWith(Parameterized.class)
  public static class DeleteIfExistsTest extends PathDeleteIfExistsTest {
    public DeleteIfExistsTest(String input, Object expected) {
      super(RWZipFilePathWriteTest.fs, input, expected);
    }

    @Parameters
    public static List<Object[]> cases() {
      return Arrays.asList(new Object[][] {
        // Input
        { "/notAFile", null                                },
        { "/foo",      null                                },
        { "/dirInZip", t(DirectoryNotEmptyException.class) },
        { "bar",       null                                }
      });
    }
  }


}

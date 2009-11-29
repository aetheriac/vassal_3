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

import VASSAL.tools.nio.file.FileSystem;
import VASSAL.tools.nio.file.FileSystems;
import VASSAL.tools.nio.file.Path;
import VASSAL.tools.nio.file.Paths;
import VASSAL.tools.nio.file.PathEndsWithTest;
import VASSAL.tools.nio.file.PathGetNameTest;
import VASSAL.tools.nio.file.PathGetNameIntTest;
import VASSAL.tools.nio.file.PathGetParentTest;
import VASSAL.tools.nio.file.PathGetRootTest;
import VASSAL.tools.nio.file.PathIsAbsoluteTest;
import VASSAL.tools.nio.file.PathNormalizationTest;
import VASSAL.tools.nio.file.PathNormalizeTest;
import VASSAL.tools.nio.file.PathRelativizeTest;
import VASSAL.tools.nio.file.PathResolveTest;
import VASSAL.tools.nio.file.PathStartsWithTest;
import VASSAL.tools.nio.file.PathSubpathTest;

@RunWith(Suite.class)
@SuiteClasses({
  ZipFilePathOpsTest.EndsWithTest.class,
  ZipFilePathOpsTest.GetNameTest.class,
  ZipFilePathOpsTest.GetNameIntTest.class,
  ZipFilePathOpsTest.GetParentTest.class,
  ZipFilePathOpsTest.GetRootTest.class,
  ZipFilePathOpsTest.IsAbsoluteTest.class,
//  ZipFilePathOpsTest.NormalizationTest.class,
  ZipFilePathOpsTest.NormalizeTest.class,
  ZipFilePathOpsTest.RelativizeTest.class,
  ZipFilePathOpsTest.ResolveTest.class,
  ZipFilePathOpsTest.StartsWithTest.class,
  ZipFilePathOpsTest.SubpathTest.class
})
public class ZipFilePathOpsTest {
  protected static FileSystem fs;

  protected static final String zfName = "testZipFile.zip";
  protected static final String zfPathName =
    "test/VASSAL/tools/nio/file/zipfs/".replace("/", File.separator) + zfName;

  protected static Path zfPath;

  @BeforeClass
  public static void setupFS() throws IOException {
    zfPath = Paths.get(zfPathName).toAbsolutePath();

    fs = (ZipFileSystem) FileSystems.newFileSystem(
      URI.create("zip://" + zfPath), null);
  }

  @RunWith(Parameterized.class)
  public static class EndsWithTest extends PathEndsWithTest {
    public EndsWithTest(String left, String right, boolean expected) {
      super(ZipFilePathOpsTest.fs, left, right, expected);
    }

    @Parameters
    public static List<Object[]> cases() {
      return Arrays.asList(new Object[][] {
        // Left       Right       Expected
        { "/",        "/",        true  },
        { "/",        "foo",      false },
        { "/",        "/foo",     false },
        { "/foo",     "foo",      true  },
        { "/foo",     "/foo",     true  },
        { "/foo",     "/",        false },
        { "/foo/bar", "bar",      true  },
        { "/foo/bar", "foo/bar",  true  },
        { "/foo/bar", "/foo/bar", true  },
        { "/foo/bar", "/bar",     false },
        { "foo",      "foo",      true  },
        { "foo/bar",  "bar",      true  },
        { "foo/bar",  "foo/bar",  true  }
      });
    }
  }

  @RunWith(Parameterized.class)
  public static class GetNameTest extends PathGetNameTest{
    public GetNameTest(String input, String expected) {
      super(ZipFilePathOpsTest.fs, input, expected);
    }

    @Parameters
    public static List<String[]> cases() {
      return Arrays.asList(new String[][] {
        // Input    Expected
        { "/a/b/c", "c"  },
        { "/",      null },
        { "a/b",    "b"  },
        { "a",      "a"  },
      });
    }
  }

  @RunWith(Parameterized.class)
  public static class GetNameIntTest extends PathGetNameIntTest{
    public GetNameIntTest(String input, int index,
                          String expected, Class<? extends Throwable> tclass) {
      super(ZipFilePathOpsTest.fs, input, index, expected, tclass);
    }

    @Parameters
    public static List<Object[]> cases() {
      return Arrays.asList(new Object[][] {
        // Input   Index Expected Throws
        { "a/b/c",  -1,  null,    IllegalArgumentException.class }, 
        { "a/b/c",   0,  "a",     null                           },
        { "a/b/c",   1,  "b",     null                           },
        { "a/b/c",   2,  "c",     null                           },
        { "a/b/c",   3,  null,    IllegalArgumentException.class },
        { "/a/b/c", -1,  null,    IllegalArgumentException.class }, 
        { "/a/b/c",  0,  "a",     null                           },
        { "/a/b/c",  1,  "b",     null                           },
        { "/a/b/c",  2,  "c",     null                           },
        { "/a/b/c",  3,  null,    IllegalArgumentException.class },
        { "/",       0,  null,    IllegalArgumentException.class }
      });
    }
  }

  @RunWith(Parameterized.class)
  public static class GetParentTest extends PathGetParentTest{
    public GetParentTest(String input, String expected) {
      super(ZipFilePathOpsTest.fs, input, expected);
    }

    @Parameters
    public static List<String[]> cases() {
      return Arrays.asList(new String[][] {
        // Input    Expected
        { "/a/b/c", "/a/b" },
        { "/",      null   },
        { "/a",     "/"    },
        { "a/b",    "a"    },
        { "a",      null   }
      });
    }
  }

  @RunWith(Parameterized.class)
  public static class GetRootTest extends PathGetRootTest {
    public GetRootTest(String input, String expected) {
      super(ZipFilePathOpsTest.fs, input, expected);
    }

    @Parameters
    public static List<String[]> cases() {
      return Arrays.asList(new String[][] {
        // Input    Expected
        { "/a/b/c", "/"  },
        { "/",      "/"  },
        { "a/b",    null },
        { "a",      null }
      });
    }
  }

  @RunWith(Parameterized.class)
  public static class IsAbsoluteTest extends PathIsAbsoluteTest {
    public IsAbsoluteTest(String input, boolean expected) {
      super(ZipFilePathOpsTest.fs, input, expected);
    }

    @Parameters
    public static List<Object[]> cases() {
      return Arrays.asList(new Object[][] {
        // Input  Expected
        { "/",    true  },
        { "/tmp", true  },
        { "tmp",  false }
      });
    }
  }

/*
  @RunWith(Parameterized.class)
  public static class NormalizationTest extends PathNormalizationTest{
    public NormalizationTest(String input, String expected) {
      super(ZipFilePathOpsTest.fs, input, expected);
    }

    @Parameters
    public static List<String[]> cases() {
      return Arrays.asList(new String[][] {
        // Input                Expected
        { "/.",            "/"       },
        { "//foo",         "/foo"    },
        { "/foo//",        "/foo"    },
        { "/foo/./",       "/foo"    },
        { "/foo/./.././.", "/foo/.." },
        { ".",             null      },
        { "foo",           "foo"     },
        { "foo//",         "foo"     },
        { "foo/./",        "foo"     },
        { "foo/./.././.",  "foo/.."  }
      });
    }
  }
*/

  @RunWith(Parameterized.class)
  public static class NormalizeTest extends PathNormalizeTest{
    public NormalizeTest(String input, String expected) {
      super(ZipFilePathOpsTest.fs, input, expected);
    }

    @Parameters
    public static List<String[]> cases() {
      return Arrays.asList(new String[][] {
        // Input                Expected
        { "/",                  "/"         },
        { "foo",                "foo"       },
        { ".",                  null        },
        { "..",                 ".."        },
        { "/..",                "/"         },
        { "/../..",             "/"         },
        { "foo/.",              "foo"       },
        { "./foo",              "foo"       },
        { "foo/..",             null        },
        { "../foo",             "../foo"    },
        { "../../foo",          "../../foo" },
        { "foo/bar/..",         "foo"       },
        { "foo/bar/baz/../..",  "foo"       },
        { "/foo/bar/baz/../..", "/foo"      }
      });
    }
  }

  @RunWith(Parameterized.class)
  public static class RelativizeTest extends PathRelativizeTest {
    public RelativizeTest(String left, String right,
                          String expected, Class<? extends Throwable> tclass) {
      super(ZipFilePathOpsTest.fs, left, right, expected, tclass);
    }

    @Parameters
    public static List<Object[]> cases() {
      return Arrays.asList(new Object[][] {
        // Left     Right         Expected   Throws
        { "/a/b/c", "/a/b/c",     null,      null                           },
        { "/a/b/c", "/a/b/c/d/e", "d/e",     null                           },
        { "/a/b/c", "/a/x",       "../../x", null                           },
        { "a/b/c", "a/b/c",       null,      null                           }, 
        { "a/b/c", "a/b/c/d/e",   "d/e",     null                           }, 
        { "a/b/c", "a/x",         "../../x", null                           }, 
        { "a/b/c",  "/a/b/c",     null,      IllegalArgumentException.class },
        { "a/b/c",  "/a/b/c/d/e", null,      IllegalArgumentException.class },
        { "a/b/c",  "/a/x",       null,      IllegalArgumentException.class },
        { "/a/b/c", "a/b/c",      null,      IllegalArgumentException.class },
        { "/a/b/c", "a/b/c/d/e",  null,      IllegalArgumentException.class },
        { "/a/b/c", "a/x",        null,      IllegalArgumentException.class }
      });
    }
  }

  @RunWith(Parameterized.class)
  public static class ResolveTest extends PathResolveTest {
    public ResolveTest(String left, String right, String expected) {
      super(ZipFilePathOpsTest.fs, left, right, expected);
    }

    @Parameters
    public static List<String[]> cases() {
      return Arrays.asList(new String[][] {
        // Left   Right   Expected
        { "/tmp", "foo",  "/tmp/foo" },
        { "/tmp", "/foo", "/foo"     },
        { "tmp",  "foo",  "tmp/foo"  },
        { "tmp",  "/foo", "/foo"     }
      });
    }
  }

  @RunWith(Parameterized.class)
  public static class StartsWithTest extends PathStartsWithTest{
    public StartsWithTest(String left, String right, boolean expected) {
      super(ZipFilePathOpsTest.fs, left, right, expected);
    }

    @Parameters
    public static List<Object[]> cases() {
      return Arrays.asList(new Object[][] {
        // Left       Right       Expected
        { "/",        "/",        true  },
        { "/",        "/foo",     false },
        { "/foo",     "/",        true  },
        { "/foo",     "/foo",     true  },
        { "/foo",     "/f",       false },
        { "/foo/bar", "/",        true  },
        { "/foo/bar", "/foo",     true  },
        { "/foo/bar", "/foo/bar", true  },
        { "/foo/bar", "/f",       false },
        { "/foo/bar", "foo",      false },
        { "/foo/bar", "foo/bar",  false },
        { "foo",      "foo",      true  },
        { "foo",      "f",        false },
        { "foo/bar",  "foo",      true  },
        { "foo/bar",  "foo/bar",  true  },
        { "foo/bar",  "f",        false },
        { "foo/bar",  "/foo",     false },
        { "foo/bar",  "/foo/bar", false },
      });
    }
  }

  @RunWith(Parameterized.class)
  public static class SubpathTest extends PathSubpathTest{
    public SubpathTest(String input, int begin, int end, String expected,
                                                       Class<? extends Throwable> tclass) {
      super(ZipFilePathOpsTest.fs, input, begin, end, expected, tclass);
    }

    @Parameters
    public static List<Object[]> cases() {
      return Arrays.asList(new Object[][] {
        // Input       Begin End Expected   Throws 
        { "/",             0, 1, null,      IllegalArgumentException.class },
        { "/foo/bar/baz", -1, 0, null,      IllegalArgumentException.class },
        { "/foo/bar/baz",  0, 1, "foo",     null                           },
        { "/foo/bar/baz",  0, 2, "foo/bar", null                           },
        { "/foo/bar/baz",  0, 3, "foo/bar/baz", null                       },
        { "/foo/bar/baz",  1, 2, "bar",     null                           },
        { "/foo/bar/baz",  1, 3, "bar/baz", null                           },
        { "/foo/bar/baz",  2, 3, "baz",     null                           },
        { "/foo/bar/baz",  1, 0, null,      IllegalArgumentException.class },
        { "foo/bar/baz",  -1, 0, null,      IllegalArgumentException.class },
        { "foo/bar/baz",   0, 1, "foo",     null                           },
        { "foo/bar/baz",   0, 2, "foo/bar", null                           },
        { "foo/bar/baz",   0, 3, "foo/bar/baz", null                       },
        { "foo/bar/baz",   1, 2, "bar",     null                           },
        { "foo/bar/baz",   1, 3, "bar/baz", null                           },
        { "foo/bar/baz",   2, 3, "baz",     null                           },
        { "foo/bar/baz",   1, 0, null,      IllegalArgumentException.class }
      });
    }
  }
}  

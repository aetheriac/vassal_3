package VASSAL.tools.nio.file.fs;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import static org.junit.Assume.assumeTrue;

import VASSAL.Info;

import VASSAL.tools.nio.file.FileSystem;
import VASSAL.tools.nio.file.PathEndsWithTest;
import VASSAL.tools.nio.file.PathGetNameIntTest;
import VASSAL.tools.nio.file.PathIsAbsoluteTest;
import VASSAL.tools.nio.file.PathNormalizationTest;
import VASSAL.tools.nio.file.PathNormalizeTest;
import VASSAL.tools.nio.file.PathRelativizeTest;
import VASSAL.tools.nio.file.PathResolveTest;
import VASSAL.tools.nio.file.PathStartsWithTest;
import VASSAL.tools.nio.file.PathSubpathTest;

@RunWith(Suite.class)
@SuiteClasses({
  RealWindowsPathTest.EndsWithTest.class,
  RealWindowsPathTest.GetNameIntTest.class,
  RealWindowsPathTest.IsAbsoluteTest.class,
//  RealWindowsPathTest.NormalizationTest.class,
  RealWindowsPathTest.NormalizeTest.class,
  RealWindowsPathTest.RelativizeTest.class,
  RealWindowsPathTest.ResolveTest.class,
  RealWindowsPathTest.StartsWithTest.class,
  RealWindowsPathTest.SubpathTest.class
})
public class RealWindowsPathTest {
  protected static FileSystem fs;

  @BeforeClass
  public static void setupFS() throws IOException {
    assumeTrue(Info.isWindows());

    final RealFileSystemProvider provider = new RealFileSystemProvider();
    fs = new RealFileSystem(provider);
  }

  @RunWith(Parameterized.class)
  public static class EndsWithTest extends PathEndsWithTest {
    public EndsWithTest(String left, String right, boolean expected) {
      super(RealWindowsPathTest.fs, left, right, expected);
    }

    @Parameters
    public static List<Object[]> cases() {
      return Arrays.asList(new Object[][] {
        // Left                Right                  Expected
        { "C:\\",              "C:\\",                true  },
        { "C:\\",              "c:\\",                true  },
        { "C:\\",              "\\",                  false },
        { "C:",                "C:",                  true  },
        { "C:",                "c:",                  true  },
        { "\\",                "\\",                  true  },
        { "C:\\foo\\bar",      "bar",                 true  },
        { "C:\\foo\\bar",      "BAR",                 true  },
        { "C:\\foo\\bar",      "foo\\bar",            true  },
        { "C:\\foo\\bar",      "Foo\\bar",            true  },
        { "C:\\foo\\bar",      "C:\\foo\\bar",        true  },
        { "C:\\foo\\bar",      "c:\\foO\\baR",        true  },
        { "C:\\foo\\bar",      "r",                   false },
        { "C:\\foo\\bar",      "\\foo\\bar",          false },
        { "\\foo\\bar",        "bar",                 true  },
        { "\\foo\\bar",        "BaR",                 true  },
        { "\\foo\\bar",        "foo\\bar",            true  },
        { "\\foo\\bar",        "foO\\baR",            true  },
        { "\\foo\\bar",        "\\foo\\bar",          true  },
        { "\\foo\\bar",        "\\Foo\\Bar",          true  },
        { "\\foo\\bar",        "oo\\bar",             false },
        { "foo\\bar",          "bar",                 true  },
        { "foo\\bar",          "BAR",                 true  },
        { "foo\\bar",          "foo\\bar",            true  },
        { "foo\\bar",          "Foo\\Bar",            true  },
        { "foo\\bar",          "ar",                  false },
        { "\\\\server\\share", "\\\\server\\share",   true  },
        { "\\\\server\\share", "\\\\server\\share\\", true  },
        { "\\\\server\\share", "shared",              false },
        { "\\\\server\\share", "\\",                  false }
      });
    }
  }

  @RunWith(Parameterized.class)
  public static class GetNameIntTest extends PathGetNameIntTest{
    public GetNameIntTest(String input, int index,
                          String expected, Class<? extends Throwable> tclass) {
      super(RealWindowsPathTest.fs, input, index, expected, tclass);
    }

    @Parameters
    public static List<Object[]> cases() {
      return Arrays.asList(new Object[][] {
        // Input      Index  Expected Throws
        { "a\\b\\c",     -1,  null,    IllegalArgumentException.class }, 
        { "a\\b\\c",      0,  "a",     null                           },
        { "a\\b\\c",      1,  "b",     null                           },
        { "a\\b\\c",      2,  "c",     null                           },
        { "a\\b\\c",      3,  null,    IllegalArgumentException.class },
        { "C:\\a\\b\\c", -1,  null,    IllegalArgumentException.class }, 
        { "C:\\a\\b\\c",  0,  "a",     null                           },
        { "C:\\a\\b\\c",  1,  "b",     null                           },
        { "C:\\a\\b\\c",  2,  "c",     null                           },
        { "C:\\a\\b\\c",  3,  null,    IllegalArgumentException.class },
        { "C:\\",         0,  null,    IllegalArgumentException.class },
        { "\\\\server\\a\\b\\c", -1,  null, IllegalArgumentException.class }, 
        { "\\\\server\\a\\b\\c",  0,  "a",  null                           },
        { "\\\\server\\a\\b\\c",  1,  "b",  null                           },
        { "\\\\server\\a\\b\\c",  2,  "c",  null                           },
        { "\\\\server\\a\\b\\c",  3,  null, IllegalArgumentException.class }
      });
    }
  }

  @RunWith(Parameterized.class)
  public static class IsAbsoluteTest extends PathIsAbsoluteTest {
    public IsAbsoluteTest(String input, boolean expected) {
      super(RealWindowsPathTest.fs, input, expected);
    }

    @Parameters
    public static List<Object[]> cases() {
      return Arrays.asList(new Object[][] {
        // Input                 Expected
        { "foo",                 false },
        { "C:",                  false },
        { "C:\\",                true  },
        { "C:\\abc",             true  },
        { "\\\\server\\share\\", true  }
      });
    }
  }

/*
  @RunWith(Parameterized.class)
  public static class NormalizationTest extends PathNormalizationTest{
    public NormalizationTest(String input, String expected) {
      super(RealWindowsPathTest.fs, input, expected);
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
      super(RealWindowsPathTest.fs, input, expected);
    }

    @Parameters
    public static List<String[]> cases() {
      return Arrays.asList(new String[][] {
        // Input                               Expected
        { "C:\\",                              "C:\\"                   },
        { "C:\\.",                             "C:\\"                   },
        { "C:\\..",                            "C:\\"                   },
        { "\\\\server\\share",                 "\\\\server\\share\\"    },
        { "\\\\server\\share\\.",              "\\\\server\\share\\"    },
        { "\\\\server\\share\\..",             "\\\\server\\share\\"    },
        { "C:",                                "C:"                     },
        { "C:.",                               "C:"                     },
        { "C:..",                              "C:.."                   },
        { "\\",                                "\\"                     },
        { "\\.",                               "\\"                     },
        { "\\..",                              "\\"                     },
        { "foo",                               "foo"                    },
        { "foo\\",                             "foo"                    },
        { "foo\\..",                           null                     },
        { "C:\\foo",                           "C:\\foo"                },
        { "C:\\foo\\.",                        "C:\\foo"                },
        { "C:\\.\\foo",                        "C:\\foo"                },
        { "C:\\foo\\..",                       "C:\\"                   },
        { "C:\\..\\foo",                       "C:\\foo"                },
        { "\\\\server\\share\\foo",            "\\\\server\\share\\foo" },
        { "\\\\server\\share\\foo\\.",         "\\\\server\\share\\foo" },
        { "\\\\server\\share\\.\\foo",         "\\\\server\\share\\foo" },
        { "\\\\server\\share\\foo\\..",        "\\\\server\\share\\"    },
        { "\\\\server\\share\\..\\foo",        "\\\\server\\share\\foo" },
        { "C:foo",                             "C:foo"                  },
        { "C:foo\\.",                          "C:foo"                  },
        { "C:.\\foo",                          "C:foo"                  },
        { "C:foo\\..",                         "C:"                     },
        { "C:..\\foo",                         "C:..\\foo"              },
        { "\\foo",                             "\\foo"                  },
        { "\\foo\\.",                          "\\foo"                  },
        { "\\.\\foo",                          "\\foo"                  },
        { "\\foo\\..",                         "\\"                     },
        { "\\..\\foo",                         "\\foo"                  },
        { ".",                                 null                     },
        { "..",                                ".."                     },
        { "\\..\\..",                          "\\"                     },
        { "..\\..\\foo",                       "..\\..\\foo"            },
        { "foo\\bar\\..",                      "foo"                    },
        { "foo\\bar\\.\\..",                   "foo"                    },
        { "foo\\bar\\baz\\..\\..",             "foo"                    },
        { ".\\foo\\.\\bar\\.\\baz\\..\\.\\..", "foo"                    }
      });
    }
  }

  @RunWith(Parameterized.class)
  public static class RelativizeTest extends PathRelativizeTest {
    public RelativizeTest(String left, String right, String expected) {
      super(RealWindowsPathTest.fs, left, right, expected);
    }

    @Parameters
    public static List<String[]> cases() {
      return Arrays.asList(new String[][] {
        // Left                     Right                     Expected
        { "foo\\bar",               "foo\\bar",               null      },
        { "foo\\bar",               "foo",                    ".."      },
        { "C:\\a\\b\\c",            "C:\\a",                  "..\\.."  },
        { "\\\\server\\share\\foo", "\\\\server\\share\\bar", "..\\bar" }
      });
    }
  }

  @RunWith(Parameterized.class)
  public static class ResolveTest extends PathResolveTest {
    public ResolveTest(String left, String right, String expected) {
      super(RealWindowsPathTest.fs, left, right, expected);
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
      super(RealWindowsPathTest.fs, left, right, expected);
    }

    @Parameters
    public static List<Object[]> cases() {
      return Arrays.asList(new Object[][] {
        // Left                Right                  Expected
        { "C:\\",              "C:\\",                true  },
        { "C:\\",              "c:\\",                true  },
        { "C:\\",              "C",                   false },
        { "C:\\",              "C:",                  false },
        { "C:",                "C:",                  true  },
        { "C:",                "c:",                  true  },
        { "C:",                "C",                   false },
        { "\\",                "\\",                  true  },
        { "C:\\foo\\bar",      "C:\\",                true  },
        { "C:\\foo\\bar",      "C:\\foo",             true  },
        { "C:\\foo\\bar",      "C:\\FOO",             true  },
        { "C:\\foo\\bar",      "C:\\foo\\bar",        true  },
        { "C:\\foo\\bar",      "C:\\foo\\Bar",        true  },
        { "C:\\foo\\bar",      "C:",                  false },
        { "C:\\foo\\bar",      "C",                   false },
        { "C:\\foo\\bar",      "C:foo",               false },
        { "\\foo\\bar",        "\\",                  true  },
        { "\\foo\\bar",        "\\foo",               true  },
        { "\\foo\\bar",        "\\foO",               true  },
        { "\\foo\\bar",        "\\foo\\bar",          true  },
        { "\\foo\\bar",        "\\fOo\\BaR",          true  },
        { "\\foo\\bar",        "foo",                 false },
        { "\\foo\\bar",        "foo\\bar",            false },
        { "foo\\bar",          "foo",                 true  },
        { "foo\\bar",          "foo\\bar",            true  },
        { "foo\\bar",          "\\",                  false },
        { "\\\\server\\share", "\\\\server\\share",   true  },
        { "\\\\server\\share", "\\\\server\\share\\", true  },
        { "\\\\server\\share", "\\",                  false }
      });
    }
  }

  @RunWith(Parameterized.class)
  public static class SubpathTest extends PathSubpathTest{
    public SubpathTest(String input, int begin, int end, String expected,
                                           Class<? extends Throwable> tclass) {
      super(RealWindowsPathTest.fs, input, begin, end, expected, tclass);
    }

    @Parameters
    public static List<Object[]> cases() {
      return Arrays.asList(new Object[][] {
        // Input            Begin End Expected   Throws 
        { "C:\\",               0, 1, null, IllegalArgumentException.class },
        { "C:\\foo\\bar\\baz", -1, 0, null, IllegalArgumentException.class },
        { "C:\\foo\\bar\\baz",  0, 1, "foo", null                          },
        { "C:\\foo\\bar\\baz",  0, 2, "foo\\bar", null                     },
        { "C:\\foo\\bar\\baz",  0, 3, "foo\\bar\\baz", null                },
        { "C:\\foo\\bar\\baz",  1, 2, "bar", null                          },
        { "C:\\foo\\bar\\baz",  1, 3, "bar\\baz", null                     },
        { "C:\\foo\\bar\\baz",  2, 3, "baz", null                          },
        { "C:\\foo\\bar\\baz",  1, 0, null, IllegalArgumentException.class },
        { "foo\\bar\\baz",     -1, 0, null, IllegalArgumentException.class },
        { "foo\\bar\\baz",      0, 1, "foo", null                          },
        { "foo\\bar\\baz",      0, 2, "foo\\bar", null                     },
        { "foo\\bar\\baz",      0, 3, "foo\\bar\\baz", null                },
        { "foo\\bar\\baz",      1, 2, "bar", null                          },
        { "foo\\bar\\baz",      1, 3, "bar\\baz", null                     },
        { "foo\\bar\\baz",      2, 3, "baz", null                          },
        { "foo\\bar\\baz",      1, 0, null, IllegalArgumentException.class },
        { "\\\\server\\share\\foo\\bar\\baz", -1, 0, null, IllegalArgumentException.class },
        { "\\\\server\\share\\foo\\bar\\baz", 0, 1, "foo", null            },
        { "\\\\server\\share\\foo\\bar\\baz", 0, 2, "foo\\bar", null       },
        { "\\\\server\\share\\foo\\bar\\baz", 0, 3, "foo\\bar\\baz", null  },
        { "\\\\server\\share\\foo\\bar\\baz", 1, 2, "bar", null            },
        { "\\\\server\\share\\foo\\bar\\baz", 1, 3, "bar\\baz", null       },
        { "\\\\server\\share\\foo\\bar\\baz", 2, 3, "baz", null            },
        { "\\\\server\\share\\foo\\bar\\baz", 1, 0, null, IllegalArgumentException.class }
      });
    }
  }
}

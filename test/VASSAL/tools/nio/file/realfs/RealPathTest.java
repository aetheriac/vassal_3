package VASSAL.tools.nio.file.realfs;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import VASSAL.tools.io.IOUtils;
import VASSAL.tools.nio.channels.FileChannelAdapter;
import VASSAL.tools.nio.file.AccessMode;
import VASSAL.tools.nio.file.FileSystems;
import VASSAL.tools.nio.file.LinkOption;
import VASSAL.tools.nio.file.NoSuchFileException;
import VASSAL.tools.nio.file.Path;
import VASSAL.tools.nio.file.Paths;
import VASSAL.tools.nio.file.WatchEvent;
import VASSAL.tools.nio.file.StandardOpenOption;
import VASSAL.tools.nio.file.attribute.BasicFileAttributeView;
import VASSAL.tools.nio.file.attribute.FileAttributeView;
import VASSAL.tools.nio.file.attribute.FileTime;

/*
 * This test is to be called through its subclasses: RealWindowsPathTest and RealUnixPathTest.
 */
public abstract class RealPathTest extends AbstractPathTest {

  final String separator = File.separator;
  final String curDir = "." + separator;
  final String prevDir = ".." + separator;
  final String stringPathToFileFsTest = "test" + separator + "VASSAL" + separator + "tools"
      + separator + "nio" + separator + "file" + separator + "realfs";

  final File pwd = new File(stringPathToFileFsTest);

  File testFileCreated;
  String testFileCreatedName;

  File testFileOther;
  String testFileOtherName;

  File testingDirectory;
  String testingDirectoryName;
  String testingDirectoryAbsolutePath;

  File testDirOther;
  String testDirOtherName;

  String pathRootName;
  RealPath pathRoot;

  RealPath pathTestFileCreated;
  RealPath pathTestFileOther;
  RealPath pathTestDirOther;
  RealPath pathTestingDirectory;

  int nc;
  Path endPath;

  RealFileSystem fs;
  RealFileSystemProvider provider;

  @Before
  public void setUp() throws Exception {
    provider = new RealFileSystemProvider();
    fs = new RealFileSystem(provider);

    testingDirectoryName = "testingDirectory";
    testingDirectory = new File(pwd.getAbsolutePath() + separator + testingDirectoryName);
    pathTestingDirectory = (RealPath) Paths.get(testingDirectory.getPath());
    //   testingDirectory.mkdir();
    pathRoot = (RealPath) pathTestingDirectory.getRoot();
    pathRootName = pathRoot.toString();

    testFileCreatedName = "testFile1";
    testFileCreated = new File(testingDirectory.getAbsolutePath() + separator + testFileCreatedName);
    pathTestFileCreated = (RealPath) Paths.get(testFileCreated.getPath());
    nc = pathTestFileCreated.getNameCount();
    endPath = pathTestFileCreated.subpath(nc - 1, nc);

    //   testFileCreated.createNewFile();

    testFileOtherName = "testFile2";
    testFileOther = new File(testingDirectory.getAbsolutePath() + separator + testFileOtherName);
    pathTestFileOther = (RealPath) Paths.get(testFileOther.getPath());

    testDirOtherName = "testDirOther";
    testDirOther = new File(testingDirectory.getAbsolutePath() + separator + testDirOtherName);
    pathTestDirOther = (RealPath) Paths.get(testDirOther.getPath());
  }

  @After
  public void tearDown() throws Exception {
    //  pathTestFileCreated.deleteIfExists();
    pathTestFileOther.deleteIfExists();
    pathTestDirOther.deleteIfExists();
    // pathTestingDirectory.deleteIfExists();
  }

  @Test
  public void testHashCode() {
    assertEquals("Path Hashcode does not match File hashcode", pathTestFileCreated.hashCode(),
        testFileCreated.hashCode());

  }

  @Test(expected = NoSuchFileException.class)
  public void testCheckAccessExist() throws IOException {
    pathTestFileOther.checkAccess(AccessMode.WRITE);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testCheckAccessExecute() throws IOException {
    pathTestFileCreated.checkAccess(AccessMode.EXECUTE);
  }

  @Test
  public void testCheckAccessRead() throws IOException {
    pathTestFileCreated.checkAccess(AccessMode.READ);
  }

  @Test
  public void testCheckAccessWrite() throws IOException {
    pathTestFileCreated.checkAccess(AccessMode.READ);
  }

  @Test
  public void testCreateDirectory() throws IOException {
    try {
      pathTestDirOther.createDirectory();
      assertTrue("Test directory was not created.", testDirOther.isDirectory());
    }
    finally {
      testDirOther.delete();
    }
  }

  @Test
  public void testCreateFile() throws IOException {
    try {
      pathTestFileOther.createFile();
      assertTrue("Test file was not created.", testFileOther.exists());
    }
    finally {
      testFileOther.delete();
    }
  }

  @Test
  public void testDelete() throws IOException {
    try {
      testFileOther.createNewFile();
      pathTestFileOther.delete();
      assertFalse(testFileOther.exists());
    }
    finally {
      testFileOther.delete();
    }
  }

  @Test
  public void testDeleteIfExists() throws IOException {
    try {
      testFileOther.createNewFile();
      pathTestFileOther.delete();
      assertFalse(testFileOther.exists());
    }
    finally {
      testFileOther.delete();
    }
  }

  @Test
  public void testEndsWithEndSelf() {
    assertTrue(pathTestFileCreated.endsWith(pathTestFileCreated));
  }

  @Test
  public void testEndsWithEndOther() {
    assertTrue(pathTestFileCreated.endsWith(endPath));
  }

  @Test
  public void testEndsWithFalse() {
    assertFalse(pathTestFileOther.endsWith(endPath));
  }

  @Test
  public void testEqualsObject() {
    assertTrue(pathTestFileCreated.equals(Paths.get(pathTestFileCreated.toString())));
  }

  @Test
  public void testEqualsSelf() {
    assertTrue("Path is not equal with itself", pathTestFileCreated.equals(pathTestFileCreated));
  }

  @Test
  public void testEqualsFalse() {
    assertFalse("Different paths should not equal", pathTestFileCreated.equals(pathTestFileOther));
  }

  @Test
  public void testExistsTrue() {
    assertTrue(pathTestFileCreated.exists());
  }

  @Test
  public void testExistsFalse() {
    assertFalse(pathTestFileOther.exists());
  }

  @Test
  public void testGetFileStore() throws IOException {
    assertEquals(
      pathTestingDirectory.getFileSystem().getFileStores().iterator().next(),
      pathTestingDirectory.getFileStore()
    );
  }

  @Test
  public void testGetFileSystem() {
    assertEquals(pathTestFileCreated.getFileSystem(), FileSystems.getDefault());
  }

  @Test
  public void testGetName() {
    assertEquals(testFileCreated.getName(), pathTestFileCreated.getName().toString());
  }

  @Test
  public void testGetNameInt() {
    File f = testFileCreated;
    for (int i = pathTestFileCreated.getNameCount()-1; i >= 0; --i) {
      assertEquals(f.getName(), pathTestFileCreated.getName(i).toString());
      f = f.getParentFile();
    }
  }

  @Test
  public void testGetNameCount() {
    final String p = "first" + separator + "second" + separator + "third" + separator + "fourth";
    Path path4 = Paths.get(p);
    assertEquals(4, path4.getNameCount());
  }

  @Test
  public void testGetRootSelf() {
    assumeTrue(File.listRoots().length > 0);
    File root = File.listRoots()[0];
    assertEquals(Paths.get(root.getAbsolutePath()), Paths.get(root.getAbsolutePath()).getRoot());
  }

  @Test
  public void testGetRoot() {
    File[] rootList = File.listRoots();
    boolean rootFound = false;

    for (File singleRoot : rootList) {
      Path testRoot = Paths.get(singleRoot.getAbsolutePath());
      Path testPath = pathTestingDirectory.getRoot();
      if (testPath.equals(testRoot)) {
        rootFound = true;
      }
    }

    assertTrue("Expected root not found: \"" + pathTestingDirectory.getRoot()
        + "\" returned. Expected root in the form of \"" + rootList[0].getAbsolutePath() + "\"",
        rootFound);

  }

  @Test
  public void testIsAbsoluteTrue() {
    assertTrue((Paths.get(File.listRoots()[0].getAbsolutePath())).isAbsolute());
  }

  @Test
  public void testIsAbsoluteFalse() {
    assertFalse(Paths.get("somedir" + separator + "somefile").isAbsolute());
  }

  @Test
  public void testIsHidden() throws IOException {
    assertFalse(pathTestFileCreated.isHidden());
  }

  @Test
  public void testGetParentTrue() {
    assertEquals(pathTestFileCreated.getParent(), pathTestingDirectory);
  }

  @Test
  public void testGetParentFalse() {
    assertFalse(pathTestingDirectory.getParent().equals(pathTestingDirectory));
  }

  @Test
  public void testIsSameFile() {
    assertTrue(pathTestingDirectory.isSameFile(Paths.get(testingDirectory.getAbsolutePath())));
  }

  @Test
  public void testIterator() {
    String test = null;
    Iterator<Path> iter = pathTestingDirectory.iterator();
    while (iter.hasNext()) {
      test = iter.next().toString();
    }

    assertEquals(testingDirectoryName, test);
  }

  // TODO add exception and replace testing
  @Test
  public void testMoveTo() throws IOException {

    File sourceFile = new File(testingDirectory.getAbsolutePath() + separator + "fileCopySource");
    Path pathSourceFile = Paths.get(sourceFile.getAbsolutePath());

    pathTestFileCreated.copyTo(pathSourceFile);

    pathSourceFile.moveTo(pathTestFileOther);
    assertTrue("Source file not moved correctly",
      testFileOther.exists() && !sourceFile.exists());

    sourceFile.delete();
    testFileOther.delete();
  }

  // FIXME can check success only with lack of exception. 
  @Test
  public void testNewDirectoryStream() throws IOException {
    pathTestingDirectory.newDirectoryStream();
  }

  @Test
  public void testNewDirectoryStreamFilterOfQsuperPath() throws IOException {
    assertTrue(pathTestingDirectory.newDirectoryStream().iterator().hasNext());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testNewDirectoryStreamString() throws IOException {
    pathTestingDirectory.newDirectoryStream("anyGlob");
  }

  @Test
  public void testNormalizeCurDir() {

    // in Windows: "C:\thisDir\\.\\.\\dir2\\.\\."
    String redundantPathString = pathRootName + "./thisDir/./././dir2/././trail/.".replace("/", separator);
    String normalizedPathString = pathRootName + "thisDir/dir2/trail".replace("/", separator);

    assertEquals(normalizedPathString, Paths.get(redundantPathString).normalize().toString());

  }

  @Test
  public void testNormalizePrevDirNonSaturated() {
    String redundantPathString = "dir1/dir2/dir3/../dir4/dir5/../../../trail".replace("/", separator);
    String normalizedPathString = "dir1/trail".replace("/", separator);

    assertEquals(normalizedPathString, Paths.get(redundantPathString).normalize().toString());
  }

  @Test
  public void testNormalizePrevDirSaturatedRelative() {
    String redundantPathString = "dir1/dir2/dir3/../../../../../trail".replace("/", separator);
    String normalizedPathString = "../../trail".replace("/", separator);

    assertEquals(normalizedPathString, Paths.get(redundantPathString).normalize().toString());

  }

  @Test
  public void testNormalizePrevDirSaturatedAbsolute() {
    String redundantPathString = pathRootName + "dir1/dir2/dir3/../../../../../trail".replace("/", separator);
    String normalizedPathString = pathRootName + "trail";

    assertEquals(normalizedPathString, Paths.get(redundantPathString).normalize().toString());

  }

  @Test
  public void testNormalizePrevDirRoot() {
    String redundantPathString = pathRootName + "../../../../..".replace("/", separator);
    String normalizedPathString = pathRootName;

    assertEquals(normalizedPathString, Paths.get(redundantPathString).normalize().toString());
  }

  @Test
  public void testNotExistsTrue() {
    assertTrue(pathTestFileOther.notExists());
  }

  @Test
  public void testNotExistsFalse() {
    assertFalse(pathTestFileCreated.notExists());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testRelativize() {
    pathTestingDirectory.relativize(pathTestDirOther);
  }

  @Test
  public void testResolvePathSelf() {
    assertEquals(pathTestFileOther, pathTestFileCreated.resolve(pathTestFileOther));
  }

  @Test
  public void testResolvePathNull() {
    assertEquals(pathTestFileCreated, pathTestFileCreated.resolve((Path) null));
  }

  @Test
  public void testResolvePathOther() {
    String resolvedPath = (new File(testingDirectory, testFileCreatedName)).toString();
    assertEquals(resolvedPath, pathTestingDirectory.resolve(pathTestFileCreated).toString());
  }

  @Test
  public void testResolveString() {
    assertEquals(pathTestFileOther, pathTestFileCreated.resolve(pathTestFileOther.toString()));
  }

  @Test
  public void testStartsWithTrue() {
    assertTrue(pathTestingDirectory.startsWith(Paths.get(pathTestingDirectory.getRoot().toString()
        + pathTestingDirectory.subpath(0, 2).toString())));
  }

  @Test
  public void testStartsWithFalse() {
    assertFalse(pathTestingDirectory.startsWith(pathTestingDirectory.subpath(1, 3)));
  }

  @Test
  public void testSubpath() {
    String targetName = "target";
    Path test = Paths.get("firstIgnored" + separator + targetName + separator + "secondIgnored");
    Path targetPath = Paths.get(targetName);
    int index = 1;

    assertEquals(targetPath, test.subpath(index, index + 1));
  }

  @Test
  public void testToAbsolutePath() throws IOException {
    Path test = Paths.get("name");
    assertEquals(Paths.get(new File(".").getCanonicalPath() + separator + "name"), test.toAbsolutePath());
  }

  @Test
  public void testToRealPath() throws IOException {
    Path test = Paths.get("name");
    assertEquals(Paths.get(new File(".").getCanonicalPath() + separator + "name"), test.toRealPath(true));
  }

  @Test
  public void testToString() {
    assertEquals(testFileCreated.toString(), pathTestFileCreated.toString());
  }

  @Test
  public void testToUri() {
    assertEquals(testFileCreated.toURI(), pathTestFileCreated.toUri());
  }

  @Test
  public void testGetFileAttributeViewBasicFileAttributeView() {
    assertNotNull(pathTestFileCreated.getFileAttributeView(BasicFileAttributeView.class));
  }

  @Test
  public void testGetFileAttributeViewFileAttributeView() {
    assertNull(pathTestFileCreated.getFileAttributeView(FileAttributeView.class));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGetFileAttributeViewLinkOptions() {
    pathTestFileCreated.getFileAttributeView(BasicFileAttributeView.class,
        LinkOption.NOFOLLOW_LINKS);
  }

  @Test(expected = NullPointerException.class)
  public void testGetAttributeNull() throws IOException {
    pathTestingDirectory.getAttribute(null);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGetAttributeUnsupported() throws Exception {
    pathTestingDirectory.getAttribute("whatever", LinkOption.NOFOLLOW_LINKS);
  }

  @Test
  public void testGetAttributeBasic() throws IOException {
    assertTrue(
      (Boolean) pathTestingDirectory.getAttribute("basic:isDirectory"));
  }

  @Test
  public void testNewByteChannelOpenOptionArray() throws IOException {
    final StandardOpenOption opt = StandardOpenOption.READ;

    FileChannelAdapter fca = null; 
    try {
      fca = pathTestFileCreated.newByteChannel(opt);
    }
    finally {
      IOUtils.closeQuietly(fca);
    }
  }

  @Test
  public void testStandardOpenOptionSet() {
    final StandardOpenOption[] opts = new StandardOpenOption[] {
      StandardOpenOption.APPEND,
      StandardOpenOption.CREATE
    };

    assertArrayEquals(
      (Object[]) opts,
      pathTestFileOther.standardOpenOptionSet(opts).toArray()
    );
  }

  @Test
  public void testNewByteChannelSetOfQextendsOpenOptionFileAttributeOfQArray()
                                                           throws IOException {
    final Set<StandardOpenOption> opt =
      pathTestFileCreated.standardOpenOptionSet(StandardOpenOption.READ);

    FileChannelAdapter fca = null; 
    try {
      fca = pathTestFileCreated.newByteChannel(opt);
    }
    finally {
      IOUtils.closeQuietly(fca);
    }
  }

  @Test
  public void testNewInputStream() throws IOException {
    FileInputStream in = null;
    try {
      in = pathTestFileCreated.newInputStream();
      assertTrue(in != null);
    }
    finally {
      IOUtils.closeQuietly(in);
    }
  }

  @Test
  public void testNewOutputStreamOpenOptionArray() throws IOException {

    FileOutputStream out = null;
    try {
      out = pathTestFileOther.newOutputStream(StandardOpenOption.CREATE_NEW);
      assertTrue(out != null);
    }
    finally {
      IOUtils.closeQuietly(out);
    }
  }

  @Test
  public void testReadAttributes() throws IOException {
    assertEquals(testingDirectory.isDirectory(), pathTestingDirectory.readAttributes("basic:isDirectory").values().toArray()[0]);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testReadAttributesFailOptions() throws IOException {
    pathTestingDirectory.readAttributes("basic:isDirectory", LinkOption.NOFOLLOW_LINKS);
  }

  @Test (expected = UnsupportedOperationException.class)
  public void testSetAttributeUnsupportedOption() throws IOException {
    pathTestFileCreated.setAttribute("whateverName", "whateverValue", LinkOption.NOFOLLOW_LINKS);
  }
  
  @Test (expected = UnsupportedOperationException.class)
  public void testSetAttributeUnsupportedView() throws IOException {
    pathTestFileCreated.setAttribute("nonBasic:Name", "whateverValue");
  }
  
  @Test (expected = UnsupportedOperationException.class)
  public void testSetAttributeUnsupportedName() throws IOException {
    pathTestFileCreated.setAttribute("basic:creationTime", "whateverValue");
  }
  
  @Test
  public void testSetAttributeModifTime() throws IOException {
    pathTestFileCreated.setAttribute("basic:lastModifiedTime", FileTime.fromMillis(System.currentTimeMillis()));
  }

  @Test
  public void testCompareTo() {
    assertEquals(pathTestFileCreated.toString().compareTo(pathTestFileOther.toString()),
        pathTestFileCreated.compareTo(pathTestFileOther));
  }

  @Test
  public void testCopyTo() throws IOException {
    try {
      pathTestFileCreated.copyTo(pathTestFileOther);
      assertTrue(pathTestFileOther.exists());

      byte[] expected = null;
      byte[] actual = null;

      InputStream in = null;
      try {
        in = pathTestFileCreated.newInputStream();
        expected = IOUtils.toByteArray(in);
      }
      finally {
        IOUtils.closeQuietly(in);
      }
  
      try {
        in = pathTestFileCreated.newInputStream();
        actual = IOUtils.toByteArray(in);
      }
      finally {
        IOUtils.closeQuietly(in);
      }

      assertArrayEquals(expected, actual);
    }
    finally {
      pathTestFileOther.deleteIfExists();
    }
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testCreateLink() throws IOException {
    pathTestFileOther.createLink(pathTestFileCreated);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testCreateSymbolicLink() throws IOException {
    pathTestFileOther.createSymbolicLink(pathTestFileCreated);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testReadSymbolicLink() throws IOException {
    pathTestFileOther.readSymbolicLink();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testRegisterWatchServiceKindOfQArray() throws IOException {
    pathTestFileOther.register(null);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testRegisterWatchServiceKindOfQArrayModifierArray() throws IOException {
    pathTestFileOther.register(null, (WatchEvent.Kind<?>[]) null);
  }

  @Test
  public abstract void testRealPathStringRealFileSystem();
}

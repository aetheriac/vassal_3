package VASSAL.tools.nio.file.fs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import VASSAL.tools.nio.file.AccessMode;
import VASSAL.tools.nio.file.LinkOption;
import VASSAL.tools.nio.file.NoSuchFileException;
import VASSAL.tools.nio.file.Path;
import VASSAL.tools.nio.file.Paths;
import VASSAL.tools.nio.file.attribute.BasicFileAttributeView;
import VASSAL.tools.nio.file.attribute.FileAttributeView;

/*
 * This test is to be called through its subclasses: RealWindowsPathTest and RealUnixPathTest.
 */
public abstract class RealPathTest extends AbstractPathTest {

  final String separator = File.separator;
  final String curDir = "." + separator;
  final String prevDir = ".." + separator;
  final String stringPathToFileFsTest = "test" + separator + "VASSAL" + separator + "tools"
      + separator + "nio" + separator + "file" + separator + "fs";

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
  public void testCreateDirectory() {
    try {
      pathTestDirOther.createDirectory();
      assertTrue("Test directory was not created.", testDirOther.isDirectory());
    }
    catch (IOException e) {
      fail(e.getMessage());
    } finally {
      testDirOther.delete();
    }

  }

  @Test
  public void testCreateFile() {
    try {
      pathTestFileOther.createFile();
      assertTrue("Test file was not created.", testFileOther.exists());
    }
    catch (Exception e) {
      fail(e.getMessage());
    } finally {
      testFileOther.delete();
    }
  }

  @Test
  public void testDelete() {
    try {
      testFileOther.createNewFile();
      pathTestFileOther.delete();
      assertFalse(testFileOther.exists());
    }
    catch (IOException e) {
      fail(e.getMessage());
    } finally {
      testFileOther.delete();
    }
  }

  @Test
  public void testDeleteIfExists() {
    try {
      testFileOther.createNewFile();
      pathTestFileOther.delete();
      assertFalse(testFileOther.exists());
    }
    catch (IOException e) {
      fail(e.getMessage());
    } finally {
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
  @Ignore
  //Will implement it later;
  public void testGetFileStore() {
    fail("Not yet implemented");
  }

  @Test
  public void testGetFileSystem() {
    assertEquals(pathTestFileCreated.getFileSystem(), fs);
  }

  @Test
  public void testGetName() {
    assertEquals(testFileCreated.getName(), pathTestFileCreated.getName().toString());
  }

  @Test
  public void testGetNameCount() {
    final String p = "first" + separator + "second" + separator + "third" + separator + "fourth";
    Path path3 = Paths.get(p);
    assertEquals(path3.getNameCount(), 4);
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
  public void testIsHidden() {
    try {
      assertFalse(pathTestFileCreated.isHidden());
    }
    catch (IOException e) {
      fail(e.getMessage());
    }
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
  @Ignore
  public void testIterator() {
    fail("Not yet implemented");
  }

  // TODO add exception and replace testing
  @Test
  public void testMoveTo() {

    File sourceFile = new File(testingDirectory.getAbsolutePath() + separator + "fileCopySource");
    Path pathSourceFile = Paths.get(sourceFile.getAbsolutePath());

    try {
      pathTestFileCreated.copyTo(pathSourceFile);
    }
    catch (IOException e1) {
      fail(e1.getMessage());
    }

    try {
      pathSourceFile.moveTo(pathTestFileOther);
      assertTrue("Source file not moved correctly", testFileOther.exists() && !sourceFile.exists());
    }
    catch (IOException e) {
      fail(e.getMessage());
    }

    sourceFile.delete();
    testFileOther.delete();

  }

  // FIXME can check success only with lack of exception. 
  @Test
  public void testNewDirectoryStream() {
    try {
      pathTestingDirectory.newDirectoryStream();
    }
    catch (IOException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testNewDirectoryStreamFilterOfQsuperPath() {

    RealDirectoryStream rds = new RealDirectoryStream(pathTestingDirectory);

    try {
      assertEquals(rds.toString(), pathTestingDirectory.newDirectoryStream().toString());
    }
    catch (IOException e) {
      fail(e.getMessage());
    }

  }

  @Test(expected = UnsupportedOperationException.class)
  public void testNewDirectoryStreamString() throws IOException {
    pathTestingDirectory.newDirectoryStream("anyGlob");
  }

  @Test
  public void testNormalizeCurDir() {

    // in Windows: "C:\thisDir\\.\\.\\dir2\\.\\."
    String redundantPathString = pathRootName + "thisDir" + separator + curDir + curDir + "dir2"
        + curDir + curDir;
    String normalizedPathString = pathRootName + "thisDir" + separator + "dir2";

    assertEquals(normalizedPathString, Paths.get(redundantPathString).normalize());

  }
  
  @Test
  public void testNormalizePrevDirNonSaturated() {
    
    // in Windows: "dir1\\dir2\\dir3\\..\\dir4\\dir5\\..\\..\\.."
    String redundantPathString = "dir1" + separator + "dir2" + separator + "dir3" + separator
        + prevDir + "dir4" + separator + "dir5" + separator + prevDir + prevDir + prevDir;
    // Sanitised to "dir1"
    String normalizedPathString = "dir1";

    assertEquals(normalizedPathString, Paths.get(redundantPathString).normalize());
  }
  
  @Test
  public void testNormalizePrevDirSaturatedRelative() {
    // in Windows: "dir1\\dir2\\dir3\\..\\..\\..\\..\\.."
    String redundantPathString = "dir1" + separator + "dir2" + separator + "dir3" + separator
        + prevDir + prevDir + prevDir + prevDir + prevDir;
    // becomes "..\\.."
    String normalizedPathString = prevDir + "..";

    assertEquals(normalizedPathString, Paths.get(redundantPathString).normalize());

  }
  
  @Test
  public void testNormalizePrevDirSaturatedAbsolute() {
    // in Windows: "dir1\\dir2\\dir3\\..\\..\\..\\..\\.."
    String redundantPathString = pathRootName + "dir1" + separator + "dir2" + separator + "dir3" + separator
        + prevDir + prevDir + prevDir + prevDir + prevDir;
    // becomes "..\\.."
    String normalizedPathString = pathRootName + prevDir + "..";

    assertEquals(normalizedPathString, Paths.get(redundantPathString).normalize());

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
    fail("actual path resolving TEST not implemented.");
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
  public void testToAbsolutePath() {

    Path test = Paths.get("name");
    try {
      assertEquals(Paths.get(new File(".").getCanonicalPath() + separator + "name"), test
          .toAbsolutePath());
    }
    catch (IOException e) {
      // TODO Auto-generated catch block
      fail(e.getMessage());
    }

  }

  @Test
  public void testToRealPath() {
    Path test = Paths.get("name");
    try {
      assertEquals(Paths.get(new File(".").getCanonicalPath() + separator + "name"), test
          .toRealPath(true));
    }
    catch (IOException e) {
      // TODO Auto-generated catch block
      fail(e.getMessage());
    }
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

  @Test
  public void testGetAttribute() {
    fail("Not yet implemented");
  }

  @Test
  public void testNewByteChannelOpenOptionArray() {
    fail("Not yet implemented");
  }

  @Test
  public void testStandardOpenOptionSet() {
    fail("Not yet implemented");
  }

  @Test
  public void testNewByteChannelSetOfQextendsOpenOptionFileAttributeOfQArray() {
    fail("Not yet implemented");
  }

  @Test
  public void testNewInputStream() {
    fail("Not yet implemented");
  }

  @Test
  public void testNewOutputStreamOpenOptionArray() {
    fail("Not yet implemented");
  }

  @Test
  public void testReadAttributes() {
    fail("Not yet implemented");
  }

  @Test
  public void testSetAttribute() {
    fail("Not yet implemented");
  }

  @Test
  public void testCompareTo() {
    assertEquals(pathTestFileCreated.toString().compareTo(pathTestFileOther.toString()),
        pathTestFileCreated.compareTo(pathTestFileOther));
  }

  @Test
  public void testCopyTo() {

    try {
      pathTestFileCreated.copyTo(pathTestFileOther);
      assertTrue(pathTestFileOther.exists());
    }
    catch (IOException e) {
      fail(e.getMessage());
    }

    Scanner sC = null;
    Scanner sT = null;

    try {
      sC = new Scanner(testFileCreated);
      sT = new Scanner(testFileOther);

      boolean fileEquals = false;
      while (sC.hasNext()) {
        sC.next().compareTo(sT.next());
        fileEquals = true;
      }
      assertTrue("Target file content not equal to source", fileEquals);
    }

    catch (FileNotFoundException e) {
      fail(e.getMessage());

    } finally {
      sC.close();
      sT.close();
    }

    try {
      pathTestFileOther.deleteIfExists();
    }
    catch (IOException e) {
      fail(e.getMessage());
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
    pathTestFileOther.register(null, null);
  }

  @Test
  public void testNewByteChannelOpenOptionArray1() {
    fail("Not yet implemented");
  }

  @Test
  public void testNewByteChannelSetOfQextendsOpenOptionFileAttributeOfQArray1() {
    fail("Not yet implemented");
  }

  @Test
  public void testNewOutputStreamOpenOptionArray1() {
    fail("Not yet implemented");
  }

  @Test
  @Ignore
  public void testRealPathFileRealFileSystem() {
    fail("Not yet implemented");

  }

  @Test
  @Ignore
  public void testRealPathStringRealFileSystem() {
    fail("Not yet implemented");

  }
}

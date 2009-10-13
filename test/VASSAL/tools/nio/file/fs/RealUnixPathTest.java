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

public class RealUnixPathTest extends AbstractPathTest {

  final String separator = File.separator;
  final String stringPathToUnixFsTest = "test" + separator + "VASSAL" + separator + "tools"
      + separator + "nio" + separator + "file" + separator + "fs";

  final File pwd = new File(stringPathToUnixFsTest);

  File testFileCreated;
  String testFileCreatedName;

  File testFileOther;
  String testFileOtherName;

  File testingDirectory;
  String testingDirectoryName;
  String testingDirectoryAbsolutePath;

  File testDirOther;
  String testDirOtherName;

  RealPath pathFileCreated;
  RealPath pathFileOther;
  RealPath pathTestDirOther;
  RealPath pathTestingDirectory;

  RealFileSystem fs;
  RealFileSystemProvider provider;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  @Before
  public void setUp() throws Exception {
    provider = new RealFileSystemProvider();
    fs = new RealFileSystem(provider);

    testingDirectoryName = "testingDirectory";
    testingDirectory = new File(pwd.getAbsolutePath() + separator + testingDirectoryName);
    pathTestingDirectory = new RealUnixPath(testingDirectory.getPath(), fs);
    //   testingDirectory.mkdir();

    testFileCreatedName = "testFile1";
    testFileCreated = new File(testingDirectory.getAbsolutePath() + separator + testFileCreatedName);
    pathFileCreated = new RealUnixPath(testFileCreated.getPath(), fs);
    //   testFileCreated.createNewFile();

    testFileOtherName = "testFile2";
    testFileOther = new File(testingDirectory.getAbsolutePath() + separator + testFileOtherName);
    pathFileOther = new RealUnixPath(testFileOther.getPath(), fs);

    testDirOtherName = "testDirOther";
    testDirOther = new File(testingDirectory.getAbsolutePath() + separator + testDirOtherName);
    pathTestDirOther = new RealUnixPath(testDirOther.getPath(), fs);
  }

  @After
  public void tearDown() throws Exception {
    //  pathFileCreated.deleteIfExists();
    pathFileOther.deleteIfExists();
    pathTestDirOther.deleteIfExists();
    // pathTestingDirectory.deleteIfExists();
  }

  @Test
  public void testFindRootSep() {
    assertEquals(0, pathTestingDirectory.findRootSep("/TestServer/testDir"));
    assertEquals(-1, pathTestingDirectory.findRootSep("somethingelse"));
  }

  @Test
  public void testUnixPathStringUnixFileSystem() {
    assumeTrue(!Info.isWindows());
    RealPath p1 = new RealUnixPath(testFileCreated.getPath(), fs);
    assertEquals(p1.toString(), testFileCreated.toString());
  }

  @Test
  public void testUnixPathFileUnixFileSystem() {
    assumeTrue(!Info.isWindows());
    RealPath p1 = new RealUnixPath(testFileCreated.getPath(), fs);
    assertEquals(p1.toString(), testFileCreated.toString());
  }

  @Test
  public void testHashCode() {
    assertEquals(
      "Path Hashcode does not match File hashcode", 
      pathFileCreated.hashCode(),
      testFileCreated.hashCode()
    );
  }

  @Test(expected = NoSuchFileException.class)
  public void testCheckAccessExist() throws IOException {
    pathFileOther.checkAccess(AccessMode.WRITE);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testCheckAccessExecute() throws IOException {
    pathFileCreated.checkAccess(AccessMode.EXECUTE);
  }

  @Test
  public void testCheckAccessRead() throws IOException {
    try {
      pathFileCreated.checkAccess(AccessMode.READ);
    }
    catch (Exception e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testCheckAccessWrite() throws IOException {
    try {
      pathFileCreated.checkAccess(AccessMode.READ);
    }
    catch (Exception e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testCreateDirectory() {
    try {
      pathTestDirOther.createDirectory();
    }
    catch (IOException e) {
      fail(e.getMessage());
    } 
    finally {
      assertTrue("Test directory was not created.", testDirOther.isDirectory());
      testDirOther.delete();
    }
  }

  @Test
  public void testCreateFile() {
    try {
      pathFileOther.createFile();
    }
    catch (Exception e) {
      fail(e.getMessage());
    } 
    finally {
      assertTrue("Test file was not created.", testFileOther.exists());
      testFileOther.delete();
    }
  }

  @Test
  public void testDelete() {
    try {
      testFileOther.createNewFile();
      pathFileOther.delete();
      assertFalse(testFileOther.exists());
    }
    catch (IOException e) {
      fail(e.getMessage());
    }
    finally {
      testFileOther.delete();
    }
  }

  @Test
  public void testDeleteIfExists() {
    try {
      testFileOther.createNewFile();
      pathFileOther.delete();
      assertFalse(testFileOther.exists());
    }
    catch (IOException e) {
      fail(e.getMessage());
    } 
    finally {
      testFileOther.delete();
    }
  }

  @Test
  public void testEndsWith() {
    int nc = pathFileCreated.getNameCount();
    Path endPath = pathFileCreated.subpath(nc - 1, nc);

    assertTrue(pathFileCreated.endsWith(pathFileCreated));
    assertTrue(pathFileCreated.endsWith(endPath));
    assertFalse(pathFileOther.endsWith(endPath));
  }

  @Test
  public void testEqualsObject() {
    assertEquals(pathFileCreated, Paths.get(pathFileCreated.toString()));
    assertEquals("Path is not equal with itself", pathFileCreated, (pathFileCreated));
    assertFalse("Different paths are considered equal", pathFileCreated.equals(pathFileOther));
  }

  @Test
  public void testExists() {
    assertTrue(pathFileCreated.exists());
    assertFalse(pathFileOther.exists());
  }

  @Test
  @Ignore
  //Will implement it later;
  public void testGetFileStore() {
    fail("Not yet implemented");
  }

  @Test
  public void testGetFileSystem() {
    assertEquals(pathFileCreated.getFileSystem(), fs);
  }

  @Test
  public void testGetName() {
    assertEquals(testFileCreated.getName(), pathFileCreated.getName().toString());
  }

  @Test
  public void testGetNameCount() {
    final String p = "first" + separator + "second" + separator +
                     "third" + separator + "fourth";
    RealPath path3 = new RealUnixPath(p, fs);
    assertEquals(path3.getNameCount(), 4);
  }

  @Test
  public void testGetParent() {
    assertEquals(pathFileCreated.getParent(), pathTestingDirectory);
    assertFalse(pathTestingDirectory.getParent().equals(pathTestingDirectory));
  }

  @Test
  public void testGetRoot() {
    File[] rootList = File.listRoots();
    assertTrue(rootList.length > 0);
    boolean rootFound = false;

    assertEquals(Paths.get(rootList[0].getAbsolutePath()), Paths.get(rootList[0].getAbsolutePath())
        .getRoot());

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
  public void testIsAbsolute() {
    assertTrue((Paths.get(File.listRoots()[0].getAbsolutePath())).isAbsolute());
    assertFalse(Paths.get("somedir" + separator + "somefile").isAbsolute());
  }

  @Test
  public void testIsHidden() {
    try {
      assertFalse(pathFileCreated.isHidden());
    }
    catch (IOException e) {
      fail(e.getMessage());
    }
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
      pathFileCreated.copyTo(pathSourceFile);
    }
    catch (IOException e1) {
      fail(e1.getMessage());
    }

    try {
      pathSourceFile.moveTo(pathFileOther);
      assertTrue(pathFileOther.exists());
      assertFalse(pathSourceFile.exists());
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
  public void testNormalize() {
    // in Windows: "thisDir\\.\\ignoredDir1\\ignoredDir2\\..\\.."
    String redundantPathString = "thisDir" + separator + "." + separator + "ignoredDir1"
        + separator + "ignoredDir2" + separator + ".." + separator + "..";

    String normalizedPathString = "thisDir";

    assertEquals(Paths.get(normalizedPathString), Paths.get(redundantPathString).normalize());
  }

  @Test
  public void testNotExists() {
    assertTrue(pathFileOther.notExists());
    assertFalse(pathFileCreated.notExists());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testRelativize() {
    pathTestingDirectory.relativize(pathTestDirOther);
  }

  @Test
  public void testResolvePath() {
    assertEquals(pathFileCreated, pathFileCreated.resolve((Path) null));
    assertEquals(pathFileOther, pathFileCreated.resolve(pathFileOther));
    fail("actual path resolving TEST not implemented.");
  }

  @Test
  public void testResolveString() {
    assertEquals(pathFileOther, pathFileCreated.resolve(pathFileOther.toString()));
  }

  @Test
  public void testStartsWith() {
    assertTrue(pathTestingDirectory.startsWith(Paths.get(pathTestingDirectory.getRoot().toString()
        + pathTestingDirectory.subpath(0, 2).toString())));
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
    assertEquals(testFileCreated.toString(), pathFileCreated.toString());
  }

  @Test
  public void testToUri() {
    assertEquals(testFileCreated.toURI(), pathFileCreated.toUri());
  }

  @Test
  public void testGetFileAttributeViewBasicFileAttributeView() {
    assertNotNull(
      pathFileCreated.getFileAttributeView(BasicFileAttributeView.class));
  }

  @Test
  public void testGetFileAttributeViewFileAttributeView() {
    assertNull(pathFileCreated.getFileAttributeView(FileAttributeView.class));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGetFileAttributeViewLinkOptions() {
    pathFileCreated.getFileAttributeView(
      BasicFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
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
    assertEquals(pathFileCreated.toString().compareTo(pathFileOther.toString()), pathFileCreated
        .compareTo(pathFileOther));
  }

  @Test
  public void testCopyTo() {

    try {
      pathFileCreated.copyTo(pathFileOther);
      assertTrue(pathFileOther.exists());
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
      pathFileOther.deleteIfExists();
    }
    catch (IOException e) {
      fail(e.getMessage());
    }

  }

  @Test(expected = UnsupportedOperationException.class)
  public void testCreateLink() throws IOException {
    pathFileOther.createLink(pathFileCreated);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testCreateSymbolicLink() throws IOException {
    pathFileOther.createSymbolicLink(pathFileCreated);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testReadSymbolicLink() throws IOException {
    pathFileOther.readSymbolicLink();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testRegisterWatchServiceKindOfQArray() throws IOException {
    pathFileOther.register(null); 
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testRegisterWatchServiceKindOfQArrayModifierArray()
                                                           throws IOException {
    pathFileOther.register(null, null);
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

package VASSAL.tools.nio.file.winfs;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import VASSAL.tools.nio.file.AccessMode;
import VASSAL.tools.nio.file.AccessDeniedException;
import VASSAL.tools.nio.file.NoSuchFileException;
import VASSAL.tools.nio.file.Path;
import VASSAL.tools.nio.file.fs.RealPathTest;

public class WindowsPathTest extends RealPathTest{

  final File pwd = new File(".");

  File file1;
  String fileName1;
  File file2;
  String fileName2;

  File testDir;
  String testDirName;

  WindowsPath path1;
  WindowsPath path2;
  WindowsPath pathTestDir;
  WindowsFileSystem fs;
  WindowsFileSystemProvider provider;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  @Before
  public void setUp() throws Exception {
    provider = new WindowsFileSystemProvider();
    fs = new WindowsFileSystem(provider);

    fileName1 = "testFile1";
    file1 = new File(pwd + File.pathSeparator + fileName1);
    path1 = new WindowsPath(pwd + File.pathSeparator + fileName1, fs);

    fileName2 = "testFile2";
    file2 = new File(pwd + File.pathSeparator + fileName2);
    path2 = new WindowsPath(pwd + File.pathSeparator + fileName2, fs);

    testDirName = "testDir";
    testDir = new File(pwd + File.pathSeparator + testDir);
    pathTestDir = new WindowsPath(pwd + File.pathSeparator + testDir, fs);

    file1.createNewFile();
  }

  @After
  public void tearDown() throws Exception {
    file1.delete();
    file2.delete();
    testDir.delete();
  }

  @Test
  public void testFindRootSep() {
    assertEquals(2, path1.findRootSep("\\\\TestServer\\Test"));
    assertEquals(1, path1.findRootSep("z:\\somedir\\somethingelse"));
    assertEquals(-1, path1.findRootSep("somethingelse"));
  }

  @Test
  public void testWindowsPathStringWindowsFileSystem() {
    fail("Not yet implemented");
  }

  @Test
  public void testWindowsPathFileWindowsFileSystem() {
    fail("Not yet implemented");
  }

  @Test
  public void testHashCode() {
    assertEquals("Path Hashcode does not match File hashcode", path1.hashCode(), file1.hashCode());

  }

  @Test(expected = NoSuchFileException.class)
  public void testCheckAccessExist() throws IOException {
    path2.checkAccess(AccessMode.WRITE);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testCheckAccessExecute() throws IOException {
    path1.checkAccess(AccessMode.EXECUTE);

  }

  @Test(expected = AccessDeniedException.class)
  public void testCheckAccessRead() throws IOException {
    fail("Must create a file that cannot be read");
  }

  @Test(expected = AccessDeniedException.class)
  public void testCheckAccessWrite() throws IOException {
    fail("Must create a file that cannot be written");
  }

  @Test
  public void testCreateDirectory() {
    try {
      pathTestDir.createDirectory();
    }
    catch (IOException e) {
      fail(e.getMessage());
    } finally {
      assertTrue("Test directory was not created.", testDir.isDirectory());
      testDir.delete();
    }

  }

  @Test
  public void testCreateFile() {
    try {
      path2.createFile();
    }
    catch (Exception e) {
      fail(e.getMessage());
    } finally {
      assertTrue("Test file was not created.", file2.exists());

      file2.delete();
    }
  }

  @Test
  public void testDelete() {
    try {
      file2.createNewFile();
      path2.delete();
      assertFalse(file2.exists());
    }
    catch (IOException e) {
      fail(e.getMessage());
    } finally {
      file2.delete();
    }
  }

  @Test
  public void testDeleteIfExists() {
    try {
      file2.createNewFile();
      path2.delete();
      assertFalse(file2.exists());
    }
    catch (IOException e) {
      fail(e.getMessage());
    } finally {
      file2.delete();
    }
  }

  @Test
  public void testEndsWith() {

    int nc = path1.getNameCount();
    Path endPath = path1.subpath(nc - 1, nc);

    assertTrue(path1.endsWith(path1));
    assertTrue(path1.endsWith(endPath));
    assertFalse(path2.endsWith(endPath));
  }

  @Test
  public void testEqualsObject() {
    assertTrue("Copied path is not equal to parent ", path1.equals(path1.subpath(0, path1
        .getNameCount())));
    assertTrue("Path is not equal with itself", path1.equals(path1));
    assertFalse("Different paths are considered equal", path1.equals(path2));
  }

  @Test
  public void testExists() {
    assertTrue(path1.exists());
    assertFalse(path2.exists());
  }

  @Test
  public void testGetFileStore() {
    fail("Not yet implemented");
  }

  @Test
  public void testGetFileSystem() {
    assertEquals(path1.getFileSystem(), fs);
  }

  @Test
  public void testGetName() {
    assertEquals(file1.getName(), path1.getName().toString());
  }

  //TODO implement all the following tests.
  
  @Test
  public void testGetNameCount() {
    fail("Not yet implemented");
  }

  @Test
  public void testGetParent() {
    fail("Not yet implemented");
  }

  @Test
  public void testGetRoot() {
    fail("Not yet implemented");
  }

  @Test
  public void testIsAbsolute() {
    fail("Not yet implemented");
  }

  @Test
  public void testIsHidden() {
    fail("Not yet implemented");
  }

  @Test
  public void testIsSameFile() {
    fail("Not yet implemented");
  }

  @Test
  public void testIterator() {
    fail("Not yet implemented");
  }

  @Test
  public void testMoveTo() {
    fail("Not yet implemented");
  }

  @Test
  public void testNewDirectoryStream() {
    fail("Not yet implemented");
  }

  @Test
  public void testNewDirectoryStreamFilterOfQsuperPath() {
    fail("Not yet implemented");
  }

  @Test
  public void testNewDirectoryStreamString() {
    fail("Not yet implemented");
  }

  @Test
  public void testNormalize() {
    fail("Not yet implemented");
  }

  @Test
  public void testNotExists() {
    fail("Not yet implemented");
  }

  @Test
  public void testRelativize() {
    fail("Not yet implemented");
  }

  @Test
  public void testResolvePath() {
    fail("Not yet implemented");
  }

  @Test
  public void testResolveString() {
    fail("Not yet implemented");
  }

  @Test
  public void testStartsWith() {
    fail("Not yet implemented");
  }

  @Test
  public void testSubpath() {
    fail("Not yet implemented");
  }

  @Test
  public void testToAbsolutePath() {
    fail("Not yet implemented");
  }

  @Test
  public void testToRealPath() {
    fail("Not yet implemented");
  }

  @Test
  public void testToString() {
    fail("Not yet implemented");
  }

  @Test
  public void testToUri() {
    fail("Not yet implemented");
  }

  @Test
  public void testRealPathStringRealFileSystem() {
    fail("Not yet implemented");
  }

  @Test
  public void testRealPathFileRealFileSystem() {
    fail("Not yet implemented");
  }

  @Test
  public void testGetFileAttributeView() {
    fail("Not yet implemented");
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
    fail("Not yet implemented");
  }

  @Test
  public void testCopyTo() {
    fail("Not yet implemented");
  }

  @Test
  public void testCreateLink() {
    fail("Not yet implemented");
  }

  @Test
  public void testCreateSymbolicLink() {
    fail("Not yet implemented");
  }

  @Test
  public void testReadSymbolicLink() {
    fail("Not yet implemented");
  }

  @Test
  public void testRegisterWatchServiceKindOfQArray() {
    fail("Not yet implemented");
  }

  @Test
  public void testRegisterWatchServiceKindOfQArrayModifierArray() {
    fail("Not yet implemented");
  }

  @Test
  public void testPath() {
    fail("Not yet implemented");
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

}

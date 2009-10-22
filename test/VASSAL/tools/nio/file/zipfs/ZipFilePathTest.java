package VASSAL.tools.nio.file.zipfs;

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
import VASSAL.tools.nio.file.fs.AbstractPathTest;

public class ZipFilePathTest extends AbstractPathTest {

  final String separator = File.separator;
  final String curDir = "." + separator;
  final String prevDir = ".." + separator;
  final String stringPathToFileFsTest = "test" + separator + "VASSAL" + separator + "tools" + separator + "nio" + separator + "file" + separator + "fs";

  String testFileCreatedName;
  String testFileOtherName;
  String testingDirectoryName;
  String testingDirectoryAbsolutePath;
  String testDirOtherName;
  String pathRootName;

  ZipFilePath pathTestFileCreated;
  ZipFilePath pathTestFileOther;
  ZipFilePath pathTestingDirectory;
  ZipFilePath pathTestDirOther;
  ZipFilePath pathRoot;

  int nc;
  Path endPath;

  ZipFileSystem fs;
  ZipFileSystemProvider provider;

  @Before
  public void setUp() throws Exception {
    provider = new ZipFileSystemProvider();
//    fs = provider.newFileSystem();

    testingDirectoryName = "testingDirectory";
    pathTestingDirectory = (ZipFilePath) Paths.get(testingDirectoryName);

    pathRoot = pathTestingDirectory.getRoot();
    pathRootName = pathRoot.toString();

    testFileCreatedName = "testFile1";
    pathTestFileCreated = (ZipFilePath) Paths.get(testFileCreatedName);
    nc = pathTestFileCreated.getNameCount();
    endPath = pathTestFileCreated.subpath(nc - 1, nc);

    testFileOtherName = "testFile2";
    pathTestFileOther = (ZipFilePath) Paths.get(testFileOtherName);

    testDirOtherName = "testDirOther";
    pathTestDirOther = (ZipFilePath) Paths.get(testDirOtherName);
  }

  @After
  public void tearDown() throws Exception {
    //  pathTestFileCreated.deleteIfExists();
//    pathTestFileOther.deleteIfExists();
//    pathTestDirOther.deleteIfExists();
    // pathTestingDirectory.deleteIfExists();
  }

/*
  @Test
  public void testHashCode() {
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
*/

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

/*
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
  public void testGetFileStore() {
    try {
      assertEquals(pathTestingDirectory.getFileSystem().getFileStores().iterator().next(),
          pathTestingDirectory.getFileStore());
    }
    catch (IOException e) {
      fail(e.getMessage());
    }
  }
*/

  @Test
  public void testGetFileSystem() {
    assertEquals(pathTestFileCreated.getFileSystem(), FileSystems.getDefault());
  }

  @Test
  public void testGetName() {
    assertEquals(testFileCreatedName, pathTestFileCreated.getName().toString());
  }

/*
  @Test
  public void testGetNameInt() {
    File f = testFileCreated;
    for (int i = pathTestFileCreated.getNameCount()-1; i >= 0; --i) {
      assertEquals(f.getName(), pathTestFileCreated.getName(i).toString());
      f = f.getParentFile();
    }
  }
*/

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
  public void testIsHidden() {
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
  public void testIsSameFile() throws IOException {
    assertTrue(pathTestingDirectory.isSameFile(Paths.get(testingDirectoryName)));
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

/*
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

    try {
      assertTrue(pathTestingDirectory.newDirectoryStream().iterator().hasNext());
    }
    catch (IOException e) {
      fail(e.getMessage());
    }

  }

  @Test(expected = UnsupportedOperationException.class)
  public void testNewDirectoryStreamString() throws IOException {
    pathTestingDirectory.newDirectoryStream("anyGlob");
  }
*/

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

/*
  @Test
  public void testResolvePathOther() {
    String resolvedPath = (new File(testingDirectory, testFileCreatedName)).toString();
    assertEquals(resolvedPath, pathTestingDirectory.resolve(pathTestFileCreated).toString());
  }
*/

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
    assertEquals(testFileCreatedName, pathTestFileCreated.toString());
  }

/*
  @Test
  public void testToUri() {
    assertEquals(testFileCreated.toURI(), pathTestFileCreated.toUri());
  }
*/

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
  public void testGetAttributeNull() {
    try {
      pathTestingDirectory.getAttribute(null);
    }
    catch (IOException e) {
      fail(e.getMessage());
    }
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGetAttributeUnsupported() throws Exception {
    pathTestingDirectory.getAttribute("whatever", LinkOption.NOFOLLOW_LINKS);
  }

  @Test
  public void testGetAttributeBasic() {
    try {
      assertTrue((Boolean) pathTestingDirectory.getAttribute("basic:isDirectory"));
    }
    catch (IOException e) {
      fail(e.getMessage());
    }
  }

/*
  @Test(expected = UnsupportedOperationException.class)
  public void testNewByteChannelOpenOptionArray() throws Exception {
    StandardOpenOption opt = StandardOpenOption.READ;
    FileChannelAdapter fca = pathTestFileCreated.newByteChannel(opt);
  }

  @Test
  public void testStandardOpenOptionSet() {
    StandardOpenOption[] opts = new StandardOpenOption[] { StandardOpenOption.APPEND,
        StandardOpenOption.CREATE };

    assertArrayEquals((Object[]) opts, pathTestFileOther.standardOpenOptionSet(opts).toArray());

  }

  @Test(expected = UnsupportedOperationException.class)
  public void testNewByteChannelSetOfQextendsOpenOptionFileAttributeOfQArray() throws Exception {
    StandardOpenOption[] opts = new StandardOpenOption[] { StandardOpenOption.READ };
    final Set<StandardOpenOption> opt = pathTestFileCreated.standardOpenOptionSet(opts);
    FileChannelAdapter fca = pathTestFileCreated.newByteChannel(opt);
  }
*/

  @Test
  public void testNewInputStream() {
    InputStream in = null;
    try {
      in = pathTestFileCreated.newInputStream();
      assertTrue(in != null);
    }
    catch (IOException e) {
      fail(e.getMessage());
    } finally {
      IOUtils.closeQuietly(in);
    }
  }

/*
  @Test
  public void testNewOutputStreamOpenOptionArray() {

    FileOutputStream out = null;
    try {
      out = pathTestFileOther.newOutputStream(StandardOpenOption.CREATE_NEW);
      assertTrue(out != null);
    }
    catch (IOException e) {
      fail(e.getMessage());
    } finally {
      IOUtils.closeQuietly(out);
      testFileOther.delete();
    }
  }
*/

/*
  @Test
  public void testReadAttributes() {
    try {
      
      assertEquals(testingDirectory.isDirectory(), pathTestingDirectory.readAttributes(
          "basic:isDirectory").values().toArray()[0]);
      
    }
    catch (IOException e) {
      fail(e.getMessage());
    }
  }
*/

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
 
/* 
  @Test (expected = UnsupportedOperationException.class)
  public void testSetAttributeUnsupportedName() throws IOException {
    pathTestFileCreated.setAttribute("basic:creationTime", "whateverValue");
  }
  
  @Test
  public void testSetAttributeModifTime() {
    try {
      pathTestFileCreated.setAttribute("basic:lastModifiedTime", FileTime.fromMillis(System.currentTimeMillis()));
    }
    catch (IOException e) {
      fail(e.getMessage());
    }
  }
*/

  @Test
  public void testCompareTo() {
    assertEquals(pathTestFileCreated.toString().compareTo(pathTestFileOther.toString()),
        pathTestFileCreated.compareTo(pathTestFileOther));
  }

  @Test
  public void testCopyTo() {
/*
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
*/
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

/*
  @Test
  public abstract void testRealPathStringRealFileSystem();
*/
}

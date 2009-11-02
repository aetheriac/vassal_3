package VASSAL.tools.nio.file.zipfs;

import static org.junit.Assert.*;
import static org.junit.Assume.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Scanner;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import VASSAL.tools.io.IOUtils;
import VASSAL.tools.nio.channels.FileChannelAdapter;
import VASSAL.tools.nio.file.AccessMode;
import VASSAL.tools.nio.file.LinkOption;
import VASSAL.tools.nio.file.Path;
import VASSAL.tools.nio.file.Paths;
import VASSAL.tools.nio.file.NoSuchFileException;
import VASSAL.tools.nio.file.ReadOnlyFileSystemException;
import VASSAL.tools.nio.file.StandardOpenOption;
import VASSAL.tools.nio.file.WatchEvent;
import VASSAL.tools.nio.file.attribute.BasicFileAttributeView;
import VASSAL.tools.nio.file.attribute.FileAttributeView;
import VASSAL.tools.nio.file.attribute.FileTime;
import VASSAL.tools.nio.file.fs.RealPath;

public class ZipFilePathTest {

  final String zipScheme = "zip";
  final String testZipFileName = "testZipFile.zip";
  final String pathToTestZipFileName = "test/VASSAL/tools/nio/file/zipfs/".replace("/", File.separator)
      + testZipFileName;

  RealPath testZipFilePath;

  ZipFileSystemProvider provider;
  ZipFileSystem fs;
  
  final String testFileCreatedName = "testFileInZip.txt";
  ZipFilePath pathTestFileCreated; 

  final String testingDirectoryName = "/dirInZip";
  ZipFilePath pathTestingDirectory;
  
  final String testFileOtherName = testingDirectoryName + "/testFileVolatile";
  ZipFilePath pathTestFileOther;
  
  final String testDirOtherName = "/testDirOther";
  ZipFilePath pathTestDirOther;
  
  String pathRootName;
  ZipFilePath pathRoot;
  ZipFilePath endPath;


  @Before
  public void setUp() throws Exception {
    

    testZipFilePath = (RealPath) Paths.get(pathToTestZipFileName);

    ZipFileSystemProvider provider = new ZipFileSystemProvider();
    ZipFileSystem fs = new ZipFileSystem(provider, testZipFilePath);
    

    
        
    pathTestingDirectory = new ZipFilePath(fs,testingDirectoryName.getBytes());
    pathTestDirOther = new ZipFilePath(fs, testDirOtherName.getBytes());
    pathTestFileCreated = new ZipFilePath(fs, testFileCreatedName.getBytes());
    pathTestFileOther = new ZipFilePath(fs,testFileOtherName.getBytes());
    
    pathRoot = pathTestingDirectory.getRoot();
    pathRootName = pathRoot.toString();

    int nc = pathTestFileCreated.getNameCount();
    endPath = pathTestFileCreated.subpath(nc - 1, nc);
    
  }

  @After
  public void tearDown() throws Exception {
    //  testFileInZipPath.deleteIfExists();
//    pathTestFileOther.deleteIfExists();
//    pathTestDirOther.deleteIfExists();
    // pathTestingDirectory.deleteIfExists();
  }
  
  

  @Test
  public void testHashCode() {
    assertFalse(pathTestFileCreated.hashCode() == pathTestFileOther.hashCode());
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

//  @Test
  @Test(expected = ReadOnlyFileSystemException.class)
  public void testCreateDirectory() throws IOException {
    try {
      pathTestDirOther.createDirectory();
      assertTrue("Test directory was not created.",
                 pathTestDirOther.isDirectory());
    }
    finally {
      pathTestDirOther.deleteIfExists();
    }
  }

//  @Test
  @Test(expected = ReadOnlyFileSystemException.class)
  public void testCreateFile() throws IOException {
    try {
      pathTestFileOther.createFile();
      assertTrue("Test file was not created.", pathTestFileOther.exists());
    }
    finally {
      pathTestFileOther.delete();
    }
  }

//  @Test
  @Test(expected = ReadOnlyFileSystemException.class)
  public void testDelete() throws IOException {
    try {
      pathTestFileOther.createFile();
      pathTestFileOther.delete();
      assertFalse(pathTestFileOther.exists());
    }
    finally {
      pathTestFileOther.delete();
    }
  }

//  @Test
  @Test(expected = ReadOnlyFileSystemException.class)
  public void testDeleteIfExistsDoesExist() throws IOException {
    try {
      pathTestFileOther.createFile();
      pathTestFileOther.deleteIfExists();
      assertFalse(pathTestFileOther.exists());
    }
    finally {
      pathTestFileOther.delete();
    }
  }

//  @Test
  @Test(expected = ReadOnlyFileSystemException.class)
  public void testDeleteIfExistsDoesNotExist() throws IOException {
    try {
      pathTestFileOther.deleteIfExists();
      assertFalse(pathTestFileOther.exists());
    }
    finally {
      pathTestFileOther.delete();
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
    assertTrue(pathTestFileCreated.equals(new ZipFilePath(fs, pathTestFileCreated.toString().getBytes())));
  }

  @Test
  public void testEqualsSelf() {
    assertTrue("Path is not equal with itself",
               pathTestFileCreated.equals(pathTestFileCreated));
  }

  @Test
  public void testEqualsFalse() {
    assertFalse("Different paths should not equal",
                pathTestFileCreated.equals(pathTestFileOther));
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
    assertEquals(fs.getFileStores().iterator().next(),
                 pathTestingDirectory.getFileStore());
  }

  @Test
  public void testGetFileSystem() {
    assertEquals(fs, pathTestFileCreated.getFileSystem());
  }

  @Test
  public void testGetName() {
    assertEquals(testFileCreatedName, pathTestFileCreated.getName().toString());
  }


//  @Test
//  public void testGetNameInt() {
//    File f = testFileCreated;
//    for (int i = pathTestFileCreated.getNameCount()-1; i >= 0; --i) {
//      assertEquals(f.getName(), pathTestFileCreated.getName(i).toString());
//      f = f.getParentFile();
//    }
//  }

  @Test
  public void testGetNameCount() {
    final String p = "/first/second/third/fourth";
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
    assertFalse(Paths.get("somedir/somefile").isAbsolute());
  }

  @Test
  public void testIsHidden() {
    assertFalse(pathTestFileCreated.isHidden());
  }

  @Test
  public void testGetParentTrue() {
    assertEquals(pathTestFileOther.getParent(), pathTestingDirectory);
  }

  @Test
  public void testGetParentFalse() {
    assertFalse(pathTestingDirectory.getParent().equals(pathTestingDirectory));
  }

  @Test
  public void testIsSameFile() throws IOException {
    assertTrue(pathTestingDirectory.isSameFile(new ZipFilePath(fs,testingDirectoryName.getBytes())));
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
  public void testMoveTo() {
    fail("read only so far");
/*
    File sourceFile = new File(testingDirectory.getAbsolutePath() + separator + "fileCopySource");
    Path pathSourceFile = Paths.get(sourceFile.getAbsolutePath());

    try {
      testFileInZipPath.copyTo(pathSourceFile);
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
*/
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

  @Test
  public void testNormalizeCurDir() {

    // in Windows: "C:\thisDir\\.\\.\\dir2\\.\\."
    String redundantPathString = pathRootName + "./thisDir/./././dir2/././trail/.";
    String normalizedPathString = pathRootName + "thisDir/dir2/trail";

    assertEquals(normalizedPathString, 
        (new ZipFilePath(fs, redundantPathString.getBytes())).normalize().toString());

  }

  @Test
  public void testNormalizePrevDirNonSaturated() {
    String redundantPathString = "dir1/dir2/dir3/../dir4/dir5/../../../trail";
    String normalizedPathString = "dir1/trail";

    assertEquals(normalizedPathString, 
        (new ZipFilePath(fs, redundantPathString.getBytes())).normalize().toString());
  }

  @Test
  public void testNormalizePrevDirSaturatedRelative() {
    String redundantPathString = "dir1/dir2/dir3/../../../../../trail";
    String normalizedPathString = "../../trail";

    assertEquals(normalizedPathString, 
        (new ZipFilePath(fs, redundantPathString.getBytes())).normalize().toString());
  }

  @Test
  public void testNormalizePrevDirSaturatedAbsolute() {
    String redundantPathString = pathRootName + "dir1/dir2/dir3/../../../../../trail";
    String normalizedPathString = pathRootName + "trail";

    assertEquals(normalizedPathString, 
        (new ZipFilePath(fs, redundantPathString.getBytes())).normalize().toString());

  }

  @Test
  public void testNormalizePrevDirRoot() {
    String redundantPathString = pathRootName + "../../../../..";
    String normalizedPathString = pathRootName;

    assertEquals(normalizedPathString, 
        (new ZipFilePath(fs, redundantPathString.getBytes())).normalize().toString());
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
    String resolvedPath = ".." + File.separator + testFileCreatedName;
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
    Path test = Paths.get("firstIgnored/" + targetName + "/secondIgnored");
    Path targetPath = Paths.get(targetName);
    int index = 1;

    assertEquals(targetPath, test.subpath(index, index + 1));
  }

  @Test
  public void testToAbsolutePath() {

    Path test = Paths.get("name");
    try {
      assertEquals(Paths.get(new File(".").getCanonicalPath() + "/name"), test
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
      assertEquals(Paths.get(new File(".").getCanonicalPath() + "/name"), test
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

  @Test
  public void testToUri() throws URISyntaxException {
    URI expectedUri = new URI(testZipFilePath.toUri().toString() + testFileCreatedName);
    assertEquals(expectedUri, pathTestFileCreated.toUri());
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


  @Test(expected = UnsupportedOperationException.class)
  public void testNewByteChannelOpenOptionArray() throws Exception {
    StandardOpenOption opt = StandardOpenOption.READ;
    FileChannelAdapter fca = (FileChannelAdapter) pathTestFileCreated.newByteChannel(opt);
  }

  @Test
  public void testStandardOpenOptionSet() {
    fail("ZipFilePath.standardOpenOptionSet() does not exist");
//    StandardOpenOption[] opts = new StandardOpenOption[] { StandardOpenOption.APPEND,
//        StandardOpenOption.CREATE };
//
//    assertArrayEquals((Object[]) opts, pathTestFileOther.standardOpenOptionSet(opts).toArray());

  }

  @Test(expected = UnsupportedOperationException.class)
  public void testNewByteChannelSetOfQextendsOpenOptionFileAttributeOfQArray() throws Exception { 
    fail("ZipFilePath.standardOpenOptionSet() does not exist");
//    StandardOpenOption[] opts = new StandardOpenOption[] { StandardOpenOption.READ };
//    final Set<StandardOpenOption> opt = pathTestFileCreated.standardOpenOptionSet(opts);
//    FileChannelAdapter fca = pathTestFileCreated.newByteChannel(opt);
  }


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

  @Test
  public void testNewOutputStreamOpenOptionArray() throws IOException {

    OutputStream out = null;
      out = pathTestFileOther.newOutputStream(StandardOpenOption.CREATE_NEW);
      assertTrue(out != null);

      IOUtils.closeQuietly(out);
      pathTestFileOther.deleteIfExists();
    
  }


  @Test
  public void testReadAttributes() {
    try {
      
      assertEquals(pathTestingDirectory.isDirectory(), pathTestingDirectory.readAttributes(
          "basic:isDirectory").values().toArray()[0]);
      
    }
    catch (IOException e) {
      fail(e.getMessage());
    }
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
    pathTestFileCreated.setAttribute(
      "basic:lastModifiedTime",
      FileTime.fromMillis(System.currentTimeMillis())
    );
  }


  @Test
  public void testCompareTo() {
    assertEquals(pathTestFileCreated.toString().compareTo(pathTestFileOther.toString()),
        pathTestFileCreated.compareTo(pathTestFileOther));
  }
  

  @Test
  public void testCopyTo() {

    fail("ZipFilePath is still read only");
    
//    try {
//      pathTestFileCreated.copyTo(pathTestFileOther);
//      assertTrue(pathTestFileOther.exists());
//    }
//    catch (IOException e) {
//      fail(e.getMessage());
//    }
//
//    Scanner sC = null;
//    Scanner sT = null;
//
//    try {
//      sC = new Scanner(testFileCreated);
//      sT = new Scanner(testFileOther);
//
//      boolean fileEquals = false;
//      while (sC.hasNext()) {
//        sC.next().compareTo(sT.next());
//        fileEquals = true;
//      }
//      assertTrue("Target file content not equal to source", fileEquals);
//    }
//
//    catch (FileNotFoundException e) {
//      fail(e.getMessage());
//
//    } finally {
//      sC.close();
//      sT.close();
//    }
//
//    try {
//      pathTestFileOther.deleteIfExists();
//    }
//    catch (IOException e) {
//      fail(e.getMessage());
//    }

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

}

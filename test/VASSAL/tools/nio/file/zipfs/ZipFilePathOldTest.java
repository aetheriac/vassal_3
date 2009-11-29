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
import VASSAL.tools.nio.channels.SeekableByteChannel;
import VASSAL.tools.nio.file.AccessDeniedException;
import VASSAL.tools.nio.file.AccessMode;
import VASSAL.tools.nio.file.FileSystems;
import VASSAL.tools.nio.file.LinkOption;
import VASSAL.tools.nio.file.Path;
import VASSAL.tools.nio.file.Paths;
import VASSAL.tools.nio.file.NoSuchFileException;
import VASSAL.tools.nio.file.ReadOnlyFileSystemException;
import VASSAL.tools.nio.file.StandardOpenOption;
import VASSAL.tools.nio.file.StandardCopyOption;
import VASSAL.tools.nio.file.WatchEvent;
import VASSAL.tools.nio.file.attribute.BasicFileAttributeView;
import VASSAL.tools.nio.file.attribute.FileAttributeView;
import VASSAL.tools.nio.file.attribute.FileTime;

public class ZipFilePathOldTest {

  final String zipScheme = "zip";
  final String testZipFileName = "testZipFile.zip";
  final String pathToTestZipFileName = "test/VASSAL/tools/nio/file/zipfs/".replace("/", File.separator) + testZipFileName;

  Path testZipFilePath;

  ZipFileSystem fs;
  
  final String testFileCreatedName = "testFileInZip.txt";
  ZipFilePath pathTestFileCreated; 

  final String testingDirectoryName = "dirInZip";
  ZipFilePath pathTestingDirectory;
 
  final String testingDirectory2Name = "dirInZip/foo";
  ZipFilePath pathTestingDirectory2; 
 
  final String testFileOtherName = testingDirectoryName + "/testFileVolatile";
  ZipFilePath pathTestFileOther;
  
  final String testDirOtherName = "/testDirOther";
  ZipFilePath pathTestDirOther;
  
  String pathRootName;
  ZipFilePath pathRoot;
  ZipFilePath endPath;

  Path externalPath;

  @Before
  public void setUp() throws Exception {
    testZipFilePath = Paths.get(pathToTestZipFileName).toAbsolutePath();
    externalPath = testZipFilePath.getParent().resolve("tmpFile");

    fs = (ZipFileSystem) FileSystems.newFileSystem(
      URI.create("zip://" + testZipFilePath), null);

    pathRoot = (ZipFilePath) fs.getRootDirectories().iterator().next();
    pathRootName = pathRoot.toString();

    pathTestingDirectory = fs.getPath(testingDirectoryName);
    pathTestingDirectory2 = fs.getPath(testingDirectory2Name);
    pathTestDirOther = fs.getPath(testDirOtherName);
    pathTestFileCreated = fs.getPath(testFileCreatedName);
    pathTestFileOther = fs.getPath(testFileOtherName);
    
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

// FIXME: need to also check a file which does exist!
  @Test(expected = NoSuchFileException.class)
  public void testCheckAccessExist() throws IOException {
    pathTestFileOther.checkAccess();
  }

// FIXME: need to also check a file which has execute permission!
  @Test(expected = AccessDeniedException.class)
  public void testCheckAccessExecute() throws IOException {
    pathTestFileCreated.checkAccess(AccessMode.EXECUTE);
  }

// FIXME: need to also check a file which cannot be read!
  @Test
  public void testCheckAccessRead() throws IOException {
    pathTestFileCreated.checkAccess(AccessMode.READ);
  }

// FIXME: need to also check a file which does not have write access!
// FIXME: need to also check a file which has write access!
//  @Test
  @Test(expected = AccessDeniedException.class)
  public void testCheckAccessWrite() throws IOException {
    pathTestFileCreated.checkAccess(AccessMode.WRITE);
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
  public void testExistsTrue() {
    assertTrue(pathTestFileCreated.exists());
  }

  @Test
  public void testExistsFalse() {
    assertFalse(pathTestFileOther.exists());
  }

  @Test
  public void testGetFileStore() throws IOException {
// FIXME: File stores not guaranteed to be identical, and FileStore
// does not reimiplement equals().
    assertEquals(fs.getFileStores().iterator().next(),
                 pathTestingDirectory.getFileStore());
  }

  @Test
  public void testGetFileSystem() {
    assertEquals(fs, pathTestFileCreated.getFileSystem());
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
  public void testIsHidden() {
    assertFalse(pathTestFileCreated.isHidden());
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
  public void testNotExistsTrue() {
    assertTrue(pathTestFileOther.notExists());
  }

  @Test
  public void testNotExistsFalse() {
    assertFalse(pathTestFileCreated.notExists());
  }

  @Test
  public void testToAbsolutePathRelative() throws IOException {
    assertEquals("/name", fs.getPath("name").toAbsolutePath().toString());
  }

  @Test
  public void testToAbsolutePathAbsolute() throws IOException {
    assertEquals("/name", fs.getPath("/name").toAbsolutePath().toString());
  }

  @Test
  public void testToRealPath() throws IOException {
    assertEquals("/" + testFileCreatedName,
                 fs.getPath(testFileCreatedName).toRealPath(true).toString());
  }

  @Test
  public void testToUri() {
    final URI expectedUri = 
      URI.create("zip://" + testZipFilePath + "#/" + testFileCreatedName);
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

  @Test
  public void testGetFileAttributeViewLinkOptions() {
    assertNotNull(pathTestFileCreated.getFileAttributeView(BasicFileAttributeView.class, LinkOption.NOFOLLOW_LINKS));
  }

  @Test(expected = NullPointerException.class)
  public void testGetAttributeNull() throws IOException {
    pathTestingDirectory.getAttribute(null);
  }

  @Test
  public void testGetAttributeUnknown() throws Exception {
    assertNull(pathTestingDirectory.getAttribute("whatever"));
  }

  @Test
  public void testGetAttributeBasic() throws IOException {
    assertTrue(
      (Boolean) pathTestingDirectory.getAttribute("basic:isDirectory"));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testNewByteChannelOpenOptionArray() throws Exception {
// FIXME: do a read?
    StandardOpenOption opt = StandardOpenOption.READ;
    SeekableByteChannel sbc = pathTestFileCreated.newByteChannel(opt);
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
  public void testNewInputStream() throws IOException {
// FIXME: do a read test
    InputStream in = null;
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
// FIXME: do a write test
    OutputStream out = null;
      out = pathTestFileOther.newOutputStream(StandardOpenOption.CREATE_NEW);
      assertTrue(out != null);

      IOUtils.closeQuietly(out);
      pathTestFileOther.deleteIfExists();
    
  }

  @Test
  public void testReadAttributes() throws IOException {
    assertEquals(pathTestingDirectory.isDirectory(), pathTestingDirectory.readAttributes("basic:isDirectory").values().toArray()[0]);
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
  public void testCopyToZip() throws IOException {
    fail("ZipFilePath is still read only");
  }

  @Test
  public void testCopyToExternalReplaceExisting() throws IOException {
// FIXME: copy to a path on disk
    try {
      pathTestFileCreated.copyTo(
        externalPath, StandardCopyOption.REPLACE_EXISTING);
      assertTrue(externalPath.exists());

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
        in = externalPath.newInputStream();
        actual = IOUtils.toByteArray(in);
      }
      finally {
        IOUtils.closeQuietly(in);
      }

      assertArrayEquals(expected, actual);
    }
    finally {
      externalPath.deleteIfExists();
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
}

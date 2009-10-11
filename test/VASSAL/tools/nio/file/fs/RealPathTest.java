package VASSAL.tools.nio.file.fs;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public abstract class RealPathTest extends AbstractPathTest {

  @Test
  public abstract void testHashCode();

  @Test
  public abstract void testCheckAccessExist() throws IOException;

  @Test
  public abstract void testCheckAccessExecute() throws IOException;

  @Test
  public abstract void testCheckAccessRead() throws IOException;

  @Test
  public abstract void testCheckAccessWrite() throws IOException;

  @Test
  public abstract void testCreateDirectory();

  @Test
  public abstract void testCreateFile();

  @Test
  public abstract void testDelete();

  @Test
  public abstract void testDeleteIfExists();

  @Test
  public abstract void testEndsWith();

  @Test
  public abstract void testEqualsObject();

  @Test
  public abstract void testExists();

  @Test
  public abstract void testGetFileStore();

  @Test
  public abstract void testGetFileSystem();

  @Test
  public abstract void testGetName();

  @Test
  public abstract void testGetNameCount();

  @Test
  public abstract void testGetParent();

  @Test
  public abstract void testGetRoot();

  @Test
  public abstract void testIsAbsolute();

  @Test
  public abstract void testIsHidden();

  @Test
  public abstract void testIsSameFile();

  @Test
  public abstract void testIterator();

  @Test
  public abstract void testMoveTo();

  @Test
  public abstract void testNewDirectoryStream();

  @Test
  public abstract void testNewDirectoryStreamFilterOfQsuperPath();

  @Test
  public abstract void testNewDirectoryStreamString();

  @Test
  public abstract void testNormalize();

  @Test
  public abstract void testNotExists();

  @Test
  public abstract void testRelativize();

  @Test
  public abstract void testResolvePath();

  @Test
  public abstract void testResolveString();

  @Test
  public abstract void testStartsWith();

  @Test
  public abstract void testSubpath();

  @Test
  public abstract void testToAbsolutePath();

  @Test
  public abstract void testToRealPath();

  @Test
  public abstract void testToString();

  @Test
  public abstract void testToUri();

  @Test
  public abstract void testRealPathStringRealFileSystem();

  @Test
  public abstract void testRealPathFileRealFileSystem();

  @Test
  public abstract void testFindRootSep();

  @Test
  public abstract void testGetFileAttributeView();

  @Test
  public abstract void testGetAttribute();

  @Test
  public abstract void testNewByteChannelOpenOptionArray();

  @Test
  public abstract void testStandardOpenOptionSet();

  @Test
  public abstract void testNewByteChannelSetOfQextendsOpenOptionFileAttributeOfQArray();

  @Test
  public abstract void testNewInputStream();

  @Test
  public abstract void testNewOutputStreamOpenOptionArray();

  @Test
  public abstract void testReadAttributes();

  @Test
  public abstract void testSetAttribute();

}

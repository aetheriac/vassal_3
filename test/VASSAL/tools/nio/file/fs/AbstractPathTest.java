package VASSAL.tools.nio.file.fs;

import java.io.IOException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public abstract class AbstractPathTest {

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public abstract void testCompareTo();

  @Test
  public abstract void testCopyTo();

  @Test
  public abstract void testCreateLink() throws IOException;

  @Test
  public abstract void testCreateSymbolicLink() throws IOException;

  @Test
  public abstract void testReadSymbolicLink() throws IOException;

  @Test
  public abstract void testRegisterWatchServiceKindOfQArray()
                                                            throws IOException;

  @Test
  public abstract void testRegisterWatchServiceKindOfQArrayModifierArray()
                                                            throws IOException;

}

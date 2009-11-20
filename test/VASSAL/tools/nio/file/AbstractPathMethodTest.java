package VASSAL.tools.nio.file;

import org.junit.Test;

public abstract class AbstractPathMethodTest {
  protected final FileSystem fs;

  public AbstractPathMethodTest(FileSystem fs) {
    this.fs = fs;
  }
  
  protected abstract void doTest() throws Throwable;

  @Test
  public void test() throws Throwable {
    doTest();
  }
}

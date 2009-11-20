package VASSAL.tools.nio.file;

import static org.junit.Assert.fail;

import org.junit.Test;

public abstract class AbstractThrowingPathMethodTest
                                               extends AbstractPathMethodTest {
  protected final Class<? extends Throwable> tclass;

  public AbstractThrowingPathMethodTest(FileSystem fs,
                                        Class<? extends Throwable> tclass) {
    super(fs);
    this.tclass = tclass;
  }
  
  @Test
  @Override
  public void test() throws Throwable {
    if (tclass == null) {
      // We are not expecting an exception.
      doTest();
    }
    else {
      // We are expecting an exception of type tclass.
      try {
        doTest();
      }
      catch (Throwable t) {
        // We still fail on exception of the wrong type.
        if (tclass.isInstance(t)) return;
        else throw t;
      }

      // We didn't see the expected exception.
      fail();
    }
  }
}

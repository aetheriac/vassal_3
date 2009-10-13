package VASSAL.tools.nio.file.fs;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RealWindowsPath extends RealPath {
  public RealWindowsPath(String path, RealFileSystem fs) {
    super(path, fs);
  }

  /** {@inheritDoc} */
  protected int findRootSep(String s) {
    Matcher matcherCColonBslash = Pattern.compile("^[a-zA-Z]:\\\\").matcher(s);
    Matcher matcherBslashBslash = Pattern.compile("^\\\\\\\\").matcher(s);

    if (matcherCColonBslash.find()) {
      return matcherCColonBslash.end() -1;  // Should return 2
    }
    if (matcherBslashBslash.find()) {
      return matcherBslashBslash.end() -1;  // Should return 1
    } else {
      return -1;
    }
  }
}

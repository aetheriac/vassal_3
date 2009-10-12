package VASSAL.tools.nio.file.winfs;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import VASSAL.tools.nio.file.fs.RealPath;
import VASSAL.tools.nio.file.fs.RealFileSystem;


class WindowsPath extends RealPath { 
  
  public WindowsPath(String path, RealFileSystem fs) {
    super(path, fs);
  }

  public WindowsPath(File file, WindowsFileSystem fs) {
    super(file, fs);
  }

  protected int findRootSep(String s) {
    File[] rootList = File.listRoots();
    boolean rootFoundInList = false;
    int rootLength = -1;
    
    Pattern patternBslashBslash = Pattern.compile("^\\\\\\\\");
    Matcher matcherBslashBslash = patternBslashBslash.matcher(s);

    for (File testRoot : rootList) {
      String testString = testRoot.getAbsolutePath().toLowerCase();
      if (s.toLowerCase().startsWith(testString)) {
        rootLength = testString.length();
        rootFoundInList = true;
        break;
      }
    }

    if (rootFoundInList) {
      return rootLength - 1;   // should return 2
    } else if (matcherBslashBslash.find()) {
      return matcherBslashBslash.end() -1;  // Should return 1
    } else {
      return -1;
    }

  }
}

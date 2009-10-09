package VASSAL.tools.nio.file.fs;

import java.io.File;
import java.io.IOException;

class WindowsPath extends RealPath {
  public WindowsPath(String path, WindowsFileSystem fs) {
    super(path, fs);
  }
  
  public WindowsPath(File file, WindowsFileSystem fs) {
    super(file, fs);
  }
}

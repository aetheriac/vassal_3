package VASSAL.tools.nio.file.fs;

import java.net.URI;

import VASSAL.tools.nio.file.Path;

public class WindowsFileSystemProvider extends RealFileSystemProvider {
  public WindowsFileSystemProvider() {
    fs = new WindowsFileSystem(this);
  }

  public Path getPath(URI uri) {
    return new WindowsPath(uriToPath(uri), (WindowsFileSystem) fs);
  }
}

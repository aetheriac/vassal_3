package VASSAL.tools.nio.file.winfs;

import java.net.URI;

import VASSAL.tools.nio.file.Path;
import VASSAL.tools.nio.file.fs.RealFileSystemProvider;

public class WindowsFileSystemProvider extends RealFileSystemProvider {
  public WindowsFileSystemProvider() {
    fs = new WindowsFileSystem(this);
  }

  public Path getPath(URI uri) {
    return new WindowsPath(uriToPath(uri), (WindowsFileSystem) fs);
  }
}

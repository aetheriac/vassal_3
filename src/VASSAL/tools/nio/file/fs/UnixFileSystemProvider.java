package VASSAL.tools.nio.file.fs;

import java.net.URI;

import VASSAL.tools.nio.file.Path;

public class UnixFileSystemProvider extends RealFileSystemProvider {
  public UnixFileSystemProvider() {
    fs = new UnixFileSystem(this);
  }
 
  public Path getPath(URI uri) {
    return new UnixPath(uriToPath(uri), (UnixFileSystem) fs);
  }
}

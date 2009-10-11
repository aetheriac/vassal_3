package VASSAL.tools.nio.file.unixfs;

import java.net.URI;

import VASSAL.tools.nio.file.Path;
import VASSAL.tools.nio.file.fs.RealFileSystemProvider;

public class UnixFileSystemProvider extends RealFileSystemProvider {
  public UnixFileSystemProvider() {
    fs = new UnixFileSystem(this);
  }
 
  public Path getPath(URI uri) {
    return new UnixPath(uriToPath(uri), (UnixFileSystem) fs);
  }
}

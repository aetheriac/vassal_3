package VASSAL.tools.nio.file.unixfs;

import VASSAL.tools.nio.file.Path;
import VASSAL.tools.nio.file.fs.RealFileSystem;
import VASSAL.tools.nio.file.spi.FileSystemProvider;

class UnixFileSystem extends RealFileSystem {
  private final UnixFileSystemProvider provider;

  public UnixFileSystem(UnixFileSystemProvider provider) {
    this.provider = provider;
  }

  public Path getPath(String path) {
    return new UnixPath(path, this);
  }

  public FileSystemProvider provider() {
    return provider;
  }
}

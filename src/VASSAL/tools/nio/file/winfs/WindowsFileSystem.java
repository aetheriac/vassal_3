package VASSAL.tools.nio.file.winfs;

import VASSAL.tools.nio.file.Path;
import VASSAL.tools.nio.file.spi.FileSystemProvider;
import VASSAL.tools.nio.file.fs.RealFileSystem;

class WindowsFileSystem extends RealFileSystem {
  private final WindowsFileSystemProvider provider;

  public WindowsFileSystem(WindowsFileSystemProvider provider) {
    this.provider = provider;
  }

  public Path getPath(String path) {
    return new WindowsPath(path, this);
  }

  public FileSystemProvider provider() {
    return provider;
  }
}

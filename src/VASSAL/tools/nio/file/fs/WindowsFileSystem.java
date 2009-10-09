package VASSAL.tools.nio.file.fs;

import VASSAL.tools.nio.file.Path;
import VASSAL.tools.nio.file.spi.FileSystemProvider;

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

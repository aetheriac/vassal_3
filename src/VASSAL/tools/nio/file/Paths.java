package VASSAL.tools.nio.file;

import java.net.URI;

public final class Paths {
  public static Path get(String path) {
    return FileSystems.getDefault().getPath(path);
  }

  public static Path get(URI uri) {
    return FileSystems.getFileSystem(uri).provider().getPath(uri);
  }
}

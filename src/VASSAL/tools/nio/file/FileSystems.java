package VASSAL.tools.nio.file;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import VASSAL.tools.nio.file.spi.FileSystemProvider;

public final class FileSystems {
  private FileSystems() {}

  public static FileSystem getDefault() {
    return getFileSystem(URI.create("file:///"));
  }

  private static FileSystemProvider getProviderForScheme(String scheme) {
    for (FileSystemProvider p : FileSystemProvider.installedProviders()) {
      if (scheme.equalsIgnoreCase(p.getScheme())) return p;
    }
    return null;
  }

  public static FileSystem getFileSystem(URI uri) {
    final FileSystemProvider p = getProviderForScheme(uri.getScheme());
    if (p == null) throw new ProviderNotFoundException();
    return p.getFileSystem(uri);
  }

  public static FileSystem newFileSystem(URI uri, Map<String,?> env)
                                                           throws IOException {
    final FileSystemProvider p = getProviderForScheme(uri.getScheme());
    if (p == null) throw new ProviderNotFoundException();
    return p.newFileSystem(uri, env);
  }

  public static FileSystem newFileSystem(
    URI uri,
    Map<String,?> env,
    ClassLoader loader) throws IOException
  {
    // ignore the ClassLoader, since we don't have ServiceLoader yet
    return newFileSystem(uri, env);
  }

  public static FileSystem newFileSystem(
    FileRef file,
    Map<String,?> env,
    ClassLoader loader) throws IOException
  {
    if (file == null) throw new NullPointerException();

    for (FileSystemProvider p : FileSystemProvider.installedProviders()) {
      try {
        return p.newFileSystem(file, env);
      }
      catch (UnsupportedOperationException ignore) {
      }
    }
    
    throw new ProviderNotFoundException();
  }
}

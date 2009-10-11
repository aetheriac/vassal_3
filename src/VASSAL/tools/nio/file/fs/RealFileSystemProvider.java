package VASSAL.tools.nio.file.fs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.net.URI;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import VASSAL.tools.nio.file.FileAlreadyExistsException;
import VASSAL.tools.nio.file.FileRef;
import VASSAL.tools.nio.file.FileSystem;
import VASSAL.tools.nio.file.FileSystemAlreadyExistsException;
import VASSAL.tools.nio.file.OpenOption;
import VASSAL.tools.nio.file.Path;
import VASSAL.tools.nio.file.StandardOpenOption;
import VASSAL.tools.nio.file.attribute.FileAttribute;
import VASSAL.tools.nio.file.spi.FileSystemProvider;

import static VASSAL.tools.nio.file.StandardOpenOption.*;

public abstract class RealFileSystemProvider extends FileSystemProvider {
  protected RealFileSystem fs;

  public FileSystem getFileSystem(URI uri) {
    if (!URI.create("file:///").equals(uri))
      throw new IllegalArgumentException();

    return fs;
  }

  public abstract Path getPath(URI uri);

  protected String uriToPath(URI uri) {
    if (!uri.isAbsolute()) throw new IllegalArgumentException();
    if (uri.isOpaque()) throw new IllegalArgumentException();
    if ("file".equalsIgnoreCase(uri.getScheme()))
      throw new IllegalArgumentException();
    if (uri.getAuthority() != null) throw new IllegalArgumentException();
    if (uri.getQuery() != null) throw new IllegalArgumentException();
    if (uri.getFragment() != null) throw new IllegalArgumentException();

    String path = uri.getPath();
    if ("".equals(path)) throw new IllegalArgumentException();
    if (path.endsWith(File.separator) && !path.equals(File.separator)) {
      path = path.substring(0, path.length()-1);
    }

    return path;
  }

  public String getScheme() {
    return "file";
  }

  protected static final Set<StandardOpenOption> supportedOpenOptions =
    EnumSet.of(
      APPEND,
      CREATE,
      CREATE_NEW,
      READ,
      TRUNCATE_EXISTING,
      WRITE
    );
 
  public FileChannel newFileChannel(
    RealPath path,
    Set<? extends OpenOption> options,
    FileAttribute<?>... attrs) throws IOException
  {
    if (attrs.length > 0) throw new UnsupportedOperationException();

    if (!supportedOpenOptions.containsAll(options)) 
      throw new UnsupportedOperationException();

    if (options.contains(CREATE_NEW) && path.file.exists())
      throw new FileAlreadyExistsException(path.toString());

    if (options.contains(APPEND)) {
      if (options.contains(READ) ||
          options.contains(TRUNCATE_EXISTING)) {
        throw new IllegalArgumentException();
      }

      return new FileOutputStream(path.file, true).getChannel();
    }

    if (options.contains(WRITE)) {
      if (options.contains(READ)) {
        // read-write
        final FileChannel fc =
          new RandomAccessFile(path.file, "rw").getChannel();
        if (options.contains(TRUNCATE_EXISTING)) fc.truncate(0);
        return fc;
      }
      else {
        // write-only
        return new FileOutputStream(path.file, false).getChannel();
      }
    }
    else {
      // read-only
      return path.newInputStream().getChannel();
    }
  }
  
  public FileSystem newFileSystem(FileRef file, Map<String,?> env)
                                                           throws IOException {
     throw new FileSystemAlreadyExistsException();
  }

  public FileSystem newFileSystem(URI uri, Map<String,?> env)
                                                           throws IOException {
    throw new FileSystemAlreadyExistsException();
  }
}

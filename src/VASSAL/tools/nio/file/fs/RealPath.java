package VASSAL.tools.nio.file.fs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import VASSAL.tools.io.IOUtils;
import VASSAL.tools.nio.channels.FileChannelAdapter;
import VASSAL.tools.nio.file.AccessDeniedException;
import VASSAL.tools.nio.file.AccessMode;
import VASSAL.tools.nio.file.AtomicMoveNotSupportedException;
import VASSAL.tools.nio.file.CopyOption;
import VASSAL.tools.nio.file.DirectoryNotEmptyException;
import VASSAL.tools.nio.file.DirectoryStream;
import VASSAL.tools.nio.file.FileAlreadyExistsException;
import VASSAL.tools.nio.file.FileStore;
import VASSAL.tools.nio.file.FileSystem;
import VASSAL.tools.nio.file.FileSystemException;
import VASSAL.tools.nio.file.LinkOption;
import VASSAL.tools.nio.file.NotDirectoryException;
import VASSAL.tools.nio.file.NoSuchFileException;
import VASSAL.tools.nio.file.OpenOption;
import VASSAL.tools.nio.file.Path;
import VASSAL.tools.nio.file.StandardCopyOption;
import VASSAL.tools.nio.file.StandardOpenOption;
import VASSAL.tools.nio.file.attribute.BasicFileAttributeView;
import VASSAL.tools.nio.file.attribute.FileAttribute;
import VASSAL.tools.nio.file.attribute.FileAttributeView;
import VASSAL.tools.nio.file.attribute.FileTime;

import static VASSAL.tools.nio.file.StandardCopyOption.*;
import static VASSAL.tools.nio.file.StandardOpenOption.*;

abstract class RealPath extends AbstractPath {
  final File file;
  final RealFileSystem fs;
  
  protected RealPath(String path, RealFileSystem fs) {
    this(new File(path), fs);
  }

  protected RealPath(File file, RealFileSystem fs) {
    this.file = file;
    this.fs = fs;
  }

  public void checkAccess(AccessMode... modes) throws IOException {
    if (!file.exists()) throw new NoSuchFileException(file.toString());

    for (AccessMode m : modes) {
      switch (m) {
      case READ:
        if (!file.canRead()) {
          throw new AccessDeniedException(file.toString());
        }
        break;
      case WRITE:
        if (!file.canWrite()) {
          throw new AccessDeniedException(file.toString());
        }
        break;
      case EXECUTE:
        if (!file.canExecute()) {
          throw new AccessDeniedException(file.toString());
        }
        break;
      default:
        throw new UnsupportedOperationException(m.toString());
      }
    }
  }
  
  public Path createDirectory(FileAttribute<?>... attrs) throws IOException {
    if (attrs.length > 0) throw new UnsupportedOperationException();
    if (file.exists()) throw new FileAlreadyExistsException(file.toString());
    if (!file.mkdir()) throw new FileSystemException(file.toString());
  }

  public Path createFile(FileAttribute<?>... attrs) throws IOException {
    if (attrs.length > 0) throw new UnsupportedOperationException();
    if (file.exists()) throw new FileAlreadyExistsException(file.toString());
    if (!file.createNewFile()) throw new FileSystemException(file.toString());
  }

  public void delete() throws IOException {
    if (!file.exists()) throw new NoSuchFileException(file.toString());
    if (file.isDirectory() && file.list().length > 0)
      throw new DirectoryNotEmptyException(file.toString());
    if (!file.delete()) throw new FileSystemException(file.toString());
  }

  public void deleteIfExists() throws IOException {
    if (exists()) delete();
  }

  public abstract boolean endsWith(Path other);

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || !(o instanceof RealPath)) return false;
    return file.equals(((RealPath) o).file);
  }

  public boolean exists() {
    return file.exists();
  }

  public <V extends FileAttributeView> V getFileAttributeView(
    Class<V> type, LinkOption... options)
  {
    if (options.length > 0) throw new UnsupportedOperationException();
    if (!BasicFileAttributeView.class.equals(type)) return null;
    return type.cast(new RealFileAttributeView(this));
  }

  public Object getAttribute(String attribute, LinkOption... options)
                                                           throws IOException {
    if (options.length > 0) throw new UnsupportedOperationException();
    if (attribute == null) return null;

    if (attribute.indexOf(':') == -1) attribute = "basic:" + attribute;
    final int colon = attribute.indexOf(':');
    final String vname = attribute.substring(0, colon);
    final String aname = attribute.substring(colon+1);

    if ("basic".equals(vname)) {
      final RealFileAttributeView view = new RealFileAttributeView(this);
      final RealFileAttributes attrs = view.readAttributes();

      if ("lastModifiedTime".equals(aname)) {
        return attrs.lastModifiedTime();
      }
      else if ("lastAccessTime".equals(aname)) {
       return attrs.lastAccessTime();
      }
      else if ("creationTime".equals(aname)) {
        return attrs.creationTime();
      }
      else if ("size".equals(aname)) {
        return attrs.size();
      }
      else if ("isRegularFile".equals(aname)) {
        return attrs.isRegularFile();
      }
      else if ("isDirectory".equals(aname)) {
        return attrs.isDirectory();
      }
      else if ("isOther".equals(aname)) {
        return attrs.isOther();
      }
      else if ("fileKey".equals(aname)) {
        return attrs.fileKey();
      }
    }

    return null;
  }

  public abstract FileStore getFileStore() throws IOException;

  public FileSystem getFileSystem() {
    return fs;
  }

  public Path getName() {
    return fs.getPath(file.getName());
  }

  public abstract int getNameCount();

  public Path getParent() {
    return fs.getPath(file.getParent());
  }

  public abstract Path getRoot();

  @Override
  public int hashCode() {
    return file.hashCode();
  }

  public boolean isAbsolute() {
    return file.isAbsolute();
  }

  public boolean isHidden() throws IOException {
    return file.isHidden();
  }

  public boolean isSameFile(Path other) {
    return this.equals(other);
  }

  public abstract Iterator<Path> iterator();

  public Path moveTo(Path target, CopyOption... options) throws IOException {
    if (!target.isSameFile(this)) {
      boolean replace = false;

      for (CopyOption c : options) {
        if (c == REPLACE_EXISTING) replace = true;
        else if (c == ATOMIC_MOVE) {
          throw new AtomicMoveNotSupportedException(
            this.toString(), target.toString(), ""
          );
        }
        else throw new UnsupportedOperationException(c.toString());
      }

      if (!replace && target.exists()) {
        throw new FileAlreadyExistsException(
          file.toString(), target.toString(), ""
        );
      }

      if (fs == target.getFileSystem()) {
        // we're on this fs, do the easy thing
        if (!file.renameTo(((RealPath) target).file)) {
          throw new FileSystemException(
            file.toString(), target.toString(), ""
          );
        }
      }
      else {
        // different fs: copy, then delete
        copyTo(target, options);
        delete();
      }
    }
    
    return target;
  }

  public FileChannelAdapter newByteChannel(OpenOption... options)
                                                           throws IOException {
    final Set<StandardOpenOption> opt = standardOpenOptionSet(options);
    if (opt.isEmpty()) opt.add(READ);

    return newByteChannel(opt);
  }

  protected Set<StandardOpenOption> standardOpenOptionSet(OpenOption[] opts) {
    final Set<StandardOpenOption> opt =
      EnumSet.noneOf(StandardOpenOption.class);

    for (OpenOption o : opts) {
      if (o instanceof StandardOpenOption) {
        opt.add((StandardOpenOption) o);
      }
      else {
        throw new UnsupportedOperationException(o.toString());
      }
    }
  }

  public FileChannelAdapter newByteChannel(
    Set<? extends OpenOption> options, FileAttribute<?>... attrs)
                                                           throws IOException {
    return new FileChannelAdapter(
      fs.provider().newFileChannel(this, options, attrs));
  }

  public DirectoryStream<Path> newDirectoryStream() throws IOException {
    if (!file.isDirectory()) throw new NotDirectoryException(file.toString());
    return new RealDirectoryStream(this);
  }

  public DirectoryStream<Path> newDirectoryStream(
    DirectoryStream.Filter<? super Path> filter) throws IOException {

    if (!file.isDirectory()) throw new NotDirectoryException(file.toString());
    return new RealDirectoryStream(this, filter);
  }

  public abstract DirectoryStream<Path> newDirectoryStream(String glob)
                                                            throws IOException;

  public FileInputStream newInputStream(OpenOption... options)
                                                           throws IOException {
    for (OpenOption o : options) {
      if (o != StandardOpenOption.READ) {
        throw new UnsupportedOperationException(o.toString());
      }
    }

    return new FileInputStream(file);
  }

  public FileOutputStream newOutputStream(OpenOption... options)
                                                           throws IOException {
    final Set<StandardOpenOption> opts = standardOpenOptionSet(options); 
    
    if (opts.contains(READ)) throw new IllegalArgumentException();

    if (opts.contains(CREATE_NEW) && file.exists())
      throw new FileAlreadyExistsException(file.toString());

    boolean append = false;
    if (opts.contains(APPEND)) {
      if (opts.contains(TRUNCATE_EXISTING))
        throw new IllegalArgumentException();
     
      append = true;
    }

    return new FileOutputStream(file, append);
  }

  public Path normalize() {
  }

  public boolean notExists() {
    return !file.exists();
  }

  public Map<String,?> readAttributes(String attributes, LinkOption... options)
                                                           throws IOException {
    if (options.length > 0) throw new UnsupportedOperationException();

    final Map<String,Object> map = new HashMap<String,Object>();

    for (String attr : attributes.split(",")) {
      if ("*".equals(attr) || ("basic:*".equals(attr))) {
        map.putAll(readAttributes("basic:lastModifiedTime,basic:lastAccessTime,basic:creationTime,basic:size,basic:isRegularFile,basic:isDirectory,basic:isSymbolicLink,basic:isOther,basic:fileKey"));
        return map;
      }
      else {
        if (attr.indexOf(':') == -1) attr = "basic:" + attr;
        map.put(attr, getAttribute(attr));
      }
    }

    return map;
  }

  public abstract Path relativize(Path other);

  public Path resolve(RealPath other) {
    if (other == null) return this;
    if (other.isAbsolute()) return other;
    return fs.getPath(new File(file, other.file.toString()).toString());     
  }

  public Path resolve(Path other) {
    if (other == null) return this;
    if (other.isAbsolute()) return other;
    return null;
  }

  public Path resolve(String other) {
    return resolve(fs.getPath(other));
  }

  public void setAttribute(String attribute,
                           Object value, LinkOption... options)
                                                           throws IOException {
    if (options.length > 0) throw new UnsupportedOperationException();

    if (attribute.indexOf(':') == -1) attribute = "basic:" + attribute;
    final int colon = attribute.indexOf(':');
    final String vname = attribute.substring(0, colon);
    final String aname = attribute.substring(colon+1);
                                                       
    if ("basic".equals(vname)) {
      final RealFileAttributeView view = new RealFileAttributeView(this);

      if ("lastModifiedTime".equals(aname)) {
        view.setTimes((FileTime) value, null, null);
      }
      else {
        throw new UnsupportedOperationException(attribute);
      }
    }
    else {
      throw new UnsupportedOperationException(attribute);
    }   
  }

  public abstract boolean startsWith(Path other);

  public abstract Path subpath(int beginIndex, int endIndex);

  public Path toAbsolutePath() {
    return fs.getPath(file.getAbsolutePath());
  }

  public Path toRealPath(boolean resolveLinks) throws IOException {
    return fs.getPath(file.getCanonicalPath());
  }

  @Override
  public String toString() {
    return file.toString();
  }

  public URI toUri() {
    return file.toURI();
  }
}

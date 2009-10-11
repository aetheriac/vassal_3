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
 
  final String path; 
  final int[] parts;

  protected RealPath(String path, RealFileSystem fs) {
    this(new File(path), fs);
  }

  protected RealPath(File file, RealFileSystem fs) {
    this.file = file;
    this.fs = fs;

    path = file.toString();
    parts = splitPath(path);
  }

  private int[] splitPath(String path) {
    // File ctor removes duplicate and trailing separators. Hence, each
    // instance of separator splits two names.

    final ArrayList<Integer> l = new ArrayList<Integer>();
    int i = 0;

    // find end of root, if present
    i = findRootSep(path);
    l.add(++i);

    // if at end, then we are just a root
    if (i >= path.length()) return new int[0];

    // record positions of all separators
    while ((i = path.indexOf(File.separator, i)) >= 0) l.add(++i);
   
    // record end of path + 1
    l.add(path.length()+1);
 
    // convert from List<Integer> to int[]
    final int[] parts = new int[l.size()];
    for (i = 0; i < parts.length; ++i) parts[i] = l.get(i);

    // The result is a list of offsets for the starts of the names, plus
    // a final element for the position of end of the path.
    return parts;
  }

  protected abstract int findRootSep(String s);

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
      default:
        throw new UnsupportedOperationException(m.toString());
      }
    }
  }
  
  public Path createDirectory(FileAttribute<?>... attrs) throws IOException {
    if (attrs.length > 0) throw new UnsupportedOperationException();
    if (file.exists()) throw new FileAlreadyExistsException(file.toString());
    if (!file.mkdir()) throw new FileSystemException(file.toString());
    return this;
  }

  public Path createFile(FileAttribute<?>... attrs) throws IOException {
    if (attrs.length > 0) throw new UnsupportedOperationException();
    if (file.exists()) throw new FileAlreadyExistsException(file.toString());
    if (!file.createNewFile()) throw new FileSystemException(file.toString());
    return this;
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

  public boolean endsWith(Path other) {
    if (other.isAbsolute()) {
      return this.isAbsolute() ? this.equals(other) : false;
    }
    else {
      final int oc = other.getNameCount();
      final int tc = this.getNameCount();
      return oc <= tc ? other.equals(this.subpath(tc-oc, tc)) : false;
    }
  }

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
    return parts.length == 0 ? null : subpath(parts.length-2, parts.length-1);
  }

  public int getNameCount() {
    return parts.length > 0 ? parts.length-1 : 0;
  }

  public Path getParent() {
    if (parts.length == 0) return null;  // a root has no parent
    return fs.getPath(path.substring(0, parts[parts.length-2]-1));
  }

  public Path getRoot() {
    if (parts.length == 0) return this;  // we are a root
    if (parts[0] == 0) return null;      // we are relative
    return fs.getPath(path.substring(0, parts[0]));
  }

  @Override
  public int hashCode() {
    return file.hashCode();
  }

  public boolean isAbsolute() {
    return parts.length == 0 || parts[0] != 0;
  }

  public boolean isHidden() throws IOException {
    return file.isHidden();
  }

  public boolean isSameFile(Path other) {
    return this.equals(other);
  }

  public Iterator<Path> iterator() {
    return new Iterator<Path>() {
      private int i = 0;

      public boolean hasNext() {
        return i < parts.length-1;
      }

      public Path next() {
        return fs.getPath(path.substring(parts[i], parts[i+1]-1));
      }

      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

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

    return opt;
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
    if (parts.length == 0) return this;   // root is already normalized

    final ArrayList<String> l = new ArrayList<String>(parts.length);

    // Remove redundant parts.
    for (int i = parts.length; i > 0; --i) {
      final String n = path.substring(parts[i-1], parts[i]-1);

      // ".": Skip.
      if (n.equals(".")) continue;

      // "..": If not at the beginning, skip the previous name
      if (n.equals("..") && i != 1) { --i; continue; }
      
      l.add(n);
    }

    // Rebuild the normalized path.
    final StringBuilder sb =
      new StringBuilder(parts[0] == 0 ? "" : path.substring(0, parts[0]-1));
    for (int i = l.size()-1; i >= 0; --i) {
      sb.append(File.separator).append(l.get(i));
    } 

    return fs.getPath(sb.toString());
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

  public Path relativize(Path other) {
    // FIXME: Implementing this will require some thought...
    throw new UnsupportedOperationException();
  }

  public Path resolve(Path other) {
    if (other == null) return this;
    if (other.isAbsolute()) return other;
    return fs.getPath(new File(file, other.toString()).toString());     
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

  public boolean startsWith(Path other) {
    if (this.isAbsolute() != other.isAbsolute()) return false;

    final int oc = other.getNameCount();
    final int tc = this.getNameCount();
    return oc <= tc ? other.equals(this.subpath(0, oc)) : false;
  }

  public Path subpath(int start, int end) {
    if (start < 0) throw new IllegalArgumentException();
    if (start >= parts.length) throw new IllegalArgumentException();
    if (end <= start) throw new IllegalArgumentException();
    if (end > parts.length) throw new IllegalArgumentException();

    return fs.getPath(path.substring(parts[start], parts[end]-1));
  }

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

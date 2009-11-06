package VASSAL.tools.nio.file.fs;

import static VASSAL.tools.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static VASSAL.tools.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static VASSAL.tools.nio.file.StandardOpenOption.APPEND;
import static VASSAL.tools.nio.file.StandardOpenOption.CREATE_NEW;
import static VASSAL.tools.nio.file.StandardOpenOption.READ;
import static VASSAL.tools.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import VASSAL.tools.StringUtils;
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
import VASSAL.tools.nio.file.NoSuchFileException;
import VASSAL.tools.nio.file.NotDirectoryException;
import VASSAL.tools.nio.file.OpenOption;
import VASSAL.tools.nio.file.Path;
import VASSAL.tools.nio.file.StandardOpenOption;
import VASSAL.tools.nio.file.attribute.BasicFileAttributeView;
import VASSAL.tools.nio.file.attribute.FileAttribute;
import VASSAL.tools.nio.file.attribute.FileAttributeView;
import VASSAL.tools.nio.file.attribute.FileTime;

public abstract class RealPath extends AbstractPath {
  protected final File file;
  protected final RealFileSystem fs;
 
  protected final String path; 
  protected final int[] seps;

  public RealPath(String path, RealFileSystem fs) {
    this.file = new File(path);
    this.fs = fs;

    this.path = file.toString();
    seps = splitPath(this.path);
  }

  protected int[] splitPath(String path) {
    // File ctor removes duplicate separators. Hence, each
    // instance of separator splits two names.

    final ArrayList<Integer> sl = new ArrayList<Integer>();
    int i = 0;

    // find end of root, if present
    i = findRootSep(path);
    sl.add(i++);

    // if at end, then we are just a root
    if (i >= path.length()) return new int[0];

    // record positions of all separators
    while ((i = path.indexOf(File.separator, i)) >= 0) sl.add(i++);

    // record end of path
    sl.add(path.length());

// FIXME: replace with a method in ArrayUtils or something from Apache Commons
    // convert from List<Integer> to int[]
    final int[] seps = new int[sl.size()];
    for (i = 0; i < seps.length; ++i) seps[i] = sl.get(i);
// END FIXME

    // The result is a list of offsets for the starts of the names, plus
    // a final element for the position of end of the path.
    return seps;
  }

  /**
   * Returns the position of the separator after the root element.
   *
   * @param s the {@code String} to check
   *
   * @return the position of the separator following the root element, or
   * -1 if the path is relative
   */
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

  public FileStore getFileStore() throws IOException {
    // RealFileSystem has only one FileStore
    return fs.getFileStores().iterator().next();
  }

  public FileSystem getFileSystem() {
    return fs;
  }

  public Path getName() {
    return seps.length == 0 ? null : subpath(seps.length-2, seps.length-1);
  }

  public Path getName(int index) {
    return subpath(index, index+1);
  }

  public int getNameCount() {
    return seps.length > 0 ? seps.length-1 : 0;
  }

  public Path getParent() {
    if (seps.length == 0) return null;  // a root has no parent
    return fs.getPath(path.substring(0, seps[seps.length-2]));
  }

  public Path getRoot() {
    if (seps.length == 0) return this;  // we are a root
    if (seps[0] == -1) return null;     // we are relative
    return fs.getPath(path.substring(0, seps[0]+1));
  }

  @Override
  public int hashCode() {
    return file.hashCode();
  }

  public boolean isAbsolute() {
    return seps.length == 0 || seps[0] != -1;
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
        return i < seps.length-1;
      }

      public Path next() {
        return fs.getPath(path.substring(seps[i]+1, seps[++i]));
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

  protected Set<StandardOpenOption> standardOpenOptionSet(OpenOption... opts) {
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

  public DirectoryStream<Path> newDirectoryStream(String glob)
                                                           throws IOException {
    throw new UnsupportedOperationException();
  }

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
    if (seps.length == 0) return this;   // root is already normalized
    
    int previousDirsAtBeginning = 0;

    final ArrayList<String> outputParts = new ArrayList<String>(seps.length);

    // Remove redundant parts.
    for (int i = 0; i < seps.length-1; ++i) {
      final String currentInputPart = path.substring(seps[i]+1, seps[i+1]);

      // ".": Skip.
      if (currentInputPart.equals(".")) continue;

      // "..": Scratch this and the previous name, if any.
      if (currentInputPart.equals("..")) {
        final int s = outputParts.size();
        if (s > previousDirsAtBeginning) {
          outputParts.remove(s-1);
        } else if (!this.isAbsolute()){
          outputParts.add(currentInputPart);
          previousDirsAtBeginning++;
        }
        continue;
      }

      // Otherwise add this name to the list.
      outputParts.add(currentInputPart);
    }

    // Rebuild the normalized path.
    return fs.getPath(
      (seps[0] == -1 ? "" : path.substring(0, seps[0]+1)) +
      StringUtils.join(File.separator, outputParts)
    );
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
    return oc <= tc ? other.subpath(0,oc).equals(this.subpath(0, oc)) : false;
  }

  public Path subpath(int start, int end) {
    if (start < 0) throw new IllegalArgumentException();
    if (start >= seps.length-1) throw new IllegalArgumentException();
    if (end <= start) throw new IllegalArgumentException();
    if (end > seps.length-1) throw new IllegalArgumentException();

    return fs.getPath(path.substring(seps[start]+1, seps[end]));
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

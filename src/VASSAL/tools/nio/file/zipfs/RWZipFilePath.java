package VASSAL.tools.nio.file.zipfs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

import VASSAL.tools.io.IOUtils;

import VASSAL.tools.nio.channels.SeekableByteChannel;
import VASSAL.tools.nio.file.AccessDeniedException;
import VASSAL.tools.nio.file.AccessMode;
import VASSAL.tools.nio.file.CopyOption;
import VASSAL.tools.nio.file.DirectoryNotEmptyException;
import VASSAL.tools.nio.file.DirectoryStream;
import VASSAL.tools.nio.file.FileAlreadyExistsException;
import VASSAL.tools.nio.file.LinkOption;
import VASSAL.tools.nio.file.NoSuchFileException;
import VASSAL.tools.nio.file.OpenOption;
import VASSAL.tools.nio.file.Path;
import VASSAL.tools.nio.file.Paths;
import VASSAL.tools.nio.file.StandardOpenOption;
import VASSAL.tools.nio.file.attribute.FileAttribute;

import static VASSAL.tools.nio.file.zipfs.RWZipFileSystem.DELETED;

class RWZipFilePath extends ZipFilePath {
  protected final RWZipFileSystem fs;

  RWZipFilePath(RWZipFileSystem fileSystem, byte[] pathInZip) {
    super(fileSystem, pathInZip);
    fs = fileSystem;
  }

  RWZipFilePath(RWZipFileSystem fileSystem,
                                         byte[] pathInZip, byte[] pathForZip) {
    super(fileSystem, pathInZip, pathForZip);
    fs = fileSystem;
  }

  @Override
  RWZipFilePath create(ZipFileSystem fileSystem, byte[] pathInZip) {
    return new RWZipFilePath((RWZipFileSystem) fileSystem, pathInZip);
  }

  @Override
  RWZipFilePath create(ZipFileSystem fileSystem,
                                         byte[] pathInZip, byte[] pathForZip) {
    return new RWZipFilePath(
      (RWZipFileSystem) fileSystem, pathInZip, pathForZip);
  }

  @Override
  public RWZipFilePath getParent() {
    return (RWZipFilePath) super.getParent();
  }

  @Override
  public RWZipFileSystem getFileSystem() {
    return fs;
  }

  @Override
  public Path createDirectory(FileAttribute<?>... attrs) throws IOException {
    try {
      fs.writeLock();

      if (exists()) throw new FileAlreadyExistsException(toString());
      fs.real.put(this, fs.createTempDirectory(attrs));
      return this;
    }
    finally {
      fs.writeUnlock();
    }
  }

  @Override
  public Path createFile(FileAttribute<?>... attrs) throws IOException {
    try {
      fs.writeLock();

      if (exists()) throw new FileAlreadyExistsException(toString());
      fs.real.put(this, fs.createTempFile(attrs));
      return this;
    }
    finally {
      fs.writeUnlock();
    }
  }

  @Override
  public InputStream newInputStream(OpenOption... options) throws IOException {
    try {
      fs.readLock();

      final Path rpath = fs.real.get(this);
      if (rpath != null) {
        if (rpath == DELETED) throw new NoSuchFileException(toString());
        return fs.wrapReadLocked(this, rpath.newInputStream(options));
      }
      else {
        return fs.wrapReadLocked(this, super.newInputStream(options));
      }      
    }
    finally {
      fs.readUnlock();
    }
  }

  @Override
  public DirectoryStream<Path> newDirectoryStream(
              DirectoryStream.Filter<? super Path> filter) throws IOException {
    try {
      fs.readLock();
      return new RWZipFileStream(
        (RWZipFilePath) getResolvedPathForZip(), filter);
    }
    finally {
      fs.readUnlock();
    } 
  }
 
  @Override
  public void delete() throws IOException {
    try {
      fs.writeLock();

      if (!exists()) throw new NoSuchFileException(toString());

      // delete only empty directories
      if (Boolean.TRUE.equals(getAttribute("isDirectory"))) {
        DirectoryStream<Path> ds = null;
        try {
          ds = newDirectoryStream();
          if (ds.iterator().hasNext()) {
            throw new DirectoryNotEmptyException(toString());
          }
          ds.close();
        }
        finally {
          IOUtils.closeQuietly(ds);
        }
      }

      final Path old = fs.real.put(this, DELETED);
      if (old != null) old.delete();
    }
    finally {
      fs.writeUnlock();
    }
  }

  @Override
  public void deleteIfExists() throws IOException {
    try {
      fs.writeLock();
      if (exists()) delete();
    }
    finally {
      fs.writeLock();
    }
  }

  @Override
  public SeekableByteChannel newByteChannel(Set<? extends OpenOption> options,
                                            FileAttribute<?>... attrs)
                                                           throws IOException {
    if (options.contains(StandardOpenOption.WRITE) ||
        options.contains(StandardOpenOption.APPEND)) {
      try {
        fs.writeLock();

        Path rpath = fs.real.get(this);
        if (rpath == null || rpath == DELETED) {
          rpath = fs.createTempFile(attrs);
          fs.real.put(this, rpath);
        }

        return fs.wrapWriteLocked(this, rpath.newByteChannel(options, attrs));
      }
      finally {
        fs.writeUnlock();
      }
    }
    else {
      try {
        fs.readLock();

        final Path rpath = fs.real.get(this);
        if (rpath != null) {
          if (rpath == DELETED) throw new NoSuchFileException(toString());
          return fs.wrapReadLocked(this, rpath.newByteChannel(options, attrs));
        }
        else {
          return fs.wrapReadLocked(this, super.newByteChannel(options, attrs));
        }  
      }
      finally {
        fs.readUnlock();
      }
    }
  }

  @Override
  public void checkAccess(AccessMode... modes) throws IOException {
    boolean w = false;
    boolean x = false;

    for (AccessMode mode : modes) {
      switch (mode) {
        case READ:
          break;
        case WRITE:
          w = true;
          break;
        case EXECUTE:
          x = true;
          break;
        default:
          throw new UnsupportedOperationException();
      }
    }

    try {
      fs.begin();

      final ZipFilePath resolvedZipPath = getResolvedPathForZip();
      final int nameCount = resolvedZipPath.getNameCount();
      if (nameCount == 0) {
        throw new NoSuchFileException(toString());
      }

      try {
        fs.readLock();

        final Path rpath = fs.real.get(this);
        if (rpath != null) {
          // the path has been modified
          if (rpath == DELETED) throw new NoSuchFileException(toString());
          rpath.checkAccess(modes);
        }
        else {
          // the path is original to the ZIP archive
          final ZipEntryInfo ze = ZipUtils.getEntry(resolvedZipPath);

          if (w) {
            // check write access on archive file
            try {
              final Path zpath = Paths.get(fs.getZipFileSystemFile());
              zpath.checkAccess(AccessMode.WRITE);
            }
            catch (AccessDeniedException e) {
              throw (IOException) new AccessDeniedException(
                "write access denied for the file: " + toString()).initCause(e);
            }
            catch (IOException e) {
              throw (IOException) new IOException().initCause(e);
            }
          }

          if (x) {
            long attrs = ze.extAttrs;
            if (!((((attrs << 4) >> 24) & 0x04) == 0x04)) {
              throw new AccessDeniedException(
                "execute access denied for the file: " + this.toString());
            }
          }
        }
      }
      finally {
        fs.readUnlock();
      }
    }
    finally {
      fs.end();
    }
  }

  @Override
  public OutputStream newOutputStream(OpenOption... options)
                                                           throws IOException {
    try {
      fs.writeLock();

      Path rpath = fs.real.get(this);
      if (rpath == null || rpath == DELETED) {
        rpath = fs.createTempFile();
        fs.real.put(this, rpath);
      }

      return fs.wrapWriteLocked(this, rpath.newOutputStream(options));
    }
    finally {
      fs.writeUnlock();
    }
  }

  @Override
  public Path moveTo(Path target, CopyOption... options) throws IOException {
    try {
      fs.writeLock();

      if (target.getFileSystem().provider() != fs.provider()) {
        // destination is not local
        super.copyTo(target, options);
        delete();
      }
      else {
        // destination is local
        final Path src = fs.real.remove(this);
        if (src != null) {
          // file is modified, just remap
          if (src == DELETED) throw new NoSuchFileException(toString());
          fs.real.put((RWZipFilePath) target, src);
        }
        else {
          // file is unmodified, copy out to temp
          final Path dst = fs.createTempFile();
          super.copyTo(dst, options);
          fs.real.put((RWZipFilePath) target, dst);
        }
      }
      
      return target;
    }
    finally {
      fs.writeUnlock();
    }
  }

  @Override
  public Path copyTo(Path target, CopyOption... options) throws IOException {
    if (target.getFileSystem().provider() != fs.provider()) {
      // destination is not local
      try {
        fs.readLock();

        final Path src = fs.real.get(this);
        if (src != null) {
          // file is modified
          if (src == DELETED) throw new NoSuchFileException(toString());
          src.copyTo(target, options);
        }
        else {
          // file is unmodified
          super.copyTo(target, options);
        }
      }
      finally {
        fs.readUnlock();
      }
    }
    else {
      // destination is local
      try {
        fs.writeLock();

        final Path dst = fs.createTempFile();
        final Path src = fs.real.get(this);
        if (src != null) {
          // file is modified
          if (src == DELETED) throw new NoSuchFileException(toString());
          src.copyTo(dst, options);
        }
        else {
          // file is unmodified
          super.copyTo(dst, options);
        }

        fs.real.put((RWZipFilePath) target, dst);
      }
      finally {
        fs.writeUnlock();
      }
    }

    return target;
  }

  @Override
  public Object getAttribute(String attribute, LinkOption... options)
                                                           throws IOException {
    try {
      fs.readLock();

      final Path rpath = fs.real.get(this);
      if (rpath != null) {
        if (rpath == DELETED) throw new NoSuchFileException(toString());
        return rpath.getAttribute(attribute, options);
      }
      else {
        return super.getAttribute(attribute, options);
      }
    }
    finally {
      fs.readUnlock();
    }
  }

/*
  @Override
  public void setAttribute(String attribute, Object value,
                                                       LinkOption... options)
                                                           throws IOException {
    throw new UnsupportedOperationException();
  }
*/

/* 
  @Override
  public Map<String,?> readAttributes(String attribute, LinkOption... options)
                                                           throws IOException {
  }
  
  @Override
  public <V extends FileAttributeView> V getFileAttributeView(Class<V> type, LinkOption... options) {
  }
*/

}

package VASSAL.tools.nio.file.zipfs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import VASSAL.tools.io.IOUtils;
import VASSAL.tools.nio.channels.SeekableByteChannel;
import VASSAL.tools.nio.file.ClosedFileSystemException;
import VASSAL.tools.nio.file.FileRef;
import VASSAL.tools.nio.file.InvalidPathException;
import VASSAL.tools.nio.file.Path;
import VASSAL.tools.nio.file.Paths;
import VASSAL.tools.nio.file.StandardCopyOption;
import VASSAL.tools.nio.file.attribute.FileAttribute;

public class RWZipFileSystem extends ZipFileSystem {

  protected final ConcurrentMap<RWZipFilePath,Path> real =
    new ConcurrentHashMap<RWZipFilePath,Path>();

  // dummy value for real map
  static final Path DELETED = new RWZipFilePath(null, null, null);

  protected final ReadWriteLock lock = new ReentrantReadWriteLock();

  RWZipFileSystem(ZipFileSystemProvider provider, FileRef fref)
                                                           throws IOException {
    this(provider, fref.toString(), "/");
  }

  RWZipFileSystem(ZipFileSystemProvider provider,
                  String path, String defaultDir) throws IOException {
    super(provider, path, defaultDir);
  }

  @Override
  public RWZipFilePath getPath(String path) {
    if (path == null) {
      throw new NullPointerException();
    }
    if (path.equals("")) {
      throw new InvalidPathException(path, "path should not be empty");
    }
    try {
      begin();
      byte[] parsedPath = ZipPathParser.normalize(path).getBytes();
      return new RWZipFilePath(this, parsedPath);
    }
    finally {
      end();
    }
  }

  @Override
  public boolean isReadOnly() {
    return false;
  }

  @Override
  public void close() throws IOException {
    final RWZipFilePath root = getPath("/");

    try {
      writeLock(root);
      flush();
      super.close();
    }
    finally {
      writeUnlock(root);
    }
  }

/*
  private class CopyUnmodifiedVisitor extends SimpleFileVisitor<RWZipFilePath> {
    private final ZipOutputStream out;

    public CopyUnmodifiedVisitor(ZipOutputStream out) {
      this.out = out;
    }

    private IOException fail = null;

    public IOException getException() {
      return fail;
    }

    @Override
    public FileVisitResult preVisitDirectory(RWZipFilePath dir) {
    }

    @Override
    public FileVisitResult visitFile(RWZipFilePath file,
                                     BasicFileAttributes attrs) {
      if (!real.contains(file)) {
        InputStream in = null;
        try {
          in = file.newInputStream();
          IOUtils.copy(in, out);


          in.close();
        }
        catch (IOException e) {
          fail = e;
          return FileVisitResult.TERMINATE;
        }
        finally {
          IOUtils.closeQuietly(in):
        }
      }
          
     return FileVisitResult.CONTINUE;
    }
  }

        final CopyUnmodifiedVistor visitor = new CopyUnmodifiedVisitor(out);
        Files.walkFileTree(getPath("/"), visitor);

        final IOException fail = visitor.getException();
        if (fail != null) {
          throw (IOException) new IOException().initCause(fail);
        }

*/

  Path createTempFile(FileAttribute<?>... attrs) throws IOException {
    final Path tmp =
      Paths.get(File.createTempFile("rwzipfs", "tmp").toString());

    tmp.deleteIfExists();
    tmp.createFile(attrs);

    return tmp;
  }

  Path createTempDirectory(FileAttribute<?>... attrs) throws IOException {
    final Path tmp =
      Paths.get(File.createTempFile("rwzipfs", "tmp").toString());

    tmp.deleteIfExists();
    tmp.createDirectory(attrs);

    return tmp;
  }

  public void flush() throws IOException {
    final RWZipFilePath root = getPath("/");

    try {
      writeLock(root);

      // no modifications, nothing to do
      if (real.isEmpty()) return;

      // create a temp file into which to write the new ZIP archive
      final Path nzip =
        Paths.get(File.createTempFile("rwzipfs", "zip").toString());

      ZipOutputStream out = null;
      try {
        out = new ZipOutputStream(nzip.newOutputStream());

        ZipFile zf = null;
        try {
          zf = new ZipFile(getZipFileSystemFile());
        
          // copy all unchanged files from old archive to new archive
          final Enumeration<? extends ZipEntry> en = zf.entries();
          while (en.hasMoreElements()) {
            final ZipEntry e = en.nextElement();
            final RWZipFilePath path = getPath(e.toString());
     
            if (real.containsKey(path)) continue;

            if (Boolean.TRUE.equals(path.getAttribute("isDirectory"))) {
              out.putNextEntry(e);
            }
            else {
              InputStream in = null;
              try {
                in = zf.getInputStream(e);
         
                // We can't reuse entries for compressed files because
                // there's no way to reset the fields to acceptable values. 
                final ZipEntry ze = new ZipEntry(e.getName());
                ze.setMethod(ZipEntry.DEFLATED);

                out.putNextEntry(ze);
                IOUtils.copy(in, out);
                out.closeEntry();
                in.close();
              }
              finally {
                IOUtils.closeQuietly(in);
              }
            }
          }
        
          zf.close();
        }
        finally {
          IOUtils.closeQuietly(zf);
        }

        // copy all new files to new archive
        for (Map.Entry<RWZipFilePath,Path> e : real.entrySet()) {
          final Path rpath = e.getValue();
          if (rpath == DELETED) continue;  // path was deleted, skip

          final ZipEntry ze = new ZipEntry(rpath.toString());
          if (Boolean.TRUE.equals(rpath.getAttribute("isDirectory"))) {
            out.putNextEntry(ze);
          }
          else {
            ze.setMethod(ZipEntry.DEFLATED);

            InputStream in = null;
            try {
              in = rpath.newInputStream();
              out.putNextEntry(ze);
              IOUtils.copy(in, out);
              out.closeEntry();
              in.close();
            }
            finally {
              IOUtils.closeQuietly(in);
            }
          }
        }
  
        out.close();
      }
      finally {
        IOUtils.closeQuietly(out);
      }

      final Path ozip = Paths.get(getZipFileSystemFile());

      // replace the old archive with the new one
      nzip.moveTo(ozip, StandardCopyOption.REPLACE_EXISTING);

      // delete external versions of new files
      for (Path rpath : real.values()) {
        if (rpath != DELETED) rpath.delete();
      }
      real.clear();
    }
    finally {
      writeUnlock(root);
    }
  }

  void readLock(RWZipFilePath path) {
    lock.readLock().lock();
  }

  void readUnlock(RWZipFilePath path) {
    lock.readLock().unlock();
  }

  void writeLock(RWZipFilePath path) {
    lock.writeLock().lock();
  }

  void writeUnlock(RWZipFilePath path) {
    lock.writeLock().unlock();
  }

  final InputStream wrapReadLocked(RWZipFilePath path, InputStream in) {
    return new LockedInputStream(path, in);
  }

  final OutputStream wrapWriteLocked(RWZipFilePath path, OutputStream in) {
    return new LockedOutputStream(path, in);  
  }

  final SeekableByteChannel wrapReadLocked(RWZipFilePath path,
                                                      SeekableByteChannel ch) {
    return new ReadLockedChannel(path, ch);
  }

  final SeekableByteChannel wrapWriteLocked(RWZipFilePath path,
                                                      SeekableByteChannel ch) {
    return new WriteLockedChannel(path, ch);
  }

  private class LockedInputStream extends ZipIO.RegisteredInputStream {
    protected boolean closed = false;
    protected RWZipFilePath path;

    public LockedInputStream(RWZipFilePath path, InputStream in) {
      super(path.getFileSystem(), in);
      this.path = path;
      readLock(path);
    }

    @Override
    public void close() throws IOException {
      if (closed) return;

      try {
        super.close();
        closed = true;
      }
      finally {
        readUnlock(path);
      }
    }
  }

  private class LockedOutputStream extends ZipIO.RegisteredOutputStream {
    protected boolean closed = false;
    protected RWZipFilePath path;

    public LockedOutputStream(RWZipFilePath path, OutputStream out) {
      super(path.getFileSystem(), out);
      this.path = path;
      writeLock(path);
    }

    @Override
    public void close() throws IOException {
      if (closed) return;

      try {
        super.close();
        closed = true;
      }
      finally {
        writeUnlock(path);
      }
    }
  }

  private class ReadLockedChannel extends ZipIO.RegisteredChannel {
    protected boolean closed = false;
    protected RWZipFilePath path;

    public ReadLockedChannel(RWZipFilePath path, SeekableByteChannel ch) {
      super(path.getFileSystem(), ch);
      this.path = path;
      readLock(path);
    }

    @Override
    public void close() throws IOException {
      if (closed) return;    

      try {
        super.close();
        closed = true;
      }
      finally {
        readUnlock(path);
      }
    }
  }

  private class WriteLockedChannel extends ZipIO.RegisteredChannel {
    protected boolean closed = false;
    protected RWZipFilePath path;

    public WriteLockedChannel(RWZipFilePath path, SeekableByteChannel ch) {
      super(path.getFileSystem(), ch);
      this.path = path;
      writeLock(path);
    }

    @Override
    public void close() throws IOException {
      if (closed) return;

      try {
        super.close();
        closed = true;
      }
      finally {
        writeUnlock(path);
      }
    }
  }
}

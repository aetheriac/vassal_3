package VASSAL.tools.nio.file.zipfs; 

import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import VASSAL.tools.nio.channels.FileChannelAdapter;
import VASSAL.tools.nio.channels.SeekableByteChannel;
import VASSAL.tools.nio.file.OpenOption;
import VASSAL.tools.nio.file.Path;
import VASSAL.tools.nio.file.Paths;

class ZipIO {
  private ZipIO() {}

  // This node is the parent of all nodes corresponding to ZIP archive
  // roots. This node should never be locked.
  private static final ZipNode roots = new ZipNode(null, "");

  static void readLock(ZipFilePath path) {
    lockSubtree(path);
  }

  static void writeLock(ZipFilePath path) {
    ZipNode zn = lockSubtree(path.getParent());
    zn = buildNode(zn, path.getName().toString());
    zn.lock.writeLock().lock();
  }

  private static ZipNode lockSubtree(ZipFilePath path) {

    ZipNode zn = buildNode(roots, path.getFileSystem().getZipFileSystemFile());
    zn.lock.readLock().lock();

    for (Path part : path) {
      zn = buildNode(zn, part.toString());
      zn.lock.readLock().lock();
    }
    
    return zn;
  }

  private static ZipNode buildNode(ZipNode parent, String name) {
    final ZipNode n0 = new ZipNode(parent, name);
    final ZipNode n1 = parent.children.putIfAbsent(name, n0);
    return n1 == null ? n0 : n1;
  }

  static void readUnlock(ZipFilePath path) {
    unlockSubtree(path);  
  }

  static void writeUnlock(ZipFilePath path) {
    ZipNode zn =
      roots.children.get(path.getFileSystem().getZipFileSystemFile());
   
    for (Path part : path) zn = zn.children.get(part.toString());
    zn.lock.writeLock().unlock();

    readUnlock(path.getParent());
  }

  private static void unlockSubtree(ZipFilePath path) {
    ZipNode zn =
      roots.children.get(path.getFileSystem().getZipFileSystemFile());
    
    for (Path part : path) zn = zn.children.get(part.toString());

    // unlock from root to leaf
    while (zn != roots) {
System.out.println("u " + zn.name);
      zn.lock.readLock().unlock();
      zn = zn.parent;
    }
  }

  static InputStream wrap(ZipFilePath path, InputStream in) {
    return new RegisteredInputStream(path.getFileSystem(), in);
  }

  static OutputStream wrap(ZipFilePath path, OutputStream in) {
    return new RegisteredOutputStream(path.getFileSystem(), in);
  }

  static SeekableByteChannel wrap(ZipFilePath path, SeekableByteChannel ch) {
    return new RegisteredChannel(path.getFileSystem(), ch);
  }

  static InputStream wrapLocked(ZipFilePath path, InputStream in) {
    return new LockedInputStream(path, in);
  }

  static OutputStream wrapLocked(ZipFilePath path, OutputStream in) {
    return new LockedOutputStream(path, in);
  }

  static SeekableByteChannel wrapReadLocked(ZipFilePath path,
                                            SeekableByteChannel ch) {
    return new ReadLockedChannel(path, ch);
  }

  static SeekableByteChannel wrapWriteLocked(ZipFilePath path,
                                             SeekableByteChannel ch) {
    return new WriteLockedChannel(path, ch);
  }

  private static class RegisteredInputStream extends FilterInputStream {
    private final ZipFileSystem fs;

    public RegisteredInputStream(ZipFileSystem fs, InputStream in) {
      super(in);
      this.fs = fs;
      fs.registerCloseable(this);
    }

    @Override
    public void close() throws IOException {
      try {
        in.close();
      }
      finally {
        fs.unregisterCloseable(this);
      }
    }
  }

  private static class LockedInputStream extends RegisteredInputStream {
    private final ZipFilePath path;

    public LockedInputStream(ZipFilePath path, InputStream in) {
      super(path.getFileSystem(), in);
      this.path = path;
      readLock(path);
    }

    @Override
    public void close() throws IOException {
      try {
        super.close();
      }
      finally {
        readUnlock(path);
      }
    }
  }

  private static class RegisteredOutputStream extends FilterOutputStream {
    private final ZipFileSystem fs;

    public RegisteredOutputStream(ZipFileSystem fs, OutputStream out) {
      super(out);
      this.fs = fs;
      fs.registerCloseable(this);
    }

    @Override
    public void close() throws IOException {
      try {
        out.close();
      }
      finally {
        fs.unregisterCloseable(this);
      }
    }
  }

  private static class LockedOutputStream extends RegisteredOutputStream {
    private final ZipFilePath path;

    public LockedOutputStream(ZipFilePath path, OutputStream out) {
      super(path.getFileSystem(), out);
      this.path = path;
      writeLock(path);
    }

    @Override
    public void close() throws IOException {
      try {
        super.close();
      }
      finally {
        writeUnlock(path);
      }
    }
  }

  private static class RegisteredChannel implements SeekableByteChannel {
    private final ZipFileSystem fs;
    private final SeekableByteChannel ch;

    public RegisteredChannel(ZipFileSystem fs, SeekableByteChannel ch) {
      this.fs = fs;
      this.ch = ch;
      fs.registerCloseable(this);
    }

    public long position() throws IOException {
      return ch.position();
    }

    public SeekableByteChannel position(long newPosition) throws IOException {
      ch.position(newPosition);
      return this;
    }
    
    public int read(ByteBuffer dst) throws IOException {
      return ch.read(dst);
    }
    
    public long size() throws IOException {
      return ch.size();
    }

    public SeekableByteChannel truncate(long size) throws IOException {
      ch.truncate(size);
      return this;
    }

    public int write(ByteBuffer src) throws IOException {
      return ch.write(src);
    }

    public boolean isOpen() {
      return ch.isOpen();
    }

    public void close() throws IOException {
      try {
        ch.close();
      }
      finally {
        fs.unregisterCloseable(this);
      }
    }
  }

  private static class ReadLockedChannel extends RegisteredChannel {
    private final ZipFilePath path;

    public ReadLockedChannel(ZipFilePath path, SeekableByteChannel ch) {
      super(path.getFileSystem(), ch);
      this.path = path;
    }

    @Override
    public void close() throws IOException {
      try {
        super.close();
      }
      finally {
        readUnlock(path);
      }
    }
  }

  private static class WriteLockedChannel extends RegisteredChannel {
    private final ZipFilePath path;

    public WriteLockedChannel(ZipFilePath path, SeekableByteChannel ch) {
      super(path.getFileSystem(), ch);
      this.path = path;
    }

    @Override
    public void close() throws IOException {
      try {
        super.close();
      }
      finally {
        writeUnlock(path);
      }
    }
  }

  static InputStream in(ZipFilePath path, OpenOption... opts)
                                                           throws IOException {
    final String zf = path.getZipFile();
    final ZipFile zfile = new ZipFile(zf);
    final String entryStr =
      path.getEntryName(path.getEntryNameCount() - 1).toString();

// FIXME: zfile not closed along some paths!!! 
    final ZipEntry entry = zfile.getEntry(entryStr);
    if (entry == null) {
      zfile.close();
      throw new IOException("entry not found" + entryStr);
    }

    return wrap(path, zfile.getInputStream(entry));
  }

/*
  static OutputStream out(ZipFilePath path, OpenOption... opts)
                                                           throws IOException {
  }
*/

  static SeekableByteChannel channel(ZipFilePath path,
                                     Set<? extends OpenOption> opts)
                                                           throws IOException {
    final String zf = path.getZipFile();
    final ZipFile zfile = new ZipFile(zf);
    final String entryStr =
      path.getEntryName(path.getEntryNameCount() - 1).toString();
    
// FIXME: zfile not closed along some paths!!! 
    final ZipEntry entry = zfile.getEntry(entryStr);
    if (entry == null) {
      throw new IOException("entry not found" + entryStr);
    }

    final InputStream in = zfile.getInputStream(entry);
    final Path pathToZip = Paths.get(ZipUtils.readFileInZip(in));
    zfile.close();

    final ZipFileSystem fs = path.getFileSystem();
    return wrap(path,
      new FileChannelAdapter(fs.provider().newFileChannel(pathToZip, opts))
    );
  }
}

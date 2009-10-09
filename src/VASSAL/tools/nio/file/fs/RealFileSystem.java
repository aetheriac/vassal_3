package VASSAL.tools.nio.file.fs;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import VASSAL.tools.nio.file.FileStore;
import VASSAL.tools.nio.file.FileSystem;
import VASSAL.tools.nio.file.Path;

abstract class RealFileSystem extends AbstractFileSystem {
  protected final RealFileStore store;

  protected RealFileSystem() {
    store = new RealFileStore(this);
  }

  public void close() throws IOException {
    throw new UnsupportedOperationException();
  }

  public Iterable<FileStore> getFileStores() {
    return Collections.<FileStore>singletonList(store);
  }

  public Iterable<Path> getRootDirectories() {
    final File[] roots = File.listRoots();
    final List<Path> l = new ArrayList<Path>(roots.length);
    for (File r : roots) l.add(getPath(r.toString()));
    return l;
  }

  public String getSeparator() {
    return File.separator;
  }

  public boolean isOpen() {
    return true;
  }

  public boolean isReadOnly() {
    return false;
  }

  public Set<String> supportedFileAttributeViews() {
    return Collections.singleton("basic");
  }
}

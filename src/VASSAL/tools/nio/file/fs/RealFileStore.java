package VASSAL.tools.nio.file.fs;

import java.io.IOException;

import VASSAL.tools.nio.file.FileStore;
import VASSAL.tools.nio.file.attribute.BasicFileAttributeView;
import VASSAL.tools.nio.file.attribute.FileAttributeView;
import VASSAL.tools.nio.file.attribute.FileStoreAttributeView;
import VASSAL.tools.nio.file.attribute.FileStoreSpaceAttributeView;

class RealFileStore extends FileStore {
  protected final RealFileSystem fs;
  protected final DummyFileStoreAttributeView view;

  public RealFileStore(RealFileSystem fs) {
    this.fs = fs;
    this.view = new DummyFileStoreAttributeView();
  }

  public Object getAttribute(String attribute) throws IOException {
  }
  
  public <V extends FileStoreAttributeView> V getFileStoreAttributeView(
    Class<V> type)
  {
    return FileStoreSpaceAttributeView.class.equals(type) ?
      type.cast(view) : null;
  }

   public boolean isReadOnly() {
    return false;
  }

  public String name() {
    return "/";
  }

  public boolean supportsFileAttributeView(
                                     Class<? extends FileAttributeView> type) {
    return BasicFileAttributeView.class.equals(type);
  }

  public boolean supportsFileAttributeView(String name) {
    return "basic".equals(name);
  }

  public String type() {
    return "realfs";
  }
}

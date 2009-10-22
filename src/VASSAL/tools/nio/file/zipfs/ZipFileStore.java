/*
 * Copyright 2007-2009 Sun Microsystems, Inc.  All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Sun Microsystems nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package VASSAL.tools.nio.file.zipfs;

import java.io.IOException;
/*
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileStoreAttributeView;
import java.nio.file.attribute.FileStoreSpaceAttributeView;
import java.nio.file.attribute.FileStoreSpaceAttributes;
import java.nio.file.attribute.Attributes;
import java.nio.file.attribute.BasicFileAttributeView;
*/
import VASSAL.tools.nio.file.FileStore;
import VASSAL.tools.nio.file.FileSystems;
import VASSAL.tools.nio.file.Path;
import VASSAL.tools.nio.file.attribute.FileAttributeView;
import VASSAL.tools.nio.file.attribute.FileStoreAttributeView;
import VASSAL.tools.nio.file.attribute.FileStoreSpaceAttributeView;
import VASSAL.tools.nio.file.attribute.FileStoreSpaceAttributes;
import VASSAL.tools.nio.file.attribute.Attributes;
import VASSAL.tools.nio.file.attribute.BasicFileAttributeView;

public class ZipFileStore extends FileStore {

  private final ZipFilePath root;
  private final String zipFileName;
  private final String type = "zipfs";

  ZipFileStore(ZipFilePath path) {
    this.root = path;
    zipFileName = path.getFileSystem().getZipFileSystemFile();
  }

  static FileStore create(ZipFilePath root) throws IOException {
    return new ZipFileStore(root);
  }

  @Override
  public String name() {
    return zipFileName;
  }

  @Override
  public String type() {
    return type;
  }

  @Override
  public boolean isReadOnly() {
    return root.getFileSystem().isReadOnly();
  }

  @Override
  public boolean supportsFileAttributeView(Class<? extends FileAttributeView> type) {
    if (type == BasicFileAttributeView.class)
      return true;
    if (type == ZipFileAttributeView.class)
      return true;
    if (type == JarFileAttributeView.class)
      return true;
    return false;
  }

  @Override
  public boolean supportsFileAttributeView(String name) {
    if (name.equals("basic") || name.equals("zip") || name.equals("jar")) {
      return true;
    }
    return false;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <V extends FileStoreAttributeView> V getFileStoreAttributeView(Class<V> viewType) {
    if (viewType == FileStoreSpaceAttributeView.class) {
      return (V) new ZipFileStoreAttributeView(this);
    }
    return null;
  }

  @Override
  public Object getAttribute(String attribute) throws IOException {
     if (attribute.equals("space:totalSpace" )){
         return new ZipFileStoreAttributeView(this).readAttributes().totalSpace();
     }
     if (attribute.equals("space:usableSpace")   ){
         return new ZipFileStoreAttributeView(this).readAttributes().usableSpace();
     }
     if (attribute.equals("space:unallocatedSpace")){
         return new ZipFileStoreAttributeView(this).readAttributes().unallocatedSpace();
     }
     throw new UnsupportedOperationException("does not support the given attribute");
  }

  private static class ZipFileStoreAttributeView implements FileStoreSpaceAttributeView {

    private final ZipFileStore fileStore;

    public ZipFileStoreAttributeView(ZipFileStore fileStore) {
      this.fileStore = fileStore;
    }

    public String name() {
      return "space";
    }

    public FileStoreSpaceAttributes readAttributes() throws IOException {
      // get the size of the zip file
      String file = fileStore.name();
      Path path = FileSystems.getDefault().getPath(file);
      final long size = Attributes.readBasicFileAttributes(path).size();
      return new FileStoreSpaceAttributes() {

        public long totalSpace() {
          return size; // size of the zip/jar file
        }

        public long usableSpace() {
          return 0; // no usable space in zip/jar file
        }

        public long unallocatedSpace() {
          return 0; // no unallocated space in zip/jar file.
        }
      };
    }
  }
}

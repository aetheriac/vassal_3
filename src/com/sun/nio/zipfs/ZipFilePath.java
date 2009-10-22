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

import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
//import java.nio.channels.SeekableByteChannel;
//import java.nio.file.*;
//import java.nio.file.DirectoryStream.Filter;
//import java.nio.file.spi.*;
import java.util.*;
//import java.nio.file.attribute.*;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
//import java.nio.file.attribute.Attributes;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import VASSAL.tools.nio.channels.FileChannelAdapter;
import VASSAL.tools.nio.channels.SeekableByteChannel;
import VASSAL.tools.nio.file.*;
import VASSAL.tools.nio.file.DirectoryStream.Filter;
import VASSAL.tools.nio.file.attribute.*;
import VASSAL.tools.nio.file.spi.*;

/**
 * Jar/Zip path implementation of Path
 * We use "/" as the Zip File entry seperator.
 * @author    Rajendra Gutupalli,Jaya Hangal
 */
public class ZipFilePath extends Path {

  private ZipFileSystem fileSystem;
  //zip file separator
  public static final String separator = "/";
  // path inside zip and it can contain nested zip/jar paths
  private final byte[] path;
  // array of offsets of components in path - created lazily
  private volatile ArrayList<Integer> offsets;
  // array of offsets of entry elements in the path
  private volatile ArrayList<Integer> entryOffsets;
  // resolved path for locating zip inside zip file
  // resloved path does not contain ./ and .. components
  private final byte[] pathForZip;
  private final ReadLock readLock = new ReentrantReadWriteLock().readLock();
  private ZipFilePath pathToZip;
  private final byte[] pathForprint;

  // package-private
  ZipFilePath(ZipFileSystem fileSystem, byte[] pathInZip) {
    this.fileSystem = fileSystem;
    this.path = pathInZip;
    this.pathForprint = pathInZip;
    boolean isAbs = (path[0] == '/');
    String toResolve = new String(path);
    if (!isAbs) {
      String defdir = fileSystem.getDefaultDir();
      boolean endsWith = defdir.endsWith("/");
      if (endsWith) {
        toResolve = defdir + toResolve;
      }
      else {
        toResolve = defdir + "/" + toResolve;
      }
    }
    pathForZip = ZipPathParser.resolve(toResolve).getBytes();
  }
  // if given path is resolved

  ZipFilePath(ZipFileSystem fileSystem, byte[] pathInZip, byte[] pathForZip) {
    this.fileSystem = fileSystem;
    this.path = pathForZip;
    this.pathForZip = pathForZip;
    this.pathForprint = pathInZip; //given path
  }

  public boolean isNestedZip() {
    Pattern pattern = Pattern.compile("\\.(?i)(zip|jar)");
    Matcher matcher = null;
    for (int i = 0; i < getNameCount(); i++) {
      String entry = getName(i).toString();
      matcher = pattern.matcher(entry);
      if (matcher.find()) {
        return true;
      }
    }
    return false;
  }

  public boolean isArchiveFile() {
    Path name = getName();
    if (name == null) {
      return false;
    }
    String fileName = name.toString().toLowerCase();
    return (fileName.endsWith(".zip") || fileName.endsWith(".jar"));
  }

  /**
   * A path represents directory if it ends with '/'.
   * The normalize method does not remove the trailing '/'
   */
  public boolean isDirectory() {
    try {
      begin();
      try {
        ZipFilePath resolved = getResolvedPathForZip();
        return Attributes.readBasicFileAttributes(resolved, LinkOption.NOFOLLOW_LINKS).isDirectory();
      }
      catch (IOException e) {
        return false;
      }
    }
    finally {
      end();
    }
  }

  static int nextSeparator(byte[] path, int index) {

    int length = path.length;

    while (index < length && path[index] != '/') {
      index++;
    }
    return index;
  }

  final void begin() {
    readLock.lock();
    if (!fileSystem.isOpen()) {
      throw new ClosedFileSystemException();
    }
  }

  final void end() {
    readLock.unlock();
  }

  static int nextNonSeparator(byte[] path, int index) {

    int length = path.length;
    while (index < length && path[index] == '/') {
      index++;
    }
    return index;
  }

  // create offset list if not already created
  private void initOffsets() {
    if (offsets == null) {
      ArrayList<Integer> list = new ArrayList<Integer>();
      int pathLen = path.length;
      int index = nextNonSeparator(path, 0) - 1;

      int root = index;

      while ((index = nextSeparator(path, index + 1)) < pathLen && (index + 1 != pathLen)) {
        list.add(index + 1); // puls 1 for file separator
      }

      if (root + 1 < index) { // begin index
        list.add(0, root + 1);
      }

      offsets = list;
    }

  }

  private void initEntryOffsets() {
    if (entryOffsets == null) {

      ArrayList<Integer> list1 = new ArrayList<Integer>();
      int count = getNameCount();
      Pattern pattern = Pattern.compile("\\.(?i)(zip|jar)");
      Matcher matcher = null;
      int i = 0;
      int off = 0;
      while (i < (count - 1)) {
        String name = getName(i).toString();
        matcher = pattern.matcher(name);
        if (matcher.find()) {
          off = offsets.get(i + 1);
          list1.add(off);
        }
        i++;
      }
      if (count > 0) {
        int firstNonSeparatorIndex = nextNonSeparator(path, 0);
        list1.add(0, firstNonSeparatorIndex);
      }
      entryOffsets = list1;
    }
  }

  @Override
  public ZipFilePath getRoot() {
    if (this.isAbsolute()) {
      return new ZipFilePath(this.fileSystem, new byte[]{path[0]});
    } 
    else {
      return null;
    }
  }

  @Override
  public Path getName() {
    initOffsets();
    if (offsets.size() == 0) {
      return null;
    }
    String result = subString(offsets.get(offsets.size() - 1), path.length);
    result = (result.endsWith("/")) ? result.substring(0, result.length() - 1) : result;
    return new ZipFilePath(this.fileSystem, result.getBytes());
  }

  public ZipFilePath getEntryName() {
    initEntryOffsets();
    if (entryOffsets.size() == 0) {
      return null;
    }
    String result = subString(entryOffsets.get(entryOffsets.size() - 1), path.length);
    result = (result.endsWith("/")) ? result.substring(0, result.length() - 1) : result;
    return new ZipFilePath(this.fileSystem, result.getBytes());

  }

  @Override
  public ZipFilePath getParent() {
    int count = getNameCount();
    if (count == 0 || count == 1) {
      return null;
    }
    int position = offsets.get(count - 1);
    String parent = subString(0, position - 1);
    return new ZipFilePath(this.fileSystem, parent.getBytes());
  }

  public ZipFilePath getParentEntry() {
    int entryCount = getEntryNameCount();
    if (entryCount == 0 || entryCount == 1) {
      return null;
    }
    int position = entryOffsets.get(entryCount - 1);
    String parent = subString(0, position - 1);
    byte[] parentBytes = parent.getBytes();
    ZipFilePath path1 = new ZipFilePath(this.fileSystem, parentBytes);
    return path1;
  }

  @Override
  public int getNameCount() {
    initOffsets();
    return offsets.size();
  }

  public int getEntryNameCount() {
    initEntryOffsets();
    return entryOffsets.size();
  }

  @Override
  public ZipFilePath getName(int index) {
    initOffsets();
    if (index < 0 || index >= offsets.size()) {
      throw new IllegalArgumentException();
    }

    if (index == offsets.size() - 1) {
      String s = subString(offsets.get(index), path.length);
      s = (s.endsWith("/")) ? s.substring(0, s.length() - 1) : s;
      return new ZipFilePath(this.fileSystem, s.getBytes());
    }

    byte[] pathInBytes = subString(offsets.get(index), offsets.get(index + 1) - 1).getBytes();
    return new ZipFilePath(this.fileSystem, pathInBytes);
  }

  public ZipFilePath getEntryName(int index) {
    initEntryOffsets();
    if (index < 0 || index >= entryOffsets.size()) {
      throw new IllegalArgumentException();
    }
    if (index == entryOffsets.size() - 1) {
      String s = subString(entryOffsets.get(index), path.length);
      s = (s.endsWith("/")) ? s.substring(0, s.length() - 1) : s;
      return new ZipFilePath(this.fileSystem, s.getBytes());
    }
    byte[] pathInBytes = subString(entryOffsets.get(index), entryOffsets.get(index + 1) - 1).getBytes();
    return new ZipFilePath(this.fileSystem, pathInBytes);
  }

  String subString(int beginIndex, int endIndex) {
    int length = endIndex - beginIndex;
    byte[] arr = new byte[length];
    System.arraycopy(path, beginIndex, arr, 0, length);
    return new String(arr);
  }

  @Override
  public ZipFilePath subpath(int beginIndex, int endIndex) {
    initOffsets();
    if (beginIndex < 0) {
      throw new IllegalArgumentException();
    }
    if (beginIndex >= (1 + offsets.size())) {
      throw new IllegalArgumentException();
    }
    if (endIndex > (1 + offsets.size())) {
      throw new IllegalArgumentException();
    }
    if (beginIndex >= endIndex) {
      throw new IllegalArgumentException();
    }

    int elements = endIndex - beginIndex;
    String result = null;
    StringBuffer result1 = new StringBuffer("");
    int index = beginIndex;
    for (; elements-- != 0;) {
      if (endIndex == offsets.size() && elements == 0) {
        result1.append(subString(offsets.get(index), path.length));
        break;
      }
      result1.append(subString(offsets.get(index), offsets.get(++index)));
    }
    result = result1.toString();
    result = (result.endsWith("/")) ? result.substring(0, result.length() - 1) : result;
    return new ZipFilePath(fileSystem, result.getBytes());
  }

  public ZipFilePath subEntryPath(int beginIndex, int endIndex) {
    initEntryOffsets();
    if (beginIndex < 0) {
      throw new IllegalArgumentException();
    }
    if (beginIndex >= (1 + entryOffsets.size())) {
      throw new IllegalArgumentException();
    }
    if (endIndex > (1 + entryOffsets.size())) {
      throw new IllegalArgumentException();
    }
    if (beginIndex >= endIndex) {
      throw new IllegalArgumentException();
    }

    int elements = endIndex - beginIndex;
    String result = null;
    StringBuffer result1 = new StringBuffer("");
    int index = beginIndex;

    for (; elements-- != 0;) {
      if (endIndex == entryOffsets.size() && elements == 0) {
        result1.append(subString(entryOffsets.get(index), path.length));
        break;
      }
      result1.append(subString(entryOffsets.get(index), entryOffsets.get(++index)));
    }
    result = result1.toString();
    result = (result.endsWith("/")) ? result.substring(0, result.length() - 1) : result;
    return new ZipFilePath(fileSystem, result.getBytes());
  }

  @Override
  public ZipFilePath toRealPath(boolean resolveLinks) throws IOException {
    ZipFilePath realPath = new ZipFilePath(this.fileSystem, pathForZip);
    realPath.checkAccess();
    return realPath;
  }

  @Override
  public boolean isHidden() {
    return false;
  }

  @Override
  public ZipFilePath toAbsolutePath() {
    if (isAbsolute()) {
      return this;
    } 
    else {
      // add / bofore the existing path
      byte[] defaultdir = fileSystem.getDefaultDir().getBytes();
      int defaultlen = defaultdir.length;
      boolean endsWith = (defaultdir[defaultlen - 1] == '/');
      byte[] t = null;
      if (endsWith) {
        t = new byte[defaultlen + path.length];
      }
      else {
        t = new byte[defaultlen + 1 + path.length];
      }

      System.arraycopy(defaultdir, 0, t, 0, defaultlen);
      if (!endsWith) {
        t[defaultlen++] = '/';
      }
      System.arraycopy(path, 0, t, defaultlen, path.length);
      return new ZipFilePath(this.fileSystem, t);
    }
  }

  @Override
  public URI toUri() {

    String fullPath = fileSystem.getZipFileSystemFile();
    if (File.separatorChar == '\\') {
      fullPath = "/" + fullPath.replace("\\", "/"); // if Windows replace all separators by '/'
    }
    boolean endsWithSlash = (path[path.length - 1] == '/');  //
    byte[] t = this.path;
    if (!endsWithSlash) {
      if (this.isArchiveFile() || this.isDirectory()) {
        t = new byte[path.length + 1];
        System.arraycopy(path, 0, t, 0, path.length);
        t[t.length - 1] = '/';
      }
    }
    String pathStr = new String(t);
    if (!isAbsolute()) {
      String defaultdir = fileSystem.getDefaultDir();
      if (defaultdir.endsWith("/")) {
        pathStr = defaultdir + pathStr;
      } else {
        pathStr = defaultdir + "/" + pathStr;
      }
    }
    try {
      return new URI("zip", "", fullPath, pathStr);
    }
    catch (Exception ex) {
      throw new AssertionError(ex);
    }
  }

  // package private

  URI toUri0() {
    try {
      String fullPath = fileSystem.getZipFileSystemFile();
      if (File.separatorChar == '\\') {
        fullPath = "/" + fullPath.replace("\\", "/"); // if Windows replace all separators by '/'
      }
      boolean endsWithSlash = (path.length > 1 && path[path.length - 1] == '/');  //0 for root
      byte[] t = this.path;
      if (!endsWithSlash && this.isArchiveFile()) {
        t = new byte[path.length + 1];
        System.arraycopy(path, 0, t, 0, path.length);
        t[t.length - 1] = '/';
      }
      String pathStr = new String(t);
      if (!isAbsolute()) {
        String defaultdir = fileSystem.getDefaultDir();
        if (defaultdir.endsWith("/")) {
          pathStr = defaultdir + pathStr;
        }
        else {
          pathStr = defaultdir + "/" + pathStr;
        }
      }
      return new URI("zip", "", fullPath, pathStr);
    } 
    catch (Exception ex) {
      throw new AssertionError(ex);
    }
  }

  @Override
  public Path relativize(Path other) {
    if (other == null) {
      throw new NullPointerException();
    }
    if (!(other instanceof ZipFilePath)) {
      throw new ProviderMismatchException();
    }
    ZipFilePath other1 = (ZipFilePath) other;
    if (other1.equals(this)) {
      return null;
    }
    if (this.isAbsolute() != other1.isAbsolute()) {
      return other1;
    }

    int i = 0;
    int ti = this.getNameCount();
    int oi = other1.getNameCount();

    for (; i < ti && i < oi; i++) {
      if (!this.getName(i).equals(other1.getName(i))) {
        break;
      }
    }
    int nc = ti - i;
    byte[] arr = new byte[nc * 3];
    for (int j = 0; j < arr.length; j += 3) {
      arr[j] = arr[j + 1] = '.';
      arr[j + 2] = '/';
    }
    //contruct final path
    ZipFilePath subPath = null;
    int subpathlen = 0;
    if (i < oi) {
      subPath = other1.subpath(i, oi);
      subpathlen = subPath.path.length;
    }
    byte[] result = new byte[arr.length + subpathlen];
    if (nc > 0) {
      System.arraycopy(arr, 0, result, 0, arr.length - 1);
    }
    if (subpathlen > 0) {
      if (arr.length > 0) {
        result[arr.length - 1] = '/';
      }
      System.arraycopy(subPath.path, 0, result, arr.length, subpathlen);
    }
    return new ZipFilePath(this.fileSystem, result);
  }

  //@Override
  public ZipFileSystem getFileSystem() {
    return fileSystem;
  }

  @Override
  public boolean isAbsolute() {
    return (this.path[0] == '/');
  }

  public ZipFilePath resolve(Path other) {
    // zip/jar path are always absolute
    if (other == null) {
      throw new NullPointerException();
    }
    if (!(other instanceof ZipFilePath)) {
      throw new ProviderMismatchException();
    }
    ZipFilePath other1 = (ZipFilePath) other;
    if (other1.isAbsolute()) {
      return other1;
    }
    byte[] resolved = null;
    if (this.path[path.length - 1] == '/') {
      resolved = new byte[path.length + other1.path.length];
      System.arraycopy(path, 0, resolved, 0, path.length);
      System.arraycopy(other1.path, 0, resolved, path.length, other1.path.length);
    }
    else {
      resolved = new byte[path.length + 1 + other1.path.length];
      System.arraycopy(path, 0, resolved, 0, path.length);
      resolved[path.length] = '/';
      System.arraycopy(other1.path, 0, resolved, path.length + 1, other1.path.length);
    }
    return new ZipFilePath(this.fileSystem, resolved);
  }

  @Override
  public ZipFilePath resolve(String other) {
    return resolve(getFileSystem().getPath(other));
  }

  @Override
  public boolean startsWith(Path other) {

    ZipFilePath other1 = null;
    if (other == null) {
      throw new NullPointerException();
    }
    if (other instanceof ZipFilePath) {
      other1 = (ZipFilePath) other;
    }

    int otherCount = other1.getNameCount();
    if (getNameCount() < otherCount) {
      return false;
    }

    for (int i = 0; i < otherCount; i++) {
      if (other1.getName(i).equals(getName(i))) {
        continue;
      }
      else {
        return false;
      }
    }
    return true;

  }

  @Override
  public boolean endsWith(Path other) {
    ZipFilePath other1 = null;
    if (other == null) {
      throw new NullPointerException();
    }
    if (other instanceof ZipFilePath) {
      other1 = (ZipFilePath) other;
    }
    int i = other1.getNameCount();
    int j = getNameCount();

    if (j < i) {
      return false;
    }

    for (--i, --j; i >= 0; i--, j--) {
      if (other1.getName(i).equals(getName(j))) {
        continue;
      }
      else {
        return false;
      }
    }
    return true;

  }

  public FileSystemProvider provider() {
    return fileSystem.provider();
  }

  @Override
  public String toString() {
    return new String(pathForprint);
  }

  @Override
  public int hashCode() {
    int hashCode = 0;
    int i = 0;

    while (i < path.length) {
      byte v = path[i];
      hashCode = hashCode * 31 + (v);
      i++;
    }
    return hashCode;

  }

  @Override
  public boolean equals(Object ob) {
    if ((ob != null) && (ob instanceof ZipFilePath)) {
      return compareTo((Path) ob) == 0;
    }
    return false;
  }

  @Override
  public int compareTo(Path other) {

    ZipFilePath otherPath = (ZipFilePath) other;
    //int c = zipPath.compareTo(otherPath.zipPath);

    int len1 = path.length;
    int len2 = otherPath.path.length;

    int n = Math.min(len1, len2);
    byte v1[] = path;
    byte v2[] = otherPath.path;

    int k = 0;
    while (k < n) {
      int c1 = v1[k];
      int c2 = v2[k];
      if (c1 != c2) {
        return c1 - c2;
      }
      k++;
    }
    return len1 - len2;
  }

  @Override
  public Path createSymbolicLink(
      Path target, FileAttribute<?>... attrs) throws IOException {
    throw new UnsupportedOperationException("Not supported.");
  }

  @Override
  public Path createLink(
      Path existing) throws IOException {
    throw new UnsupportedOperationException("Not supported.");
  }

  @Override
  public Path readSymbolicLink() throws IOException {
    throw new UnsupportedOperationException("Not supported.");
  }

// FIXME: write lock
  @Override
  public Path createDirectory(
      FileAttribute<?>... attrs) throws IOException {
    throw new ReadOnlyFileSystemException();
  }

  ZipFilePath getResolvedPathForZip() {
    if (pathToZip == null) {
      pathToZip = new ZipFilePath(fileSystem, path, pathForZip);
    }
    return pathToZip;
  }

  public InputStream newInputStream(OpenOption... options) throws IOException {
    if (options.length > 0) {
      for (OpenOption opt : options) {
        if (opt != StandardOpenOption.READ) {
          throw new UnsupportedOperationException("'" + opt + "' not allowed");
        }
      }
    }

// FIXME: why is this under a lock?
    try {
      begin();

      ZipFilePath realPath = getResolvedPathForZip();
      if (realPath.getNameCount() == 0) {
        throw new IOException("entry missing in the path");
      }

      return ZipIO.in(this, options);
    }
    finally {
      end();
    }
  }

  @Override
  public DirectoryStream<Path> newDirectoryStream(
      Filter<? super Path> filter) throws IOException
  {
    try {
      begin();
      return new ZipFileStream(getResolvedPathForZip(), filter);
    }
    finally {
      end();
    }
  }

  private static final DirectoryStream.Filter<Path> acceptAllFilter =
      new DirectoryStream.Filter<Path>() {

        public boolean accept(Path entry) {
          return true;
        }
      };

  @Override
  public DirectoryStream<Path> newDirectoryStream() throws IOException {
    return newDirectoryStream(acceptAllFilter);
  }

  @Override
  public DirectoryStream<Path> newDirectoryStream(String glob) throws IOException {

    if (glob.equals("*"))
      return newDirectoryStream();

    final PathMatcher matcher = getFileSystem().getPathMatcher("glob:" + glob);
    DirectoryStream.Filter<Path> filter = new DirectoryStream.Filter<Path>() {
      public boolean accept(Path entry)  {
        return matcher.matches(entry.getName());
      }
    };
    return newDirectoryStream(filter);
  }

// FIXME: write lock
  @Override
  public void delete() throws IOException {
    throw new ReadOnlyFileSystemException();
  }

// FIXME: write lock
  @Override
  public void deleteIfExists() throws IOException {
    throw new ReadOnlyFileSystemException();
  }

  @SuppressWarnings("unchecked")
  public <V extends FileAttributeView> V getFileAttributeView(Class<V> type, LinkOption... options) {
    if (type == null) {
      throw new NullPointerException();
    }
    if (type == BasicFileAttributeView.class) {
      return (V) new ZipFileBasicAttributeView(this);
    }
    if (type == ZipFileAttributeView.class) {
      return (V) new ZipFileAttributeView(this);
    }
    if (type == JarFileAttributeView.class) {
      return (V) new JarFileAttributeView(this);
    }
    return null;
  }

  private ReadableAttributeViewByName getFileAttributeView(String view) {
    if (view == null) {
      throw new NullPointerException();
    }
    if (view.equals("basic")) {
      return new ZipFileBasicAttributeView(this);
    }
    if (view.equals("zip")) {
      return new ZipFileAttributeView(this);
    }
    if (view.equals("jar")) {
      return new JarFileAttributeView(this);
    }
    return null;
  }

// FIXME: write lock
  public void setAttribute(String attribute, Object value, LinkOption... options) {
    throw new UnsupportedOperationException();
  }

  public Object getAttribute(String attribute, LinkOption... options) throws IOException {

    String view = null;
    String attr = null;
    int colonPos = attribute.indexOf(':');
    if (colonPos == -1) {
      view = "basic";
      attr = attribute;
    }
    else {
      view = attribute.substring(0, colonPos++);
      attr = attribute.substring(colonPos);
    }

    ReadableAttributeViewByName fileView = getFileAttributeView(view);
    if (fileView == null) {
      throw new UnsupportedOperationException("view not supported");
    }
    return fileView.getAttribute(attr);
  }

// FIXME: read lock
  public Map<String,?> readAttributes(String attribute, LinkOption... options)
    throws IOException
  {
    throw new RuntimeException("not implemented");
  }

  @Override
  public FileStore getFileStore() throws IOException {
    try {
      begin();
      if (isAbsolute()) {
        return ZipFileStore.create(getRoot());
      }
      else {
        return ZipFileStore.create(getResolvedPathForZip().getRoot());
      }
    }
    finally {
      end();
    }
  }

  @Override
  public boolean isSameFile(Path other) throws IOException {
    if ((other != null) && (other instanceof ZipFilePath)) {

      // check both file systems are same.

      ZipFilePath other1 = (ZipFilePath) other;
      String fileSystem1 = this.getFileSystem().getZipFileSystemFile();
      String fileSystem2 = other1.getFileSystem().getZipFileSystemFile();
      boolean isSameFileSystem = fileSystem1.equals(fileSystem2);
      if (!isSameFileSystem) {
        return false;
      }

      // if file systems are same then do they exist
      // finally compare the paths
      // compare the real paths
      ZipFilePath thisZip = this.toRealPath(false);
      ZipFilePath otherZip = other1.toRealPath(false);
      return (thisZip.startsWith(otherZip) && thisZip.endsWith(otherZip));
    }
    return false;
  }

  public WatchKey register(
      WatchService watcher,
      WatchEvent.Kind<?>[] events,
      WatchEvent.Modifier... modifiers) {
    if (watcher == null || events == null || modifiers == null) {
      throw new NullPointerException();
    }
    throw new ProviderMismatchException();
  }

  @Override
  public WatchKey register(WatchService watcher, WatchEvent.Kind<?>... events) {
    return register(watcher, events, new WatchEvent.Modifier[0]);
  }

  @Override
  public Iterator<Path> iterator() {
    return new Iterator<Path>() {

      private int i = 0;

      public boolean hasNext() {
        return (i < getNameCount());
      }

      public Path next() {
        if (i < getNameCount()) {
          Path result = getName(i);
          i++;

          return result;
        }
        else {
          throw new NoSuchElementException();
        }
      }

      public void remove() {
        throw new ReadOnlyFileSystemException();
      }
    };
  }

// FIXME: read lock
// FIXME: write lock
  @Override
  public SeekableByteChannel newByteChannel(
      Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {

    // check for options of null type and option is an intance of StandardOpenOption
    for (OpenOption option : options) {
      if (option == null) {
        throw new NullPointerException();
      }
      if (!(option instanceof StandardOpenOption)) {
        throw new IllegalArgumentException();
      }
    }

    boolean openedForWriteOrAppend = options.contains(StandardOpenOption.WRITE) ||
        options.contains(StandardOpenOption.APPEND);
    if (openedForWriteOrAppend) {
      throw new ReadOnlyFileSystemException();
    }

    boolean openedForRead = options.contains(StandardOpenOption.READ);
    openedForRead = openedForRead || true; // if not opened for read then set openedForRed to true;

    if (!openedForRead) {
      throw new IllegalArgumentException("not opened for Read"); //this is never thrown
    }

    try {
      begin();

      ZipFilePath realPath = getResolvedPathForZip();
      if (realPath.getNameCount() == 0) {
        throw new IOException("entry Not Found");
      }

      return ZipIO.channel(this, options);
    }
    finally {
      end();
    }
  }

  @Override
  public SeekableByteChannel newByteChannel(OpenOption... options)
      throws IOException {
    Set<OpenOption> set = new HashSet<OpenOption>(options.length);
    Collections.addAll(set, options);
    return newByteChannel(set);
  }

  String getZipFile() throws IOException {

    String pathtoZip = null;
    ZipFilePath realPath = getResolvedPathForZip();
    int entryCount = realPath.getEntryNameCount();
    if (realPath.isNestedZip()) {
      if (realPath.isArchiveFile() && entryCount == 1) {
        pathtoZip = this.fileSystem.getZipFileSystemFile();
      }
      else {
        pathtoZip = ZipUtils.extractNestedZip(realPath.getParentEntry()).toString();
      }
    }
    else {
      pathtoZip = this.fileSystem.getZipFileSystemFile();
    }

    return pathtoZip;
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
      begin();
      ZipFilePath resolvedZipPath = getResolvedPathForZip();
      int nameCount = resolvedZipPath.getNameCount();
      if (nameCount == 0) {
        throw new NoSuchFileException(toString());
      }

      try {
        ZipIO.readLock(resolvedZipPath);

        ZipEntryInfo ze = ZipUtils.getEntry(resolvedZipPath);
        if (w) {
          throw new AccessDeniedException(
            "write access denied for the file: " + this.toString());
        }

        if (x) {
          long attrs = ze.extAttrs;
          if (!((((attrs << 4) >> 24) & 0x04) == 0x04)) {
            throw new AccessDeniedException(
              "execute access denied for the file: " + this.toString());
          }
        }
      }
      finally {
        ZipIO.readUnlock(resolvedZipPath);
      }
    }
    finally {
      end();
    }
  }

  @Override
  public boolean exists() {
    try {
      checkAccess();
      return true;
    }
    catch (IOException x) {
      // unable to determine if file exists
    }
    return false;
  }

  @Override
  public boolean notExists() {
    try {
      checkAccess();
      return false;
    }
    catch (NoSuchFileException x) {
      // file confirmed not to exist
      return true;
    }
    catch (IOException x) {
      return false;
    }
  }

  /*
   * /foo//        -- >  /foo/
   * /foo/./         -- > /foo/
   * /foo/../bar       -- > /bar
   * /foo/../bar/../baz  -- > /baz
   * //foo//./bar      -- > /foo/bar
   * /../          -- > /
   * ../foo        -- > foo
   * foo/bar/..      -- > foo
   * foo/../../bar     -- > bar
   * foo/../bar      -- > bar
   **/
  @Override
  public Path normalize() {
    //pathForZip is normalized path for ZipFileSystem
    return new ZipFilePath(fileSystem, pathForZip, pathForZip);
  }

// FIXME: write lock
  @Override
  public Path createFile(FileAttribute<?>... attrs) {
    throw new ReadOnlyFileSystemException();
  }

// FIXME: write lock
  @Override
  public OutputStream newOutputStream(OpenOption... options) {
    throw new ReadOnlyFileSystemException();
  }

// FIXME: write lock
  @Override
  public Path moveTo(Path target, CopyOption... options) {
    throw new ReadOnlyFileSystemException();
  }

// FIXME: read lock
// FIXME: write lock
  @Override
  public Path copyTo(Path target, CopyOption... options) throws IOException {
    if ((getFileSystem().provider() == target.getFileSystem().provider()))
      throw new ReadOnlyFileSystemException();

    boolean replaceExisting = false;
    boolean copyAttributes = false;

    for (CopyOption option: options) {
      if (option == StandardCopyOption.REPLACE_EXISTING) {
        replaceExisting = true;
        continue;
      }
      if (option == LinkOption.NOFOLLOW_LINKS) {
        // ignore
        continue;
      }
      if (option == StandardCopyOption.COPY_ATTRIBUTES) {
        copyAttributes = true;
        continue;
      }
      if (option == null)
        throw new NullPointerException();
      throw new IllegalArgumentException("'" + option +
        "' is not a valid copy option");
    }

    // attributes of source file
    BasicFileAttributes attrs = Attributes.readBasicFileAttributes(this);
    if (attrs.isSymbolicLink())
      throw new IOException("Copying of symbolic links not supported");

    // check for or delete target file.
    if (replaceExisting) {
      target.deleteIfExists();
    }
    else {
      if (target.exists())
        throw new FileAlreadyExistsException(target.toString());
    }

    // create directory or file
    if (attrs.isDirectory()) {
      target.createDirectory();
    }
    else {
      copyToTarget(target);
    }

    // copy basic attributes to target
    if (copyAttributes) {
      BasicFileAttributeView view = target
        .getFileAttributeView(BasicFileAttributeView.class);
      try {
        view.setTimes(attrs.lastModifiedTime(),
                attrs.lastAccessTime(),
                attrs.creationTime());
      }
      catch (IOException x) {
        // rollback
        try {
          target.delete();
        }
        catch (IOException ignore) { }
        throw x;
      }
    }

    return this;
  }

  private void copyToTarget(Path target) throws IOException {
    InputStream in = newInputStream();
    try {
      // open target file for writing
      OutputStream out = target.newOutputStream();
      // simple copy loop
      try {
        byte[] buf = new byte[8192];
        int n = 0;
        for (;;) {
          n = in.read(buf);
          if (n < 0)
            break;
          out.write(buf, 0, n);
        }

      }
      finally {
        out.close();
      }
    }
    finally {
      in.close();
    }
  }
}

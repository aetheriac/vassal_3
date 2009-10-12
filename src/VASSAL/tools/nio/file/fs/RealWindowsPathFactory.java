package VASSAL.tools.nio.file.fs;

class RealWindowsPathFactory implements RealPathFactory {
  public RealPath getPath(String path, RealFileSystem fs) {
    return new RealWindowsPath(path, fs);
  }
}

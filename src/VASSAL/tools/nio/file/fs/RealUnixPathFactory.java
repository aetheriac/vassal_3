package VASSAL.tools.nio.file.fs;

class RealUnixPathFactory implements RealPathFactory {
  public RealPath getPath(String path, RealFileSystem fs) {
    return new RealUnixPath(path, fs); 
  }
}

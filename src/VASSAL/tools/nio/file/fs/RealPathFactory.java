package VASSAL.tools.nio.file.fs;

interface RealPathFactory {
  public RealPath getPath(String path, RealFileSystem fs);
}

package VASSAL.tools.nio.file;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;

import VASSAL.tools.nio.file.attribute.FileAttribute;

public final class Files {
  public static void createDirectories(Path dir, FileAttribute<?>... attrs)
                                                           throws IOException {
    if (attrs.length > 0) throw new UnsupportedOperationException();

    // iterate over all name elements, creating the ones which don't exist
    for (Iterator<Path> i = dir.iterator(); i.hasNext(); ) {
      final Path p = i.next();
      if (p.exists()) {
        if (Boolean.FALSE.equals(p.getAttribute("basic:isDirectory"))) {
          throw new FileAlreadyExistsException(dir.toString());
        }
      }
      else {
        p.createDirectory();
      }
    }
  }

  public static String probeContentType(FileRef file) {
    throw new UnsupportedOperationException();
  } 
  
  public static void walkFileTree(Path start,
                                  FileVisitor<? super Path> visitor) {
    walkFileTree(
      start,
      EnumSet.noneOf(FileVisitOption.class),
      Integer.MAX_VALUE,
      visitor
    );
  }

  public static void walkFileTree(
    Path start,
    Set<FileVisitOption> options,
    int maxDepth,
    FileVisitor<? super Path> visitor)
  {
    // FIMXE: should implement
  }
}

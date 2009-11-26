package VASSAL.tools.nio.file.realfs;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

import VASSAL.tools.io.IOUtils;
import VASSAL.tools.nio.file.CopyOption;
import VASSAL.tools.nio.file.FileAlreadyExistsException;
import VASSAL.tools.nio.file.Path;
import VASSAL.tools.nio.file.StandardCopyOption;
import VASSAL.tools.nio.file.WatchEvent;
import VASSAL.tools.nio.file.WatchKey;
import VASSAL.tools.nio.file.WatchService;
import VASSAL.tools.nio.file.attribute.FileAttribute;

public abstract class AbstractPath extends Path {

  public int compareTo(Path owner) {
    return toString().compareTo(owner.toString());
  }

  public Path copyTo(Path target, CopyOption... options) throws IOException {
    if (!target.isSameFile(this)) {
      boolean replace = false;

      for (CopyOption c : options) {
        if (c == StandardCopyOption.REPLACE_EXISTING) replace = true;
        else throw new UnsupportedOperationException(c.toString());
      }

      if (!replace && target.exists()) {
        throw new FileAlreadyExistsException(
          this.toString(), target.toString(), ""
        );
      }

      InputStream in = null;
      OutputStream out = null;
      try {
        in = this.newInputStream();
        out = target.newOutputStream();
        IOUtils.copy(in, out);
        in.close();
        out.close();
      }
      finally {
        IOUtils.closeQuietly(in);
        IOUtils.closeQuietly(out);
      }
    }

    return target;
  }

  public Path createLink(Path existing) throws IOException {
    throw new UnsupportedOperationException();
  }

  public Path createSymbolicLink(Path target, FileAttribute<?>... attrs)
                                                           throws IOException {
    throw new UnsupportedOperationException();
  }

  public Path readSymbolicLink() throws IOException {
    throw new UnsupportedOperationException();
  }

  public WatchKey register(WatchService watcher, WatchEvent.Kind<?>... events)
                                                           throws IOException {
    throw new UnsupportedOperationException();
  }

  public WatchKey register(WatchService watcher,
                           WatchEvent.Kind<?>[] events,
                           WatchEvent.Modifier... modifiers)
                                                           throws IOException {
    throw new UnsupportedOperationException();
  }
}

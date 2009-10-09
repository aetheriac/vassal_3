package VASSAL.tools.nio.file;

import java.io.IOException;

public class FileSystemException extends IOException {
  private static final long serialVersionUID = 1L;

  private final String file;
  private final String other;
  private final String reason;

  public FileSystemException(String file) {
    this(file, "", "");
  }
  
  public FileSystemException(String file, String other, String reason) {
    super();
    this.file = file;
    this.other = other;
    this.reason = reason;
  }

  public String getFile() {
    return file;
  }
 
  public String getOtherFile() {
    return other;
  }

  public String getReason() {
    return reason;
  }
}

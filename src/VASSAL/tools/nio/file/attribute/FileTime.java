package VASSAL.tools.nio.file.attribute;

import java.util.concurrent.TimeUnit;

public final class FileTime implements Comparable<FileTime> {
  public int compareTo(FileTime other) {
  }

  public static FileTime from(long value, TimeUnit unit) {
  }

  public static FileTime fromMillis(long value) {
    return from(value, TimeUnit.MILLISECONDS);
  }

  public long to(TimeUnit unit) {
  }

  public long toMillis() {
    return to(TimeUnit.MILLISECONDS);
  }  

  @Override
  public String toString() {
  }

  @Override
  public int hashCode() {
  }

  @Override
  public boolean equals(Object o) {
  }
}

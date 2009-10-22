package VASSAL.tools.nio.file.zipfs;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;

class ZipNode {

  final String name;
  final ZipNode parent;

  final ReadWriteLock lock = new ZipLock();

  final ConcurrentMap<String,ZipNode> children =
    new ConcurrentHashMap<String,ZipNode>();

  public ZipNode(ZipNode parent, String name) {
    this.name = name;
    this.parent = parent;
  }

  @Override
  public boolean equals(Object o) {
    return name.equals(o);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }
}

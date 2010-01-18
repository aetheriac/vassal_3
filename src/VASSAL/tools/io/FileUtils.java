/*
 * $Id$
 *
 * Copyright (c) 2008-2010 by Joel Uckelman
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License (LGPL) as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, copies are available
 * at http://www.opensource.org.
 */
package VASSAL.tools.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import VASSAL.tools.nio.file.Path;

/**
 * Some general purpose file manipulation utilities.
 * 
 * @author Joel Uckelman
 * @since 3.1.0
 */ 
public class FileUtils {
  private FileUtils() {}

// FIXME: deprecate
  /**
   * Deletes a file.
   *
   * @param file the file to delete
   * @throws IOException if the file cannot be deleted
   */    
  public static void delete(File file) throws IOException {
    if (!file.delete())
      throw new IOException("Failed to delete " + file.getAbsolutePath());
  }

// FIXME: deprecate
  /**
   * Creates all directories named by a path.
   *
   * @param dir the path to create
   * @throws IOException if the path cannot be created
   */ 
  public static void mkdirs(File dir) throws IOException {
    if (!dir.mkdirs()) throw new IOException(
      "Failed to create directory " + dir.getAbsolutePath());
  }

// FIXME: deprecate
  /**
   * Recursively deletes all files in the tree rooted at the given path.
   *
   * @param base the root to delete
   * @throws IOException if some file cannot be deleted
   */ 
  public static void recursiveDelete(File base) throws IOException {
    // we delete as many files as we can
    final List<File> failed = new ArrayList<File>();
    recursiveDeleteHelper(base, failed);

    // if any deletions failed, we list them
    if (!failed.isEmpty()) {
      final StringBuilder sb = new StringBuilder("Failed to delete");
      for (File f : failed) sb.append(' ').append(f.getAbsolutePath());
      throw new IOException(sb.toString());
    }
  }

  private static void recursiveDeleteHelper(File parent, List<File> failed) {
    // delete children, depth first
    if (parent.isDirectory()) {
      for (File child : parent.listFiles()) {
        recursiveDeleteHelper(child, failed);
      }
    }

    // store leaves which can't be deleted in the failed list
    if (!parent.delete()) failed.add(parent);
  }

// FIXME: deprecate
  /**
   * Tries very hard to move a file.
   *
   * @param src the source file
   * @param dst the destination file
   * @throws IOException on failure
   */
  public static void move(File src, File dst) throws IOException {
    if (src.equals(dst)) return;

    // done if File.renameTo() works on the first shot
    if (src.renameTo(dst)) return;
    // otherwise, maybe we're on a platform where we must delete dst first
    dst.delete();
    if (src.renameTo(dst)) return;

    // otherwise, do the copy manually
    InputStream in = null;
    try {
      in = new FileInputStream(src);
      OutputStream out = null;
      try {
        out = new FileOutputStream(dst);
        IOUtils.copy(in, out);
        out.close();
      }
      finally {
        IOUtils.closeQuietly(out);
      }

      in.close();
    }
    finally {
      IOUtils.closeQuietly(in);
    }

    src.delete();
  }

  /**
   * Test whether the contents of two {@link Path}s are equal.
   *
   * @param a one path
   * @param b the other path
   * @return <code>true</code> if the contents are equal
   * @throws IOException if one of the paths is a directory, or the
   *    underlying operations throw one
   */
  public static boolean contentEquals(Path a, Path b) throws IOException {

    // check the easy cases before doing a byte-byte comparison
    if (a.equals(b)) return true;
    if (a.exists() != b.exists()) return false;
    if (!a.exists()) return true;

    if (Boolean.TRUE.equals(a.getAttribute("isDirectory")) ||
        Boolean.TRUE.equals(b.getAttribute("isDirectory"))) {
      throw new IOException("Only contents of files may be compared.");
    }

    if (!a.getAttribute("size").equals(b.getAttribute("size"))) return false;

    if (a.isSameFile(b)) return true; 

    // files might have the same contents, compare bytes
    boolean result;

    InputStream ain = null;
    try {
      ain = a.newInputStream();

      InputStream bin = null;
      try {
        bin = b.newInputStream();
        result = IOUtils.contentEquals(ain, bin); 
        bin.close();
      }
      finally {
        IOUtils.closeQuietly(bin);
      }

      ain.close();
    }
    finally {
      IOUtils.closeQuietly(ain);
    }

    return result;
  }
}

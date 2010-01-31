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

import VASSAL.tools.nio.file.*;
import VASSAL.tools.nio.file.attribute.*;

//import java.nio.file.*;
//import java.nio.file.attribute.*;
import java.io.IOException;
import java.util.Calendar;

import static VASSAL.tools.nio.file.zipfs.ZipFileSystem.DELETED;

public class ZipFileBasicAttributes implements BasicFileAttributes {

  ZipEntryInfo ze;

  /** Creates a new instance of ZipFileAttributes */
  public ZipFileBasicAttributes(ZipFilePath file) throws IOException {
    final ZipFileSystem fs = file.getFileSystem();

    try {
      fs.readLock(file);

      Path rpath = fs.getReal(file);
      if (rpath != null) {
        if (rpath == DELETED) {
          throw new NoSuchFileException(file.toString());
        }

        ze = fs.getInfo(file);
        if (ze == null) {
          // build fake ZipEntryInfo from temporary file
          final BasicFileAttributes attrs =
            Attributes.readBasicFileAttributes(rpath);

          ze = new ZipEntryInfo();

          ze.filename = file.toAbsolutePath().toString().getBytes();
          ze.compSize = -1;
// FIXME: int cast is a problem---test what happens when we try to write an
// archive containing a >4GB file 
          ze.size = (int) attrs.size();
          ze.isDirectory = attrs.isDirectory();
          ze.isOtherFile = attrs.isOther();
          ze.isRegularFile = attrs.isRegularFile();
          ze.lastModifiedTime = attrs.lastModifiedTime().toMillis();

          fs.putInfo(file, ze);
        }
      }
      else {
        ze = fs.getInfo(file);
        if (ze == null) {
          ze = ZipUtils.getEntry(file);
          fs.putInfo(file, ze);
        }
      }
    }
    finally {
      fs.readUnlock(file);
    }
  }

  public FileTime creationTime() {
    // is createTime in DOS or java time???
    return FileTime.fromMillis(ze.createTime);
  }

  public boolean isDirectory() {
    return ze.isDirectory;
  }

  public boolean isOther() {
    return ze.isOtherFile;
  }

  public boolean isRegularFile() {
    return ze.isRegularFile;
  }

  public FileTime lastAccessTime() {
    // lastAccessTime in DOS or java time???
    return FileTime.fromMillis(ze.lastAccessTime);
  }

  public FileTime lastModifiedTime() {
    final Calendar cal = dosTimeToJavaTime(ze.lastModifiedTime);
    return FileTime.fromMillis(cal.getTimeInMillis());
  }

  private Calendar dosTimeToJavaTime(long time) {
    final Calendar cal = Calendar.getInstance();
    cal.clear();  // to set the milliseconds 0
    cal.set((int) (((time >> 25) & 0x7f) + 1980),
            (int) (((time >> 21) & 0x0f) - 1),
            (int) ( (time >> 16) & 0x1f),
            (int) ( (time >> 11) & 0x1f),
            (int) ( (time >>  5) & 0x3f),
            (int) ( (time <<  1) & 0x3e));
    return cal;
  }

  public long size() {
    return ze.size;
  }

  public boolean isSymbolicLink() {
    return false;
  }

  public Object fileKey() {
    return null;
  }
}

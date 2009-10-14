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
package com.sun.nio.zipfs;

import VASSAL.tools.nio.file.*;
import VASSAL.tools.nio.file.attribute.*;

//import java.nio.file.*;
//import java.nio.file.attribute.*;
import java.io.IOException;
import java.util.Calendar;

public class ZipFileBasicAttributes implements
        BasicFileAttributes {

    ZipEntryInfo ze;

    /** Creates a new instance of ZipFileAttributes */
    public ZipFileBasicAttributes(FileRef file)
            throws IOException {
        if (file instanceof ZipFilePath && !((ZipFilePath) file).getFileSystem().isOpen()) {
            throw new ClosedFileSystemException();
        }
        ze = ZipUtils.getEntry(file);
    }

    @Override
    public FileTime creationTime() {
        // is createTime in DOS or java time???
        return FileTime.fromMillis(ze.createTime);
    }

    @Override
    public boolean isDirectory() {
        return ze.isDirectory;
    }

    @Override
    public boolean isOther() {
        return ze.isOtherFile;
    }

    @Override
    public boolean isRegularFile() {
        return ze.isRegularFile;
    }

    @Override
    public FileTime lastAccessTime() {
        // lastAccessTime in DOS or java time???
        return FileTime.fromMillis(ze.lastAccessTime);
    }

    @Override
    public FileTime lastModifiedTime() {
        long time = ze.lastModifiedTime;
        Calendar cal = dosTimeToJavaTime(time);
        return FileTime.fromMillis(cal.getTimeInMillis());
    }

    private Calendar dosTimeToJavaTime(long time) {
        Calendar cal = Calendar.getInstance();
        cal.set((int) (((time >> 25) & 0x7f) + 1980),
                (int) (((time >> 21) & 0x0f) - 1),
                (int) ((time >> 16) & 0x1f),
                (int) ((time >> 11) & 0x1f),
                (int) ((time >> 5) & 0x3f),
                (int) ((time << 1) & 0x3e));
        return cal;
    }

    @Override
    public long size() {
        return ze.size;
    }

    @Override
    public boolean isSymbolicLink() {
        return false;
    }

    @Override
    public Object fileKey() {
        return null;
    }
}

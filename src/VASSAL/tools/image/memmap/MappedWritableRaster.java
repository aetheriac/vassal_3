/*
 * $Id$
 *
 * Copyright (c) 2007 by Joel Uckelman
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

package VASSAL.tools.image.memmap;

import java.awt.Point;
import java.awt.image.DataBuffer;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;

/**
 * A {@link WritableRaster} backed by a memory-mapped file.
 *
 * @see MappedBufferedImage
 * @since 3.1.0
 * @author Joel Uckelman
 */
public class MappedWritableRaster extends WritableRaster {
  /**
   * Constructs a <code>MappedWritableRaster</code> with the given
   * <code>SampleModel</code> and <code>DataBuffer</code>.
   *
   * @param sm the <code>SampleModel</code> that specifies the layout
   * @param db the <code>DataBuffer</code> that contains the image data
   * @param origin the <code>Point</code> that specifies the origin
   */
  protected MappedWritableRaster(SampleModel sm, DataBuffer db, Point origin) {
    super(sm, db, origin);
  }

  public static WritableRaster createWritableRaster(SampleModel sm,
                                                    DataBuffer db,
                                                    Point loc) {
    if (sm == null)
      throw new NullPointerException("SampleModel cannot be null");
   
    if (db == null)
      throw new NullPointerException("DataBuffer cannot be null");

    return new MappedWritableRaster(sm, db, loc);
  }
}

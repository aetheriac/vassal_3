/*
 * $Id: AbstractTiledOp.java 2734 2007-12-21 14:30:36Z uckelman $
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

package VASSAL.tools.imageop;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * The abstract base class for {@link ImageOp}s which support multiple tiles.
 *
 * @since 3.1.0
 * @author Joel Uckelman
 */
public abstract class AbstractTiledOpImpl extends AbstractOpImpl {
  /** The standard size of this <code>ImageOp</code>s tiles. */
  protected Dimension tileSize;

  /** The number of tiles along the x-axis. */
  protected int numXTiles;

  /** The number of tiles along the y-axis. */
  protected int numYTiles;

  /** The tiles already created, stored as <code>y*numXTiles + x</code>. */
  protected ImageOp[] tiles; 

  /** {@inheritDoc} */
  public Dimension getTileSize() {
    if (tileSize == null) fixSize();
    return new Dimension(tileSize);
  }

  /** {@inheritDoc} */
  public int getTileHeight() {
    if (tileSize == null) fixSize();
    return tileSize.height;
  }
    
  /** {@inheritDoc} */
  public int getTileWidth() {
    if (tileSize == null) fixSize();
    return tileSize.width;
  }

  /** {@inheritDoc} */
  public int getNumXTiles() {
    if (tileSize == null) fixSize();
    return numXTiles;
  }
  
  /** {@inheritDoc} */
  public int getNumYTiles() {
    if (tileSize == null) fixSize();
    return numYTiles;
  }

  /**
   * Returns the <code>ImageOp</code> which produces tile
   * <code>(tileX,tileY)</code>, creating it if necessary.
   *
   * @return the <code>ImageOp</code> for tile <code>(tileX,tileY)</code>
   */
  protected abstract ImageOp getTileOp(int tileX, int tileY);

  /**
   * {@inheritDoc}
   *
   * @throws IndexOutOfBoundsException unless {@code 0 <= tileX < numXTiles}
   * and {@code 0 <= tileY < numYTiles}.
   */
  public Image getTile(int tileX, int tileY, ImageOpObserver obs)
    throws CancellationException, InterruptedException, ExecutionException {
    if (tileX < 0 || tileX >= numXTiles ||
        tileY < 0 || tileY >= numYTiles)
      throw new IndexOutOfBoundsException();

    return getTileOp(tileX, tileY).getImage(obs);
  }

  /**
   * {@inheritDoc}
   *
   * @throws IndexOutOfBoundsException unless {@code 0 <= tileX < numXTiles}
   * and {@code 0 <= tileY < numYTiles}.
   */
  public Future<Image> getFutureTile(int tileX, int tileY, ImageOpObserver obs)
    throws ExecutionException { 
    if (tileX < 0 || tileX >= numXTiles ||
        tileY < 0 || tileY >= numYTiles)
      throw new IndexOutOfBoundsException();

    return getTileOp(tileX, tileY).getFutureImage(obs);
  }

  /**
   * {@inheritDoc}
   *
   * @throws IllegalArgumentException if <code>rect == null</code>.
   */
  public Point[] getTileIndices(Rectangle rect) {
    if (rect == null) throw new IllegalArgumentException();

    if (size == null) fixSize();

// FIXME: maybe do this without creating new Rectangles
    rect = rect.intersection(new Rectangle(size));
    if (rect.isEmpty()) {
      return new Point[0];
    }

    final int minTileX = rect.x/tileSize.width;
    final int minTileY = rect.y/tileSize.height;
    final int maxTileX = (rect.x + rect.width - 1)/tileSize.width;
    final int maxTileY = (rect.y + rect.height - 1)/tileSize.height;
   
    final Point[] tilesInRect =
      new Point[(maxTileX-minTileX+1)*(maxTileY-minTileY+1)];

// FIXME: Maybe do this by keeping a MRU cache of Points.
// Maybe not, profiling shows that this isn't causing the gc to run much.
    int offset = 0;
    for (int ty = minTileY; ty <= maxTileY; ++ty) {
      for (int tx = minTileX; tx <= maxTileX; ++tx) {
        tilesInRect[offset++] = new Point(tx,ty);
      }
    }

    return tilesInRect;
  }
}

package VASSAL.tools;

import VASSAL.tools.ExtensionFileFilter;

/**
 * A FileFilter for GIF, JPEG, PNG, and SVG images. Used by file choosers
 * to filter out files which aren't images.
 */
public class ImageFileFilter extends ExtensionFileFilter {
   public static final String[] types = {
      ".gif", ".jpg", ".jpeg", ".png", ".svg"
   };

   public ImageFileFilter() {
      super("Image files", types);
   }
}

package VASSAL.tools;

import VASSAL.tools.ExtensionFileFilter;

/**
 * A FileFilter for AIFF, AU, and WAV files. Used by file choosers to
 * filter out files which aren't audio files.
 */
public class AudioFileFilter extends ExtensionFileFilter {
   public static final String[] types = {
      ".aiff", ".au", ".wav"
   };

   public AudioFileFilter() {
      super("Audio files", types);
   }  
}

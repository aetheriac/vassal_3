
package VASSAL.tools;

import java.awt.Component;
import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;

import VASSAL.tools.FileFilter;

public class FileChooser {
   protected JFileChooser fc;

   protected File fd_cur;
   protected String fd_title;
   protected FileFilter fd_filter;

   protected Component parent;

   public final static int APPROVE_OPTION = JFileChooser.APPROVE_OPTION;
   public final static int CANCEL_OPTION = JFileChooser.CANCEL_OPTION;
   public final static int ERROR_OPTION = JFileChooser.ERROR_OPTION;

   public final static int FILES_ONLY = JFileChooser.FILES_ONLY;
   public final static int DIRECTORIES_ONLY = JFileChooser.DIRECTORIES_ONLY;
   public final static int FILES_AND_DIRECTORIES =
                                         JFileChooser.FILES_AND_DIRECTORIES;
   
   public FileChooser(Component parent) {
      // determine what OS this is
      String os = System.getProperty("os.name");
      if (os == null) {
         // should not happen!
         fc = new JFileChooser();
      }
      else if (os.startsWith("Windows") ||
               os.startsWith("Mac OS")) {
         // use a native file chooser on Windows and Mac OS
         fc = null;
      }
      else {
         // use a Swing file chooser anywhere without a good native one
         fc = new JFileChooser();
      }
   
      this.parent = parent;
   }

   public File getCurrentDirectory() {
      if (fc != null) return fc.getCurrentDirectory();
      else if (fd_cur == null) return null;
      else if (fd_cur.isDirectory()) return fd_cur;
      else return fd_cur.getParentFile();
   }

   public void setCurrentDirectory(File dir) {
      if (fc != null) fc.setCurrentDirectory(dir);
      else fd_cur = dir;
   }

   public void rescanCurrentDirectory() {
      if (fc != null) fc.rescanCurrentDirectory();
   }

   public File getSelectedFile() {
      if (fc != null) return fc.getSelectedFile();
      else if (fd_cur.isFile()) return fd_cur;
      else return null;
   }

   public void setSelectedFile(File file) {
      if (fc != null) fc.setSelectedFile(file);
      else fd_cur = file;
   }

   public void selectDotSavFile() {
      File file = getSelectedFile();
      if (file != null) {
         String name = file.getPath();
         if (name != null) {
            int index = name.lastIndexOf('.');
            if (index > 0) {
               name = name.substring(0, index) + ".sav";
               setSelectedFile(new File(name));
            }
         }
      }
   }

   public int getFileSelectionMode() {
      return fc != null ? fc.getFileSelectionMode() : FILES_ONLY;
   }

   public void setFileSelectionMode(int mode) {
      if (fc != null) fc.setFileSelectionMode(mode);
   }

   public String getDialogTitle() {
      return fc != null ? fc.getDialogTitle() : fd_title;
   }

   public void setDialogTitle(String title) {
      if (fc != null) fc.setDialogTitle(title);
      else fd_title = title;
   }

   protected FileDialog awt_file_dialog_init(Component parent) {
      Frame frame = parent instanceof Frame ? (Frame) parent
         : (Frame) SwingUtilities.getAncestorOfClass(Frame.class, parent);
      FileDialog fd = new FileDialog(frame, fd_title);
      fd.setModal(true);
      fd.setFilenameFilter(fd_filter);

      if (fd_cur != null) {
         if (fd_cur.isDirectory()) fd.setDirectory(fd_cur.getPath());
         else {
            fd.setDirectory(fd_cur.getParent());
            fd.setFile(fd_cur.getName());
         }
      }
      
      return fd;
   }

   public int showOpenDialog() {
      return showOpenDialog(parent);
   }

   public int showOpenDialog(Component parent) {
      if (fc != null) return fc.showOpenDialog(parent);
      else {
         FileDialog fd = awt_file_dialog_init(parent);
         fd.setMode(FileDialog.LOAD);
         fd.setVisible(true);
         
         if (fd.getFile() != null) {
            fd_cur = new File(fd.getDirectory(), fd.getFile());
            return FileChooser.APPROVE_OPTION;
         }
         else return FileChooser.CANCEL_OPTION;
      }
   }
   
   public int showSaveDialog() {
      return showSaveDialog(parent);
   }

   public int showSaveDialog(Component parent) {
      if (fc != null) return fc.showSaveDialog(parent);
      else {
         FileDialog fd = awt_file_dialog_init(parent);
         fd.setMode(FileDialog.SAVE);
         fd.setVisible(true);

         if (fd.getFile() != null) {
            fd_cur = new File(fd.getDirectory(), fd.getFile());
            return FileChooser.APPROVE_OPTION;
         }
         else return FileChooser.CANCEL_OPTION;
      }
   }

   public FileFilter getFileFilter() {
      if (fc != null) {
         javax.swing.filechooser.FileFilter ff = fc.getFileFilter();
         return ff instanceof FileFilter ? (FileFilter) ff : null;
      }
      else return fd_filter;
   }

   public void setFileFilter(FileFilter filter) {
      if (fc != null) fc.setFileFilter(filter);
      else fd_filter = filter;
   }

   public void addChoosableFileFilter(FileFilter filter) {
      if (fc != null) fc.addChoosableFileFilter(filter);
   }

   public boolean removeChoosableFileFilter(FileFilter filter) {
      return fc != null ? fc.removeChoosableFileFilter(filter) : false;
   }

   public void resetChoosableFileFilters() {
      if (fc != null) fc.resetChoosableFileFilters();
   }
}

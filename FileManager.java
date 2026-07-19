import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileManager {
  public static boolean isFileValid(String path) {
    File file = new File(path);
    return file.exists() && file.isFile();
  }

  public static String getFileName(String path) {
    File file = new File(path);
    return file.getName();
  }

  public static long getFileSize(String path) {
    File file = new File(path);
    return file.length();
  }

  public static FileInputStream getInputStream(String path) throws IOException {
    return new FileInputStream(path);
  }

  public static FileOutputStream getOutputStream(String filename) throws IOException {
    return new FileOutputStream(filename);
  }
}

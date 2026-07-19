package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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

  public static String calculateSHA256(String path) throws IOException, NoSuchAlgorithmException {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    try (FileInputStream fis = new FileInputStream(path)) {
      byte[] byteArray = new byte[1024];
      int bytesCount = 0;
      while ((bytesCount = fis.read(byteArray)) != -1) {
        digest.update(byteArray, 0, bytesCount);
      }
    }
    byte[] bytes = digest.digest();
    StringBuilder sb = new StringBuilder();
    for (byte b : bytes) {
      sb.append(String.format("%02x", b));
    }
    return sb.toString();
  }
}

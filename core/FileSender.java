package core;

import utils.FileManager;
import interfaces.*;
import loggers.LogManager;
import java.io.*;
import java.net.*;
import java.util.Random;
import java.util.Scanner;

public class FileSender extends Peer implements IHashable, IProgressTrackable {
  public static final int BUFFER_SIZE = 65536;
  Socket s;
  String filePath;

  public FileSender(String userName, String targetIp, int port, String filePath) {
    super(userName, targetIp, port);
    this.filePath = filePath;
  }

  @Override
  public void start() {
    String fileName = FileManager.getFileName(filePath);
    long fileSize = FileManager.getFileSize(filePath);
    long startTime = System.nanoTime();
    boolean transferSuccess = false;
    String errorMessage = null;

    try {
      String pin = String.format("%04d", new Random().nextInt(10000));
      System.out.println("Pairing PIN: " + pin);
      System.out.println("Share this code with the receiver.");

      s = new Socket(ip, port);
      DataOutputStream dos = new DataOutputStream(s.getOutputStream());
      DataInputStream dis = new DataInputStream(s.getInputStream());

      dos.writeUTF(name);
      dos.writeUTF(fileName);
      dos.writeLong(fileSize);

      String receivedPin = dis.readUTF();
      if (!pin.equals(receivedPin)) {
        System.err.println("Verification failed! PIN mismatch.");
        dos.writeBoolean(false);
        errorMessage = "PIN mismatch";
        return;
      }
      dos.writeBoolean(true);

      System.out.println("Handshake successful. Starting transfer...");

      String hash = generateHash(filePath);
      dos.writeUTF(hash);

      try (FileInputStream fis = FileManager.getInputStream(filePath)) {
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead;
        long totalSent = 0;
        while ((bytesRead = fis.read(buffer)) != -1) {
          dos.write(buffer, 0, bytesRead);
          totalSent += bytesRead;
          updateProgress(totalSent, fileSize);
        }
      }
      System.out.println();
      transferSuccess = true;
      System.out.println("File sent in: " + (System.nanoTime() - startTime) / 1000000 + "ms");
      System.out.println("Done.");
    } catch (Exception e) {
      System.err.println("Error: " + e.getMessage());
      errorMessage = e.getMessage();
    } finally {
      long duration = System.nanoTime() - startTime;
      LogManager.getInstance().logTransfer(name, "Unknown", fileName, fileSize, duration, transferSuccess, "FILE", errorMessage);
    }
  }

  @Override
  public String generateHash(String filePath) {
    try {
      System.out.println("Calculating SHA-256 hash...");
      return FileManager.calculateSHA256(filePath);
    } catch (Exception e) {
      return "ERROR";
    }
  }

  @Override
  public boolean verifyHash(String filePath, String expectedHash) {
    return false;
  }

  @Override
  public void updateProgress(long current, long total) {
    int percent = (int) ((current * 100) / total);
    System.out.print("\rSending: [" + "#".repeat(percent / 10) + " ".repeat(10 - percent / 10) + "] " + percent + "%");
  }
}

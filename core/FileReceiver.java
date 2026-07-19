package core;

import utils.FileManager;
import interfaces.*;
import loggers.LogManager;
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class FileReceiver extends Peer implements IHashable, IProgressTrackable {
  public static final int BUFFER_SIZE = 65536;
  ServerSocket s;

  public FileReceiver(String userName, int port) {
    super(userName, "localhost", port);
  }

  @Override
  public void start() {
    long startTime = System.nanoTime();
    String senderName = null;
    String fileName = null;
    long fileSize = 0;
    boolean transferSuccess = false;
    String errorMessage = null;

    try (Scanner scanner = new Scanner(System.in)) {
      s = new ServerSocket(port);
      System.out.println("Waiting for connection on port " + port + "...");
      Socket clientSocket = s.accept();
      DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
      DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());

      senderName = dis.readUTF();
      fileName = dis.readUTF();
      fileSize = dis.readLong();

      System.out.println("\n--- Incoming Transfer ---");
      System.out.println("From: " + senderName);
      System.out.println("File: " + fileName);
      System.out.println("Size: " + (fileSize / 1024) + " KB");
      System.out.print("\nAccept this transfer? (y/n): ");

      String choice = scanner.nextLine().trim().toLowerCase();
      if (!choice.equals("y")) {
        System.out.println("Transfer rejected.");
        errorMessage = "Transfer rejected by receiver";
        clientSocket.close();
        s.close();
        return;
      }

      System.out.print("Enter the 4-digit PIN from " + senderName + ": ");
      String enteredPin = scanner.nextLine().trim();
      dos.writeUTF(enteredPin);

      boolean verified = dis.readBoolean();
      if (!verified) {
        System.err.println("Verification failed! PIN is incorrect. Aborting.");
        errorMessage = "PIN verification failed";
        clientSocket.close();
        s.close();
        return;
      }

      System.out.println("Verification success. Starting download...");

      String expectedHash = dis.readUTF();

      try (FileOutputStream fos = FileManager.getOutputStream(fileName)) {
        byte[] buffer = new byte[BUFFER_SIZE];
        long totalRead = 0;
        int bytesRead;
        while (totalRead < fileSize
            && (bytesRead = dis.read(buffer, 0, (int) Math.min(buffer.length, fileSize - totalRead))) != -1) {
          fos.write(buffer, 0, bytesRead);
          totalRead += bytesRead;
          updateProgress(totalRead, fileSize);
        }
      }
      System.out.println();

      if (verifyHash(fileName, expectedHash)) {
        System.out.println("File integrity verified [SUCCESS]");
        transferSuccess = true;
      } else {
        System.err.println("File integrity check [FAILED] - Corrupted transfer");
        errorMessage = "Hash verification failed";
      }

      System.out.println("Done.");
    } catch (Exception e) {
      System.err.println("Error: " + e.getMessage());
      errorMessage = e.getMessage();
    } finally {
      long duration = System.nanoTime() - startTime;
      if (senderName != null && fileName != null) {
        LogManager.getInstance().logTransfer(senderName, name, fileName, fileSize, duration, transferSuccess, "FILE", errorMessage);
      }
    }
  }

  @Override
  public String generateHash(String filePath) {
    return "";
  }

  @Override
  public boolean verifyHash(String filePath, String expectedHash) {
    try {
      System.out.println("Verifying file integrity...");
      String actualHash = FileManager.calculateSHA256(filePath);
      return actualHash.equalsIgnoreCase(expectedHash);
    } catch (Exception e) {
      return false;
    }
  }

  @Override
  public void updateProgress(long current, long total) {
    int percent = (int) ((current * 100) / total);
    System.out.print("\rReceiving: [" + "#".repeat(percent / 10) + " ".repeat(10 - percent / 10) + "] " + percent + "%");
  }
}

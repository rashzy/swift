package core;

import utils.FileManager;
import interfaces.*;
import loggers.LogManager;
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class FolderReceiver extends Peer implements IProgressTrackable, IHashable {
  public static final int BUFFER_SIZE = 65536;

  public FolderReceiver(String userName, int port) {
    super(userName, "localhost", port);
  }

  @Override
  public void start() {
    long startTime = System.nanoTime();
    String senderName = null;
    String folderName = null;
    long totalSize = 0;
    boolean transferSuccess = false;
    String errorMessage = null;

    try (Scanner scanner = new Scanner(System.in);
        ServerSocket server = new ServerSocket(port)) {
      System.out.println("Folder mode: waiting for connection on port " + port + "...");
      Socket clientSocket = server.accept();
      DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
      DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());

      senderName = dis.readUTF();
      folderName = dis.readUTF();
      int fileCount = dis.readInt();

      System.out.println("\n--- Incoming Transfer ---");
      System.out.println("From: " + senderName);
      System.out.println("Folder: " + folderName);
      System.out.println("Files: " + fileCount);
      System.out.print("\nAccept this transfer? (y/n): ");

      String choice = scanner.nextLine().trim().toLowerCase();
      if (!choice.equals("y")) {
        System.out.println("Transfer rejected.");
        errorMessage = "Transfer rejected by receiver";
        clientSocket.close();
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
        return;
      }

      System.out.println("Verification success. Starting download...");

      File outDir = new File(folderName);
      outDir.mkdirs();

      System.out.println("Receiving folder '" + folderName + "' (" + fileCount + " files) from " + senderName);

      for (int i = 0; i < fileCount; i++) {
        String fileName = dis.readUTF();
        long fileSize = dis.readLong();
        totalSize += fileSize;

        System.out.println("\n[" + (i + 1) + "/" + fileCount + "] " + fileName);
        File outFile = new File(outDir, fileName);
        try (FileOutputStream fos = new FileOutputStream(outFile)) {
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
      }
      System.out.println("\nDone.");
      transferSuccess = true;
    } catch (Exception e) {
      System.err.println("Error: " + e.getMessage());
      errorMessage = e.getMessage();
    } finally {
      long duration = System.nanoTime() - startTime;
      if (senderName != null && folderName != null) {
        LogManager.getInstance().logTransfer(senderName, name, folderName, totalSize, duration, transferSuccess, "FOLDER", errorMessage);
      }
    }
  }

  @Override
  public void updateProgress(long current, long total) {
    int percent = (int) ((current * 100) / total);
    System.out.print("\rReceiving: [" + "#".repeat(percent / 10) + " ".repeat(10 - percent / 10) + "] " + percent + "%");
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
}

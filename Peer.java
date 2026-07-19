import java.io.*;
import java.net.*;

public abstract class Peer {
  String ip;
  int port;

  public Peer(String ip, int port) {
    this.ip = ip;
    this.port = port;
  }

  public abstract void start();
}

class FileSender extends Peer {
  Socket s;
  String filePath;

  public FileSender(String targetIp, int port, String filePath) {
    super(targetIp, port);
    this.filePath = filePath;
  }

  @Override
  public void start() {
    try {
      s = new Socket(ip, port);
      long start = System.nanoTime();

      DataOutputStream dos = new DataOutputStream(s.getOutputStream());
      String fileName = FileManager.getFileName(filePath);
      long fileSize = FileManager.getFileSize(filePath);

      dos.writeUTF(fileName);
      dos.writeLong(fileSize);

      try (FileInputStream fis = FileManager.getInputStream(filePath)) {
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = fis.read(buffer)) != -1) {
          dos.write(buffer, 0, bytesRead);
        }
      }
      long end = System.nanoTime();
      long duration = (end - start);
      System.out.println("file send in : " + duration / 1000000 + "ms");
      System.out.println("done");
    } catch (UnknownHostException u) {
      System.err.println(u.getMessage());
    } catch (IOException i) {
      System.err.println(i.getMessage());
    }
  }
}

class FileReceiver extends Peer {
  ServerSocket s;

  public FileReceiver(int port) {
    super("localhost", port);
  }

  @Override
  public void start() {
    try {
      s = new ServerSocket(port);
      Socket clientSocket = s.accept();
      DataInputStream dis = new DataInputStream(clientSocket.getInputStream());

      String fileName = dis.readUTF();
      long fileSize = dis.readLong();

      try (FileOutputStream fos = FileManager.getOutputStream(fileName)) {
        byte[] buffer = new byte[4096];
        long totalRead = 0;
        int bytesRead;
        while (totalRead < fileSize
            && (bytesRead = dis.read(buffer, 0, (int) Math.min(buffer.length, fileSize - totalRead))) != -1) {
          fos.write(buffer, 0, bytesRead);
          totalRead += bytesRead;
        }
      }
      System.out.println("done");
    } catch (IOException i) {
      System.err.println(i.getMessage());
    }
  }
}

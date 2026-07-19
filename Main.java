import core.*;
import utils.FileManager;
import loggers.*;
import java.util.Scanner;

public class Main {
  public static void main(String[] args) {
    initializeLoggers();

    Scanner scanner = new Scanner(System.in);
    System.out.print("Enter your name: ");

    String userName = scanner.nextLine().trim();

    System.out.println("\nMain Menu");
    System.out.print(
        
        "1: file receiver, 2: file sender, 3: folder receiver, 4: folder sender, 5: view stats, 6: clear logs: ");

    int role = scanner.nextInt();
    scanner.nextLine();
    int Port = 8888;

    if (role == 1) {
      new FileReceiver(userName, Port).start();
    } else if (role == 2) {
      System.out.print("ip: ");
      String ip = scanner.nextLine().trim();
      System.out.print("path: ");
      String path = scanner.nextLine().trim();
      if (FileManager.isFileValid(path)) {
        new FileSender(userName, ip, Port, path).start();
      } else {
        System.err.println("invalid");
      }
    } else if (role == 3) {
      new FolderReceiver(userName, Port).start();
    } else if (role == 4) {
      System.out.print("ip: ");
      String ip = scanner.nextLine().trim();
      System.out.print("folder path: ");
      String path = scanner.nextLine().trim();
      java.io.File folder = new java.io.File(path);
      if (folder.exists() && folder.isDirectory()) {
        new FolderSender(userName, ip, Port, path).start();
      } else {
        System.err.println("invalid folder");
      }
    } else if (role == 5) {
      LogManager.getInstance().printStatistics();
    } else if (role == 6) {
      System.out.print("Are you sure you want to clear all logs? (y/n): ");
      if (scanner.nextLine().trim().toLowerCase().equals("y")) {
        LogManager.getInstance().clearAllLogs();
      }
    }

    scanner.close();
  }

  private static void initializeLoggers() {
    LogManager logManager = LogManager.getInstance();
    logManager.registerLogger(new ConsoleLogger());
    try {
      logManager.registerLogger(new FileLogger("transfer_logs.csv", true));
    } catch (Exception e) {
      System.err.println("Failed to initialize FileLogger: " + e.getMessage());
    }
    System.out.println("Loggers initialized: ConsoleLogger and FileLogger (CSV format)");
  }
}

package loggers;

import interfaces.ILogger;
import java.time.LocalDateTime;
import java.util.*;

public class LogManager {
  private static LogManager instance;
  private List<ILogger> loggers = new ArrayList<>();

  private LogManager() {
  }

  public static synchronized LogManager getInstance() {
    if (instance == null) {
      instance = new LogManager();
    }
    return instance;
  }

  public void registerLogger(ILogger logger) {
    loggers.add(logger);
  }

  public void unregisterLogger(ILogger logger) {
    loggers.remove(logger);
  }

  public void logTransfer(String sender, String receiver, String fileName, long fileSize,
      long duration, boolean success, String transferType) {
    logTransfer(sender, receiver, fileName, fileSize, duration, success, transferType, null);
  }

  public void logTransfer(String sender, String receiver, String fileName, long fileSize,
      long duration, boolean success, String transferType, String errorMessage) {
    TransferLog log = new TransferLog(
        sender,
        receiver,
        fileName,
        fileSize,
        duration,
        LocalDateTime.now(),
        success,
        transferType,
        errorMessage);

    for (ILogger logger : loggers) {
      logger.log(log);
    }
  }

  public List<TransferLog> getLogHistory() {
    List<TransferLog> all = new ArrayList<>();
    for (ILogger logger : loggers) {
      all.addAll(logger.getLogHistory());
    }
    all.sort(Comparator.comparing(TransferLog::getTimestamp));
    return all;
  }

  public void clearAllLogs() {
    for (ILogger logger : loggers) {
      logger.clearLogs();
    }
  }

  public void printStatistics() {
    if (loggers.isEmpty()) {
      System.out.println("No loggers registered.");
      return;
    }

    List<TransferLog> history = getLogHistory();
    if (history.isEmpty()) {
      System.out.println("No transfer logs available.");
      return;
    }

    System.out.println("\n========== TRANSFER STATISTICS ==========");
    System.out.println("Total Transfers: " + history.size());

    long successCount = history.stream().filter(TransferLog::isSuccess).count();
    long failureCount = history.size() - successCount;
    System.out.println("Successful: " + successCount);
    System.out.println("Failed: " + failureCount);
    System.out.println("Success Rate: " + String.format("%.2f%%", (successCount * 100.0) / history.size()));

    long totalSize = history.stream().mapToLong(TransferLog::getFileSize).sum();
    System.out.println("Total Data Transferred: " + formatBytes(totalSize));

    long avgDuration = Math.round(history.stream()
        .filter(TransferLog::isSuccess)
        .mapToLong(TransferLog::getDuration)
        .average()
        .orElse(0));
    System.out.println("Average Transfer Time: " + formatDuration(avgDuration));

    Map<String, Integer> transfersByType = new HashMap<>();
    for (TransferLog log : history) {
      transfersByType.put(log.getTransferType(),
          transfersByType.getOrDefault(log.getTransferType(), 0) + 1);
    }
    System.out.println("Transfers by Type: " + transfersByType);
    System.out.println("=========================================\n");
  }

  private String formatBytes(long bytes) {
    if (bytes <= 0)
      return "0 B";
    final String[] units = new String[] { "B", "KB", "MB", "GB" };
    int unitIndex = (int) (Math.log10(bytes) / Math.log10(1024));
    double size = bytes / Math.pow(1024, unitIndex);
    return String.format("%.2f %s", size, units[unitIndex]);
  }

  private String formatDuration(long nanos) {
    long seconds = nanos / 1000000000;
    long millis = (nanos % 1000000000) / 1000000;
    if (seconds > 0) {
      return seconds + "s " + millis + "ms";
    }
    return millis + "ms";
  }
}

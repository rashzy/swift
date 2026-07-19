package loggers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TransferLog {
  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private String sender;
  private String receiver;
  private String fileName;
  private long fileSize;
  private long duration;
  private LocalDateTime timestamp;
  private boolean success;
  private String transferType;
  private String errorMessage;

  public TransferLog(String sender, String receiver, String fileName, long fileSize,
      long duration, LocalDateTime timestamp, boolean success,
      String transferType, String errorMessage) {
    this.sender = sender;
    this.receiver = receiver;
    this.fileName = fileName;
    this.fileSize = fileSize;
    this.duration = duration;
    this.timestamp = timestamp;
    this.success = success;
    this.transferType = transferType;
    this.errorMessage = errorMessage;
  }

  public String getSender() {
    return sender;
  }

  public String getReceiver() {
    return receiver;
  }

  public String getFileName() {
    return fileName;
  }

  public long getFileSize() {
    return fileSize;
  }

  public long getDuration() {
    return duration;
  }

  public LocalDateTime getTimestamp() {
    return timestamp;
  }

  public boolean isSuccess() {
    return success;
  }

  public String getTransferType() {
    return transferType;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public String getFormattedSize() {
    if (fileSize <= 0)
      return "0 B";
    final String[] units = new String[] { "B", "KB", "MB", "GB" };
    int unitIndex = (int) (Math.log10(fileSize) / Math.log10(1024));
    double size = fileSize / Math.pow(1024, unitIndex);
    return String.format("%.2f %s", size, units[unitIndex]);
  }

  public String getFormattedDuration() {
    long seconds = duration / 1000000000;
    long millis = (duration % 1000000000) / 1000000;
    if (seconds > 0) {
      return seconds + "s " + millis + "ms";
    }
    return millis + "ms";
  }

  private static String csvEscape(String value) {
    if (value == null)
      return "";
    if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
      return "\"" + value.replace("\"", "\"\"") + "\"";
    }
    return value;
  }

  @Override
  public String toString() {
    String base = String.format("[%s] %s -> %s | %s (%s) | %s | Status: %s",
        timestamp.format(FORMATTER),
        sender,
        receiver,
        fileName,
        getFormattedSize(),
        transferType,
        success ? "SUCCESS" : "FAILED");
    if (!success && errorMessage != null) {
      base += " | Error: " + errorMessage;
    }
    return base;
  }

  public String toCSV() {
    return String.format("%s,%s,%s,%s,%d,%s,%s,%s,%s",
        timestamp.format(FORMATTER),
        csvEscape(sender),
        csvEscape(receiver),
        csvEscape(fileName),
        fileSize,
        duration,
        transferType,
        success,
        csvEscape(errorMessage));
  }
}

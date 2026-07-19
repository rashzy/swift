package loggers;

import interfaces.ILogger;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class FileLogger implements ILogger, Closeable {
  private static final int MAX_LOGS = 500;
  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  private List<TransferLog> logs = new ArrayList<>();
  private String logFilePath;
  private boolean isCSVFormat;
  private FileWriter fileWriter;

  public FileLogger(String logFilePath, boolean isCSVFormat) throws IOException {
    this.logFilePath = logFilePath;
    this.isCSVFormat = isCSVFormat;
    initializeLogFile();
  }

  private void initializeLogFile() throws IOException {
    File file = new File(logFilePath);
    if (file.getParentFile() != null) {
      file.getParentFile().mkdirs();
    }

    boolean fileExists = file.exists();
    fileWriter = new FileWriter(file, true);

    if (!fileExists) {
      if (isCSVFormat) {
        fileWriter.write("Timestamp,Sender,Receiver,FileName,FileSize,Duration,TransferType,Success,ErrorMessage\n");
        fileWriter.flush();
      }
    } else {
      loadExistingLogs();
    }
  }

  private void loadExistingLogs() throws IOException {
    try (BufferedReader reader = new BufferedReader(new FileReader(logFilePath))) {
      String line;
      boolean isFirstLine = true;
      while ((line = reader.readLine()) != null) {
        if (isFirstLine) {
          isFirstLine = false;
          continue;
        }
        TransferLog log = parseCSVLine(line);
        if (log != null) {
          logs.add(log);
          if (logs.size() > MAX_LOGS) {
            logs.subList(0, logs.size() - MAX_LOGS).clear();
          }
        }
      }
    }
  }

  private TransferLog parseCSVLine(String line) {
    try {
      String[] parts = parseCSV(line);
      if (parts.length < 9) return null;

      LocalDateTime timestamp = LocalDateTime.parse(parts[0], FORMATTER);
      String sender = parts[1];
      String receiver = parts[2];
      String fileName = parts[3];
      long fileSize = Long.parseLong(parts[4]);
      long duration = Long.parseLong(parts[5]);
      String transferType = parts[6];
      boolean success = Boolean.parseBoolean(parts[7]);
      String errorMessage = parts[8].isEmpty() ? null : parts[8];

      return new TransferLog(sender, receiver, fileName, fileSize, duration, timestamp, success, transferType, errorMessage);
    } catch (Exception e) {
      return null;
    }
  }

  private String[] parseCSV(String line) {
    List<String> result = new ArrayList<>();
    StringBuilder sb = new StringBuilder();
    boolean inQuotes = false;

    for (int i = 0; i < line.length(); i++) {
      char c = line.charAt(i);
      if (c == '"') {
        inQuotes = !inQuotes;
      } else if (c == ',' && !inQuotes) {
        result.add(sb.toString());
        sb = new StringBuilder();
      } else {
        sb.append(c);
      }
    }
    result.add(sb.toString());
    return result.toArray(new String[0]);
  }

  @Override
  public void log(TransferLog transferLog) {
    logs.add(transferLog);
    if (logs.size() > MAX_LOGS) {
      logs.subList(0, logs.size() - MAX_LOGS).clear();
    }
    writeToFile(transferLog);
  }

  private void writeToFile(TransferLog transferLog) {
    try {
      if (isCSVFormat) {
        fileWriter.write(transferLog.toCSV() + "\n");
      } else {
        fileWriter.write(transferLog.toString() + "\n");
      }
      fileWriter.flush();
    } catch (IOException e) {
      System.err.println("Error writing to log file: " + e.getMessage());
    }
  }

  @Override
  public List<TransferLog> getLogHistory() {
    return new ArrayList<>(logs);
  }

  @Override
  public void clearLogs() {
    logs.clear();
    try {
      fileWriter.close();
      try (FileWriter fw = new FileWriter(logFilePath, false)) {
        if (isCSVFormat) {
          fw.write("Timestamp,Sender,Receiver,FileName,FileSize,Duration,TransferType,Success,ErrorMessage\n");
        }
      }
      fileWriter = new FileWriter(logFilePath, true);
      System.out.println("[LOG] File logs cleared.");
    } catch (IOException e) {
      System.err.println("Error clearing log file: " + e.getMessage());
    }
  }

  @Override
  public void close() throws IOException {
    if (fileWriter != null) {
      fileWriter.close();
    }
  }
}

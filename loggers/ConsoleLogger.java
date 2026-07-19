package loggers;

import interfaces.ILogger;
import java.util.ArrayList;
import java.util.List;

public class ConsoleLogger implements ILogger {
  private static final int MAX_LOGS = 500;
  private List<TransferLog> logs = new ArrayList<>();

  @Override
  public void log(TransferLog transferLog) {
    logs.add(transferLog);
    if (logs.size() > MAX_LOGS) {
      logs.subList(0, logs.size() - MAX_LOGS).clear();
    }
    System.out.println("[LOG] " + transferLog.toString());
  }

  @Override
  public List<TransferLog> getLogHistory() {
    return new ArrayList<>(logs);
  }

  @Override
  public void clearLogs() {
    logs.clear();
    System.out.println("[LOG] Console logs cleared.");
  }
}

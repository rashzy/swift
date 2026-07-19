package interfaces;

import java.util.List;
import loggers.TransferLog;

public interface ILogger {
  void log(TransferLog transferLog);

  List<TransferLog> getLogHistory();

  void clearLogs();
}

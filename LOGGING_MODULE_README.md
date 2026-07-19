# Logging & Analytics Module Documentation

## Overview
This module provides a comprehensive logging system for tracking file and folder transfer history with polymorphic logger implementations and statistical analysis capabilities.

## Architecture

### 1. **ILogger Interface** (`interfaces/ILogger.java`)
Defines the contract for all logger implementations:
- `log(TransferLog)` - Log a transfer event
- `getLogHistory()` - Retrieve all logged transfers
- `clearLogs()` - Clear all logs

**Why this design**: Interface-based contract allows for multiple logger implementations without modifying existing code (Open/Closed Principle).

---

### 2. **TransferLog POJO** (`loggers/TransferLog.java`)
Encapsulates transfer metadata:
- **Attributes**:
  - `sender` - Name of the peer sending data
  - `receiver` - Name of the peer receiving data
  - `fileName` - Name of file/folder being transferred
  - `fileSize` - Size in bytes
  - `duration` - Transfer time in nanoseconds
  - `timestamp` - When transfer occurred
  - `success` - Whether transfer completed successfully
  - `transferType` - "FILE" or "FOLDER"
  - `errorMessage` - Error details if transfer failed

- **Key Methods**:
  - `getFormattedSize()` - Convert bytes to human-readable format (B, KB, MB, GB)
  - `getFormattedDuration()` - Convert nanoseconds to seconds/milliseconds
  - `toCSV()` - Export as CSV row
  - `toString()` - Pretty print format

**Why this design**: Immutable POJO ensures data consistency and thread-safety.

---

### 3. **ConsoleLogger** (`loggers/ConsoleLogger.java`)
Implements `ILogger` - logs transfer events to console output.

**Features**:
- Prints formatted logs to stdout
- Maintains in-memory history
- Real-time feedback during transfers

**Use case**: Development and debugging

---

### 4. **FileLogger** (`loggers/FileLogger.java`)
Implements `ILogger` - logs transfer events to a file on disk.

**Features**:
- Writes to `transfer_logs.csv` (configurable)
- CSV format with headers (configurable to text format)
- Auto-initializes log file with headers
- Persistent storage for historical analysis

**Constructor**:
```java
new FileLogger("transfer_logs.csv", true);  // true = CSV format
new FileLogger("transfer_logs.txt", false); // false = text format
```

**Use case**: Persistent record-keeping and data analysis

---

### 5. **LogManager Singleton** (`loggers/LogManager.java`)
Central manager for all logging operations.

**Key Features**:
- **Singleton Pattern**: Only one instance across the application
- **Multi-logger Support**: Register multiple loggers simultaneously
- **Unified API**: Single point to log all transfers
- **Statistics Reporting**: `printStatistics()` generates insights

**Methods**:
```java
LogManager manager = LogManager.getInstance();

// Register loggers
manager.registerLogger(new ConsoleLogger());
manager.registerLogger(new FileLogger("logs.csv", true));

// Log transfers
manager.logTransfer(sender, receiver, fileName, fileSize, 
                    duration, success, transferType);

// Get logs and statistics
List<TransferLog> history = manager.getLogHistory();
manager.printStatistics();
manager.clearAllLogs();
```

**Statistics Output**:
```
========== TRANSFER STATISTICS ==========
Total Transfers: 15
Successful: 14
Failed: 1
Success Rate: 93.33%
Total Data Transferred: 5.24 GB
Average Transfer Time: 2s 345ms
Transfers by Type: {FILE=10, FOLDER=5}
=========================================
```

---

## Integration with Existing Code

### Modified Files:
1. **FileSender.java** - Logs file send events with timing
2. **FileReceiver.java** - Logs file receive events with hash verification status
3. **FolderSender.java** - Logs folder send events with total size
4. **FolderReceiver.java** - Logs folder receive events with file count
5. **Main.java** - Initializes loggers and provides menu options (5 & 6)

### Key Integration Points:
```java
// In each sender/receiver class:
long startTime = System.nanoTime();
try {
    // ... transfer logic ...
    transferSuccess = true;
} catch (Exception e) {
    errorMessage = e.getMessage();
} finally {
    long duration = System.nanoTime() - startTime;
    LogManager.getInstance().logTransfer(
        sender, receiver, fileName, fileSize,
        duration, transferSuccess, transferType, errorMessage
    );
}
```

---

## Usage Examples

### Example 1: Log a Successful File Transfer
```java
LogManager.getInstance().logTransfer(
    "Alice", "Bob", "document.pdf", 1024000,
    5000000000L, true, "FILE", null
);
// Output: [LOG] [2024-04-28 14:30:45] Alice -> Bob | document.pdf (1000.00 KB) | FILE | Status: SUCCESS
```

### Example 2: View Transfer Statistics
```java
LogManager.getInstance().printStatistics();
// Prints formatted table with totals, success rate, averages
```

### Example 3: Custom Logger Implementation
```java
public class DatabaseLogger implements ILogger {
    @Override
    public void log(TransferLog transferLog) {
        // Save to database
    }
    // ... other methods ...
}

// Register it
LogManager.getInstance().registerLogger(new DatabaseLogger());
```

---

## Design Patterns Used

1. **Strategy Pattern**: Multiple logger implementations (`ConsoleLogger`, `FileLogger`)
2. **Singleton Pattern**: `LogManager` ensures single instance
3. **Observer Pattern**: Multiple loggers listen to transfer events
4. **POJO Pattern**: `TransferLog` as data container
5. **Factory Pattern**: Potential to create loggers based on configuration

---

## Extension Points for New Team Members

This module is designed for easy extension:

1. **Add Database Logger**
   - Create `DatabaseLogger implements ILogger`
   - Register in `Main.java`

2. **Add Email Alerts**
   - Extend `ILogger` with alert threshold logic
   - Notify on failures or large transfers

3. **Add Compression Analytics**
   - Track compression ratio in `TransferLog`
   - Add to statistics report

4. **Add Transfer Scheduling**
   - Create queue system using `TransferLog` history
   - Reschedule failed transfers

---

## Testing

All classes compiled successfully without errors.

To run the application:
```bash
javac -d . interfaces/*.java loggers/*.java core/*.java Main.java
java Main
```

Select option 5 to view statistics or option 6 to clear logs.

---

## File Structure
```
swift/
├── interfaces/
│   ├── ILogger.java          (✨ NEW)
│   ├── IHashable.java
│   ├── IProgressTrackable.java
│   └── ITransferable.java
├── loggers/                  (✨ NEW FOLDER)
│   ├── TransferLog.java
│   ├── ConsoleLogger.java
│   ├── FileLogger.java
│   └── LogManager.java
├── core/
│   ├── FileSender.java       (MODIFIED)
│   ├── FileReceiver.java     (MODIFIED)
│   ├── FolderSender.java     (MODIFIED)
│   ├── FolderReceiver.java   (MODIFIED)
│   └── Peer.java
├── utils/
│   └── FileManager.java
├── Main.java                 (MODIFIED)
└── LOGGING_MODULE_README.md  (✨ NEW)
```

---

## OOP Concepts Demonstrated

✅ **Interfaces & Contracts** - ILogger defines behavior contract  
✅ **Inheritance** - Loggers implement ILogger  
✅ **Polymorphism** - Different logger implementations, same interface  
✅ **Encapsulation** - Private attributes, public getters in TransferLog  
✅ **Singleton Pattern** - LogManager is single instance  
✅ **Collections** - ArrayList to manage loggers and logs  
✅ **Exception Handling** - Try-catch-finally in all loggers  
✅ **Method Overloading** - Multiple logTransfer() signatures  

---

## Summary

This logging module provides a **production-ready, extensible system** for tracking transfers with:
- Multiple logger implementations (Console, File)
- Comprehensive statistics and reporting
- Easy integration with existing code
- Clear extension points for new features
- Strong OOP design principles

A new team member can easily extend this by implementing `ILogger` and registering a custom logger!

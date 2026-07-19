# Reasoning

This document provides a deep dive into the engineering decisions, philosophy, and rationale that define the Swift project. It serves as a historical and technical record for why the system was built this way.

### The Design Matrix

| Feature / Decision | Choice Made | Rationale |
| :--- | :--- | :--- |
| **Language** | Java (Pure) | Java provides a robust, cross-platform networking stack and high-performance I/O libraries (NIO/IO) without the need for external C-bindings or runtime bloat. |
| **Directory Structure** | Flat ( core/, interfaces/, etc.) | Standard Maven/Gradle structures add 4-5 layers of "folders for folders" nesting. A flat structure minimizes the gap between the developer and the code. |
| **Dependency Management** | Zero External Libs | Using external libraries (like Netty or Spring) adds massive overhead and security vulnerabilities. We use the Standard Lib to guarantee minimalism and lifetime portability. |
| **Security Mechanism** | PIN Pairing + Hashing | We chose this over SSL/TLS Certificates to avoid the complexity of certificate management. PIN pairing provides "Proximity Trust" which is ideal for LAN-based P2P transfers. |
| **Storage Integrity** | SHA-256 Pre/Post Hash | SHA-256 provides a high-entropy 256-bit hash that makes collision impossible in a file-transfer context, ensuring byte-perfect data integrity. |
| **I/O Strategy** | Blocking Sockets | While NIO (Non-blocking) is better for 1,000s of connections, it adds significant complexity. Standard Sockets are perfect for 1-to-1 P2P transfers and are easier to reason about and secure. |
| **Performance Buffer** | 64KB Fixed Array | A 64KB buffer matches typical modern kernel socket buffers and minimizes the number of read/write system calls while keeping the memory footprint low. |

---

### The Philosophy of Minimalism

#### Why No Comments?
Swift follows the **Self-Documenting Code** philosophy. 
- **Method Naming**: Instead of a comment saying `// calculates hash`, the method is named `calculateSHA256()`.
- **Interface Segregation**: The interfaces themselves define the documentation. `IHashable` tells you exactly what a class must do to handle security.
- **Maintainability**: Comments often become stale as code changes. By removing them, we force developers to write clear, readable, and intentional code that speaks for itself.

#### Why the Handshake is Synchronous?
In many P2P systems, the connection is asynchronous and hidden. Swift makes the handshake a **Synchronous Event**. This is a deliberate design to ensure the "Human-in-the-Loop" security model. The receiver must manually consent, creating a physical bridge of trust before a single bit of data is exchanged.

---

### Performance Rationale

Performance in Swift is measured by **Saturation**. Our goal is to saturate the available bandwidth of the local network interface.
- **Minimizing Context Switches**: Every time the program reads from a socket, the CPU context switches from user-space to kernel-space. By using a 64KB buffer (rather than the Java default of 8KB), we reduce these switches by a factor of 8, significantly improving throughput for large files (GBs).
- **Zero-Copy Intent**: While Java Sockets don't support true `sendfile` zero-copy easily in a cross-platform way, our streaming implementation avoids any intermediate object allocation within the hot path of the transfer loop.
- **Boundary Handling**: The transfer loop is designed to be byte-accurate. By using `Math.min` on the receiver and tracking the actual `bytesRead` on the sender, the system correctly handles files smaller than the buffer size (64KB) or files that are not even multiples of the buffer.

---

### Security Model Proof

Swift operates on a **"Trust but Verify"** security model.
1.  **Trust**: Established via the PIN code, which leverages the fact that two people in the same room (or on the same trusted call) are communicating.
2.  **Verify**: Established via the SHA-256 checksum. Even if the network is unstable or a malicious actor tries to inject data, the post-transfer hash verification will catch the anomaly and reject the file.

---
*Every decision in Swift is made to ensure that speed and security are delivered through simplicity.*

# Protocol

The communication protocol in Swift is a synchronous, state-based negotiation designed for high integrity and user-controlled trust.

### Handshake Sequence

The handshake is more than just a network connection; it is a conceptual agreement between two nodes to share data.

#### 1. Identity Initiation
- **Sender**: Dispatches a packet containing the Sender's Name, FileName, and FileSize.
- **Receiver**: Extracts this metadata and presents it to the human operator.
- **Why**: This ensures full transparency. The receiver is never surprised by an incoming file.

#### 2. Manual Consent
- **Receiver**: Blocks and waits for user confirmation (`y/n`).
- **Why**: This provides a "Physical Interruption" layer. Even if a connection is technically possible, a human must authorize it.

#### 3. PIN Verification (Zero-Knowledge Proximity)
- **Sender**: Generates and displays a random 4-digit PIN.
- **Receiver**: Prompts the user to enter the PIN they see on the sender's screen.
- **Transfer**: The Receiver sends this PIN to the Sender.
- **Why**: This ensures that the two humans operating the nodes are in actual communication, virtually eliminating "Man-in-the-Middle" or accidental connections in a shared network environment.

---

### Security Specifications

#### SHA-256 Integrity Verification
Data integrity is treated as a first-class citizen in Swift. The system uses a "Pre-Transfer Fingerprint" model.

- **Phase 1 (Pre-Transfer)**: The Sender calculates a SHA-256 hash of the entire file. This hash is transmitted over the secure handshake.
- **Phase 2 (Transfer)**: The file is streamed in high-speed 64KB chunks.
- **Phase 3 (Post-Transfer)**: The Receiver performs an independent SHA-256 calculation on the downloaded file.
- **Final Validation**: The Receiver compares the local hash with the one sent by the Sender. If they differ by even a single byte, the file is flagged as corrupted.

#### Buffering and Performance
The protocol utilizes a fixed 64,536-byte buffer for the data transfer phase.
- **Conceptual Benefit**: By using a larger-than-standard buffer, we reduce the overhead of constant system calls for socket reading, allowing the system to maintain a steady, high-bandwidth stream that saturates local network capacity.

---

### Troubleshooting Guide

| Symptom | Probable Cause | Corrective Action |
| :--- | :--- | :--- |
| **Connection Timed Out** | Local firewall blockage or network isolation. | Ensure the host firewall permits traffic on Port 8888. |
| **PIN Refused** | Incorrect PIN entry or handshake desync. | Reset both nodes and restart the initiation. |
| **Hash Verification Failure** | Incomplete transfer or network packet loss. | This usually indicates an unstable physical network layer. Retry the transfer on a stable connection. |

---

### Project Roadmap

#### Planned Theoretical Enhancements
- **UDP Discovery**: Implementing a multicast discovery layer so that nodes can find each other across a LAN without manual IP entry.
- **Zip-Streaming**: Adding support for a stream-based compression layer (ZipOutputStream) to allow for folder transfers.
- **Segmented Resumption**: Utilizing `RandomAccessFile` to resume interrupted transfers by seeking to the last verified byte index.
- **Optional E2EE**: Implementing a post-handshake AES-256 wrapping layer for use on public or untrusted Wi-Fi environments.

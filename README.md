# Swift

Swift is a minimalist, high-speed, and secure peer-to-peer file transfer engine built in pure Java. It is designed to solve the complexity and bloat associated with modern file-sharing solutions by providing a direct, trust-based connection between two nodes.

### The Philosophy

Swift is built on the belief that software should be as simple as the task it performs. In a local network where high-speed throughput is physically possible, the software should not be the bottleneck. Swift strips away every non-essential layer—from complex configurations to external dependencies—leaving only a raw, high-performance transfer engine.

This is more than just an application; it is a demonstration of how a modular, interface-driven design can maintain security and integrity without sacrificing simplicity.

---

### System Lifecycle

The movement of data in Swift is governed by a multi-stage verification process. This ensures that the system is not only fast but also resilient to unauthorized access and data corruption.

```mermaid
graph LR
    A[File Selection] --> B[SHA-256 Hashing]
    B --> C[Network Discovery]
    C --> D[Handshake & PIN Pairing]
    D --> E[Buffered Transfer]
    E --> F[Integrity Verification]
    F --> G[Done]
```

#### Conceptual Journey
1.  **Preparation**: Before any data moves, the sender builds a cryptographic fingerprint of the file. This creates a "baseline of truth" for the transfer.
2.  **Negotiation**: The handshake is not just a technical connection; it is a consent-based agreement. The receiver sees what is coming and chooses to permit it.
3.  **Verification**: The PIN pairing provides an out-of-band security layer, ensuring that the human operating the receiver is the same one communicating with the sender.
4.  **Transfer**: Data moves in optimized slices, governed by high-speed buffers.
5.  **Final Trust**: Once the last byte arrives, the receiver re-calculates the fingerprint. Only if it matches the original "baseline of truth" is the file accepted.

---

### Key Features

- **Optimized Throughput**: Uses a 64KB high-speed buffer system to maximize local network utilization.
- **Physical Proximity Security**: Implements a 4-digit PIN pairing protocol to verify user intent.
- **Byte-Perfect Integrity**: Integrated SHA-256 checksum validation to ensure data remains identical during transit.
- **Modular Foundations**: A clean, interface-driven architecture that allows for easy extension and high maintainability.

---

### Quick Start

**1. Compilation**
```bash
javac -d . interfaces/*.java utils/*.java core/*.java Main.java
```

**2. Initiating a Receiver**
```bash
java Main 
# Select Option 1 (Receiver)
```

**3. Initiating a Sender**
```bash
java Main
# Select Option 2 (Sender), then provide the Target IP and File Path
```

---

### Documentation Reference

For deeper insights into the specific technical and philosophical choices that drive Swift, please refer to:

- [**BLUEPRINT.md**](./BLUEPRINT.md): Architectural theory and class relationships.
- [**PROTOCOL.md**](./PROTOCOL.md): Security models and network communication specifications.
- [**REASONING.md**](./REASONING.md): The core "How and Why" behind every design decision.

---
*Designed for performance-focused developers who value clean implementation.*

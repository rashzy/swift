package interfaces;

public interface IHashable {
    String generateHash(String filePath);
    boolean verifyHash(String filePath, String expectedHash);
}

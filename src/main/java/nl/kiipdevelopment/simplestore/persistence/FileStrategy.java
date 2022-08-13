package nl.kiipdevelopment.simplestore.persistence;

public enum FileStrategy {
    BIG (1024),
    NORMAL (512),
    SMALL (256);

    public final int maxFileSize;

    FileStrategy(int maxFileSize) {
        this.maxFileSize = maxFileSize * 1024;
    }
}

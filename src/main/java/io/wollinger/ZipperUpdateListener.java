package io.wollinger;

public interface ZipperUpdateListener {
    void update(String currentFile, int fileIndex, int totalFiles, int fileSize);
}

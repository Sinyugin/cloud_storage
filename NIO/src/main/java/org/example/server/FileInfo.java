package org.example.server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileInfo {
    private String fileName;
    private long length;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public FileInfo(Path path) {

        try {
            this.fileName = path.getFileName().toString();
            if (Files.isDirectory(path)) {
                this.length = -1L;
            } else {
                this.length = Files.size(path);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}


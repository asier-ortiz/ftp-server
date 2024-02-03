package com.ortiz.model;

import java.io.File;
import java.io.Serializable;

public class DirectoryStructure implements Serializable {

    private final String name;
    private final String path;
    private final boolean isDir;
    private int fileCount;
    private DirectoryStructure[] structures;

    public DirectoryStructure(String name) {
        File file = new File(name);
        this.name = file.getName();
        this.path = file.getPath();
        this.isDir = file.isDirectory();
        this.structures = getStructureFiles();
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            this.fileCount = 0;
            if (!(files == null)) {
                this.fileCount = files.length;
            }
        }
    }

    public DirectoryStructure(String name, String path, boolean isDir, int fileCount) {
        this.name = name;
        this.path = path;
        this.isDir = isDir;
        this.fileCount = fileCount;
    }

    public String getName() {
        String fileName = name;
        if (isDir) {
            int lastSeparatorIndex = path.lastIndexOf(File.separator);
            fileName = path.substring(lastSeparatorIndex + 1);
        }
        return fileName;
    }

    public String getPath() {
        return path;
    }

    public boolean isDir() {
        return isDir;
    }

    public int getFileCount() {
        return fileCount;
    }

    public DirectoryStructure[] getStructures() {
        return structures;
    }

    public DirectoryStructure[] getStructureFiles() {
        DirectoryStructure[] structures = null;
        File file = new File(this.path);
        File[] files = file.listFiles();
        int fileCount = files != null ? files.length : 0;
        if (fileCount > 0) {
            structures = new DirectoryStructure[fileCount];
            for (int i = 0; i < files.length; i++) {
                DirectoryStructure structure;
                String name = files[i].getName();
                String path = files[i].getPath();
                boolean isDir = files[i].isDirectory();
                int tempFileCount = 0;
                if (isDir) {
                    File[] tempFiles = files[i].listFiles();
                    if (!(tempFiles == null)) {
                        tempFileCount = tempFiles.length;
                    }
                }
                structure = new DirectoryStructure(name, path, isDir, tempFileCount);
                structures[i] = structure;
            }
        }
        return structures;
    }

    @Override
    public String toString() {
        String name = this.name;
        if (this.isDir) {
            name = "(DIR) " + this.name;
        }
        return name;
    }
}
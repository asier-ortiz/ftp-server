package com.ortiz.server;

import java.io.Serializable;

public class Request implements Serializable {

    private byte[] data;
    private String fileName;
    private String directory;
    private final RequestType requestType;

    public Request(byte[] data, String fileName, String directory) {
        this.data = data;
        this.fileName = fileName;
        this.directory = directory;
        this.requestType = RequestType.SENDFILE;
    }

    public Request(String fileName) {
        this.fileName = fileName;
        this.requestType = RequestType.REQUESTFILE;
    }

    public Request(byte[] data) {
        this.data = data;
        this.requestType = RequestType.GETFILEDATA;

    }

    public byte[] getData() {
        return data;
    }

    public String getFileName() {
        return fileName;
    }

    public String getDirectory() {
        return directory;
    }

    public RequestType getRequestType() {
        return requestType;
    }
}

enum RequestType {
    SENDFILE,
    GETFILEDATA,
    REQUESTFILE
}
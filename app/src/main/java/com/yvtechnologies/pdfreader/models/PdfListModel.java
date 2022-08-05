package com.yvtechnologies.pdfreader.models;

import android.net.Uri;

public class PdfListModel {
    private Uri uri;
    private String path;
    private String filename;
    private long dataandtime;
    private String size;

    public PdfListModel(Uri uri, String path, String filename, long dataandtime, String size) {
        this.uri = uri;
        this.path = path;
        this.filename = filename;
        this.dataandtime = dataandtime;
        this.size = size;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public long getDataandtime() {
        return dataandtime;
    }

    public void setDataandtime(long dataandtime) {
        this.dataandtime = dataandtime;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }
}
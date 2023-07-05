package com.example.downloadhelper.listener;

public interface UpperDownloadCallback {
    void onProgress(long currentSize,long totalSize,String filePath);
    void onResult(int downStatus,String filePath);
}

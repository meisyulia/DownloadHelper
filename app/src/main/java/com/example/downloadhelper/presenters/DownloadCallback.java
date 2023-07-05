package com.example.downloadhelper.presenters;

public interface DownloadCallback {
    /**
     * 下载中
     */
    void onProgress(long currentSize,long totalSize,String downloadUrl,String filePath);

    /**
     * 完成
     * @param
     */
    void onFinish(String downloadUrl,String filePath);


    /**
     * 出错
     * @param error
     */
    void onError(String error,String downloadUrl, String filePath);
}

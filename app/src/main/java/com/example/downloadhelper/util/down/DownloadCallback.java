package com.example.downloadhelper.util.down;

import java.io.File;

public interface DownloadCallback {
    /**
     * 开始
     */
    void onStart(long currentSize,long totalSize);
    /**
     * 下载中
     */
    void onProgress(long currentSize,long totalSize);
    /**
     * 暂停
     */
    void onPause();
    /**
     * 取消
     */
    void onCancel();

    /**
     * 完成
     * @param file
     */
    void onFinish(File file);

    /**
     * 等待
     */
    void onWait();

    /**
     * 出错
     * @param error
     */
    void onError(String error);
}

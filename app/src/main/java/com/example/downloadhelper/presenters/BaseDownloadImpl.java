package com.example.downloadhelper.presenters;

import android.os.Environment;


/**
 * @author liwanlian
 * @date 2023/3/15 10:14
 */
public abstract class BaseDownloadImpl {
    private static final String TAG = "lwl-DownloadImpl";
    protected DownloadCallback downloadCallback;

    public void setDownloadCallback(DownloadCallback downloadCallback) {
        this.downloadCallback = downloadCallback;
    }

    /**
     * 数据初始化
     */
    protected abstract void initData();

    /**
     * 启动下载
     *
     * @param downloadUrl
     * @param filePath
     */
    public void startDownload(String downloadUrl, String filePath) {

    }

    public void startDownload(String downloadUrl, String filePath, long startPoint, long totalSize) {

    }

    /**
     * 文件上传
     *
     * @param filePath
     * @param uploadUrl
     */
    public void uploadFile(String filePath, String uploadUrl) {

    }

    /**
     * 资源释放
     */
    public void releaseRes() {

    }

    public String getFilename(String path) {

        int start = path.lastIndexOf("/") + 1;
        String substring = path.substring(start);

        String fileName = Environment.getExternalStorageDirectory().getPath() + "/" + substring;
        return fileName;
    }

}

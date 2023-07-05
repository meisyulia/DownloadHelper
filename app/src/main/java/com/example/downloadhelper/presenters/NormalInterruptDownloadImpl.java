package com.example.downloadhelper.presenters;

import static com.example.downloadhelper.constant.DownloadConst.CANCEL;
import static com.example.downloadhelper.constant.DownloadConst.DESTROY;
import static com.example.downloadhelper.constant.DownloadConst.FINISH;
import static com.example.downloadhelper.constant.DownloadConst.PAUSE;
import static com.example.downloadhelper.database.entity.DownloadEntity_Table.downloadedBytes;

import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

public class NormalInterruptDownloadImpl extends BaseDownloadImpl{

    private static NormalInterruptDownloadImpl download;
    private HttpURLConnection conn;
    private InputStream inputStream;
    private RandomAccessFile rw;
    private boolean isPause = false;
    private String TAG = "normalInterrupt";

    public static NormalInterruptDownloadImpl getInstance(){
        if (download == null) {
            synchronized (NormalInterruptDownloadImpl.class){
                download = new NormalInterruptDownloadImpl();
            }
        }
        return download;
    }
    @Override
    protected void initData() {

    }

    @Override
    public void startDownload(String downloadUrl, String filePath) {
        boolean isPause = false;
        Log.i(TAG, "startDownload: 开始下载");
        super.startDownload(downloadUrl, filePath);
        conn = null;
        boolean isFinish = true;
        try {
            //初始化获取文件信息
            conn = (HttpURLConnection) new URL(downloadUrl).openConnection();
            conn.setConnectTimeout(10000);
            conn.setRequestMethod("GET");
            //获取已经下载的字节大小(通过获取文件的大小）
            File downFile = new File(filePath);
            long downedSize = 0;
            if (downFile.exists()){
                downedSize = downFile.length();
            }
            if (downedSize!= 0){
                conn.setRequestProperty("Range","bytes="+ downedSize +"-");
            }
            conn.connect();
            //Log.i(TAG, "saveRangeFile: totalBytes="+totalBytes);
            long totalSize = conn.getContentLength() + downedSize;
            inputStream = conn.getInputStream();
            rw = new RandomAccessFile(filePath, "rw");
            rw.seek(downedSize);
            //开始下载文件
            byte[] bytes = new byte[2048];
            int bytesRead;
            long totalDownloadedBytes = downedSize;
            //Log.i(TAG, "saveRangeFile: totalDownloadedBytes="+ Formatter.formatFileSize(context,totalDownloadedBytes));
            while ((bytesRead = inputStream.read(bytes))!=-1){
                /*if (isPause){
                    isFinish = false;
                    break;
                }*/
                rw.write(bytes,0,bytesRead);
                totalDownloadedBytes += bytesRead;
                if (downloadCallback!=null){
                    downloadCallback.onProgress(totalDownloadedBytes,totalSize,downloadUrl,filePath);
                }


            }
            inputStream.close();
            rw.close();

        } catch (Exception e) {
            e.printStackTrace();
            isFinish = false;
            if (!TextUtils.equals(e.getMessage(),"Socket closed") && !TextUtils.equals(e.getMessage(),"Socket is closed")){
                if(downloadCallback!=null){
                    //下载失败
                    downloadCallback.onError(e.toString(),downloadUrl,filePath);
                }
            }
            /*if (!e.getMessage().equals("Socket closed") && !e.getMessage().equals("Socket is closed")){
                if(downloadCallback!=null){
                    //下载失败
                    downloadCallback.onError(e.toString(),downloadUrl,filePath);
                }
            }*/

        }finally {
            try {

                if (conn !=null){
                    conn.disconnect();
                }
                if (isFinish==true){
                    if (downloadCallback != null) {
                        downloadCallback.onFinish(downloadUrl,filePath);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void startDownload(String downloadUrl, String filePath, long startPoint, long totalSize) {
        super.startDownload(downloadUrl, filePath, startPoint, totalSize);
    }

    @Override
    public void releaseRes() {
        super.releaseRes();
        try {
            /*if (inputStream != null) {
                inputStream.close();
            }
            if (rw != null) {
                rw.close();
            }*/
            if (conn != null) {
                conn.disconnect();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopDownload(){
        Log.i(TAG, "stopDownload: 停止下载！");
        //isPause = true;
        releaseRes();
    }
}

package com.example.downloadhelper.presenters;

import static com.example.downloadhelper.constant.DownloadConst.CANCEL;
import static com.example.downloadhelper.constant.DownloadConst.DESTROY;
import static com.example.downloadhelper.constant.DownloadConst.FINISH;
import static com.example.downloadhelper.constant.DownloadConst.PAUSE;

import android.text.TextUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OkhttpInterruptDownImpl extends BaseDownloadImpl{

    private static OkhttpInterruptDownImpl down;
    private FileOutputStream fos;
    private InputStream inputStream;
    private Call call;

    public static OkhttpInterruptDownImpl getInstance(){
        if (down == null) {
            synchronized (OkhttpInterruptDownImpl.class){
                down = new OkhttpInterruptDownImpl();
            }
        }
        return down;
    }

    @Override
    protected void initData() {

    }

    @Override
    public void startDownload(String downloadUrl, String filePath) {
        super.startDownload(downloadUrl, filePath);
        boolean isFinish = true;
        long downloadedBytes = 0;
        if (TextUtils.isEmpty(downloadUrl)){
            if (downloadCallback!=null){
                downloadCallback.onError("路径为空",downloadUrl,filePath);
                return;
            }
        }
        File downFile = new File(filePath);
        if (downFile.exists()){
            downloadedBytes = downFile.length();
        }
        try {
            //OkHttpClient client = new OkHttpClient();
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS) // 设置连接超时时间为10秒
                    .readTimeout(60, TimeUnit.SECONDS) // 设置读取超时时间为10秒
                    .build();
            Request request = new Request.Builder().url(downloadUrl)
                    .header("Range", "bytes=" + downloadedBytes + "-")
                    .build();
            call = client.newCall(request);
            Response response = call.execute();
            if (response.isSuccessful()){
                //从响应头获取要下载的大小
                Long totalBytes = Long.parseLong(response.header("Content-Length"))+downloadedBytes; //Content-Length获取的不是总大小，而是看你从哪里读起
                fos = new FileOutputStream(downFile, true);
                //读取并写入
                inputStream = response.body().byteStream();
                //开始下载文件
                byte[] bytes = new byte[2048];
                int bytesRead;
                long totalDownloadedBytes = downloadedBytes;
                //Log.i(TAG, "saveRangeFile: totalDownloadedBytes="+ Formatter.formatFileSize(context,totalDownloadedBytes));
                while ((bytesRead = inputStream.read(bytes))!=-1){

                    fos.write(bytes,0,bytesRead);
                    totalDownloadedBytes += bytesRead;
                    if (downloadCallback != null) {
                        downloadCallback.onProgress(totalDownloadedBytes,totalBytes,downloadUrl,filePath);
                    }
                }
                inputStream.close();
                fos.close();
            }else{
                isFinish = false;
                if (downloadCallback != null) {
                    downloadCallback.onError("响应码为："+response.code(),downloadUrl,filePath);
                }
            }
        } catch (Exception e) {
            isFinish = false;
            e.printStackTrace();
            if (!TextUtils.equals(e.getMessage(),"Socket closed") && !TextUtils.equals(e.getMessage(),"Socket is closed")){
                if(downloadCallback!=null){
                    //下载失败
                    downloadCallback.onError(e.toString(),downloadUrl,filePath);
                }
            }
        }finally {
            if (isFinish){
                if (downloadCallback != null) {
                    downloadCallback.onFinish(downloadUrl,filePath);
                }
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
            /*if (fos != null) {
                fos.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }*/
            call.cancel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopDownload(){
       releaseRes();
    }
}

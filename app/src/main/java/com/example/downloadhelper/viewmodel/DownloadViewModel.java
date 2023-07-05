package com.example.downloadhelper.viewmodel;

import static com.example.downloadhelper.constant.DownloadConst.ERROR;
import static com.example.downloadhelper.constant.DownloadConst.FINISH;
import static com.example.downloadhelper.constant.DownloadConst.WAIT;
import static com.example.downloadhelper.constant.DownloadType.HTTP_URL_CONNECTION;
import static com.example.downloadhelper.constant.DownloadType.OK_HTTP;
import static com.example.downloadhelper.constant.DownloadType.RETROFIT;

import android.app.Activity;
import android.os.Handler;

import androidx.lifecycle.ViewModel;

import com.example.downloadhelper.constant.DownloadConst;
import com.example.downloadhelper.constant.DownloadType;
import com.example.downloadhelper.database.entity.DownloadEntity;
import com.example.downloadhelper.database.helper.DownloadHelper;
import com.example.downloadhelper.listener.UpperDownloadCallback;
import com.example.downloadhelper.presenters.DownloadCallback;
import com.example.downloadhelper.presenters.NormalInterruptDownloadImpl;
import com.example.downloadhelper.presenters.OkhttpInterruptDownImpl;
import com.example.downloadhelper.presenters.RetrofitInterruptDownImpl;
import com.example.downloadhelper.util.common.DateUtil;
import com.example.downloadhelper.util.down.ThreadPool;

import java.io.File;

public class DownloadViewModel extends ViewModel implements DownloadCallback {

    private final Activity activity;
    private NormalInterruptDownloadImpl normalInterruptDownload;
    private OkhttpInterruptDownImpl okhttpInterruptDown;
    private RetrofitInterruptDownImpl retrofitInterruptDown;
    private UpperDownloadCallback upperDownloadCallback;

    public DownloadViewModel(Activity activity){
        this.activity = activity;
    }

    public void initData(){
        normalInterruptDownload = new NormalInterruptDownloadImpl();
        normalInterruptDownload.setDownloadCallback(this);
        okhttpInterruptDown = new OkhttpInterruptDownImpl();
        okhttpInterruptDown.setDownloadCallback(this);
        retrofitInterruptDown = new RetrofitInterruptDownImpl();
        retrofitInterruptDown.setDownloadCallback(this);
    }

    public void setOnUpperDownloadCallback(UpperDownloadCallback upperDownloadCallback){
        this.upperDownloadCallback = upperDownloadCallback;
    }

    public void startDownload(int downType,String downloadUrl,String filePath){
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (downType == HTTP_URL_CONNECTION){
                    normalInterruptDownload.startDownload(downloadUrl,filePath);
                }else if (downType == OK_HTTP){
                    okhttpInterruptDown.startDownload(downloadUrl,filePath);
                }else if (downType == RETROFIT){
                    retrofitInterruptDown.startDownload(downloadUrl,filePath);
                }
            }
        };
        ThreadPool.getInstance().getThreadPoolExecutor().execute(runnable);
       /* //如果正在下载的任务数量等于线程池的线程数，则新添加的任务处于等待任务
        if (ThreadPool.getInstance().getThreadPoolExecutor().getActiveCount() == ThreadPool.getInstance().getCorePoolSize()){
            saveData(filePath,WAIT);
            if (upperDownloadCallback != null) {
                upperDownloadCallback.onResult(WAIT,filePath);
            }
        }*/


    }

    public void stopDownload(int downType){
        if (downType == HTTP_URL_CONNECTION){
            normalInterruptDownload.stopDownload();
        }else if (downType == OK_HTTP){
            okhttpInterruptDown.stopDownload();
        }else if (downType == RETROFIT){
            retrofitInterruptDown.stopDownload();
        }
    }

    @Override
    public void onProgress(long currentSize, long totalSize, String downloadUrl, String filePath) {
        if (upperDownloadCallback != null) {
            upperDownloadCallback.onProgress(currentSize,totalSize,filePath);
        }
    }

    @Override
    public void onFinish(String downloadUrl, String filePath) {
        activity.runOnUiThread(()->{
            DownloadEntity entity = DownloadHelper.newInstance().queryBySavePath(filePath);
            entity.setStatus(FINISH);
            entity.setSavePath(filePath);
            entity.setEndTime(DateUtil.getNowDateTimeFormat());
            File file = new File(filePath);
            if (file.exists()){
                entity.setTotalBytes(file.length());
                //entity.setDownloadedBytes(file.length());
            }
            DownloadHelper.newInstance().save(entity);
        });
        if (upperDownloadCallback != null) {
            upperDownloadCallback.onResult(FINISH,filePath);
        }
    }

    @Override
    public void onError(String error,String downloadUrl, String filePath) {
        activity.runOnUiThread(()->{
            DownloadEntity entity = DownloadHelper.newInstance().queryBySavePath(filePath);
            entity.setStatus(ERROR);
            entity.setSavePath(filePath);
            DownloadHelper.newInstance().save(entity);
        });
        if (upperDownloadCallback != null) {
            upperDownloadCallback.onResult(ERROR,filePath);
        }
    }
}

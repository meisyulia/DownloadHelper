package com.example.downloadhelper.util.down;

import static com.example.downloadhelper.constant.DownloadConst.CANCEL;
import static com.example.downloadhelper.constant.DownloadConst.DESTROY;
import static com.example.downloadhelper.constant.DownloadConst.ERROR;
import static com.example.downloadhelper.constant.DownloadConst.FINISH;
import static com.example.downloadhelper.constant.DownloadConst.NONE;
import static com.example.downloadhelper.constant.DownloadConst.PAUSE;
import static com.example.downloadhelper.constant.DownloadConst.PROGRESS;
import static com.example.downloadhelper.constant.DownloadConst.START;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.downloadhelper.database.entity.DownloadEntity;
import com.example.downloadhelper.database.helper.DownloadHelper;
import com.example.downloadhelper.util.common.DateUtil;

import java.io.File;


public class DownloadProgressHandler {

    private static final String TAG = "cyl-handler";
    private final DownloadEntity entity;
    private final DownloadCallback downloadCallback;
    private final DownloadHelper mHelper;
    private final long id;
    private FileTask task;

    public DownloadProgressHandler(DownloadEntity entity, DownloadCallback downloadCallback){
        this.entity = entity;
        this.downloadCallback = downloadCallback;
        id = entity.getId();
        totalBytes = entity.getTotalBytes();
        downloadedBytes = entity.getDownloadedBytes();
        mCurrentState = entity.getStatus();
        mHelper = DownloadHelper.newInstance();
    }

    private int mCurrentState = NONE;
    private long totalBytes ;
    private long downloadedBytes;
    private long lastProgressTime = 0;
    public Handler mHandler = new Handler(){
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            int lastState = mCurrentState;
            //Log.i(TAG, "handleMessage: id="+entity.id);
            mCurrentState=msg.what;
            if (entity.getStatus() != mCurrentState && entity.id != 0){
                entity.setStatus(mCurrentState);
                mHelper.save(entity);
            }
            switch (mCurrentState){
                case START:
                    Bundle bundle = msg.getData();
                    totalBytes = bundle.getLong("totalBytes");
                    downloadedBytes = bundle.getLong("downloadedBytes");
                    if (downloadCallback != null) {
                        downloadCallback.onStart(downloadedBytes,totalBytes);
                        //Log.i(TAG, "handleMessage: downloadedBytes="+downloadedBytes);
                    }
                    break;
                case PROGRESS:
                    synchronized (this){
                        Bundle bundle1 = msg.getData();
                        totalBytes = bundle1.getLong("totalBytes");
                        downloadedBytes = bundle1.getLong("totalDownloadedBytes");
                        boolean needSave = bundle1.getBoolean("needSave");
                        entity.setTotalBytes(totalBytes);
                        entity.setDownloadedBytes(downloadedBytes);
                        if (needSave){
                            mHelper.save(entity);
                            Log.i(TAG, "handleMessage: progress-save="+downloadedBytes+",id="+entity.getId());
                        }
                        if (downloadCallback != null) {
                            if (System.currentTimeMillis()-lastProgressTime>=20 || downloadedBytes == totalBytes){
                                Log.i(TAG, "handleMessage:progress-- downloadedBytes="+downloadedBytes+",id="+entity.getId());
                                downloadCallback.onProgress(downloadedBytes,totalBytes);
                                lastProgressTime = System.currentTimeMillis();
                            }
                        }
                        if (downloadedBytes == totalBytes){
                            sendEmptyMessage(FINISH);
                        }
                    }
                    break;
                case PAUSE:
                    synchronized (this){
                        entity.setDownloadedBytes(downloadedBytes);
                        //Log.i(TAG, "handleMessage:pause-- downloadedBytes="+downloadedBytes);
                        entity.setTotalBytes(totalBytes);
                        //Log.i(TAG, "handleMessage: pause-total="+totalBytes);
                        boolean isSave = mHelper.save(entity);
                        //Log.i(TAG, "handleMessage: mHelper.save(entity)="+isSave);
                        if (downloadCallback!=null){
                            downloadCallback.onPause();
                        }
                    }
                    break;
                case CANCEL:
                    synchronized (this){
                        /*downloadedBytes = 0;
                        totalBytes = 0;*/
                        String savePath = entity.getSavePath();
                        if (new File(savePath).exists()){
                            new File(savePath).delete();
                        }
                        Log.i(TAG, "handleMessage: entity.id="+entity.getId());
                        //Log.i(TAG, "handleMessage: status="+entity.getStatus());
                        boolean delete = mHelper.delete(entity);
                        Log.i(TAG, "handleMessage: delete="+delete);
                        if (downloadCallback != null) {
                            downloadCallback.onProgress(0, 0);
                            downloadCallback.onCancel();
                        }
                    }
                    break;
                case FINISH:
                    entity.setDownloadedBytes(downloadedBytes);
                    entity.setTotalBytes(totalBytes);
                    entity.setEndTime(DateUtil.getNowDateTimeFormat());
                    mHelper.save(entity);
                    if (downloadCallback != null) {
                        downloadCallback.onFinish(new File(entity.getSavePath()));
                    }
                    break;
                case DESTROY:
                    synchronized (this){
                        //Log.i(TAG, "handleMessage: 进行销毁吗？");
                        entity.setDownloadedBytes(downloadedBytes);
                        entity.setTotalBytes(totalBytes);
                        mHelper.save(entity);
                        //Log.i(TAG, "handleMessage: status="+entity.getStatus());
                    }
                    break;
                case ERROR:
                    entity.setDownloadedBytes(downloadedBytes);
                    entity.setTotalBytes(totalBytes);
                    mHelper.save(entity);
                    if (downloadCallback != null) {
                        downloadCallback.onError((String)msg.obj);
                    }
                    break;
            }
        }
    };

    public Handler getHandler(){
        return mHandler;
    }
    public int getCurrentState(){
        return mCurrentState;
    }
    public DownloadEntity getDownloadEntity(){
        return entity;
    }
    public void setFileTask(FileTask task){
        this.task = task;
    }

    /**
     * 下载中退出时保存数据，释放资源
     */
    public void destroy(){
        //Log.i(TAG, "destroy: mCurrentState="+mCurrentState);
        if (mCurrentState==CANCEL || mCurrentState == PAUSE){
            return;
        }
        task.destroy();
    }

    /**
     * 暂停（正在下载才可以暂停）
     */
    public void pause(){
        if (mCurrentState == PROGRESS){
            task.pause();
        }
    }

    /**
     * 取消
     */
    public void cancel(){
        if (mCurrentState != CANCEL){
            if (mCurrentState == PROGRESS){
                if (task!=null){
                    task.cancel();
                }
            }else {
                if (mHandler!=null){
                    mHandler.sendEmptyMessage(CANCEL);
                }
            }
        }

    }

    /**
     * 在下载中刷新列表时保存数据
     */
    public void save(){
        if (mCurrentState == PROGRESS){
            task.save();
        }
    }
}

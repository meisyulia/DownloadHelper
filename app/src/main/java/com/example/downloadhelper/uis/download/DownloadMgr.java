package com.example.downloadhelper.uis.download;

import static com.example.downloadhelper.constant.DownloadConst.NONE;
import static com.example.downloadhelper.constant.DownloadConst.PROGRESS;

import android.util.Log;

import com.example.downloadhelper.constant.DownloadConst;
import com.example.downloadhelper.database.entity.DownloadEntity;
import com.example.downloadhelper.viewmodel.DownloadViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DownloadMgr {
    private static final String TAG = "DownloadMgr";
    private static DownloadMgr downloadMgr;
    private Map<String, DownloadViewModel> downloadViewModelMap = new HashMap<>();
    public static DownloadMgr getInstance(){
        if (downloadMgr == null) {
            synchronized (DownloadMgr.class){
                downloadMgr = new DownloadMgr();
            }
        }

        return downloadMgr;
    }

    public synchronized void addData(DownloadEntity entity,DownloadViewModel downloadViewModel){
        downloadViewModelMap.put(entity.getSavePath(),downloadViewModel);
        Log.i(TAG, "addData: downloadViewModelMap.size()="+downloadViewModelMap.size());
    }

    public synchronized void start(DownloadEntity entity){
        Log.i(TAG, "start: downloadViewModelMap.size()="+downloadViewModelMap.size());
        DownloadViewModel downloadViewModel = downloadViewModelMap.get(entity.getSavePath());
        if (downloadViewModel!=null){
            downloadViewModel.startDownload(entity.getDownloadType(), entity.getUrl(), entity.getSavePath());
        }

    }

    public synchronized void stop(DownloadEntity entity){
        //Log.i(TAG, "stop: downloadViewModelMap.size()="+downloadViewModelMap.size());
        DownloadViewModel downloadViewModel = downloadViewModelMap.get(entity.getSavePath());
        if (downloadViewModel != null) {
            //Log.i(TAG, "stop: downloadViewModel不为空");
            downloadViewModel.stopDownload(entity.getDownloadType());
        }
    }

    public void removeData(DownloadEntity entity){
        if (downloadViewModelMap.get(entity.getSavePath())!=null){
            downloadViewModelMap.remove(entity.getSavePath());
        }
    }

    /**
     * 重新进入应用后恢复下载
     * @param entities
     */
    public void resumeAll(ArrayList<DownloadEntity> entities){
        for (DownloadEntity entity : entities) {
            //Log.i(TAG, "resumeAll: status="+entity.getStatus());
            if (entity.getStatus() == PROGRESS || entity.getStatus()==NONE){
                start(entity);
            }
        }
    }

    /**
     * 销毁应用时同时也停止下载
     * @param entities
     */
    public void destroyAll(ArrayList<DownloadEntity> entities){
        for (DownloadEntity entity : entities) {
            if (entity.getStatus() == PROGRESS){
                stop(entity);
            }
            removeData(entity);
        }
    }
}

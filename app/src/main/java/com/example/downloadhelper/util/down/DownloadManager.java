package com.example.downloadhelper.util.down;


import static com.example.downloadhelper.constant.DownloadConst.ERROR;
import static com.example.downloadhelper.constant.DownloadConst.NONE;
import static com.example.downloadhelper.constant.DownloadConst.PAUSE;
import static com.example.downloadhelper.constant.DownloadConst.PROGRESS;

import android.content.Context;
import android.util.Log;

import com.example.downloadhelper.database.entity.DownloadEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DownloadManager {

    private static final String TAG = "DownloadManager";
    private static DownloadManager downloadManager;
    private final Context context;

    private Map<Long,DownloadProgressHandler> progressHandlerMap = new HashMap<>();
    private Map<Long,DownloadEntity> downloadEntityMap = new HashMap<>();
    private Map<Long,DownloadCallback> callbackMap = new HashMap<>();
    private Map<Long, FileTask> fileTaskMap = new HashMap<>();

    public static DownloadManager getInstance(Context context){
        if (downloadManager == null) {
            synchronized (DownloadManager.class){
                downloadManager = new DownloadManager(context);
            }
        }
        return downloadManager;
    }

    private DownloadManager(Context context){
        this.context = context;
    }

    /**
     * 配置线程池
     *
     * @param corePoolSize
     * @param maxPoolSize
     */
    public void setTaskPoolSize(int corePoolSize, int maxPoolSize) {
        if (maxPoolSize > corePoolSize && maxPoolSize * corePoolSize != 0) {
            ThreadPool.getInstance().setCorePoolSize(corePoolSize);
            ThreadPool.getInstance().setMaxPoolSize(maxPoolSize);
        }
    }

    /**
     * 根据id进行下载（注意需要先注册监听）
     * @param id
     * @return
     */
    public DownloadManager start(Long id){
        execute(downloadEntityMap.get(id),callbackMap.get(id));
        return downloadManager;
    }

    /**
     * 注册监听
     * @param entity
     * @param downloadCallback
     */
    public synchronized void setOnDownloadCallback(DownloadEntity entity,DownloadCallback downloadCallback){
        //Log.i(TAG, "setOnDownloadCallback: entity.id="+entity.getId());
        downloadEntityMap.put(entity.getId(),entity);
        callbackMap.put(entity.getId(),downloadCallback);
    }


    /**
     * 执行下载
     * @param entity
     * @param downloadCallback
     */
    private synchronized void execute(DownloadEntity entity,DownloadCallback downloadCallback){
        //防止同一个任务多个添加下载
        if (null != progressHandlerMap.get(entity.getId())){
            return;
        }
        DownloadProgressHandler downloadProgressHandler = new DownloadProgressHandler(entity, downloadCallback);
        FileTask fileTask = new FileTask(context,entity, downloadProgressHandler.getHandler());
        downloadProgressHandler.setFileTask(fileTask);

        downloadEntityMap.put(entity.getId(),entity);
        callbackMap.put(entity.getId(),downloadCallback);
        fileTaskMap.put(entity.getId(), fileTask);
        progressHandlerMap.put(entity.getId(),downloadProgressHandler);

        ThreadPool.getInstance().getThreadPoolExecutor().execute(fileTask);
        //如果正在下载的任务数量等于线程池的线程数，则新添加的任务处于等待任务
        if (ThreadPool.getInstance().getThreadPoolExecutor().getActiveCount() == ThreadPool.getInstance().getCorePoolSize()){
            downloadCallback.onWait();
        }
    }

    public void pause(Long id){
        if (progressHandlerMap.containsKey(id)){
            progressHandlerMap.get(id).pause();
        }
    }


    public void resume(Long id){
        //Log.i(TAG, "resume: progressHandlerMap.size="+progressHandlerMap.size());
        if (progressHandlerMap.containsKey(id) &&
                (progressHandlerMap.get(id).getCurrentState() == PAUSE ||
                        progressHandlerMap.get(id).getCurrentState() == ERROR)){
            progressHandlerMap.remove(id);
            execute(downloadEntityMap.get(id),callbackMap.get(id));
        }
        if (progressHandlerMap.containsKey(id) && progressHandlerMap.get(id).getCurrentState() == PROGRESS){
            //progressHandlerMap.remove(id);
            //execute(downloadEntityMap.get(id),callbackMap.get(id));
            //ThreadPool.getInstance().getThreadPoolExecutor().execute(fileTaskMap.get(id));
            progressHandlerMap.remove(id);
            execute(downloadEntityMap.get(id),callbackMap.get(id));
        }

    }

    public void cancel(long id){
        Log.i(TAG, "cancel: id="+id);
        Log.i(TAG, "cancel: progressHandlerMap.size="+progressHandlerMap.size());
        Log.i(TAG, "cancel: downloadEntityMap.size="+downloadEntityMap.size());
        //Log.i(TAG, "cancel: progressHandlerMap.size="+progressHandlerMap.size());
       /* if (progressHandlerMap.get(id)!=null){
            if (progressHandlerMap.get(id).getCurrentState()==NONE){
                //取消缓存队列中等待下载的任务
                ThreadPool.getInstance().getThreadPoolExecutor().remove(fileTaskMap.get(id));
                callbackMap.get(id).onCancel();
            }
            //取消缓存队列中等待下载的任务
            ThreadPool.getInstance().getThreadPoolExecutor().remove(fileTaskMap.get(id));
            callbackMap.get(id).onCancel();
            //取消已经开始下载的任务
            progressHandlerMap.get(id).cancel();
            progressHandlerMap.remove(id);
            fileTaskMap.remove(id);
        }else{
            //重新进去的话，之前保存的就清空，在这个情况下点击取消(直接删除）
            if (downloadEntityMap.get(id)!=null){
                DownloadProgressHandler downloadProgressHandler = new DownloadProgressHandler(downloadEntityMap.get(id), callbackMap.get(id));
                downloadProgressHandler.cancel(); //这个执行不了
            }
        }*/
        if (progressHandlerMap.get(id)==null){
            execute(downloadEntityMap.get(id),callbackMap.get(id));
        }
        if (progressHandlerMap.get(id)!=null){
            //取消缓存队列中等待下载的任务
            if (progressHandlerMap.get(id).getCurrentState()==NONE){
                ThreadPool.getInstance().getThreadPoolExecutor().remove(fileTaskMap.get(id));
                callbackMap.get(id).onCancel();
            }else{
                //取消已经开始下载的任务
                progressHandlerMap.get(id).cancel();
            }
            progressHandlerMap.remove(id);
            fileTaskMap.remove(id);
            downloadEntityMap.remove(id);
            callbackMap.remove(id);
        }

    }

    /**
     * 退出时释放资源
     *
     * @param id
     */
    public void destroy(long id) {
        Log.i(TAG, "destroy: progressHandlerMap.size="+progressHandlerMap.size());
        if (progressHandlerMap.containsKey(id)) {
            progressHandlerMap.get(id).destroy();
            progressHandlerMap.remove(id);
            callbackMap.remove(id);
            downloadEntityMap.remove(id);
            fileTaskMap.remove(id);
        }
    }

    public void destroy(ArrayList<DownloadEntity> entities){
        for (DownloadEntity entity : entities) {
            destroy(entity.getId());
        }
    }

    /**
     * 刷新列表时将正在下载的保存数据到数据库(用这个不行，列表会很卡）
     */
    public void save(long id){
        if (progressHandlerMap.containsKey(id)){
            progressHandlerMap.get(id).save();
        }
    }

    public void save(ArrayList<DownloadEntity> entities){
        for (DownloadEntity entity : entities) {
            save(entity.getId());
        }
    }

}

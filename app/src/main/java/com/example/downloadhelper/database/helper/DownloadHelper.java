package com.example.downloadhelper.database.helper;

import android.util.Log;

import com.example.downloadhelper.database.entity.DownloadEntity;
import com.example.downloadhelper.database.entity.DownloadEntity_Table;
import com.example.downloadhelper.util.common.Mutex;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class DownloadHelper {
    private static final String TAG = "DownloadHelper";
    private static DownloadHelper mHelper = null;
    private Mutex locker = new Mutex();
    public static DownloadHelper newInstance(){
        if (mHelper==null){
            synchronized (DownloadHelper.class){
                if (mHelper==null){
                    mHelper = new DownloadHelper();
                }
            }
        }
        return mHelper;
    }

    public boolean save(DownloadEntity entity){
        boolean result = false;
        locker.lock(0);
        result = entity.save();
        locker.unlock();
        return  result;
    }

    public boolean delete(DownloadEntity entity){
        boolean result = false;
        locker.lock(0);
        result = entity.delete();
        locker.unlock();
        return result;
    }

    public void clear() {
        locker.lock(0);
        SQLite.delete(DownloadEntity.class).execute();
        locker.unlock();
    }

    public DownloadEntity queryById(long id){
        DownloadEntity entity = null;
        locker.lock(0);
        entity = SQLite.select().from(DownloadEntity.class).where(DownloadEntity_Table.id.is(id)).querySingle();
        locker.unlock();
        return entity;
    }

    public DownloadEntity queryBySavePath(String savePath){
        DownloadEntity entity = null;
        locker.lock(0);
        entity = SQLite.select().from(DownloadEntity.class).where(DownloadEntity_Table.savePath.is(savePath)).querySingle();
        locker.unlock();
        return entity;
    }

    public List<DownloadEntity> queryALL(){
        List<DownloadEntity> list = null;
        locker.lock(0);
        list = SQLite.select().from(DownloadEntity.class).orderBy(DownloadEntity_Table.id.desc()).queryList();
        locker.unlock();
        return list;
    }

    public List<DownloadEntity> queryByStatus(int status){
        List<DownloadEntity> list = null;
        locker.lock(0);
        list = SQLite.select().from(DownloadEntity.class).where(DownloadEntity_Table.status.is(status)).queryList();
        locker.unlock();
        return list;
    }

    public List<DownloadEntity> queryByNotStatus(int status){
        List<DownloadEntity> list = null;
        locker.lock(0);
        list = SQLite.select().from(DownloadEntity.class).where(DownloadEntity_Table.status.isNot(status)).queryList();
        locker.unlock();
        return list;
    }

    public List<DownloadEntity> queryFileName(String fileName){
        List<DownloadEntity> list = null;
        locker.lock(0);
        list = SQLite.select().from(DownloadEntity.class).where(DownloadEntity_Table.fileName.like("%"+fileName+"%"))
                .queryList();
        locker.unlock();
        return list;
    }

    /**
     * 获取所有已下载文件下载的日期
     * @return
     */
    public List<String> queryEndDateAll(){
        locker.lock(0);
        List<DownloadEntity> list = SQLite.select(DownloadEntity_Table.endTime).from(DownloadEntity.class)
                .where(DownloadEntity_Table.endTime.isNotNull())
                .orderBy(DownloadEntity_Table.endTime.desc())
                .queryList();
        locker.unlock();
        Set<String> endDateSet = new LinkedHashSet<>();
        List<String> endDate = new ArrayList<>();
        for (DownloadEntity entity : list) {
            String[] time = entity.getEndTime().split(" ");
            if (endDateSet.add(time[0])){
                endDate.add(time[0]);
            }
        }
        return endDate;
    }

    public List<DownloadEntity> queryByEndDate(String date){
        List<DownloadEntity> list = null;
        locker.lock(0);
        list = SQLite.select().from(DownloadEntity.class)
                .where(DownloadEntity_Table.endTime.like(date + "%"))
                .orderBy(DownloadEntity_Table.endTime.desc())
                .queryList();
        locker.unlock();
        return list;
    }

    public List<DownloadEntity> queryByStartDate(String date){
        List<DownloadEntity> list = null;
        locker.lock(0);
        list = SQLite.select().from(DownloadEntity.class)
                .where(DownloadEntity_Table.startTime.like(date + "%"))
                .orderBy(DownloadEntity_Table.startTime.desc())
                .queryList();
        locker.unlock();
        return list;
    }
}

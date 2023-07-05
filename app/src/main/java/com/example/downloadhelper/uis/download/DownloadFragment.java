package com.example.downloadhelper.uis.download;

import static android.app.Activity.RESULT_OK;
import static com.example.downloadhelper.constant.DownloadConst.CANCEL;
import static com.example.downloadhelper.constant.DownloadConst.FINISH;
import static com.example.downloadhelper.constant.DownloadConst.NONE;
import static com.example.downloadhelper.constant.DownloadConst.PAUSE;
import static com.example.downloadhelper.constant.DownloadConst.PROGRESS;
import static com.example.downloadhelper.constant.DownloadConst.START;
import static com.example.downloadhelper.constant.DownloadType.HTTP_URL_CONNECTION;
import static com.example.downloadhelper.constant.DownloadType.OK_HTTP;
import static com.example.downloadhelper.constant.DownloadType.RETROFIT;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.downloadhelper.adapter.DownloadAdapter;
import com.example.downloadhelper.adapter.FinishAdapter;
import com.example.downloadhelper.adapter.ListFragPagerAdapter;
import com.example.downloadhelper.constant.DownloadConst;
import com.example.downloadhelper.constant.DownloadType;
import com.example.downloadhelper.database.entity.DownloadEntity;
import com.example.downloadhelper.database.helper.DownloadHelper;
import com.example.downloadhelper.databinding.FragmentDownloadBinding;
import com.example.downloadhelper.databinding.LayoutTabNavBinding;
import com.example.downloadhelper.listener.OnDownClickListener;
import com.example.downloadhelper.listener.OnFinishClickListener;
import com.example.downloadhelper.listener.OnHandleClickListener;
import com.example.downloadhelper.listener.UpperDownloadCallback;
import com.example.downloadhelper.uis.BaseFragment;
import com.example.downloadhelper.uis.PlayerActivity;
import com.example.downloadhelper.util.common.ClickUtil;
import com.example.downloadhelper.util.common.DateUtil;
import com.example.downloadhelper.util.common.FileUtil;
import com.example.downloadhelper.util.common.GsonUtil;
import com.example.downloadhelper.util.down.DownloadManager;
import com.example.downloadhelper.util.down.DownloadProgressHandler;
import com.example.downloadhelper.viewmodel.DownloadViewModel;
import com.example.downloadhelper.widget.SelectPopup;
import com.google.gson.Gson;
import com.huawei.hms.hmsscankit.ScanUtil;
import com.huawei.hms.ml.scan.HmsScan;
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class DownloadFragment extends BaseFragment implements OnHandleClickListener, OnFinishClickListener, UpperDownloadCallback, OnDownClickListener {

    private static final String TAG = "DownloadFragment";
    public static final int REQUEST_CODE_SCAN = 0x110;
    private com.example.downloadhelper.databinding.FragmentDownloadBinding mBinding;
    private LinkedList<Fragment> mLinkList;
    private com.example.downloadhelper.databinding.LayoutTabNavBinding mTabBinding;
    private Context mContext;
    private DownloadHelper mHelper;
    private ArrayList<DownloadEntity> mDownedList;
    private ArrayList<DownloadEntity> mFinishedList;
    private DownloadAdapter mDownAdap;
    private FinishAdapter mFinishAdap;
    private View mRootView;
    private SelectPopup mSelectPopup;
    private int selectType=-1;
    private View.OnClickListener downloadClickListener;
    private DownInfoDlg downInfoDlg;
    private Activity mActivity;
    private DownloadViewModel mDownModel;
    private long lastUpdateTime = 0;
    private DownloadEntity download;
    private DownloadEntity downEntity;
    private long lastProgressTime = 0;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContext = getContext();
        mActivity = getActivity();
        mBinding = FragmentDownloadBinding.inflate(inflater, container, false);
       // mBinding = FragmentDownloadBinding.inflate(inflater);
        mTabBinding = LayoutTabNavBinding.bind(mBinding.getRoot());
        mSelectPopup = new SelectPopup(mContext);
        return mBinding.getRoot();
    }

    @Override
    protected void initData() {
        /*mLinkList = new LinkedList<>();
        mLinkList.add(new DownListFragment());
        mLinkList.add(new CompleteListFragment());*/
        mHelper = DownloadHelper.newInstance();
        mDownedList = (ArrayList<DownloadEntity>) mHelper.queryByNotStatus(FINISH);
        mFinishedList = (ArrayList<DownloadEntity>) mHelper.queryByStatus(FINISH);
        mDownAdap = new DownloadAdapter(mContext, mDownedList);
        mFinishAdap = new FinishAdapter(mContext, mFinishedList);

    }

    @Override
    protected void initView() {
        /*ListFragPagerAdapter pagerAdapter = new ListFragPagerAdapter(getChildFragmentManager(), getLifecycle(), mLinkList);
        mBinding.vpList.setAdapter(pagerAdapter);
        mBinding.vpList.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                changePager(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });*/
        initSelectPopup();
        mBinding.llScan.setOnClickListener(v->startScan(v));
        mBinding.btnDownload.setOnClickListener(v->downloadClick(v));
        LinearLayoutManager downLLM = new LinearLayoutManager(mContext, RecyclerView.VERTICAL, false);
        mBinding.rvList.setLayoutManager(downLLM);
        mBinding.rvList.setAdapter(mDownAdap);
       // mDownAdap.setOnHandleClickListener(this);
        mDownAdap.setOnDownClickListener(this);
        LinearLayoutManager finishLLM = new LinearLayoutManager(mContext, RecyclerView.VERTICAL, false);
        mBinding.rvFinishList.setLayoutManager(finishLLM);
        mBinding.rvFinishList.setAdapter(mFinishAdap);
        mFinishAdap.setOnFinishClickListener(this);
        mTabBinding.tvDownList.setOnClickListener(v->{
            changePager(0);
        });
        mTabBinding.tvCompleteList.setOnClickListener(v->{
            changePager(1);
        });
        changePager(0);
    }

    private void startScan(View v) {
        ScanUtil.startScan(getActivity(), REQUEST_CODE_SCAN, new HmsScanAnalyzerOptions.Creator().create());
    }

    private void initSelectPopup() {
        mSelectPopup.addTextView("HttpURLConnection下载", HTTP_URL_CONNECTION);
        mSelectPopup.addTextView("OkHttp下载",OK_HTTP);
        mSelectPopup.addTextView("Retrofit下载",RETROFIT);

    }

    private void downloadClick(View v) {
        String url = mBinding.etDownLink.getText().toString();
        if (TextUtils.isEmpty(url)) {
            showTips("请输入链接");
            return;
        } else {
            mSelectPopup.showPopupSelect(mBinding.btnDownload);
            mSelectPopup.setOnItemClickListener(new SelectPopup.OnItemClickListener() {
                @Override
                public void onItemClick(String title, int callId) {
                    selectType = callId;
                    Log.i(TAG, "downloadClick: selectType="+selectType);
                    if (selectType>0){
                        DownloadEntity entity = new DownloadEntity();
                        String fileName = FileUtil.getFileName(url);
                        String savePath = mContext.getFilesDir() + "/" + fileName;
                        //判断文件名是否有一样的,有则更新文件名
                        if (FileUtil.isFileExists(savePath)) {
                            savePath = FileUtil.addSerialNumber(savePath, fileName, 1);
                            fileName = savePath.substring(savePath.lastIndexOf('/') + 1);
                        }
                        //查看是否有文件名一样的
                        for (DownloadEntity downloadEntity : mHelper.queryByNotStatus(FINISH)) {
                            if (fileName == downloadEntity.getFileName()) {
                                showTips("该文件正准备下载");
                                return;
                            }
                        }
                        entity.setUrl(url);
                        entity.setStatus(NONE);
                        entity.setFileName(fileName);
                        entity.setSavePath(savePath);
                        entity.setDownloadType(selectType);
                        entity.setStartTime(DateUtil.getNowDateTimeFormat());
                        mHelper.save(entity);
                        mBinding.etDownLink.setText("");
                        //mDownModel.startDownload(selectType,url,savePath);
                        initDownload(entity);
                        DownloadMgr.getInstance().start(entity);
                        updateDownList();
                    }
                }


            });

            /*DownloadEntity entity = new DownloadEntity();
            String fileName = FileUtil.getFileName(url);
            String savePath = mContext.getFilesDir() + "/" + fileName;
            //判断文件名是否有一样的,有则更新文件名
            if (FileUtil.isFileExists(savePath)) {
                savePath = FileUtil.addSerialNumber(savePath, fileName, 1);
                fileName = savePath.substring(savePath.lastIndexOf('/') + 1);
            }
            //查看是否有文件名一样的
            for (DownloadEntity downloadEntity : mHelper.queryByNotStatus(FINISH)) {
                if (fileName == downloadEntity.getFileName()) {
                    showTips("该文件正准备下载");
                    return;
                }
            }
            entity.setUrl(url);
            entity.setStatus(NONE);
            entity.setFileName(fileName);
            entity.setSavePath(savePath);
            //entity.setDownloadType(selectType);
            mHelper.save(entity);
            mBinding.etDownLink.setText("");
            updateDownList();*/
        }
    }

    /*private void selectPage(int i) {
        changePager(i);
//        mBinding.vpList.setCurrentItem(i);
    }*/

    private void initDownload(DownloadEntity entity) {
        for (DownloadEntity downloadEntity : mDownedList) {
            if (downloadEntity.getId() == entity.getId()){
                downEntity = downloadEntity;
                break;
            }
        }
        DownloadViewModel downloadViewModel = new DownloadViewModel(mActivity);
        downloadViewModel.initData();
        /*downloadViewModel.setOnUpperDownloadCallback(new UpperDownloadCallback() {
            private long totalSize;
            private long currentSize;
            private long lastProgressTime;
            @Override
            public void onProgress(long currentSize, long totalSize, String filePath) {
                if (TextUtils.equals(downEntity.getSavePath(),filePath)) {
                    //Log.i(TAG, "onProgress: this.currentSize="+this.currentSize);
                    if (this.currentSize != currentSize && System.currentTimeMillis()-lastProgressTime>=100){
                        downEntity.setStatus(PROGRESS);
                        downEntity.setDownloadedBytes(currentSize);
                        Log.i(TAG, "onProgress11: currentSize="+currentSize+",id="+downEntity.id);
                        downEntity.setTotalBytes(totalSize);
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(()->{
                                 mDownAdap.setDataList(mDownedList);  //刷新
                                //mDownAdap.notifyItemChanged(mDownedList.indexOf(entity), entity);
                            });
                        }
                        lastProgressTime = System.currentTimeMillis();
                    }
                    this.currentSize = currentSize;
                    this.totalSize = totalSize;
                }
            }

            @Override
            public void onResult(int downStatus, String filePath) {
                updateDownList();
            }
        });*/
        downloadViewModel.setOnUpperDownloadCallback(this);
        DownloadMgr.getInstance().addData(entity,downloadViewModel);
    }

    private void changePager(int position) {
        switch (position){
            case 0:
               mTabBinding.tvDownList.setSelected(true);
               mTabBinding.tvCompleteList.setSelected(false);
               mBinding.rvList.setVisibility(View.VISIBLE);
               mBinding.rvFinishList.setVisibility(View.GONE);
               break;
            case 1:
                mTabBinding.tvDownList.setSelected(false);
                mTabBinding.tvCompleteList.setSelected(true);
                mBinding.rvList.setVisibility(View.GONE);
                mBinding.rvFinishList.setVisibility(View.VISIBLE);
                mFinishedList = (ArrayList<DownloadEntity>) mHelper.queryByStatus(FINISH);
                mFinishAdap.setDataList(mFinishedList);
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mDownedList != null) {
            for (DownloadEntity downloadEntity : mDownedList) {
                DownloadEntity entity = mHelper.queryBySavePath(downloadEntity.getSavePath());
                initDownload(entity);
            }
            DownloadMgr.getInstance().resumeAll(mDownedList);
        }

    }

    @Override
    public void onDestroyView() {
        Log.i(TAG, "onDestroyView: destroy-->");
        mBinding = null;
        //DownloadManager.getInstance(mContext).destroy(mDownedList);
        //销毁前保存
        for (DownloadEntity downloadEntity : mDownedList) {
            DownloadEntity entity = mHelper.queryById(downloadEntity.getId());
            if (downloadEntity.getStatus()==PROGRESS){
                entity.setStatus(PROGRESS);
                mHelper.save(entity);
            }
        }
        DownloadMgr.getInstance().destroyAll(mDownedList);
        //如果是直接关闭应用要有走这个方法需要在super之前
        super.onDestroyView();
    }


    @Override
    public void onPause(DownloadEntity data, int position) {
        DownloadManager.getInstance(mContext).pause(data.getId());
       // updateDownList();
    }

    @Override
    public void onResume(DownloadEntity data, int position) {
        DownloadManager.getInstance(mContext).resume(data.getId());
        //updateDownList();
    }

    @Override
    public void onStart(DownloadEntity data, int position) {
        DownloadManager.getInstance(mContext).start(data.getId());
    }

    @Override
    public void onCancel(DownloadEntity data, int position) {
        DownloadManager.getInstance(mContext).cancel(data.getId());
        //updateDownList();
    }

    @Override
    public void onDelete(DownloadEntity data, int position) {
        if (data.getId()>0){
            mHelper.delete(data);
        }
        updateDownList();
    }

    @Override
    public void onFinish(DownloadEntity data, int position) {
        updateDownList();
    }

    private void updateDownList() {
        if (getActivity()!=null){
            getActivity().runOnUiThread(()->{
                mDownedList = (ArrayList<DownloadEntity>) mHelper.queryByNotStatus(FINISH);
                //DownloadManager.getInstance(mContext).save(mDownedList);
                mDownAdap.setDataList(mDownedList);
            });
        }



        /*getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDownAdap.setDataList(mDownedList);
            }
        });*/
        /*for (DownloadEntity entity : mDownedList) {
            downloadEntityMap.put(entity.getSavePath(),entity);
        }*/
    }

    @Override
    public void onOpenClick(DownloadEntity entity, int position) {
        Intent intent = new Intent(mContext, PlayerActivity.class);
        //Log.i(TAG, "onOpenClick: entity.getSavePath()="+entity.getSavePath());
        intent.putExtra("savePath",entity.getSavePath());
        startActivity(intent);
    }

    @Override
    public void onItemClick(DownloadEntity entity, int position) {
        String downInfo = GsonUtil.toJsonString(entity);
        if (downInfoDlg!=null && downInfoDlg.isResumed() || ClickUtil.isFastClick(800)){
            return;
        }
        downInfoDlg = DownInfoDlg.getInstance(downInfo);
        downInfoDlg.showNow(getActivity().getSupportFragmentManager(),DownInfoDlg.class.getSimpleName());
        downInfoDlg.setCancelable(false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null) {
            return;
        }
        if (requestCode == REQUEST_CODE_SCAN) {
            // 导入图片扫描返回结果
            int errorCode = data.getIntExtra(ScanUtil.RESULT_CODE, ScanUtil.SUCCESS);
            if (errorCode == ScanUtil.SUCCESS) {
                Object obj = data.getParcelableExtra(ScanUtil.RESULT);
                if (obj instanceof HmsScan) {
                    if (!TextUtils.isEmpty(((HmsScan) obj).getOriginalValue())) {
                        mBinding.etDownLink.setText(((HmsScan) obj).getOriginalValue());
                    }
                    return;
                }
            }
        }
    }

    @Override
    public void onProgress(long currentSize, long totalSize, String filePath) {
        for (DownloadEntity entity : mDownedList) {
            if (TextUtils.equals(entity.getSavePath(),filePath)) {
                if (entity.getDownloadedBytes() != currentSize && System.currentTimeMillis()-lastProgressTime>=100){
                    entity.setStatus(PROGRESS);
                    entity.setDownloadedBytes(currentSize);
                    Log.i(TAG, "onProgress: currentSize="+currentSize+",id="+entity.id);
                    entity.setTotalBytes(totalSize);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(()->{
                           // mDownAdap.setDataList(mDownedList);  //刷新
                            mDownAdap.notifyItemChanged(mDownedList.indexOf(entity), entity);
                        });
                    }
                    lastProgressTime = System.currentTimeMillis();
                }
                break;
            }
        }

    }

    @Override
    public void onResult(int downStatus, String filePath) {
        updateDownList();
    }

    @Override
    public void onItemStop(int position, DownloadEntity entity) {
        //mDownModel.stopDownload(entity.getDownloadType());
        DownloadMgr.getInstance().stop(entity);
        entity.setStatus(PAUSE);
        //Log.i(TAG, "onItemStop: entity.downed="+entity.getDownloadedBytes()+",id="+entity.getId());
        mHelper.save(entity);
        updateDownList();
        /*if (getActivity() != null) {
            getActivity().runOnUiThread(()->{
               // mDownAdap.setDataList(mDownedList);  //刷新
                mDownAdap.notifyItemChanged(mDownedList.indexOf(entity), entity);
            });
        }*/
    }

    @Override
    public void onItemStart(int position, DownloadEntity entity) {
        //mDownModel.startDownload(entity.getDownloadType(),entity.getUrl(),entity.getSavePath());
        DownloadMgr.getInstance().start(entity);
        entity.setStatus(PROGRESS);
        mHelper.save(entity);
        updateDownList();
        /*if (getActivity() != null) {
            getActivity().runOnUiThread(()->{
                //mDownAdap.setDataList(mDownedList);  //刷新
                mDownAdap.notifyItemChanged(mDownedList.indexOf(entity), entity);
            });
        }*/
    }

    @Override
    public void onItemDelete(int position, DownloadEntity entity) {
        //mDownModel.stopDownload(entity.getDownloadType());
        DownloadMgr.getInstance().stop(entity);
        DownloadMgr.getInstance().removeData(entity);
        String savePath = entity.getSavePath();
        mHelper.delete(entity);
        if (new File(savePath).exists()){
            new File(savePath).delete();
        }
       updateDownList();
        /*if (getActivity() != null) {
            getActivity().runOnUiThread(()->{
                mDownedList.remove(entity);
                mDownAdap.setDataList(mDownedList);  //刷新
            });
        }*/

    }
}

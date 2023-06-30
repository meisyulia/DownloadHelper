package com.example.downloadhelper.uis.record;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.downloadhelper.R;
import com.example.downloadhelper.adapter.DateAdapter;
import com.example.downloadhelper.adapter.FinishAdapter;
import com.example.downloadhelper.database.entity.DownloadEntity;
import com.example.downloadhelper.database.helper.DownloadHelper;
import com.example.downloadhelper.databinding.FragmentRecordBinding;
import com.example.downloadhelper.listener.OnFinishClickListener;
import com.example.downloadhelper.listener.OnItemClickListener;
import com.example.downloadhelper.uis.BaseFragment;
import com.example.downloadhelper.uis.PlayerActivity;
import com.example.downloadhelper.uis.download.DownInfoDlg;
import com.example.downloadhelper.util.common.ClickUtil;
import com.example.downloadhelper.util.common.GsonUtil;
import com.example.downloadhelper.widget.SpacesItemDecoration;

import java.util.ArrayList;
import java.util.List;

public class RecordFragment extends BaseFragment {

    private Context mContext;
    private com.example.downloadhelper.databinding.FragmentRecordBinding mBinding;
    private DownloadHelper mHelper;
    private List<String> mEndDateList;
    private String mFileName;
    private DownInfoDlg downInfoDlg;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContext = getContext();
        mBinding = FragmentRecordBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    protected void initData() {
        mHelper = DownloadHelper.newInstance();
    }

    @Override
    protected void initView() {
        mBinding.ivSearch.setOnClickListener(v->{
            showDiffAdap();
        });
        showDiffAdap();
    }

    private void showDiffAdap() {
        mFileName = mBinding.etFileName.getText().toString();
        if (TextUtils.isEmpty(mFileName)){
            showDateAdap();
        }else{
            searchFile(mFileName);
        }
    }

    private void showDateAdap() {
        List<String> mEndDateList = mHelper.queryEndDateAll();
        LinearLayoutManager dateLLM = new LinearLayoutManager(mContext, RecyclerView.VERTICAL, false);
        mBinding.rvDataList.setLayoutManager(dateLLM);
        DateAdapter dateAdapter = new DateAdapter(mContext, mEndDateList);
        mBinding.rvDataList.setAdapter(dateAdapter);
        dateAdapter.setOnItemClickListener(position -> {
            String endDate = mEndDateList.get(position);
            //更新新的列表
            showFileAdap(endDate);
        });
    }

    private void showFileAdap(String endDate) {
        List<DownloadEntity> entities = mHelper.queryByEndDate(endDate);
        LinearLayoutManager fileLLM = new LinearLayoutManager(mContext, RecyclerView.VERTICAL, false);
        mBinding.rvDataList.setLayoutManager(fileLLM);
        FinishAdapter finishAdapter = new FinishAdapter(mContext, (ArrayList<DownloadEntity>) entities);
        mBinding.rvDataList.setAdapter(finishAdapter);
        mBinding.rvDataList.addItemDecoration(new SpacesItemDecoration(1, R.color.main_color));
        finishAdapter.setOnFinishClickListener(new OnFinishClickListener() {
            @Override
            public void onOpenClick(DownloadEntity entity, int position) {
                Intent intent = new Intent(mContext, PlayerActivity.class);
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
        });
    }

    private void searchFile(String fileName) {
        List<DownloadEntity> entities = mHelper.queryFileName(fileName);
        LinearLayoutManager fileLLM = new LinearLayoutManager(mContext, RecyclerView.VERTICAL, false);
        mBinding.rvDataList.setLayoutManager(fileLLM);
        FinishAdapter finishAdapter = new FinishAdapter(mContext, (ArrayList<DownloadEntity>) entities);
        mBinding.rvDataList.setAdapter(finishAdapter);
        mBinding.rvDataList.addItemDecoration(new SpacesItemDecoration(1, R.color.main_color));
        finishAdapter.setOnFinishClickListener(new OnFinishClickListener() {
            @Override
            public void onOpenClick(DownloadEntity entity, int position) {
                Intent intent = new Intent(mContext, PlayerActivity.class);
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
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }
}

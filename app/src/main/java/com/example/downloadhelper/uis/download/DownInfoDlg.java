package com.example.downloadhelper.uis.download;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.downloadhelper.database.entity.DownloadEntity;
import com.example.downloadhelper.databinding.DialogDownInfoBinding;
import com.example.downloadhelper.util.common.GsonUtil;
import com.example.downloadhelper.util.common.Utils;

public class DownInfoDlg extends DialogFragment {

    private static DownInfoDlg downInfoDlg;
    private Context mContext;
    private com.example.downloadhelper.databinding.DialogDownInfoBinding mBinding;
    private String mDownInfo;
    private DownloadEntity mInfo;

    public static DownInfoDlg getInstance(String downInfo){
        if (downInfoDlg == null) {
            synchronized (DownInfoDlg.class){
                downInfoDlg = new DownInfoDlg();
                Bundle bundle = new Bundle();
                bundle.putString("down_info",downInfo);
                downInfoDlg.setArguments(bundle);
            }
        }
       return downInfoDlg;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContext = getContext();
        if (getArguments()!=null){
            mDownInfo = getArguments().getString("down_info");
        }
        mBinding = DialogDownInfoBinding.inflate(inflater, container, false);
        initData();
        initView();
        return mBinding.getRoot();
    }

    private void initData() {
        mInfo = GsonUtil.parserJsonToArrayBean(mDownInfo, DownloadEntity.class);
    }

    private void initView() {
        mBinding.tvFileName.setText(mInfo.getFileName());
        mBinding.tvSavePath.setText(mInfo.getSavePath());
        mBinding.tvDownTime.setText(mInfo.getEndTime());
        mBinding.tvTotalSize.setText(Formatter.formatFileSize(mContext,mInfo.getTotalBytes()));
        mBinding.tvUrl.setText(mInfo.getUrl());
        mBinding.tvCancel.setOnClickListener(v->{
            dismiss();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        initWindow();
    }

    private void initWindow() {
        Window window = getDialog().getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setLayout((int)(Utils.getScreenWidth(mContext)*0.95),ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }
}

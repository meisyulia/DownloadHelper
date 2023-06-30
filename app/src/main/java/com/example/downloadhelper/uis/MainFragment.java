package com.example.downloadhelper.uis;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.downloadhelper.R;
import com.example.downloadhelper.adapter.FunAdapter;
import com.example.downloadhelper.adapter.TodayDownAdapter;
import com.example.downloadhelper.bean.FunInfo;
import com.example.downloadhelper.constant.FunType;
import com.example.downloadhelper.database.entity.DownloadEntity;
import com.example.downloadhelper.database.helper.DownloadHelper;
import com.example.downloadhelper.databinding.FragmentMainBinding;
import com.example.downloadhelper.util.common.DateUtil;
import com.example.downloadhelper.util.common.VersionUtil;

import java.util.ArrayList;
import java.util.List;

public class MainFragment extends BaseFragment{

    private Context mContext;
    private com.example.downloadhelper.databinding.FragmentMainBinding mBinding;
    private FunAdapter mFunAdapt;
    private ArrayList<FunInfo> mFunList;
    private MainActivity mMainActivity;
    private String mNowDate;
    private DownloadHelper mHelper;
    private List<DownloadEntity> mNowList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContext = getContext();
        mMainActivity = ((MainActivity) getActivity());
        mBinding = FragmentMainBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    protected void initData() {
        mFunList = new ArrayList<>();
        mFunList.add(new FunInfo("下载文件", R.drawable.download_48, FunType.DOWNLOAD));
        mFunList.add(new FunInfo("记录查找",R.drawable.query_48,FunType.RECORD));
        mFunAdapt = new FunAdapter(mContext, mFunList);
        String nowTime = DateUtil.getNowDateTimeFormat();
        String[] split = nowTime.split(" ");
        mNowDate = split[0];
        mHelper = DownloadHelper.newInstance();
        mNowList = mHelper.queryByStartDate(mNowDate);
    }

    @Override
    protected void initView() {
        GridLayoutManager funGLM = new GridLayoutManager(mContext, 2);
        mBinding.rvMainFun.setLayoutManager(funGLM);
        mBinding.rvMainFun.setAdapter(mFunAdapt);
        mFunAdapt.setOnItemClickListener(position -> {
            FunInfo funInfo = mFunList.get(position);
            if (funInfo.getType() == FunType.DOWNLOAD){
                mMainActivity.switchPage(FunType.DOWNLOAD);
            }else if (funInfo.getType() == FunType.RECORD){
                mMainActivity.switchPage(FunType.RECORD);
            }
        });
        mBinding.tvVersion.setText("V"+ VersionUtil.getVersionName(mContext));
        LinearLayoutManager NowLLM = new LinearLayoutManager(mContext, RecyclerView.VERTICAL, false);
        mBinding.rvMainRecord.setLayoutManager(NowLLM);
        TodayDownAdapter todayAdap = new TodayDownAdapter(mContext, mNowList);
        mBinding.rvMainRecord.setAdapter(todayAdap);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }


}

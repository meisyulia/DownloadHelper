package com.example.downloadhelper.adapter;

import static com.example.downloadhelper.constant.DownloadConst.CANCEL;
import static com.example.downloadhelper.constant.DownloadConst.DESTROY;
import static com.example.downloadhelper.constant.DownloadConst.ERROR;
import static com.example.downloadhelper.constant.DownloadConst.FINISH;
import static com.example.downloadhelper.constant.DownloadConst.NONE;
import static com.example.downloadhelper.constant.DownloadConst.PAUSE;
import static com.example.downloadhelper.constant.DownloadConst.PROGRESS;
import static com.example.downloadhelper.constant.DownloadConst.START;
import static com.example.downloadhelper.constant.DownloadConst.WAIT;

import android.content.Context;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.downloadhelper.R;
import com.example.downloadhelper.constant.DownloadConst;
import com.example.downloadhelper.database.entity.DownloadEntity;
import com.example.downloadhelper.databinding.ItemTodayDownBinding;

import java.util.List;

public class TodayDownAdapter extends RecyclerView.Adapter {

    private final Context context;
    private final List<DownloadEntity> downList;

    public TodayDownAdapter(Context context, List<DownloadEntity> downList){
        this.context = context;
        this.downList = downList;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTodayDownBinding binding = ItemTodayDownBinding.inflate(LayoutInflater.from(context));
        return new ItemHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ItemHolder itemHolder = (ItemHolder) holder;
        DownloadEntity entity = downList.get(position);
        if (entity!=null){
            itemHolder.mBinding.tvFileName.setText(entity.getFileName());
            itemHolder.mBinding.tvTotalSize.setText("大小："+ Formatter.formatFileSize(context,entity.getTotalBytes()));
            itemHolder.mBinding.tvUrl.setText("来源："+entity.getUrl());
            setStatus(itemHolder,entity.getStatus());
        }
    }

    private void setStatus(ItemHolder itemHolder,int status) {
        switch (status){
            /*case START:
                showText(itemHolder,"准备中", R.color.black);
                break;
            case PAUSE:
                showText(itemHolder,"已暂停",R.color.black);
                break;
            case PROGRESS:
                showText(itemHolder,"下载中",R.color.main_color_deep);
                break;
            case FINISH:
                showText(itemHolder,"已完成",R.color.main_color);
                break;
            case ERROR:
                showText(itemHolder,"下载失败",R.color.red);
                break;
            case WAIT:
                showText(itemHolder,"等待中",R.color.grey);
                break;
            case CANCEL:
                showText(itemHolder,"取消下载",R.color.grey);
                break;
            case NONE:
                showText(itemHolder,"未下载",R.color.grey);
                break;
            case DESTROY:
                showText(itemHolder,"已释放资源",R.color.grey);
                break;*/
            case FINISH:
                showText(itemHolder,"下载成功",R.color.main_color_deep);
                break;
            case ERROR:
                showText(itemHolder,"下载出错",R.color.red);
                break;
            default:
                showText(itemHolder,"还需下载",R.color.green);
                break;



        }
    }

    private void showText(ItemHolder itemHolder,String desc, int color) {
        itemHolder.mBinding.tvStatus.setText(desc);
        itemHolder.mBinding.tvStatus.setTextColor(context.getResources().getColor(color));
    }

    @Override
    public int getItemCount() {
        if (downList!=null && downList.size()>0){
            return downList.size();
        }else{
            return 0;
        }
    }

    private class ItemHolder extends RecyclerView.ViewHolder {

        private final com.example.downloadhelper.databinding.ItemTodayDownBinding mBinding;

        public ItemHolder(ItemTodayDownBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }
    }
}

package com.example.downloadhelper.adapter;

import android.content.Context;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.downloadhelper.database.entity.DownloadEntity;
import com.example.downloadhelper.databinding.ItemFinishListBinding;
import com.example.downloadhelper.listener.OnFinishClickListener;

import java.util.ArrayList;

public class FinishAdapter extends RecyclerView.Adapter {

    private final Context context;
    private ArrayList<DownloadEntity> listArray;
    private OnFinishClickListener onFinishClickListener;

    public FinishAdapter(Context context, ArrayList<DownloadEntity> listArray){
        this.context = context;
        this.listArray = listArray;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFinishListBinding binding = ItemFinishListBinding.inflate(LayoutInflater.from(context), parent, false);
        return new ItemHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ItemHolder itemHolder = (ItemHolder) holder;
        DownloadEntity entity = listArray.get(position);
        if (entity!=null){
            itemHolder.mBinding.tvFileName.setText(entity.getFileName());
            itemHolder.mBinding.tvDownTime.setText(entity.getEndTime());
            itemHolder.mBinding.tvTotalSize.setText(Formatter.formatFileSize(context,entity.getTotalBytes()));
            itemHolder.mBinding.rlItem.setOnClickListener(v->{
                //弹出该文件相关信息
                if (onFinishClickListener!=null){
                    onFinishClickListener.onItemClick(entity,position);
                }
            });
            itemHolder.mBinding.tvOpen.setOnClickListener(v->{
                //跳转到打开页面
                if (onFinishClickListener!=null){
                    onFinishClickListener.onOpenClick(entity,position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        if (listArray!=null && listArray.size()>0){
            return listArray.size();
        }else{
            return 0;
        }
    }

    private class ItemHolder extends RecyclerView.ViewHolder {

        private final com.example.downloadhelper.databinding.ItemFinishListBinding mBinding;

        public ItemHolder(ItemFinishListBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }
    }

    public void setDataList(ArrayList<DownloadEntity> list){
        this.listArray = list;
        notifyDataSetChanged();
    }

    public void setOnFinishClickListener(OnFinishClickListener onFinishClickListener){
        this.onFinishClickListener = onFinishClickListener;
    }
}

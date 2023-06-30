package com.example.downloadhelper.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.downloadhelper.bean.FunInfo;
import com.example.downloadhelper.databinding.ItemFunBinding;
import com.example.downloadhelper.listener.OnItemClickListener;

import java.util.ArrayList;

public class FunAdapter extends RecyclerView.Adapter {

    private final Context context;
    private final ArrayList<FunInfo> infoArray;
    private OnItemClickListener onItemClickListener;

    public FunAdapter(Context context, ArrayList<FunInfo> infoArray){
        this.context = context;
        this.infoArray = infoArray;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFunBinding binding = ItemFunBinding.inflate(LayoutInflater.from(context), parent, false);
        return new ItemHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        FunInfo funInfo = infoArray.get(position);
        ItemHolder itemHolder = (ItemHolder) holder;
        if (funInfo!=null){
            itemHolder.mBinding.fvFun.initItem(funInfo.getTitle(),funInfo.getResId());
            itemHolder.mBinding.fvFun.setOnClickListener(v->{
                if (onItemClickListener!=null){
                    onItemClickListener.OnItemClick(position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        if (infoArray != null &&infoArray.size()>0){
            return infoArray.size();
        }else{
            return 0;
        }
    }

    private class ItemHolder extends RecyclerView.ViewHolder {

        private final com.example.downloadhelper.databinding.ItemFunBinding mBinding;

        public ItemHolder(ItemFunBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }
}

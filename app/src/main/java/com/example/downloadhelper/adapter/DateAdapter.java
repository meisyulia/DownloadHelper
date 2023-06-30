package com.example.downloadhelper.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.downloadhelper.databinding.ItemDownDateBinding;
import com.example.downloadhelper.listener.OnItemClickListener;

import java.util.List;
import java.util.Set;

public class DateAdapter extends RecyclerView.Adapter {

    private final Context context;
    private final List<String> dateString;
    private OnItemClickListener onItemClickListener;

    public DateAdapter(Context context, List<String> dateString){
        this.context = context;
        this.dateString = dateString;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDownDateBinding binding = ItemDownDateBinding.inflate(LayoutInflater.from(context));
        return new ItemHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ItemHolder itemHolder = (ItemHolder) holder;
        String endDate = dateString.get(position);
        if (!TextUtils.isEmpty(endDate)){
           itemHolder.mBinding.tvDate.setText(endDate);
           itemHolder.mBinding.llItem.setOnClickListener(v->{
               if (onItemClickListener!=null){
                   onItemClickListener.OnItemClick(position);
               }
           });
        }
    }

    @Override
    public int getItemCount() {
        if (dateString!=null && dateString.size()>0){
            return dateString.size();
        }else{
            return 0;
        }

    }


    private class ItemHolder extends RecyclerView.ViewHolder {

        private final com.example.downloadhelper.databinding.ItemDownDateBinding mBinding;

        public ItemHolder(ItemDownDateBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }

}

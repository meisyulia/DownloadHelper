package com.example.downloadhelper.uis;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public abstract class BaseFragment extends Fragment {

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
        initView();
    }

    protected abstract void initData();

    protected abstract void initView();



    public void showTips(String tips){
        Toast.makeText(getContext(), tips, Toast.LENGTH_SHORT).show();
    }
}

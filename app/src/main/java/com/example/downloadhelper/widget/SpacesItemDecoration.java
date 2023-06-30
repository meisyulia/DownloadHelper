package com.example.downloadhelper.widget;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SpacesItemDecoration extends RecyclerView.ItemDecoration {
    private int color;
    private int space; // 空白间隔
    private Paint dividerPaint;

    public SpacesItemDecoration(int space, int color) {
        this.space = space;
        this.color = color;
    }

    public SpacesItemDecoration(int space){
        this.space = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        /*outRect.left = space; // 左边空白间隔
        outRect.right = space; // 右边空白间隔
        outRect.top = space; // 上方空白间隔*/
        outRect.bottom = space; // 下方空白间隔
    }

    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.onDraw(c, parent, state);
        dividerPaint = new Paint();
        dividerPaint.setColor(color);
        //画item的布局
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount - 1; i++) {
            View view = parent.getChildAt(i);
            float startx = parent.getPaddingLeft();
            float starty = view.getBottom();
            float stopx = startx + view.getWidth();
            float stopy = view.getBottom();
            c.drawLine(startx,starty,stopx,stopy, dividerPaint);
        }
    }
}

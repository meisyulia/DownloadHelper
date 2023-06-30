package com.example.downloadhelper.util.common;

import android.view.View;

import java.util.HashMap;
import java.util.Map;

public class ClickUtil {

    private static final Map<Integer, Long> sLastClickTimes = new HashMap<>();
    private static final long CLICK_INTERVAL = 800; // 限制点击间隔为 800 毫秒
    private static long lastClickTime = 0;

    public static boolean isFastClick(View view) {
        long now = System.currentTimeMillis();
        int viewId = view.getId();
        if (sLastClickTimes.containsKey(viewId)) {
            long lastClickTime = sLastClickTimes.get(viewId);
            if (now - lastClickTime < CLICK_INTERVAL) {
                return true;
            }
        }
        sLastClickTimes.put(viewId, now);
        return false;
    }

    /**
     * 判断是否为快速点击
     * @param interval：连续点击的间隔时间
     * @return true: 是快速点击，false: 不是快速点击
     */
    public static boolean isFastClick(int interval) {
        long interval_time = interval;
        if (interval==0){
            interval_time = CLICK_INTERVAL;
        }
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClickTime < interval_time) {
            return true;
        }
        lastClickTime = currentTime;
        return false;
    }

}


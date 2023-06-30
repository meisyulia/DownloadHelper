package com.example.downloadhelper.util.common;

import android.text.TextUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {
    private static final String TAG = "DateUtil";

    public static String getNowDateTime(String formatStr) {
        String format = formatStr;
        if (TextUtils.isEmpty(format)) {
            format = "yyyyMMddHHmmss";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date());
    }
    public static String getNowDateTimeFull() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        return sdf.format(new Date());
    }

    public static String getNowDateTimeFormat() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date());
    }

    public static String getNowTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(new Date());
    }

    public static String getNowYearCN() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年");
        return sdf.format(new Date());
    }

    public static int getNowYear() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.YEAR);
    }

    public static int getNowMonth() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.MONTH) + 1;
    }

    public static int getNowDay() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.DAY_OF_MONTH);
    }

    //某天转化成String类型日期时间
    public static String getStringTime(Date d_date,String format){
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(d_date);
    }

    public static String getAddDate(String str, long day_num) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Date old_date;
        try {
            old_date = sdf.parse(str);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        long time = old_date.getTime();
        long diff_time = day_num * 24 * 60 * 60 * 1000;
//		LogUtil.debug(TAG, "day_num="+day_num+", diff_time="+diff_time);
        time += diff_time;
        Date new_date = new Date(time);
        return sdf.format(new_date);
    }

    //转化成长整型
    public static long getTime(String str) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = sdf.parse(str);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    //计算某天是星期几
    public static int getWeekIndex(String s_date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        Date d_date;
        try {
            d_date = format.parse(s_date);
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(d_date);
        int week_index = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (week_index == 0) {
            week_index = 7;
        }
        return week_index;
    }

    //获取某天所在的月份
    public static int getSelectedMonth(String s_date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        Date d_date;
        try {
            d_date = format.parse(s_date);
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(d_date);
        int month = cal.get(Calendar.MONTH) + 1;
        return month;
    }

    //获取所在时间的周一的日期和周日的日期
    public static String[] getMonAndSunDate(String s_date){
        String[] getString = new String[2];
        //先计算该时间是星期几
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date d_date;
        try {
            d_date = format.parse(s_date);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(d_date);
        int week_index = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (week_index == 0) {
            week_index = 7;
        }
        //获取所在周的周一日期
        cal.add(Calendar.DAY_OF_MONTH,-week_index+1);
        Date date1 = cal.getTime();
        getString[0] = format.format(date1);
        //Log.d(TAG, "getMondayDate: getString[0]="+getString[0]);
        //获取所在周的周日日期
        cal.add(Calendar.DAY_OF_MONTH,6);
        Date date2 = cal.getTime();
        //Log.d(TAG, "getMondayDate: getString[1]="+getString[1]);
        getString[1] = format.format(date2);
        return getString;
    }

    //判断某个日期是否是某一周的星期一到星期日
    public static boolean isDateInWeek(String s_date,String s_date_target){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d_date;
        Date d_date_target;
        try {
            d_date = format.parse(s_date);
            d_date_target = format.parse(s_date_target);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        Calendar cal = Calendar.getInstance();
        //西方周日为第一天，中国周一为第一天，需要设置星期一为第一天
        cal.setFirstDayOfWeek(2);
        cal.setTime(d_date_target);
        int targetWeek = cal.get(Calendar.WEEK_OF_YEAR);
        cal.setTime(d_date);
        //获取待判断日期所在的周数。
        int week = cal.get(Calendar.WEEK_OF_YEAR);
        //判断待判断日期所在的周数是否等于目标周数，如果不等于则返回false。
        if (week != targetWeek){
            return false;
        }else{
            return true;
        }
        /*//判断待判断日期是否是星期一到星期日的其中一个，如果是则返回true，否则返回false。
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)-1;
        if (dayOfWeek == 0){
            dayOfWeek = 7;
        }
        return dayOfWeek >= 1 && dayOfWeek <= 7;*/

    }
}

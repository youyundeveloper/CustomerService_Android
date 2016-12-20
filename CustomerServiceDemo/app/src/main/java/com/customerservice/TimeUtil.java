package com.customerservice;

import java.util.Calendar;

/**
 * Created by Bill on 2016/12/16.
 *
 * 根据产品要求写的时间显示规则
 */

public class TimeUtil {

    public static String format2(long targetMillis) {
        // target Calendar
        Calendar targetCal = Calendar.getInstance();
        targetCal.setTimeInMillis(targetMillis);
        int targetYear = targetCal.get(Calendar.YEAR);
        int targetMonth = targetCal.get(Calendar.MONTH) + 1;
        int targetDay = targetCal.get(Calendar.DAY_OF_MONTH);
        int targetHour = targetCal.get(Calendar.HOUR_OF_DAY);
        int targetMinute = targetCal.get(Calendar.MINUTE);
        targetCal.set(Calendar.DAY_OF_MONTH,
                targetCal.getActualMaximum(Calendar.DAY_OF_MONTH));
        int lastDay = targetCal.get(Calendar.DAY_OF_MONTH);
        String targetHourStr = "" + targetHour;
        String targetMinuteStr = "" + targetMinute;
        if (targetHour < 10)
            targetHourStr = "0" + targetHour;
        if (targetMinute < 10)
            targetMinuteStr = "0" + targetMinute;

        // now Calendar
        Calendar nowCal = Calendar.getInstance();
        int nowYear = nowCal.get(Calendar.YEAR);
        int nowMonth = nowCal.get(Calendar.MONTH) + 1;
        int nowDay = nowCal.get(Calendar.DAY_OF_MONTH);

        if (nowYear - targetYear < 2) {
            // 同一年
            if (nowMonth - targetMonth < 2 || nowMonth - targetMonth == -11) {
                // 同月或差一月
                int diffDay = -1;
                if (nowMonth - targetMonth == -11) {
                    // 跨年
                    diffDay = (31 - targetDay) + nowDay;
                } else if (nowMonth - targetMonth == 0) {
                    // 同月
                    diffDay = nowDay - targetDay;
                } else {
                    // 跨月
                    diffDay = (lastDay - targetDay) + nowDay;
                }
                if (diffDay == 0) {
                    // 今天
                    return "今天 " + targetHourStr + ":" + targetMinuteStr;
                } else if (diffDay == 1) {
                    // 昨天
                    return "昨天 " + targetHourStr + ":" + targetMinuteStr;
                } else {
                    // 超过三天,显示具体日期
                    return formatTime(targetYear, targetMonth, targetDay,
                            targetHourStr, targetMinuteStr);
                }
            } else {
                // 差两个月以上,显示具体日期
                return formatTime(targetYear, targetMonth, targetDay,
                        targetHourStr, targetMinuteStr);
            }
        } else {
            // 相差一年以上,显示具体日期
            return formatTime(targetYear, targetMonth, targetDay, targetHourStr,
                    targetMinuteStr);
        }

    }

    public static String format(long targetMillis) {
        // target Calendar
        Calendar targetCal = Calendar.getInstance();
        targetCal.setTimeInMillis(targetMillis);
        int targetYear = targetCal.get(Calendar.YEAR);
        int targetMonth = targetCal.get(Calendar.MONTH) + 1;
        int targetDay = targetCal.get(Calendar.DAY_OF_MONTH);
        int targetWeek = targetCal.get(Calendar.DAY_OF_WEEK);
        targetCal.set(Calendar.DAY_OF_MONTH,
                targetCal.getActualMaximum(Calendar.DAY_OF_MONTH));
        int lastDay = targetCal.get(Calendar.DAY_OF_MONTH);
        System.out.println(targetYear + "/" + targetMonth + "/" + targetDay
                + "/" + lastDay);

        // now Calendar
        Calendar nowCal = Calendar.getInstance();
        int nowYear = nowCal.get(Calendar.YEAR);
        int nowMonth = nowCal.get(Calendar.MONTH) + 1;
        int nowDay = nowCal.get(Calendar.DAY_OF_MONTH);
        int nowWeek = nowCal.get(Calendar.DAY_OF_WEEK);
        long nowTimeInMillis = nowCal.getTimeInMillis();
        System.out.println(nowYear + "/" + nowMonth + "/" + nowDay);

        if (nowYear - targetYear < 2) {
            // 同一年
            if (nowMonth - targetMonth < 2 || nowMonth - targetMonth == -11) {
                // 同月或差一月
                int diffDay = -1;
                if (nowMonth - targetMonth == -11) {
                    // 跨年
                    diffDay = (31 - targetDay) + nowDay;
                } else if (nowMonth - targetMonth == 0) {
                    // 同月
                    diffDay = nowDay - targetDay;
                } else {
                    // 跨月
                    diffDay = (lastDay - targetDay) + nowDay;
                }
                if (diffDay == 0) {
                    // 同一天
                    long timeDiff = (nowTimeInMillis - targetMillis) / 1000;// 时间差,秒
                    if (timeDiff < 60) { // 60秒
                        if(timeDiff < 1)
                            return "刚刚";
                        else
                            return timeDiff + "秒前";
                    } else if (timeDiff < 60 * 60) { // 1小时
                        long minute = timeDiff / 60;
                        return minute + "分钟前";
                    } else if (timeDiff < 24 * 60 * 60) { // 12小时
                        long hour = timeDiff / 3600;
                        return hour + "小时前";
                    }
                } else if (diffDay >= 1 && diffDay <= 6) {
                    return getReturn(diffDay, targetWeek, nowWeek);
                } else if (diffDay < 14) {
                    // 上星期
                    if (getWeek(nowWeek) - getWeek(targetWeek) >= 0) {
                        // 上星期内
                        return "上星期";
                    } else {
                        // 上上星期,显示具体日期
                        return formatTime(targetYear, targetMonth, targetDay);
                    }
                } else {
                    // 超过两周,显示具体日期
                    return formatTime(targetYear, targetMonth, targetDay);
                }

            } else {
                // 差两个月以上,显示具体日期
                return formatTime(targetYear, targetMonth, targetDay);
            }
        } else {
            // 相差一年以上,显示具体日期
            return formatTime(targetYear, targetMonth, targetDay);
        }

        return "";
    }

    private static String getReturn(int day, int startWeek, int endWeek) {
        if (getWeek(endWeek) - getWeek(startWeek) > 0) {
            if (day == 1) {
                return "昨天";
            } else {
                return day + "天前";
            }
        } else {
            // 不同星期
            return "上星期";
        }
    }

    private static int getWeek(int week) {
        int targetWeek = week - 1;
        if (targetWeek == 0)
            targetWeek = 7;
        return targetWeek;
    }

    /**
     * 格式化时间 格式：xxxx年x月x日 xx:xx
     *
     * @param year
     * @param month
     * @param day
     * @param hour
     * @param minute
     * @return
     */
    private static String formatTime(int year, int month, int day, String hour, String minute) {
        return year + "年" + month + "月" + day + "日" + " " + hour + ":" + minute;
    }

    /**
     * 格式化时间 格式：xxxx/x/x
     *
     * @param year
     * @param month
     * @param day
     * @return
     */
    private static String formatTime(int year, int month, int day) {
        return year + "/" + month + "/" + day;
    }

}

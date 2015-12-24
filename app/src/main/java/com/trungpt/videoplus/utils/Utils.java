package com.trungpt.videoplus.utils;

/**
 * Created by trung on 12/11/2015.
 */
public class Utils
{
    public static String calculateTime(long before, long after)
    {
        long time = after - before;
        long hours = time / (1000 * 60 * 60);
        long day = hours / 24;
        long week = day / 7;
        long month = day / 30;
        long year = day / 365;
        if (hours < 24)
        {
            if (hours == 0)
            {
                return "one hours ago";
            }
            else
            {
                return hours + " hours ago";
            }
        }
        else if (day > 0 && week < 1)
        {
            return day + " days ago";
        }
        if (week >= 1 && month < 1)
        {
            return week + " weeks ago";
        }
//        else if (day > 7 && day < 30)
//        {
//            return week + " weeks ago";
//        }
//        else if (day==30)
//        {
//            return "a months ago";
//        }
        else if (month >= 1 && year < 1)
        {
            return month + " months ago";
        }
        else
        {
            return year + " years ago";
        }
    }
}

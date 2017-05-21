package com.gaiagps.iburn.view;

/**
 * From https://github.com/jd-alexander/LikeButton
 * Created by Joel on 23/12/2015.
 */
public class Utils {
    public static double mapValueFromRangeToRange(double value, double fromLow, double fromHigh, double toLow, double toHigh) {
        return toLow + ((value - fromLow) / (fromHigh - fromLow) * (toHigh - toLow));
    }

    public static double clamp(double value, double low, double high) {
        return Math.min(Math.max(value, low), high);
    }

    public static String formatMultilang(String text) {
        if (text == null) return "";
        String[] split = text.split(";;");
        return split[0] + (split.length > 1 ? " \n" + split[1] : "");
    }
}

package org.ocr.sdk.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CqMatcher {
    /**
     * 判断是否CQ Image
     *
     * @param str 字符串
     * @return true/false
     */
    public static boolean isCqImage(String str) {
        return isStr(str, "CQ:image,(.*?)url=(.*?)?term");
    }

    private static boolean isStr(String str, String regex) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(str);
        return m.find();
    }
}

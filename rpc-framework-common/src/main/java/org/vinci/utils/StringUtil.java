package org.vinci.utils;

/**
 * String 工具类
 */
public class StringUtil {
    /**
     * 判断字符串是否为空白
     * @param s
     * @return true 表示字符串为空白
     */
    public static boolean isBlank(String s) {
        if (s == null || s.length() == 0) {
            return true;
        }
        // 检测字符是否为空格字符
        for (int i = 0; i < s.length(); ++i) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}

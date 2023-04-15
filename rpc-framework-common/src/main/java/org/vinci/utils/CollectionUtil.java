package org.vinci.utils;

import java.util.Collection;

/**
 * 集合工具类
 */
public class CollectionUtil {
    // 判别空值
    public static boolean isEmpty(Collection<?> c){
        return c == null || c.isEmpty();
    }

}

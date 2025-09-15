package com.demo.tools;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * 获取签名
 */
public class SignUtils {
    public static String getSign(Map<String, Object> map) {
        if (map == null) {
            return "";
        }

        Map orderedMap = MapUtils.orderedMap(map);
        List<String> list = new ArrayList<>();
        for (Object key : orderedMap.keySet()) {
            if (key.equals("sign")) {
                continue;
            }
            String value = map.get(key) == null ? "" : map.get(key).toString();
            list.add(key + "=" + value);
        }
        //KEY升序
        Collections.sort(list);
        String str = StringUtils.join(list, "|");
        String sign = DigestUtils.md5Hex(str);
        return sign;
    }

    public static String getSign(String url){
        Map<String, Object> map = HttpServletUtils.getParams(url);
        return getSign(map);
    }

    public static String getSign(Object bean) {
        if (bean == null) {
            return "";
        }

        Map map = BeanToMapUtils.toMap(bean);
        return getSign(map);
    }

    public static String getSign(String[] fieldNames, Object... values) {
        Map map = new HashMap<>();
        for(int i = 0; i < fieldNames.length; i ++){
            map.put(fieldNames[i], values[i]);
        }
        return getSign(map);
    }

}

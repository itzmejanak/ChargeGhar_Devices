package com.demo.tools;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/3/23 0023.
 */
public class BeanToMapUtils {

    public static Map<String, Object> toMap(Object obj){
        if(obj == null || obj instanceof Map){
            return (Map<String, Object>) obj;
        }
        BeanInfo beanInfo = null;
        Map<String, Object> map = new HashMap<String, Object>();

        try {
            beanInfo = Introspector.getBeanInfo(obj.getClass());
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor property : propertyDescriptors) {
                String key = property.getName();
                if (key.compareToIgnoreCase("class") == 0) {
                    continue;
                }
                Method getter = property.getReadMethod();
                Object value = getter!=null ? getter.invoke(obj) : null;
                map.put(key, value);

            }

        } catch (IntrospectionException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }


        return map;
    }

    public static List<Map> toListMap(List<?> list){
        List<Map> result = new ArrayList<Map>();

        for(Object o : list){

            Map map = toMap(o);
            result.add(map);
        }
        return result;
    }
}

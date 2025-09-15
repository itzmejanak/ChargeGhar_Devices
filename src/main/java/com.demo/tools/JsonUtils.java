package com.demo.tools;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/3/29 0029.
 */
public class JsonUtils {
    public JsonUtils(){}
    private static ObjectMapper mapper;

    public static ObjectMapper getMapper() {
        if(mapper == null){
            mapper = new ObjectMapper();
            //忽略单引号
            mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
            //该特性决定parser是否允许JSON整数以多个0开始(比如，如果000001赋值给json某变量，
            mapper.configure(JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS, true);
            //反序列化时忽略不需要的字段
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        }
        return mapper;
    }

    public static String toJson(Object obj){
        String json = null;
        try {
            json = getMapper().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return json;
    }

    public static <T> T toObject(String json, Class<T> clazz) {
        if(StringUtils.isBlank(json)){
            return null;
        }
        T t = null;
        try {
            t = getMapper().readValue(json, clazz);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return t;
    }


    public static <T> List<T> toList(String json, Class<T> clazz) throws IOException {
        JavaType javaType = getCollectionType(ArrayList.class, clazz);
        List<T> list = getMapper().readValue(json, javaType);
        return list;
    }

    /**
     * 获取泛型的Collection Type
     * @param collectionClass 泛型的Collection
     * @param elementClasses 元素类
     * @return JavaType Java类型
     * @since 1.0
     */
    public static JavaType getCollectionType(Class<?> collectionClass, Class<?>... elementClasses) {
        return getMapper().getTypeFactory().constructParametricType(collectionClass, elementClasses);
    }





}

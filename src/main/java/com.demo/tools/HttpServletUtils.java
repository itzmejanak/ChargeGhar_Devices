package com.demo.tools;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

public class HttpServletUtils {


    public static String getModule(HttpServletRequest request){
        String path = request.getServletPath().replaceAll("//", "/");
        String[] arr = path.split("/");
        if(arr.length < 2){
            return "";
        }
        return arr[1];
    }

    /**
     * 获取HttpServletRequest
     * @return
     */
    public static HttpServletRequest getHttpServletRequest(){
        ServletRequestAttributes requestAttributes = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes());
        if(requestAttributes != null){
            return requestAttributes.getRequest();
        }
        return null;

    }

    /**
     * 获取HttpServletResponse
     * @return
     */
    public static HttpServletResponse getHttpServletResponse(){
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        return response;
    }

    /**
     * 获取当前真实URL地址
     * @return
     */
    public static String getRealUrl(HttpServletRequest request, boolean showQueryString){
        String url = request.getRequestURL().toString();
        url = url.replaceFirst(request.getScheme(), getRealScheme(request));

        String queryString = request.getQueryString();

        if(showQueryString && !StringUtils.isBlank(queryString)){
            url += ("?" + request.getQueryString());
        }

        return url;
    }
    public static String getRealUrl(boolean showQueryString){
        return getRealUrl(getHttpServletRequest(), showQueryString);
    }

    /**
     * 获取真实HTTPS
     * @param request
     * @return
     */
    public static String getRealScheme(HttpServletRequest request){
        String scheme = request.getScheme();

        /**
         * 记录一个请求一个请求最初从浏览器发出时候，是使用什么协议。
         * 因为有可能当一个请求最初和反向代理通信时，是使用https，但反向代理和服务器通信时改变成http协议，
         * 这个时候，X-Forwarded-Proto的值应该是https
         */
        String proto = request.getHeader("x-forwarded-proto");
        if(StringUtils.isBlank(proto)){
            proto = request.getHeader("X-Forwarded-Proto");
        }
        if("https".equals(proto)){
            scheme = "https";
        }
        //花生壳调试
        if(request.getHeader("host").indexOf("https") == 0){
            scheme = "https";
        }
        return scheme;
    }
    public static String getRealScheme(){
        return getRealScheme(getHttpServletRequest());
    }

    /**
     * 获取真实Contextpath
     * @param request
     * @return
     */
    public static String getRealContextpath(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append(getRealScheme(request));
        sb.append("://" + request.getServerName());
//        if(request.getLocalPort() != 80 && request.getRequestURL().indexOf(request.getLocalPort() + "") > 0){
//            sb.append(":" + request.getLocalPort());
//        }
        if(request.getServerPort() != 80 && request.getRequestURL().indexOf(request.getServerPort() + "") > 0){
            sb.append(":" + request.getServerPort());
        }

        if(!StringUtils.isBlank(request.getContextPath())){
            sb.append(request.getContextPath());
        }

        return sb.toString();
    }
    public static String getRealContextpath(){
        return getRealContextpath(getHttpServletRequest());
    }

    /**
     * 是否是页面访问
     * @param request
     * @return
     */
    public static boolean isPage(HttpServletRequest request){
        String[] suffixs = new String[]{".html"};
        for(String suffix : suffixs){
            if(request.getServletPath().endsWith(suffix)){
                return true;
            }
        }
        return false;
    }
    public static boolean isPage(){
        return isPage(getHttpServletRequest());
    }

    /**
     * 解析url
     *
     * @param url
     * @return
     */
    public static Map<String, Object> getParams(String url) {
        Map map = new HashMap();
        if(StringUtils.isBlank(url) || url.indexOf("?") < 0){
            return map;
        }

        String[] urlParts = url.split("\\?");

        //有参数
        String[] params = urlParts[1].split("&");
        for (String param : params) {
            String[] keyValue = param.split("=");
            map.put(keyValue[0], (keyValue.length > 1 ? keyValue[1] : ""));
        }

        return map;
    }


}

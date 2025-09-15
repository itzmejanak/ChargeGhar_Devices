package com.demo.common;




/**
 * Created by Administrator on 2017/3/23 0023.
 */
public class HttpResult {

    public HttpResult() {
    }

    /**
     * 200：接口正常请求并返回
     * 4XX：客户端非法请求
     * 5XX：服务器运行错误
     */
    private int code = 200;

    /**
     * 返回自定义状态
     */
    private int type;

    /**
     *返回自定义数据
     */
    private Object data;

    /**
     *自定义消息
     */
    private String msg = "ok";

    private long time = System.currentTimeMillis();

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public long getTime() {
        return time;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}

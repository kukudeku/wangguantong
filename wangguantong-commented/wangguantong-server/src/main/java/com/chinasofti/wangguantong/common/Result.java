package com.chinasofti.wangguantong.common;

/**
 * 所有后端接口共用的返回格式。
 *
 * <p>前端收到的数据结构固定为：</p>
 * <pre>{ "code": 200, "message": "成功", "data": ... }</pre>
 *
 * <p>这样前端请求拦截器只需判断一次 code，不需要每个页面重复处理成功和失败。</p>
 *
 * @param <T> data 字段中实际保存的数据类型，例如 Member、List&lt;Computer&gt;。
 */
public class Result<T> {

    /** 业务状态码。本项目约定 200 表示成功，500 表示业务失败。 */
    private Integer code;

    /** 给用户看的结果说明，例如“成功”“会员不存在”。 */
    private String message;

    /** 接口真正返回的数据；没有返回数据时为 null。 */
    private T data;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    /**
     * 创建一个带数据的成功结果。
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("成功");
        result.setData(data);
        return result;
    }

    /**
     * 创建一个不需要返回 data 的成功结果，常用于新增、修改、删除接口。
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 创建业务失败结果。这里不抛出复杂异常，而是把可读提示交给前端显示。
     */
    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMessage(message);
        result.setData(null);
        return result;
    }
}

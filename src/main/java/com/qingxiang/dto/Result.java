package com.qingxiang.dto;

import lombok.Getter;

/**
 * 统一返回：{@code code / message / data}，code 与 HTTP 状态码一致。
 */
@Getter
public class Result<T> {

    private int code;
    private String message;
    private T data;

    private Result() {
    }

    public static <T> Result<T> ok(T data) {
        Result<T> r = new Result<>();
        r.code = 200;
        r.message = "success";
        r.data = data;
        return r;
    }

    public static <T> Result<T> ok() {
        return ok(null);
    }

    public static <T> Result<T> fail(int code, String message) {
        Result<T> r = new Result<>();
        r.code = code;
        r.message = message;
        return r;
    }
}

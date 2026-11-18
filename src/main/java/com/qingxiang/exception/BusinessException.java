package com.qingxiang.exception;

/**
 * 业务异常：带 HTTP 状态码，文案面向用户，不含堆栈与内部路径。
 */
public class BusinessException extends RuntimeException {

    private final int status;

    public BusinessException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}

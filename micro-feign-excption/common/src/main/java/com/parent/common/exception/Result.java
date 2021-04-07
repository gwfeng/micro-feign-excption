package com.parent.common.exception;

import lombok.Data;

/**
 * Result
 */
@Data
public class Result<T> {

    public Result() {
    }

    public Result(ResultStatus resultStatus) {
        this(resultStatus, null);
    }

    public Result(ResultStatus resultStatus, T data) {
        this(resultStatus.getCode(), resultStatus.getMessage(), data);
    }

    public Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    private int code;
    private String message;
    private T data;
}

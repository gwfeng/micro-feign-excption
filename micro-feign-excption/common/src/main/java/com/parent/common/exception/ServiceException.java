package com.parent.common.exception;

import org.springframework.http.HttpStatus;

import static com.parent.common.exception.ResultStatus.ERROR_SERVICE;

/**
 * ServiceException
 *
 * @author Chensong
 * @date 2018/9/29
 */
public class ServiceException extends RuntimeException {

    private int code;
    private String message;
    private Object error;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getError() {
        return error;
    }

    public void setError(Object error) {
        this.error = error;
    }

    public ServiceException() {
        this(ERROR_SERVICE.getMessage());
    }

    public ServiceException(String message) {
        this(message, null);
    }

    public ServiceException(String message, Object error) {
        super(message);
        this.code = HttpStatus.INTERNAL_SERVER_ERROR.value();
        this.error = error;
    }

    public ServiceException(ResultStatus resultStatus) {
        this(resultStatus, null);
    }

    public ServiceException(ResultStatus resultStatus, Object error) {
        super(resultStatus.getMessage());
        this.code = resultStatus.getCode();
        this.error = error;
    }

}

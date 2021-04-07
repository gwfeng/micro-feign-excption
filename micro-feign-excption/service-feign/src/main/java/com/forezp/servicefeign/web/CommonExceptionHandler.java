package com.forezp.servicefeign.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.netflix.hystrix.exception.HystrixRuntimeException;
import com.parent.common.exception.ErrorValidationNew;
import com.parent.common.exception.Result;
import com.parent.common.exception.ResultStatus;
import com.parent.common.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * 异常处理类
 */
@ControllerAdvice(annotations = {RestController.class, Controller.class})
public class CommonExceptionHandler extends ResponseEntityExceptionHandler {

    private Logger logger = LoggerFactory.getLogger(CommonExceptionHandler.class);

    @Autowired
    private ObjectMapper objectMapper;
    //外部异常 http 状态500
    @ExceptionHandler(value = {Exception.class})
    public final ResponseEntity<Result> handleGeneralException(Exception ex, HttpServletRequest request) {
        logError(ex, request);
        // 发送邮件
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(MediaType.APPLICATION_JSON_UTF8_VALUE));

        Result result = new Result(ResultStatus.FAILURE,ex.getMessage());

        return new ResponseEntity<>(result, headers, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    //内部业务异常，http 状态200
    @ExceptionHandler(value = {ServiceException.class})
    public final ResponseEntity<Result> handleServiceException(ServiceException ex, HttpServletRequest request) {
        // 注入servletRequest，用于出错时打印请求URL与来源地址
        logError(ex, request);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(MediaType.APPLICATION_JSON_UTF8_VALUE));

        Result result = new Result(ex.getCode(), ex.getMessage(), null);

        return new ResponseEntity<>(result, headers, HttpStatus.OK);
    }

    @ExceptionHandler(value = {TimeoutException.class})
    public final ResponseEntity<Result> handleServiceException(TimeoutException ex, HttpServletRequest request) {
        // 注入servletRequest，用于出错时打印请求URL与来源地址
        logError(ex, request);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(MediaType.APPLICATION_JSON_UTF8_VALUE));

        Result result = new Result(-1, ex.getMessage(), "调用系统接口超时");

        return new ResponseEntity<>(result, headers, HttpStatus.OK);
    }

    @ExceptionHandler(value = {HystrixRuntimeException.class})
    public final ResponseEntity<Result> handleServiceException(HystrixRuntimeException ex, HttpServletRequest request) {
        // 注入servletRequest，用于出错时打印请求URL与来源地址
        logError(ex, request);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(MediaType.APPLICATION_JSON_UTF8_VALUE));

        Result result = new Result(-1, ex.getMessage(), "调用PRODB系统接口没有返回值，请启动PRODB");

        return new ResponseEntity<>(result, headers, HttpStatus.OK);
    }

    @ExceptionHandler(value = {MethodArgumentTypeMismatchException.class})
    public final ResponseEntity<Result> handleNumberFormatException(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        return new ResponseEntity<>(ErrorValidationNew.getInstance(ex), headers, HttpStatus.OK);
    }

    /**
     * pathVariable 和 requestParam 参数校验
     *
     * @param ex
     * @return
     */
    @ExceptionHandler(value = {ConstraintViolationException.class})
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public Result handleConstraintViolation(ConstraintViolationException ex) {
        return ErrorValidationNew.getInstance(ex);
    }


    @Override
    protected ResponseEntity<Object> handleServletRequestBindingException(ServletRequestBindingException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        return new ResponseEntity<>(ErrorValidationNew.getInstance(ex), headers, HttpStatus.OK);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        return new ResponseEntity<>(ErrorValidationNew.getInstance(ex), headers, HttpStatus.OK);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        return new ResponseEntity<>(ErrorValidationNew.getInstance(ex), headers, HttpStatus.OK);
    }

    @Override
    protected ResponseEntity<Object> handleBindException(BindException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return new ResponseEntity<>(ErrorValidationNew.getInstance(ex), headers, HttpStatus.OK);
    }

    /**
     * requestBody 参数校验
     *
     * @param ex
     * @param headers
     * @param status
     * @param request
     * @return
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        return new ResponseEntity<>(ErrorValidationNew.getInstance(ex), headers, HttpStatus.OK);
    }

    /**
     * 重载ResponseEntityExceptionHandler的方法，加入日志
     */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
                                                             HttpStatus status, WebRequest request) {

        logError(ex);

        if (HttpStatus.INTERNAL_SERVER_ERROR.equals(status)) {
            request.setAttribute("javax.servlet.error.exception", ex, WebRequest.SCOPE_REQUEST);
        }

        return new ResponseEntity<Object>(body, headers, status);
    }

    private String toJson(Object object) {
        String json = null;
        try {
            json = objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return json;
    }

    public void logError(Exception ex) {
        Map<String, String> map = Maps.newHashMap();
        map.put("message", ex.getMessage());
        logger.error(this.toJson(map), ex);
    }

    public void logError(Exception ex, HttpServletRequest request) {
        Map<String, String> map = Maps.newHashMap();
        map.put("message", ex.getMessage());
        map.put("from", request.getRemoteAddr());
        String queryString = request.getQueryString();

        // logger.error 时，不拼接超长的 queryString
        map.put("path", (queryString != null && queryString.length() < 200) ? (request.getRequestURI() + "?" + queryString) : request.getRequestURI());
        logger.error(this.toJson(map));

        // logger.debug 时，拼接完整的 queryString
        map.put("path", queryString != null ? (request.getRequestURI() + "?" + queryString) : request.getRequestURI());
        logger.debug(this.toJson(map), ex);
    }
}

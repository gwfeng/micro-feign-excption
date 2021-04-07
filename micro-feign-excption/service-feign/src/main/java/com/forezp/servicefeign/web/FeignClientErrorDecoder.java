package com.forezp.servicefeign.web;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.parent.common.exception.ResultStatus;
import com.parent.common.exception.ServiceException;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * author  fengguangwu
 * createTime  2021/4/7
 * desc
 **/
@Slf4j
@Component
public class FeignClientErrorDecoder implements ErrorDecoder {

    private static final Gson gson = new Gson();

    @Override
    public Exception decode(String s, Response response) {
        ServiceException exception= null;
        log.info("返回的状态为:{}", JSON.toJSONString(response));
        if(response != null && response.body() != null) {
            try {
                String errorContent = Util.toString(response.body().asReader());
                log.info("original error content : {}",errorContent);
                if(StringUtils.isNotEmpty(errorContent)) {
                    int status = response.status();
                    if (status == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                        exception = gson.fromJson(errorContent,ServiceException.class);
                        return exception;
                    }
                }
            }catch (Exception e) {
                log.warn("decode exception异常",e);
            }
        }

        if (exception == null) {
            exception = new ServiceException();
            exception.setCode(ResultStatus.ERROR_SERVICE.getCode());
            exception.setError(ResultStatus.ERROR_SERVICE.getMessage());
        }
        return exception;
    }
}

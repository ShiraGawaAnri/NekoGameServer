package com.nekonade.web.gateway.exception;

import com.alibaba.fastjson.JSONObject;
import com.nekonade.common.constcollections.EnumCollections;
import com.nekonade.network.param.http.response.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


/**
 * 用于捕获Gateway本身的逻辑错误(如变量操作等)
 */
@RestControllerAdvice
public class GatewayExceptionCatch {
    private static final Logger logger = LoggerFactory.getLogger(GatewayExceptionCatch.class);

    //ControllerAdvice + ResponseBody = RestControllerAdvice
//    @ResponseBody
    @ExceptionHandler(value = Throwable.class)
    public ResponseEntity<JSONObject> exceptionHandler(Throwable ex) {
        ResponseEntity<JSONObject> response;
        if (ex instanceof WebGatewayException) {
            WebGatewayException error = (WebGatewayException) ex;
            response = new ResponseEntity<>(error.getError());
        } else {
            response = new ResponseEntity<>(WebGatewayException.newBuilder(EnumCollections.CodeMapper.WebGatewayError.UNKNOWN).build().getError());
        }
        return response;
    }

}

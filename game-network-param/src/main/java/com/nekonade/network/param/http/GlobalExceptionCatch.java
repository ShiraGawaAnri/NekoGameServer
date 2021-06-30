package com.nekonade.network.param.http;

import com.alibaba.fastjson.JSONObject;
import com.nekonade.common.constcollections.EnumCollections;
import com.nekonade.common.error.exceptions.GameErrorException;
import com.nekonade.common.error.IServerError;
import com.nekonade.network.param.http.response.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 * @ClassName: GlobalExceptionCatch
 * @Description: 全局异常捕获
 */
@ControllerAdvice
public class GlobalExceptionCatch {
    private final Logger logger = LoggerFactory.getLogger(GlobalExceptionCatch.class);

    @ResponseBody
    @ExceptionHandler(value = Throwable.class)
    public ResponseEntity<JSONObject> exceptionHandler(Throwable ex) {
        IServerError error;
        JSONObject data = new JSONObject();//统一给客户端返回结果
        if (ex instanceof GameErrorException) {
            GameErrorException gameError = (GameErrorException) ex;
            error = gameError.getError();
            logger.error("服务调用失败,{}", ex.getMessage());
        } else {
            error = EnumCollections.CodeMapper.GameCenterError.UNKNOW;
            logger.error("服务预料外异常,{}", ex.getClass().getName(), ex);
        }
        //data.put("errorMsg", ex.getMessage());
        ResponseEntity<JSONObject> response = new ResponseEntity<>(error);
        response.setData(data);
        return response;

    }

}

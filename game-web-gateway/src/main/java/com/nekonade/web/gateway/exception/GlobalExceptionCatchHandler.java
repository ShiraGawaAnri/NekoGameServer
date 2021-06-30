package com.nekonade.web.gateway.exception;

import com.nekonade.common.constcollections.EnumCollections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用于接收后端API 未被捕获的错误
 * 如果后端API启用了 @ControllerAdvice,它以JSON的形式正常返回自定义错误，这里就不会当做错误来判断
 * TODO:排查为何没有起效
 */
public class GlobalExceptionCatchHandler extends DefaultErrorWebExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionCatchHandler.class);

    public GlobalExceptionCatchHandler(ErrorAttributes errorAttributes, ResourceProperties resourceProperties, ErrorProperties errorProperties, ApplicationContext applicationContext) {
        super(errorAttributes, resourceProperties, errorProperties, applicationContext);
    }

    /**
     * 当捕获到异常之后，在这里构造返回给客户端的错误内容。这里构造的格式和用户中心服务返回的错误格式是一致的。这样方便客户端对错误信息做统一处理。
     */
    @Override
    protected Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        Throwable ex = super.getError(request);
        Map<String, Object> result = new HashMap<>();
        if (ex instanceof WebGatewayException) {
            WebGatewayException error = (WebGatewayException) ex;
            result.put("code", error.getError().getErrorCode());
            result.put("errorMsg", error.getError().getErrorDesc());
            logger.error("网关范围内异常,{}", ex.getClass().getName(), error);
        } else {
            // 这里可以根据自己的业务需求添加不同的错误码。
            result.put("code", EnumCollections.CodeMapper.WebGatewayError.UNKNOWN.getErrorCode());
            result.put("errorMsg", EnumCollections.CodeMapper.WebGatewayError.UNKNOWN.getErrorDesc());
            logger.error("网关预期外异常,{}", ex.getClass().getName(), ex);
        }
        return result;
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }

    /**
     * 根据code获取对应的HttpStatus
     *
     * @param errorAttributes
     * @return
     */
    @Override
    protected int getHttpStatus(Map<String, Object> errorAttributes) {
        // 这里正常返回消息，请客户端根据返回的code做自定义处理。
        int code = HttpStatus.INTERNAL_SERVER_ERROR.value();
        if (errorAttributes.get("code") != null) {
            code = (int) errorAttributes.get("code");
        } else if (errorAttributes.get("status") != null) {
            code = (int) errorAttributes.get("status");
        }
        return code;
    }

    @Override
    protected RequestPredicate acceptsTextHtml() {
        // 这里指定客户端不接收HTML格式的信息，全部以JSON的格式返回。
        return c -> false;
    }

}

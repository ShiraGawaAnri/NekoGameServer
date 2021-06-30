package com.nekonade.network.param.http.response;

import com.alibaba.fastjson.JSONObject;
import com.nekonade.common.error.IServerError;

public class ResponseEntity<T> {
    private int code; //返回的消息码，如果消息正常返回，code == 0，否则返回错误码
    private T data;
    private String errorMsg; //当code != 0时，这里表示错误的详细信息

    public ResponseEntity() {
    }

    public ResponseEntity(IServerError code) {
        super();
        this.code = code.getErrorCode();
        this.errorMsg = code.getErrorDesc();
    }

    public ResponseEntity(T data) {
        super();
        this.data = data;
    }

    public static <T> ResponseEntity<T> parseObject(String response, Class<T> t) {
        JSONObject root = JSONObject.parseObject(response);
        int code = root.getIntValue("code");
        ResponseEntity<T> result = new ResponseEntity<>();
        if (code == 0) {
            JSONObject dataJson = root.getJSONObject("data");
            T data = dataJson.toJavaObject(t);
            result.setData(data);
        } else {
            String errorMsg = root.getString("errorMsg");
            result.setCode(code);
            result.setErrorMsg(errorMsg);
        }
        return result;

    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

}

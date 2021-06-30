package com.nekonade.network.param.http.request;


import com.nekonade.common.error.exceptions.GameErrorException;
import com.nekonade.common.error.IServerError;

public abstract class AbstractHttpRequestParam {
    protected IServerError error;

    public void checkParam() {
        haveError();
        if (error != null) {
            throw new GameErrorException.Builder(error).message("异常类:{}", this.getClass().getName()).build();
        }
    }

    protected abstract void haveError();
}

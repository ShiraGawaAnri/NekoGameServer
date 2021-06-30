package com.nekonade.common.error.exceptions;

public class TokenException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private boolean expire;


    public TokenException(String message, Throwable cause) {
        super(message, cause);
    }

    public TokenException(String message) {
        super(message);
    }

    public boolean isExpire() {
        return expire;
    }

    public void setExpire(boolean expire) {
        this.expire = expire;
    }


}
